package ws;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import beans.ChatLocal;
import beans.MessageLocal;

@Singleton
@ServerEndpoint("/ws/{user}")
@LocalBean
public class WSEndPoint {
	static List<Session> sessions = new ArrayList<Session>();
	
	@EJB
	ChatLocal chat;
	
	@EJB
	MessageLocal mess;
	
	@OnOpen
	public void onOpen(@PathParam("user") String user, Session session) {
		if (!sessions.contains(session)) {
			session.getUserProperties().put("user", user);
			sessions.add(session);
		}
		
		for(Session s : sessions) {
			try {
				s.getBasicRemote().sendText(user + " now online");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@OnMessage
	public void echoTextMessage(String msg) {
		//System.out.println("ChatBean returned: " + chat.test());
		String sender = msg.split("-")[0];
		String receiver = msg.split("-")[1];
		String content = msg.split("-")[2];
		
		try {
	        for (Session s : sessions) {
	        	if(s.getUserProperties().get("user").equals(sender) || s.getUserProperties().get("user").equals(receiver)) {
	        		System.out.println("WSEndPoint: " + content);
	        		s.getBasicRemote().sendText(content);
	        	}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void close(Session session) {
		sessions.remove(session);
		for(Session s : sessions) {
			try {
				s.getBasicRemote().sendText(s.getUserProperties().get("user") + " now offline");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@OnError
	public void error(Session session, Throwable t) {
		sessions.remove(session);
		t.printStackTrace();
	}

}
