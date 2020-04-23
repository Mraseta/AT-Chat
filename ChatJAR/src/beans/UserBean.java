package beans;

import java.util.ArrayList;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import data.Data;
import models.User;

@Stateless
@Path("/users")
@LocalBean
public class UserBean {
	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response register(User user) {
		for(User u : Data.getAllUsers()) {
			if(u.getUsername().equalsIgnoreCase(user.getUsername())) {
				System.out.println("User with that username already exists!");
				return Response.status(400).build();
			}
		}
		
		Data.getAllUsers().add(user);
		System.out.println("User: " + user + " successfully registered!");
		
		return Response.status(201).build();
	}
	
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(User user) {
		for(User u : Data.getAllUsers()) {
			if(u.getUsername().equalsIgnoreCase(user.getUsername())) {
				if(!u.getPassword().equals(user.getPassword())) {
					System.out.println("Wrong password!");
					return Response.status(400).build();
				}
				else {
					System.out.println("User successfully logged in!");
					Data.getLoggedUsers().add(user);
					return Response.status(200).entity(u).build();
				}
			}
		}
		
		System.out.println("Username not found!");
		return Response.status(400).build();
	}
	
	@GET
	@Path("/loggedIn")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<User> loggedIn() {
		ArrayList<User> users = new ArrayList<>();
		
		if(Data.getLoggedUsers().size() == 0) {
			System.out.println("There are no logged in users!");
			return users;
		}
		
		for(User u : Data.getLoggedUsers()) {
			users.add(u);
		}
		
		return users;
	}
	
	@GET
	@Path("/registered")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<User> registered() {
		ArrayList<User> users = new ArrayList<>();
		
		if(Data.getAllUsers().size() == 0) {
			System.out.println("There are no registered users!");
			return users;
		}
		
		for(User u : Data.getAllUsers()) {
			users.add(u);
		}
		
		return users;
	}
	
	@DELETE
	@Path("/loggedIn/{user}")
	public Response logout(@PathParam("user") String user) {
		for(User u : Data.getLoggedUsers()) {
			if(u.getUsername().equals(user)) {
				Data.getLoggedUsers().remove(u);
				System.out.println("User " + user + " has successfully logged out!");
				return Response.status(200).build();
			}
		}
		
		System.out.println("User " + user + " not found!");
		return Response.status(400).build();
	}
}