package first_servlet;

import java.util.concurrent.ConcurrentHashMap;

public class SessionTbl {
	private ConcurrentHashMap<String, Session> sessiontable= 
			new ConcurrentHashMap<String, Session>();
	public SessionTbl(){
		
	}
	public ConcurrentHashMap<String, Session> getSessiontable(){
		return sessiontable;
	}
}