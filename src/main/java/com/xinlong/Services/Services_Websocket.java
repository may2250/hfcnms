package com.xinlong.Services;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;


@ServerEndpoint("/websocketservice")
public class Services_Websocket {
	private static CopyOnWriteArraySet<Session> webSocketClients = new CopyOnWriteArraySet<Session>();

	private static Logger log = Logger.getLogger(Services_Websocket.class);
	
    @OnMessage
    public void onMessage(String message, Session session) throws IOException, InterruptedException {

        // Print the client message for testing purposes
        System.out.println("Received: " + message);

        // Send the first message to the client
        session.getBasicRemote().sendText("This is the first server message");
        
        // Send 3 messages to the client every 5 seconds
        int sentMessages = 0;
        while (sentMessages < 3) {
            Thread.sleep(5000);
            session.getBasicRemote().sendText("This is an intermediate server message. Count: " + sentMessages);
            sentMessages++;
        }        
        // Send a final message to the client
        session.getBasicRemote().sendText("This is the last server message");

    }

    @OnOpen
    public void onOpen(Session session) {
    	webSocketClients.add(session);
        System.out.println("Client connected::::" + webSocketClients.size());
    }

    @OnClose
    public void onClose(Session session) {
    	webSocketClients.remove(session);
        System.out.println("Connection closed::::" + webSocketClients.size());        
    }

}