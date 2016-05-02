package first_servlet;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Servlet implementation class f_servlet
 */
@WebServlet("/f_servlet")
public class f_servlet extends HttpServlet {
	
	private static final int W=3;
	private static final int WQ=2;
	static int callID;
	final static int portProj1bRPC = 5300;
	final static int maxPacketSize = 512;
	static String file_ip = "IP_info.txt";
	static String file_localip = "local-ipv4";
	static String file_reboot = "Reboot_num.txt";
	static String file_sirID = "ami-launch-index";
	static String file_instnum = "instance_num.txt";
	int instance_num;
	static int severID; 
	
	HashMap<Integer,InetAddress> IPAddr = new HashMap<Integer,InetAddress>();

    /**
     * @see HttpServlet#HttpServlet()
     */
    public f_servlet() {
        super();
		new Thread(new GC()).start();
		new Thread(new RPC_Server()).start();
		
    }
    @Override
    public void init() {
    	severID = getServID();
    	instance_num = getInstNum();
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Cookie[] cookies = request.getCookies(); 
		String act1 = request.getParameter("Replace");  		
		String act2 = request.getParameter("Refresh");  		
		String act3 = request.getParameter("Log_out");  		
		Cookie c_cookie = null;                         		
		PrintWriter out = response.getWriter();         	 
		response.setContentType("text/html");
		boolean find_cookie = false;      
		getIP();

		/*
		 * 	first time to visit, and no cookie is found, 
		 * so need to create a new session which is a write operation, 
		 * so using RPC client stub send this session to W RPC servers and waiting for 
		 * their response.
		 */

		
		if (cookies != null)  {	
			System.out.println("not null");
			for (int i = 0; i < cookies.length; i++) {
				if (cookies[i].getName().equals("CS5300PROJ1SESSION")) {
					find_cookie = true;							
															
					c_cookie = cookies[i];						
					c_cookie.setDomain(".my437.bigdata.systems");										
					c_cookie.setMaxAge(120);			
					System.out.println(c_cookie.getName());
					System.out.println("find cookie");										
				break;											
				}
			}
		}
		
		System.out.println(act1);
		System.out.println(act2);
		
		if ((act1 == null && act2 == null && act3 == null) || cookies == null ) {       
			
			String sessid = ""+ getServID() + "_" + getrebNum() + "_";
			Session new_session = new Session("Hello User!",sessid);
				
			// read IP addresses from simpleDB file.
			InetAddress[] Dest_Addr = getRandomIP(W);
			System.out.println(Arrays.toString(Dest_Addr));
			// send to W RPC servers
			String sid = new_session.get_sid();
			String version = new_session.get_vnum() + "";
			String s = new_session.get_mess();
			String discard_time = "" + new_session.get_disctime().getTime();
			String metadata = SessionWrite(sid, version, s, discard_time, Dest_Addr);
			metadata = metadata.replaceAll("[\u0000-\u001f]","");
			String[] t_metadata = metadata.split("_");
			System.out.println("datalenght:"+t_metadata.length);
			System.out.println("mdata:" + metadata);
			*if (t_metadata.length<= WQ){
				System.out.println("test2");
				out.println("fail");
				//error
			}
			else {
				System.out.println("test");
				String v = "" + sid + "_" + version + metadata;
				System.out.println(v);
				v=v.replaceAll("[\u0000-\u001f]","");
				c_cookie = new Cookie("CS5300PROJ1SESSION", v);
				c_cookie.setDomain(".my437.bigdata.systems");
				c_cookie.setMaxAge(120);
				System.out.println("lenth:"+v.length());
				System.out.println("execute2");
				response.addCookie(c_cookie);
				System.out.println("execute");
				out.println(output(new_session.get_time(), 
						new_session.get_mess(), 
						new_session.get_sid() + "", 
						new_session.get_vnum(), v, ""+severID, ""+getrebNum(),"",Arrays.toString(t_metadata),c_cookie.getDomain()));
				System.out.println(c_cookie.getName()+"1");
	
			  
			}	                                                       	                                                                                                           	 
		}
		// set found cookie flag

	   /*
	    * replace - operation: write method
	    */
		else if (act1 != null) {											
				
			// not find cookie, same as before, first time visit
				if (!find_cookie) {
					String sessid = ""+ getServID() + "_" + getrebNum() + "_";
					Session new_session = new Session("Hello User!",sessid);	 
					
					InetAddress[] Dest_Addr = getRandomIP(W);
					String sid = new_session.get_sid();
					String version = new_session.get_vnum() + "";
					String s = new_session.get_mess();
					String discard_time = ""+new_session.get_disctime().getTime();
					String metadata = SessionWrite(sid, version, s, discard_time, Dest_Addr);
					metadata = metadata.replaceAll("[\u0000-\u001f]","");
					String[] t_metadata = metadata.split("_");
					if (t_metadata.length < WQ) {
						out.print("replace_fail");
						// error
					}
					else {
						String v = "" + sid + "_" + version + metadata;
						v=v.replaceAll("[\u0000-\u001f]","");
						c_cookie = new Cookie("CS5300PROJ1SESSION", v);
						c_cookie.setDomain(".my437.bigdata.systems");
						c_cookie.setMaxAge(120);
						response.addCookie(c_cookie);
						out.println(output(new_session.get_time(), 
								new_session.get_mess(), 
								new_session.get_sid() + "", 
								new_session.get_vnum(), v, ""+severID, ""+getrebNum(),"",Arrays.toString(t_metadata),c_cookie.getDomain()));
						System.out.println(c_cookie.getName()+"2");
					}					                 		 
				}
				
				/*
				 *  if it has the cookie, get corresponding session out of the hashmap, get its info,
				 *  update its version number, expire time and message, and put it back
				 */
				else {
					
					//first get the information for cookie: sessionID, version_number, metadata
					//second create a new session: version_number++, sessionID maintain 
					String[] c_info = c_cookie.getValue().split("_"); 
					String session_id = c_info[0]+"_"+c_info[1]+"_"+c_info[2];
					int version_number = Integer.parseInt(c_info[3]) + 1;
					Timestamp discard_time = new Timestamp(System.currentTimeMillis()+120*1000+10*1000);
					Session new_session = new Session(request.getParameter("message"), session_id, version_number,discard_time);
					
					// send to w RPC servers
					InetAddress[] Dest_Addr = getRandomIP(W);
					
					// send to W RPC servers
					String sid = new_session.get_sid();
					String version = new_session.get_vnum() + "";
					String s = new_session.get_mess();
					String metadata = SessionWrite(sid, version, s, ""+discard_time.getTime(), Dest_Addr);
					metadata = metadata.replaceAll("[\u0000-\u001f]","");
					String[] t_metadata = metadata.split("_");
					if (t_metadata.length< WQ){
						out.println("replace fail");
						//error
					}
					else {
						String v = "" + sid + "_" + version + metadata;
						v=v.replaceAll("[\u0000-\u001f]","");
						c_cookie = new Cookie("CS5300PROJ1SESSION", v);
						c_cookie.setDomain(".my437.bigdata.systems");
						c_cookie.setMaxAge(120);
						response.addCookie(c_cookie);
						out.println(output(new_session.get_time(), 
								new_session.get_mess(), 
								new_session.get_sid() + "", 
								new_session.get_vnum(), v, ""+severID, ""+getrebNum(),"",Arrays.toString(t_metadata),c_cookie.getDomain()));
					    	
					}
					
				}
			}
		/*
		 * operation = refresh -- read method
		 */
		else if (act2 != null) {  										
				
			// not found cookie create a new session, write to w RPC servers
				if (!find_cookie) {                                      
					String sessid = ""+ getServID() + "_" + getrebNum() + "_";
					Session new_session = new Session("Hello User!",sessid);	 
					
					InetAddress[] DestAddr = getRandomIP(W);
					String sid = new_session.get_sid();
					String version = new_session.get_vnum() + "";
					String s = new_session.get_mess();
					String discard_time = ""+new_session.get_disctime().getTime();
					String metadata = SessionWrite(sid, version, s, discard_time, DestAddr);
					metadata = metadata.replaceAll("[\u0000-\u001f]","");
					String[] t_metadata = metadata.split("_");
					if (t_metadata.length < WQ) {
						out.println("refresh fail");
					}
					else {
						
						String v = "" + sid + "_" + version  + metadata;
						v=v.replaceAll("[\u0000-\u001f]","");
						c_cookie = new Cookie("CS5300PROJ1SESSION", v);
						c_cookie.setDomain(".my437.bigdata.systems");
						c_cookie.setMaxAge(120);
						response.addCookie(c_cookie);
						out.println(output(new_session.get_time(), 
								new_session.get_mess(), 
								new_session.get_sid() + "", 
								new_session.get_vnum(), v, ""+severID, ""+getrebNum(),"",Arrays.toString(t_metadata),c_cookie.getDomain()));	
					}	
				}
				
				/*
				 * has the cookie,  need to read from servers (which are indicated in metadata),
				 * send read request to WQ RPC servers, and get response.
				 * and then write
				 */
				else {		
					String[] c_info = c_cookie.getValue().split("_"); 
					System.out.println("c_info_length:"+c_info.length);
					InetAddress[] DestAddr = new InetAddress[c_info.length-4];
					System.out.println("DestAddrlength:"+DestAddr.length);
					for (int i = 4; i < 4+DestAddr.length; i++) {
						DestAddr[i-4] = IPAddr.get(Integer.parseInt(c_info[i]));
						System.out.println("DestAddr***:"+DestAddr[i-4]);
					}
					// using serverID to find the corresponding IP address
					String sid = c_info[0]+"_"+c_info[1]+"_"+c_info[2];
					System.out.println("sid:"+sid);
					System.out.println("vernum:"+c_info[3]);
					String msg_ = SessionRead(sid,c_info[3],DestAddr);
					String[] msg = msg_.split("#");
					
					
					System.out.println("msg:"+msg[0]);
					if (msg[0] == "***") {
						out.println("refresh fail");
						// error!
					}
					else {
						// update session
						System.out.println("refresh:update");
						int version_number = Integer.parseInt(c_info[3]) + 1;
						Timestamp discard_time = new Timestamp(System.currentTimeMillis()+30*1000+10*1000);
						Session new_session = new Session(msg[0], sid, version_number, discard_time);
						
						InetAddress[] DestAddr2 = getRandomIP(W);
						System.out.println("sssssss");
						System.out.println(sid+";"+ version_number+";"+msg[0]+ ";"+discard_time +";"+ Arrays.toString(DestAddr2));
						String metadata = SessionWrite(sid, ""+version_number, msg[0], ""+discard_time.getTime(), DestAddr2);
						metadata = metadata.replaceAll("[\u0000-\u001f]","");
						String[] t_metadata = metadata.split("_");
						if (t_metadata.length < WQ) {
							out.println("refresh fail");
						}
						else {
							
							String v = "" + sid + "_" + version_number  + metadata;
							v=v.replaceAll("[\u0000-\u001f]","");
							c_cookie = new Cookie("CS5300PROJ1SESSION", v);
							c_cookie.setDomain(".my437.bigdata.systems");
							c_cookie.setMaxAge(120);
							response.addCookie(c_cookie);
							out.println(output(new_session.get_time(), 
									new_session.get_mess(), 
									new_session.get_sid() + "", 
									new_session.get_vnum(), v, ""+severID, ""+getrebNum(),msg[1],Arrays.toString(t_metadata),c_cookie.getDomain()));			
						}
					}
				}	
			}
			/*
			 * action == logout, (if it has the cookie, set its maxage to zero), redirect it to a different page.
			 */
		else if (act3 != null) {
			String sessid = ""+ getServID() + "_" + getrebNum() + "_";
			Timestamp dic_time = new Timestamp(10*1000);
			Session new_session = new Session("Hello User!",sessid, 0,dic_time, "0");
			InetAddress[] Dest_Addr = getRandomIP(W);
			String sid = new_session.get_sid();
			String version = new_session.get_vnum() + "";
			String s = new_session.get_mess();
			String discard_time = ""+new_session.get_disctime().getTime();
			String metadata = SessionWrite(sid, version, s, discard_time, Dest_Addr);
			metadata = metadata.replaceAll("[\u0000-\u001f]","");
			String[] t_metadata = metadata.split("_");
			if (t_metadata.length < WQ) {
				out.print("replace_fail");
				// error
			}
			else {
				String v = "" + sid + "_" + version + metadata;
				v=v.replaceAll("[\u0000-\u001f]","");
				c_cookie = new Cookie("CS5300PROJ1SESSION", v);
				c_cookie.setDomain(".my437.bigdata.systems");
				c_cookie.setMaxAge(0);
				response.addCookie(c_cookie);
				out.println("<h1>successfully log out</h1>");
			}

		}
		
	}
	
