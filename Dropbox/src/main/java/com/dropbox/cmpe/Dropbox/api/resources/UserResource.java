/**
 * 
 */
package com.dropbox.cmpe.Dropbox.api.resources;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.CreateVaultResult;
import com.dropbox.cmpe.Dropbox.domain.AmazonCommon;
import com.dropbox.cmpe.Dropbox.domain.User;
import com.dropbox.cmpe.Dropbox.dto.MyMongo;
import com.yammer.metrics.annotation.Timed;

/**
 * @author Team Projections
 *
 */

@Path("v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {
	
	private MyMongo myMongo;
	
	private String vaultName;
	
	public UserResource(MyMongo myMongo) {
		// TODO Auto-generated constructor stub
		this.myMongo = myMongo;		
	}

	@POST
	@Path("/newuser")
	@Timed(name = "new-user-new-vault")
	public Response newUser(@Valid User newUser)
	throws Exception {	
	String newUserName = myMongo.registerUser(newUser.getUsername(), newUser.getPassword(), newUser.getRole(),newUser.getEmailid());
	System.out.println("Creating new vault for new user");
	this.vaultName = myMongo.getVaultName(newUserName);
	AmazonCommon common = new AmazonCommon();
	AWSCredentials credentials = common.getCredentials();
	AmazonGlacierClient client = common.getClient(credentials);
	CreateVaultResult vaultresult = common.createVault(this.vaultName, client);
	if(newUserName!= newUser.getUsername()){
		String responceMessage = "Your username will be : "+newUserName;
		System.out.println(newUserName);
	return Response
			.status(200)
			.entity(responceMessage)
			.build();
	}
	else{
		String responceMessage = "Your username will be : "+newUserName;
		return Response
				.status(200)
				.entity(responceMessage)
				.build();
	}
}
	
	@POST
	@Path("/olduser")
	@Timed(name = "Login")
	public Response loginUser(@Valid User newUser){
		String existingUser = newUser.getUsername();
		System.out.println("usename:"+newUser.getUsername());
		System.out.println("password:"+newUser.getPassword());
		if(myMongo.isUserNameExist(existingUser) == true){
			if(myMongo.authenticateUser(newUser.getUsername(), newUser.getPassword())){
				return Response.ok(200).build();
			}
			else{
				return Response
						.status(Response.Status.UNAUTHORIZED)
						.entity("Please verify your username and password")
						.build();
			}
		}
		else{ 
			return Response
					.status(Response.Status.UNAUTHORIZED)
					.entity("Sorry!! This username doesn't exists in our system. Please sign up as new user")
					.build();
		}
	}

}