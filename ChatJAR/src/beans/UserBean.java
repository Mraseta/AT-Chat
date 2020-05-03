package beans;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import data.Data;
import models.Host;
import models.User;
import ws.WSEndPoint;

@Stateless
@Path("/users")
@LocalBean
public class UserBean {
	
	@EJB
	WSEndPoint ws;
	
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

					InetAddress ip = null;
					try {
						ip = InetAddress.getLocalHost();
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String k = ip.toString().split("/")[1].split("\n")[0];
					
					for(Host h : Data.getHosts()) {
						if(h.getAlias().equals(k)) {
							user.setHost(h);
							u.setHost(h);
						}
						
						System.out.println(h);
					}
					
					for(Host h : Data.getHosts()) {
						if(!h.getAlias().equals(k)) {
							System.out.println("usao u if " + h);
							ResteasyClient rc = new ResteasyClientBuilder().build();
							String path = "http://" + h.getAddress() + ":8080/ChatWAR/rest/users/loggedIn";
							System.out.println(path);
							ResteasyWebTarget rwt = rc.target(path);		
							Response response = rwt.request(MediaType.APPLICATION_JSON).post(Entity.entity(Data.getLoggedUsers(), MediaType.APPLICATION_JSON));
							System.out.println(response);
						}
					}
					
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
			User a = u;
			System.out.println(a);
			if(u.getUsername().equals(user)) {
				Data.getLoggedUsers().remove(u);
				System.out.println("User " + user + " has successfully logged out!");
				ws.echoTextMessage(user + " now offline");
				
				for(Host h : Data.getHosts()) {
					System.out.println(h.getAddress() + " " + a.getHost().getAddress());
					if(!h.getAddress().equals(a.getHost().getAddress())) {
						ResteasyClient rc = new ResteasyClientBuilder().build();
						String path = "http://" + h.getAddress() + ":8080/ChatWAR/rest/users/loggedIn";
						System.out.println(path);
						ResteasyWebTarget rwt = rc.target(path);
						Response response = rwt.request(MediaType.APPLICATION_JSON).post(Entity.entity(Data.getLoggedUsers(), MediaType.APPLICATION_JSON));
						System.out.println(response);
					}
				}
				
				return Response.status(200).build();
			}
		}
		
		System.out.println("User " + user + " not found!");
		return Response.status(400).build();
	}
	
	@POST
	@Path("/loggedIn")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postLoggedIn(ArrayList<User> users) {
		/*for(User u : users) {
			boolean found = false;
			for(int i=0;i<Data.getLoggedUsers().size();i++) {
				if(u.getUsername().equals(Data.getLoggedUsers().get(i).getUsername())) {
					found = true;
					break;
				}
			}
			
			if(!found) {
				Data.getLoggedUsers().add(u);
			}
		}*/
		
		/*for(User u : Data.getLoggedUsers()) {
			System.out.println(u);
		}
		
		for(User u : Data.getLoggedUsers()) {
			Data.getLoggedUsers().remove(u);
			System.out.println("obrisao");
		}*/
		
		Data.getLoggedUsers().clear();
		
		System.out.println(Data.getLoggedUsers().size());
		
		for(User u : users) {
			Data.getLoggedUsers().add(u);
		}
		
		ws.echoTextMessage("refresh logged");
		
		return Response.status(200).build();
	}
	
	@GET
	@Path("/loggedIn")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<User> getLoggedIn() {
		ArrayList<User> ret = new ArrayList<>();
		
		for(User u : Data.getLoggedUsers()) {
			ret.add(u);
		}
		
		//ws.echoTextMessage("refresh logged");
		
		return ret;
	}
}
