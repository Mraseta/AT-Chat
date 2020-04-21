package beans;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ws.WSEndPoint;

@Stateless
@Path("/chat") 
@LocalBean
public class ChatBean implements ChatRemote, ChatLocal {
	@EJB
	WSEndPoint ws;
	
	/*
	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;
	@Resource(mappedName = "java:jboss/exported/jms/queue/mojQueue")
	private Queue queue;
	*/
	
	@GET
	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		return "OK";
	}
	
	@POST
	@Path("/post/{text}")
	@Produces(MediaType.TEXT_PLAIN)
	public String post(@PathParam("text") String text) {
		System.out.println("Received message: " + text);
		ws.echoTextMessage(text);
		/*
		try {
			QueueConnection connection = (QueueConnection) connectionFactory.createConnection("guest", "guest.guest.1");
			QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			QueueSender sender = session.createSender(queue);
			// create and publish a message
			TextMessage message = session.createTextMessage();
			message.setText(text);
			sender.send(message);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		*/
		return "OK";
	}

}
