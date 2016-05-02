package first_servlet;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;



/*
 * session class, contain 5 fields (version number, expire time, session ID, message, and location_data) 
 * and corresponding 5 getter method.
 * constructor initialize the 5 fields' value.
 */
public class Session {
	private int version_number;
	private String expire_time;
	private String session_id;
	private String message;
	private Timestamp discard_time;
	static int sess_num = 0;

	public Session(String s, String sid) {
		sess_num++;
		session_id =sid + sess_num;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dt = new Date(System.currentTimeMillis()+120*1000);
		expire_time = dateFormat.format(dt);
		discard_time = new Timestamp(System.currentTimeMillis()+120*1000 + 10*1000);
		version_number = 1;
		message = s;
	}
	public Session(String s, String s_id, int num, Timestamp discard_time) {
		session_id = s_id;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dt = new Date(discard_time.getTime()-10*1000);
		expire_time = dateFormat.format(dt);
		version_number = num;
		this.discard_time = discard_time;
		message = s;
	}
	public Session(String s, String s_id, int num, Timestamp discard_time, String etime) {
		session_id = s_id;
		expire_time = etime;
		version_number = num;
		this.discard_time = discard_time;
		message = s;
	}
	public String get_time() {
		return expire_time;
	}
	public String get_sid() {
		return session_id;
	}
	public String get_mess() {
		return message;
	}
	public int get_vnum() {
		return version_number;
	}
	public Timestamp get_time2() {
		return new Timestamp(System.currentTimeMillis()+120*1000);
	}
	public Timestamp get_disctime() {
		return discard_time;
	}
}
