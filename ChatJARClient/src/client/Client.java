package client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import beans.ChatRemote;

public class Client {

	public static void main(String[] args) {
		try {
			Context context = new InitialContext();
			String remoteName = "ejb:ChatEAR/ChatJAR/ChatBean!"
					+ ChatRemote.class.getName();
			System.err.println("Looking up for: " + remoteName);
			ChatRemote chat = (ChatRemote) context.lookup(remoteName);
			System.out.println("Server responded: " + chat.post("klijent"));

		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

}
