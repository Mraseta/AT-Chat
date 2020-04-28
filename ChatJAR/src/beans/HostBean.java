package beans;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import data.Data;
import models.Host;

@Singleton
@Startup
@Path("/")
public class HostBean {
	private String master = "";
	private String hostip = "";
	
	@PostConstruct
	private void init() {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			InputStream in = getClass().getClassLoader().getResourceAsStream("master.txt");
			reader = new BufferedReader(new InputStreamReader(in));
			String fileContent = reader.readLine();
			System.out.println(fileContent);
			InetAddress ip = InetAddress.getLocalHost();
			this.hostip = ip.toString().split("/")[1].split("\n")[0];
			
			in.close();
			
			System.out.println(hostip);			
			
			if (fileContent.split("=").length == 1) {
				String a = fileContent + this.hostip;
				System.out.println(a);
				File f = new File("master2.txt");
				boolean pl = f.createNewFile();
				System.out.println(pl);
				writer = new BufferedWriter(new FileWriter(f));
				writer.write(fileContent + this.hostip);
				System.out.println("\n\n\n\n nema master");
				
			} else {
				this.master = fileContent.split("=")[1];
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("finally");
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerNode(Host host) {
		for(Host h : Data.getHosts()) {
			ResteasyClient rc = new ResteasyClientBuilder().build();
			
			String path = "http://" + h.getAddress() + ":8080/ChatWAR/rest/hosts/node";
			
			ResteasyWebTarget rwt = rc.target(path);
			
			Response response = rwt.request(MediaType.APPLICATION_JSON).post(Entity.entity(host, MediaType.APPLICATION_JSON));
			
			System.out.println(response);
		}
		
		ResteasyClient rc = new ResteasyClientBuilder().build();
		
		String path = "http://" + host.getAddress() + ":8080/ChatWAR/rest/hosts/nodes";
		
		ResteasyWebTarget rwt = rc.target(path);
		
		Response response = rwt.request(MediaType.APPLICATION_JSON).post(Entity.entity(Data.getHosts(), MediaType.APPLICATION_JSON));
		
		System.out.println(response);
		
		Data.getHosts().add(host);
		
		return Response.status(200).build();
	}
	
	@POST
	@Path("/node")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response node(Host host) {
		Data.getHosts().add(host);
		
		return Response.status(200).build();
	}
	
	@POST
	@Path("/nodes")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response nodes(ArrayList<Host> hosts) {
		for(Host h : hosts) {
			Data.getHosts().add(h);
		}
		
		return Response.status(200).build();
	}
}