	// response message print method
	public String output(String time, String mess, String sid, int vnum, String cookie, String serverID, String rebootnum, String serverIDFound, String metadata, String domain) 
			throws ServletException, IOException{

		    String opt = "<!DOCTYPE html>\n" +
						 "<html>\n" +
						 "<head>\n" +
						 "<title>Insert title here</title>\n"
						 + "</head>\n" 
						 +"<body>\n" 
						 + "<h1>" + mess+ "</h1>" 
						 + "<form metod = \"get\">" 
						 + "<p>Netid: lh567 </p>\n" + "\n"
						 + "<p>Expire:" + time + "</p>\n" + "\n"
						 + "<p>Session:" + sid + "</p>\n" + "\n"
						 + "<p>Version:" + vnum + "</p>\n" + "\n"
						 + "<p>Cookie:" + cookie + "</p>\n" + "\n"
						 +"<p>ServerID:" + serverID + "</p>\n" + "\n"
						 +"<p>RebootNum:" + rebootnum + "</p>\n" + "\n"
						 +"<p>From server:" + serverIDFound + "</p>\n" + "\n"
						 +"<p>list of server ID:" + metadata + "</p>\n" + "\n"
						 +"<p>Domain:" + domain + "</p>\n" + "\n"
						 + "<div><input type = \"submit\" name = \"Refresh\" value = \"Refresh\" ></div>"
						 + "<div><input type = \"submit\" name = \"Replace\" value = \"Replace\" ><input type"
						 + "= \"text\" name = \"message\"></div>"
						 + "<div><input type = \"submit\" name = \"Log_out\" value = \"Logout\"></div>"
						 + "</form>"
						 + "</body>"
						 + "</html>"; 
			return opt; 
		}	
	// RPC client sessionRead method
	public  String SessionRead(String sessionID, String version, InetAddress[] destAddr) throws IOException {
		
		// 打包数据
		callID = callID+1;
		int cid_now = callID;
		String op = "R";
		String mesg = callID + "," + op + "," + sessionID + "," + version; 
		byte[] outBuf = mesg.getBytes();
		
		byte[] inBuf = new byte[maxPacketSize];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		// send mesg and wait for response
		String result1 = "***";
		String result2 = "***";
		for (int i = 0; i < destAddr.length; i++) {
			DatagramSocket rpcSocket = new DatagramSocket();
			rpcSocket.setSoTimeout(10*1000);
			
			InetAddress address = destAddr[i];
			int length = outBuf.length;
			DatagramPacket sendPkt = new DatagramPacket(outBuf, length, address, portProj1bRPC);
			System.out.println("sendpktlength:"+sendPkt.getLength()+"");
			System.out.println(outBuf.length);
			System.out.println(sendPkt.getAddress());
			rpcSocket.send(sendPkt);
			try {
		
				recvPkt.setLength(inBuf.length);
				rpcSocket.receive(recvPkt);
				byte[] data = recvPkt.getData();
				String resp = new String(data);
				String[] info = resp.split(",");
				info[0] = info[0].replaceAll("[\u0000-\u001f]","");
				info[1] = info[1].replaceAll("[\u0000-\u001f]","");
				int cid = Integer.parseInt(info[0]);
				int f = Integer.parseInt(info[1]);
				if (cid == cid_now && f == 1) {
					result1 = info[2];
					result2 = info[3];
					break;
						
				}
			} catch(SocketTimeoutException stoe) {
				continue;	
			} catch (IOException ioe) {
			}
			rpcSocket.close();
		}
		result1=result1.replaceAll("[\u0000-\u001f]","");
		result2=result2.replaceAll("[\u0000-\u001f]","");
		return result1+"#"+result2;
	}

