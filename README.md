# largescaleinformationsystem

README

Team Member: Liwei Han, Mengyuan Yang, Zhenglin Lu

Overview:
When a client visiting the website, one server will create a cookie specified by the server and session information for that client sending back to the client. For communication and updating between servers, we use RPCwrite depending on client's request and send W requests to other servers randomly and looking forward to WQ responses or use RPCread, sending WQ request to other servers and waiting for 1 response. Values for W and WQ depends on the fault-tolerant need. IN our system, which is one fault-tolerant(F=1), the value of W is 3(2F+1) and the value of WQ is 2(F+1).If we fails to receive WQ response, we see it as an failure and inform client. For each server, we have a RPCserver and a garbage collector run as background thread, keep checking port and timeout session. Local server will store ip information read from a file named 'file_ip' for all the possible servers. SimpleDB stores all ip information for all the servers, helping a newly created or rebooted servers retrieve ip information of all other servers for their future communications. The script create files like local-ipv4, IP_info, instance_num, ami-launch-index, and Reboot_num for the use of our servlet.

File structure:

For Display and Server page layout:
f_servlet.java : every time the application server get request, it prints all the contents and java servlet helps interact with users' request.


For Garbage Collector:
GC.java: A thread keeps going with a 5 seconds delay and compares session data discard time with current timestamp and deletes outdated data from session table.


For RPC:
RPC client stub:
f_servlet.java:we put RPCclientSessionWrite and RPCclientSessionRead and have them called based on clients' request. Every time we call these two function based on client's request, we wrap the information into packet and send it to W or WQ(defined by our need) servers sequentially, waiting for their RPCServers' responses.
RPC server:
RPCServer.java: A thread receives and unwraps packets, processing packet depending on read or write options and then put it into SessionTable and send back responses also depending on request types.
SessionTbl.java: we use sessionTbl to manage session data such as building a local sessiontable as : ConcurrentHashMap<String, Session> sessiontable to store sessionid +"/"+ versionid(key) and corresponding session (value) pairs.
SessionTbl and RPCServer together serves as “data brick” in our system.

RPC message is defined as: String message = callID + "," + readwriteoption + "," + sessionID + "," + version; 

A RPCclientSessionWrite function is like:
public String SessionWrite(String sessionID, String version, String s, String discard_time, InetAddress[] destAddr) throws IOException, and with callID + "," + serverid as return, and we use serverid to build metadata;

RPCclientSessionRead function is like:
String SessionRead(String sessionID, String version, InetAddress[] destAddr) throws IOException and with result as return which consists of callID+","+ 1 + "," + message; (1 can be 0 when we can't find the session in local sessiontable or timeout);
		
For Session:
Session.java: defines the structure of session and all information as:
private int version_number; private String expire_time; private String session_id; private String message; private Timestamp discard_time; static int sess_num;
Sessionid is defined as serverid+"_"+rebootnum+"_"+sessionnum


For Cookies:
f_servlet.java : we use built-in cookie class and cookie is created with name "CS5300PROJ1SESSION" and value(String sessionid, int versionnumber, String metadata), and also set with domain: ".amazonaws.com", and maxAge "30".
When we use String metadata to store all serverid divided by "_" and updated when we sessionwrite or sessionwrite. So we can tell how many responses by the number of elements of splitting metadata with "_" to see if we receive WQ responses.

For parameter we need to define based on needs:
    private static final int W = 3;// send times
	private static final int WQ = 2;// looking forward to the number of responses to be successful
	final static int portProj1bRPC = 5300;
	final static int maxPacketSize = 512;
	static String file_ip = "IP_info.txt";
	static String file_localip = "local-ipv4.txt";
	static String file_reboot = "Reboot_num.txt";
	static String file_sirID = "ami-launch-index.txt";
	static String file_instnum = "instance_num.txt";

For script and simpleDB:
launch.sh: we use this script to do the prepared work: transfer .war file to AWS S3, create simpleDB domain, launch instances and run Installation script.
The parameters we used here are: S3_BUCKET, imageID, user name, key pair, security group name and security group ID.

InstallationScript.sh: In this script, we do the following tasks:
1. install java 1.8
2. install tomcat8
3. download the .war file
4. generate the instance metadata files
5. push local ip and server ID to SimpleDB
6. download all servers ip and server ID to a local file
7. start tomcat8 server

reboot.sh: we update the reboot number of the server first and start tomcat8 server.
