package wl.hfc.topd;

import java.util.LinkedList; 
import java.util.Queue; 
import java.util.Random; 
import java.util.List;  
import java.util.UUID;  

  
public class Producer extends Thread{  
  
    private List<String> storage;//生产者仓库  
    public Producer(List<String> storage) {  
        this.storage = storage;  
    }  
    public void run(){  
        //生产者每隔1s生产1~100消息  
        long oldTime = System.currentTimeMillis();  
        int i=0;
        while(true){  
            synchronized(storage){  
                	storage.add("ddd"+i);
                    System.out.println("生产"+"ddd"+i);  
                    storage.notify();                     
            
                    i++;
                }  
            try {
    						Thread.sleep(500);
    					} catch (InterruptedException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
            }  
        }  
    }  
 