package beans;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import data.Data;
import models.Message;
import models.User;
import ws.WSEndPoint;

@Stateless
@Path("/messages")
@LocalBean
public class MessageBean {
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
			}
		}
		
		System.out.println("Message: " + message.getContent() + " sent to everyone.");
		ws.echoTextMessage(message.getContent());
		return Response.status(200).build();
	}
	
	@POST
	@Path("/user")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendToUser(Message message) {
		message.setTime(LocalDateTime.now());
		Data.getMessages().add(message);
		System.out.println("Message: " + message.getContent() + " sent to " + message.getReceiver() + ".");
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
