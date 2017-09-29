package wl.hfc.topd;


import java.util.ArrayList;
import java.util.List;  
  
public class Consumer extends Thread{  
  
    private List<String> storage;//仓库  
    public Consumer(List<String> storage) {  
        this.storage = storage;  
    }  
    private List<String> tmplist=new ArrayList<String>(); 
    public void run(){  
        while(true){  
            synchronized(storage){  
                //消费者去仓库拿消息的时候，如果发现仓库数据为空，则等待  
                if (storage.isEmpty()) {  
                    try {  
                        storage.wait();  
                    } catch (InterruptedException e) {  
                        e.printStackTrace();  
                    }  
                }  
             
                tmplist.clear();
                for(String attribute : storage) {
                	tmplist.add(attribute);
                	}
                storage.clear();                
 
            }  
            for(String attribute : tmplist) {
                System.out.println("线程"+this.getName()+"成功消费  "+attribute);  
                try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	}
            
      
       
        }  
    }  
}  