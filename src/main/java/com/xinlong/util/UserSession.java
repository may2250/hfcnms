package com.xinlong.util;

import javax.websocket.Session;

public class UserSession {
	String username;
	Session session;
	
	public UserSession(String uname, Session session){
		this.username = uname;
		this.session = session;
	}
	
	public String getId(){
		return this.session.getId();
	}
}
