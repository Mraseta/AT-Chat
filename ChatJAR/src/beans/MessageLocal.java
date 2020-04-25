package beans;

import java.util.ArrayList;

import javax.ejb.Local;

import models.Message;

@Local
public interface MessageLocal {
	public ArrayList<Message> userMessages(String user);
}
