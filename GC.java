package first_servlet;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;

public class GC implements Runnable {
	private SessionTbl sm;
	private long timetosleep = 5;

	public GC() {
	}
	public GC(SessionTbl sm) {
		this.sm = sm;
	}

	@Override
	public void run() {
		while (true) {
			while (timetosleep > 0) {
				long start = System.currentTimeMillis();
				try {
					Thread.sleep(timetosleep*1000);
					break;
				} catch (InterruptedException e) {
					// every interruption, we compute time to sleep
					long now = System.currentTimeMillis();
					long range = now - start;
					timetosleep -= range;
				}
			}
			if(sm!=null){
				ConcurrentHashMap<String,Session> conhm=
						sm.getSessiontable();
				for(String key:conhm.keySet()){
					if(conhm.get(key)!=null){
						Timestamp old=sm.getSessiontable().get(key).get_disctime();
						Timestamp current=new Timestamp(System.currentTimeMillis());
						if(current.compareTo(old)>0){
							sm.getSessiontable().remove(key);
							System.out.println("gc:" + key);
						}
					}
				}
			}
			timetosleep=5;
		}
	}

}