	// RPC client sessionWrite method 
	public String SessionWrite(String sessionID, String version, String s, String discard_time, InetAddress[] destAddr) throws IOException {
		
		// 打包数据
		callID = callID + 1;
		int cid_now = callID;
		String op = "W";
		String msg = callID + "," + op + "," + sessionID + "," + version + "," + s + ","  + discard_time;
		System.out.println("mesg!!:"+msg.length());
		String metadata = "";
		byte[] outBuf = msg.getBytes();
		byte[] inBuf = new byte[maxPacketSize];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		boolean f2 = true;
		int count = 0;
		// 发送数据
		for (int i = 0; i < destAddr.length && f2; i++) {
			DatagramSocket rpcSocket = new DatagramSocket();
			rpcSocket.setSoTimeout(10*1000);
			InetAddress address = destAddr[i];
			System.out.println("destination:" + address);
			int length = outBuf.length;
			DatagramPacket sendPkt = new DatagramPacket(outBuf, length, address, portProj1bRPC);
			System.out.println("test-test");
			rpcSocket.send(sendPkt);
			boolean f1 = true;
			try {
				while(f1) {
					recvPkt.setLength(inBuf.length);
					rpcSocket.receive(recvPkt);
					byte[] data1 = recvPkt.getData();
					String resp = new String(data1);
					String[] info = resp.split(","); 
					info[0] = info[0].replaceAll("[\u0000-\u001f]","");
					int cid = Integer.parseInt(info[0]);
					info[1] = info[1].replaceAll("[\u0000-\u001f]","");
					if (cid == cid_now ) {
						
						metadata = metadata + "_" + info[1];
						
						count++;
						if (count == WQ) {
							f2 = false;
						};
						break;
					}	
				}
			} catch(SocketTimeoutException stoe) {
				f1 =false;
				continue;
				
			} catch (IOException ioe) {
				System.out.println("write fail");
			}
			rpcSocket.close();
		}
	
		return metadata;
	}
	// read the IP addresses from simpleDB file
	public InetAddress[] getRandomIP(int W) {
		InetAddress[] Dest_Addr = new InetAddress[W];
		int t = (int)Math.random() * instance_num;
		for (int i = 0; i < W; i++) {
			if (t > instance_num) t=0;
			Dest_Addr[i] = IPAddr.get(t);
			t++;
		}
		return Dest_Addr;
	}
	
	
	public void getIP() throws UnknownHostException {
		String fileName = file_ip;
		String line = null;
		String R_path =getServletContext().getRealPath("/");
		String path = R_path+".."+"/";
		System.out.println("the local host address is "+InetAddress.getLocalHost().toString());
		try {
			FileReader fileReader = new FileReader(path+fileName);
			BufferedReader bufferReader = new BufferedReader(fileReader);
			while ((line = bufferReader.readLine()) != null) {
				String[] info = line.split(" ");
				IPAddr.put(Integer.parseInt(info[0]), InetAddress.getByName(info[1]));
			}
			bufferReader.close();
			
		}catch(FileNotFoundException ex){
			System.out.println("Unable to open the file");	
			ex.printStackTrace();
		}catch(IOException ex){
			System.out.println("Error reading file");
		}
	}
	
