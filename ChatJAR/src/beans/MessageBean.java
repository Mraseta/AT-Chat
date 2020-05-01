package beans;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
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
import models.Message;
import models.User;
import ws.WSEndPoint;

@Stateless
@Path("/messages")
@LocalBean
public class MessageBean implements MessageLocal {
	@EJB
	WSEndPoint ws;
	
	@POST
	@Path("/all")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendToAll(Message message) {
		message.setTime(LocalDateTime.now());
		for(User u : Data.getAllUsers()) {
			if(!u.getUsername().equals(message.getSender())) {
				message.setReceiver(u.getUsername());
				Message m = new Message(message.getSender(), message.getReceiver(), message.getTime(), message.getSubject(), message.getContent());
				Data.getMessages().add(m);
				ws.echoTextMessage(message.getSender() + "-" + message.getReceiver() + "-" + message.getContent());
			}
		}
		
		System.out.println("Message: " + message.getContent() + " sent to everyone.");
		
		return Response.status(200).build();
	}
	
	@POST
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendToUser(Message message) {
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String h = ip.toString().split("/")[1].split("\n")[0];
		
		
		message.setTime(LocalDateTime.now());
		User receiver = null;
		for(User u : Data.getAllUsers()) {
			if(u.getUsername().equals(message.getReceiver())) {
				if(!u.getHost().getAddress().equals(h)) {
					ResteasyClient rc = new ResteasyClientBuilder().build();			
					String path = "http://" + u.getHost().getAddress() + ":8080/ChatWAR/rest/messages/user";
					ResteasyWebTarget rwt = rc.target(path);
					Response response = rwt.request(MediaType.APPLICATION_JSON).post(Entity.entity(message, MediaType.APPLICATION_JSON));
					System.out.println(response);
				}
			}
		}
		Data.getMessages().add(message);
		System.out.println("Message: " + message.getContent() + " sent to " + message.getReceiver() + ".");
		ws.echoTextMessage(message.getSender() + "-" + message.getReceiver() + "-" + message.getContent());
		return Response.status(200).build();
	}
	
	@GET
	@Path("/{user}")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<Message> userMessages(@PathParam("user") String user) {
		ArrayList<Message> ret = new ArrayList<>();
		
		for(Message m : Data.getMessages()) {
			if(m.getSender().equals(user) || m.getReceiver().equals(user)) {
				System.out.println(m);
				ret.add(m);
			}
		}
		
		return ret;
	}
}
