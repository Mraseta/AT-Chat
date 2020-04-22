package data;

import java.util.ArrayList;

import javax.ejb.Singleton;

import models.Message;
import models.User;

@Singleton
public class Data {
	private static ArrayList<User> allUsers = new ArrayList<>();
	private static ArrayList<User> loggedUsers = new ArrayList<>();
	private static ArrayList<Message> messages = new ArrayList<>();
	
	static {
		User u = new User("roki", "roki");
		
		allUsers.add(new User("pera", "pera"));
		allUsers.add(new User("mika", "mika"));
		allUsers.add(u);
		
		loggedUsers.add(u);
	}

	public static ArrayList<User> getAllUsers() {
		return allUsers;
	}

	public static void setAllUsers(ArrayList<User> allUsers) {
		Data.allUsers = allUsers;
	}

	public static ArrayList<User> getLoggedUsers() {
		return loggedUsers;
	}

	public static void setLoggedUsers(ArrayList<User> loggedUsers) {
		Data.loggedUsers = loggedUsers;
	}

	public static ArrayList<Message> getMessages() {
		return messages;
	}

	public static void setMessages(ArrayList<Message> messages) {
		Data.messages = messages;
	}
}
