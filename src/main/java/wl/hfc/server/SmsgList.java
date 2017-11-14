package wl.hfc.server;


import java.util.ArrayList;  
import java.util.List;  
  
public class SmsgList {  
  
    public static  List<String> storage;//生产者和消费者共享的仓库  
    public static  List<String> alarmstorage;//生产者和消费者共享的仓库  
    public static  List<String> paknelstorage;//生产者和消费者共享的仓库  
    public static SmsgList me;
    public SmsgList() {  
        storage = new ArrayList<String>(); 
        alarmstorage = new ArrayList<String>(); 
        paknelstorage = new ArrayList<String>(); 
        me =this;
    }  
    public List<String> getStorage() {  
        return storage;  
    }  
    public void setStorage(List<String> storage) {  
        storage = storage;  
    }  
      
}  