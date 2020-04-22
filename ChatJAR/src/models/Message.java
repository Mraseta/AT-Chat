package models;

import java.time.LocalDateTime;

public class Message {
	private String sender;
	private String receiver;
	private LocalDateTime time;
	private String subject;
	private String content;
	
	public Message() {
		super();
	}

	public Message(String sender, String receiver, LocalDateTime time, String subject, String content) {
		super();
		this.sender = sender;
		this.receiver = receiver;
		this.time = time;
		this.subject = subject;
		this.content = content;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public LocalDateTime getTime() {
		return time;
	}

	public void setTime(LocalDateTime time) {
		this.time = time;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "Message [sender=" + sender + ", receiver=" + receiver + ", time=" + time + ", subject=" + subject
				+ ", content=" + content + "]";
	}
}
