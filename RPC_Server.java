package first_servlet;

import java.io.*;

import javax.servlet.http.*;
import javax.servlet.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.HashSet;

public class RPC_Server implements Runnable {

	SessionTbl sm = new SessionTbl();
	
	public void run() {
	
		DatagramSocket RPC_socket = null;
		try {// lisetne to specific port
			RPC_socket = new DatagramSocket(5300);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		System.out.println("RPC server starts!");
		while (true) {
			HashSet<String> ips = new HashSet<String>();// 存ip

			byte[] in_buf = new byte[512];
			DatagramPacket rcv_pckt = new DatagramPacket(in_buf, in_buf.length);
			try {
				RPC_socket.receive(rcv_pckt);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} 
			// get return address
			InetAddress return_addr = rcv_pckt.getAddress();
			int return_port = rcv_pckt.getPort();
			if (return_addr != null)
				ips.add(return_addr.getHostAddress());
			// add data
			// parse recv data
			String pckt_data = new String(rcv_pckt.getData(), 0, rcv_pckt.getLength());
			String[] tuple = pckt_data.split(",");
			String callID = tuple[0];
			String op = tuple[1];
			String ssid = tuple[2];
			String ver = tuple[3];
			String result = "";
			switch (op) {
			case "R":
				Session data = sm.getSessiontable().get(ssid+"/"+ ver);
				if (data == null||System.currentTimeMillis()>data.get_time2().getTime()) {// 有问题，如果data为空
					result = callID + "," + 0;// 0代表没有, ‘_’只分
				} else {
					String message = data.get_mess();
					result=callID+","+ 1 + "," + message+","+ f_servlet.severID;
				}
				break;
			case "W":
				int wver = Integer.parseInt(ver);
				String wmessage = tuple[4];
				String discard_time = tuple[5];
				Timestamp d_t = new Timestamp(Long.parseLong(discard_time));
				Session wsession = new Session(wmessage, ssid, wver + 1, d_t);
				String key = ssid + "/" + wver;
				sm.getSessiontable().put(key, wsession);
				int serverid = f_servlet.severID;
				result = callID + "," + serverid;// as ack
				break;
			default:
				result = "invalid";
			}
			System.out.println("RPC server response: " + result);
			byte[] out_buf = result.getBytes();
			DatagramPacket send_pckt = new DatagramPacket(out_buf, out_buf.length, return_addr, return_port);
			try {
				RPC_socket.send(send_pckt);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

}