	public int getInstNum(){
		String fileName = file_instnum;
		String line = null;
		String inst_num= "";
		String R_path =getServletContext().getRealPath("/");
		String path = R_path+"../";

		try {
			FileReader fileReader = new FileReader(path+fileName);
			BufferedReader bufferReader = new BufferedReader(fileReader);
			while((line = bufferReader.readLine()) != null) {
				inst_num = line;
			}
			bufferReader.close();
		}catch(FileNotFoundException ex) {
			System.out.println("Unable to open the file");
			ex.printStackTrace();
		}catch(IOException ex) {
			System.out.println("Error reading file");
		}
		return Integer.parseInt(inst_num);
		
	}
	public int getServID() {
		String fileName = file_sirID;
		String line = null;
		String servid = "";
		String R_path =getServletContext().getRealPath("/");
		String path = R_path+"../";
		try {
			FileReader fileReader = new FileReader(path+fileName);
			BufferedReader bufferReader = new BufferedReader(fileReader);
			while((line = bufferReader.readLine()) != null) {
				servid = line;
			}
			bufferReader.close();
		}catch(FileNotFoundException ex) {
			System.out.println("Unable to open the file");
			ex.printStackTrace();
		}catch(IOException ex) {
			System.out.println("Error reading file");
		}
		return Integer.parseInt(servid);
	}
	public int getrebNum() {
		String fileName = file_reboot;
		String line = null;
		String servid = "";
		String R_path =getServletContext().getRealPath("/");
		String path = R_path + "../";
		try {
			FileReader fileReader = new FileReader(path+fileName);
			BufferedReader bufferReader = new BufferedReader(fileReader);
			while((line = bufferReader.readLine()) != null) {
				servid = line;
			}
			bufferReader.close();
		}catch(FileNotFoundException ex) {
			System.out.println("Unable to open the file");
			ex.printStackTrace();
		}catch(IOException ex) {
			System.out.println("Error reading file");
		}
		return Integer.parseInt(servid);
	}
}





