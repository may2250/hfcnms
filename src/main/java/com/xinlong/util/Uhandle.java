package com.xinlong.util;

import javax.websocket.Session;

public class Uhandle {
	public String username;
	Session session;
	
	public Uhandle(String uname, Session session){
		this.username = uname;
		this.session = session;
	}
	
	public String getId(){
		return this.session.getId();
	}
}
