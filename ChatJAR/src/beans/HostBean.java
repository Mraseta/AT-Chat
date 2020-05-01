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
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.Schedules;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TimerService;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import data.Data;
import models.Host;
import models.User;

@Singleton
@Startup
@Path("/hosts")
public class HostBean {
	@Resource
	TimerService ts;
	
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
			System.out.println("FC " + fileContent);
			InetAddress ip = InetAddress.getLocalHost();
			this.hostip = ip.toString().split("/")[1].split("\n")[0];
			
			Host n = new Host(this.hostip, this.hostip);
			Data.getHosts().add(n);
			
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
				this.master = this.hostip;
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
	
	@PreDestroy
	private void destroy() {
		String alias = "";
		for(Host h : Data.getHosts()) {
			if(h.getAddress().equals(this.hostip)) {
				alias = h.getAlias();
			}
		}
		
		for(Host h : Data.getHosts()) {
			ResteasyClient rc = new ResteasyClientBuilder().build();			
			String path = "http://" + h.getAddress() + ":8080/ChatWAR/rest/hosts/node/" + alias;
			ResteasyWebTarget rwt = rc.target(path);
			Response response = rwt.request(MediaType.APPLICATION_JSON).delete();
			System.out.println(response);
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
		
		int cnt = 0;
		
		while(cnt<2) {
			ResteasyClient rc2 = new ResteasyClientBuilder().build();
			String path2 = "http://" + host.getAddress() + ":8080/ChatWAR/rest/users/loggedIn";
			ResteasyWebTarget rwt2 = rc2.target(path2);
			Response response2 = rwt2.request(MediaType.APPLICATION_JSON).post(Entity.entity(Data.getLoggedUsers(), MediaType.APPLICATION_JSON));
			System.out.println(response2);
			
			if(response2.getStatus() != Response.Status.OK.ordinal()) {
				cnt++;
			}
		}
		
		if(cnt==2) {
			for(Host h : Data.getHosts()) {
				ResteasyClient rc3 = new ResteasyClientBuilder().build();
				String path3 = "http://" + h.getAddress() + ":8080/ChatWAR/rest/hosts/node/" + host.getAlias();
				ResteasyWebTarget rwt3 = rc3.target(path3);
				Response response3 = rwt3.request(MediaType.APPLICATION_JSON).delete();
				System.out.println(response3);
			}
			
			return Response.status(400).build();
		}
		
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
	
	@DELETE
	@Path("/node/{alias}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(@PathParam("alias") String alias) {
		for(Host h : Data.getHosts()) {
			if(h.getAlias().equals(alias)) {
				Data.getHosts().remove(h);
				return Response.status(200).build();
			}
		}
		
		return Response.status(400).build();
	}
	
	@GET
	@Path("/node")
	public Response getNode() {
		System.out.println("pingovan");
		return Response.status(200).build();
	}
	
	@Schedules({
		@Schedule(hour="*", minute="*", second="*/10")
	})
	public void heartbeat() {
		System.out.println("entered heartbeat " + Data.getHosts().size());
		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String k = ip.toString().split("/")[1].split("\n")[0];
		
		for(Host h : Data.getHosts()) {
			if(!h.getAddress().equals(k)) {
				ResteasyClient rc = new ResteasyClientBuilder().build();			
				String path = "http://" + h.getAddress() + ":8080/ChatWAR/rest/hosts/node";
				ResteasyWebTarget rwt = rc.target(path);
				Response response = rwt.request(MediaType.APPLICATION_JSON).get();
				System.out.println(response);
				
				if(response.getStatus() != Response.Status.OK.ordinal()) {
					Response response2 = rwt.request(MediaType.APPLICATION_JSON).get();
					if(response2.getStatus() != Response.Status.OK.ordinal()) {		
						for(Host h2 : Data.getHosts()) {
							if(!h2.getAddress().equals(h.getAddress())) {
								ResteasyClient rc2 = new ResteasyClientBuilder().build();			
								String path2 = "http://" + h2.getAddress() + ":8080/ChatWAR/rest/hosts/node/" + h.getAlias();
								ResteasyWebTarget rwt2 = rc2.target(path2);
								Response response3 = rwt2.request(MediaType.APPLICATION_JSON).get();
								System.out.println(response3);
							}
						}
					}
				}
			}
		}
	}
}
