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
import java.util.List;

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
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
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
	//@Resource
	//TimerService ts;
	
	private String master = "";
	private String hostip = "";
	
	@SuppressWarnings("unchecked")
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
			
			System.out.println(hostip + " aaaa " + fileContent.split("=").length);
			
			if (fileContent.split("=").length == 1) {
				String a = fileContent + this.hostip;
				System.out.println(a);
				System.out.println("\n\n\n\n nema master");
				this.master = this.hostip;
			} else {
				this.master = fileContent.split("=")[1];
				Data.getHosts().add(new Host(this.master, this.master));
				ResteasyClient rc = new ResteasyClientBuilder().build();			
				String path = "http://" + this.master + ":8080/ChatWAR/rest/hosts/register";
				System.out.println(path);
				ResteasyWebTarget rwt = rc.target(path);
				Response response = rwt.request(MediaType.APPLICATION_JSON).post(Entity.entity(n, MediaType.APPLICATION_JSON));
				System.out.println(response);
				
				ResteasyClient rc2 = new ResteasyClientBuilder().build();			
				String path2 = "http://" + this.master + ":8080/ChatWAR/rest/hosts/nodes";
				System.out.println(path2);
				ResteasyWebTarget rwt2 = rc2.target(path2);
				Response response2 = rwt2.request(MediaType.APPLICATION_JSON).get();
				ArrayList<Host> ret2 = (ArrayList<Host>) response2.readEntity(new GenericType<List<Host>>() {});
				for(Host hh : ret2) {
					boolean found = false;
					for(int i=0;i<Data.getHosts().size();i++)
						if(hh.getAddress().equals(Data.getHosts().get(i).getAddress()))
							found = true;
					if(!found)
						Data.getHosts().add(hh);
				}
				
				System.out.println(response2);
				
				int cnt = 0;
				Response response3 = null;
				while(cnt<2) {
					ResteasyClient rc3 = new ResteasyClientBuilder().build();			
					String path3 = "http://" + this.master + ":8080/ChatWAR/rest/users/loggedIn";
					System.out.println(path3);
					ResteasyWebTarget rwt3 = rc3.target(path3);
					response3 = rwt3.request(MediaType.APPLICATION_JSON).get();
					if(response3.getStatus() != 200) {
						cnt++;
						continue;
					} else {
						break;
					}
				}
				
				if(cnt < 2) {
					ArrayList<User> ret3 = (ArrayList<User>) response3.readEntity(new GenericType<List<User>>() {});
					for(User uu : ret3) {
						Data.getLoggedUsers().add(uu);
					}
				} else {
					for(Host h : Data.getHosts()) {
						if(!h.getAddress().equals(this.hostip)) {
							ResteasyClient rc4 = new ResteasyClientBuilder().build();
							String path4 = "http://" + h.getAddress() + ":8080/ChatWAR/rest/hosts/node/" + this.hostip;
							ResteasyWebTarget rwt4 = rc4.target(path4);
							Response response4 = rwt4.request(MediaType.APPLICATION_JSON).delete();
							System.out.println(response4);
						}
					}
				}				
			}

		} catch (Exception e) {
			e.printStackTrace();
		} /*finally {
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
		}*/
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
			if(!h.getAddress().equals(this.hostip)) {
				ResteasyClient rc = new ResteasyClientBuilder().build();			
				String path = "http://" + h.getAddress() + ":8080/ChatWAR/rest/hosts/node/" + alias;
				ResteasyWebTarget rwt = rc.target(path);
				Response response = rwt.request(MediaType.APPLICATION_JSON).delete();
				System.out.println(response);
			}
		}
	}
	
	@POST
	@Path("/register")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response registerNode(Host host) {
		System.out.println("usao u register" + this.hostip);
		System.out.println(host);
		for(Host h : Data.getHosts()) {
			System.out.println(h);
		}
		//Data.getHosts().add(host);
		for(Host h : Data.getHosts()) {
			if(!h.getAddress().equals(this.hostip)) {
				ResteasyClient rc = new ResteasyClientBuilder().build();
				String path = "http://" + h.getAddress() + ":8080/ChatWAR/rest/hosts/node";
				System.out.println(path);
				ResteasyWebTarget rwt = rc.target(path);
				Response response = rwt.request(MediaType.APPLICATION_JSON).post(Entity.entity(host, MediaType.APPLICATION_JSON));
				System.out.println(response);
			}
		}
		
		System.out.println("prosao for");
		
		/*ResteasyClient rc = new ResteasyClientBuilder().build();
		String path = "http://" + host.getAddress() + ":8080/ChatWAR/rest/hosts/nodes";
		System.out.println(path);
		ResteasyWebTarget rwt = rc.target(path);
		Response response = rwt.request(MediaType.APPLICATION_JSON).post(Entity.entity(Data.getHosts(), MediaType.APPLICATION_JSON));
		System.out.println(response);*/
		
		System.out.println("prosao nodes");
		
		int cnt = 0;
		
		/*while(cnt<2) {
			ResteasyClient rc2 = new ResteasyClientBuilder().build();
			String path2 = "http://" + host.getAddress() + ":8080/ChatWAR/rest/users/loggedIn";
			ResteasyWebTarget rwt2 = rc2.target(path2);
			Response response2 = rwt2.request(MediaType.APPLICATION_JSON).post(Entity.entity(Data.getLoggedUsers(), MediaType.APPLICATION_JSON));
			System.out.println(response2);
			
			if(response2.getStatus() != Response.Status.OK.ordinal()) {
				cnt++;
			} else {
				break;
			}
		}*/
		
		System.out.println("prosao while");
		
		if(cnt==2) {
			for(Host h : Data.getHosts()) {
				if(!h.getAddress().equals(this.hostip)) {
					ResteasyClient rc3 = new ResteasyClientBuilder().build();
					String path3 = "http://" + h.getAddress() + ":8080/ChatWAR/rest/hosts/node/" + host.getAlias();
					ResteasyWebTarget rwt3 = rc3.target(path3);
					Response response3 = rwt3.request(MediaType.APPLICATION_JSON).delete();
					System.out.println(response3);
				}
			}
			
			return Response.status(400).build();
		}
		
		Data.getHosts().add(host);
		
		for(Host h : Data.getHosts()) {
			System.out.println(h);
		}
		
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
		System.out.println("usao u nodes");
		for(Host h : hosts) {
			boolean found = false;
			for(int i=0;i<Data.getHosts().size();i++) {
				if(Data.getHosts().get(i).getAddress().equals(h.getAddress())) 
					found = true;
			}
			
			if(!found)
				Data.getHosts().add(h);
			
			System.out.println(h);
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
		@Schedule(hour="*", minute="*", second="*/30", info="heartbeat")
	})
	public void heartbeat() {
		System.out.println("entered heartbeat " + Data.getHosts().size());
		
		for(Host h : Data.getHosts()) {
			if(!h.getAddress().equals(this.hostip)) {
				ResteasyClient rc = new ResteasyClientBuilder().build();			
				String path = "http://" + h.getAddress() + ":8080/ChatWAR/rest/hosts/node";
				System.out.println(path);
				ResteasyWebTarget rwt = rc.target(path);
				Response response = rwt.request(MediaType.APPLICATION_JSON).get();
				System.out.println(response);
				
				if(response.getStatus() != 200) {
					Response response2 = rwt.request(MediaType.APPLICATION_JSON).get();
					if(response2.getStatus() != 200) {
						Data.getHosts().remove(h);
						for(Host h2 : Data.getHosts()) {
							if(!h2.getAddress().equals(h.getAddress()) && !h2.getAddress().equals(this.hostip)) {
								ResteasyClient rc2 = new ResteasyClientBuilder().build();			
								String path2 = "http://" + h2.getAddress() + ":8080/ChatWAR/rest/hosts/node/" + h.getAlias();
								ResteasyWebTarget rwt2 = rc2.target(path2);
								Response response3 = rwt2.request(MediaType.APPLICATION_JSON).delete();
								System.out.println(response3);
							}
						}
					}
				}
			}
		}
		
		for(Host h : Data.getHosts()) {
			System.out.println(h);
		}
	}
	
	@GET
	@Path("/nodes")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<Host> getNodes() {
		ArrayList<Host> ret = new ArrayList<>();
		
		for(Host h : Data.getHosts()) {
			ret.add(h);
		}
		
		return ret;
	}
}
