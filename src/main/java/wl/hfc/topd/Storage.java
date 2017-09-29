package wl.hfc.topd;


import java.util.ArrayList;  
import java.util.List;  
  
public class Storage {  
  
    private List<String> storage;//生产者和消费者共享的仓库  
    public Storage() {  
        storage = new ArrayList<String>();  
    }  
    public List<String> getStorage() {  
        return storage;  
    }  
    public void setStorage(List<String> storage) {  
        this.storage = storage;  
    }  
      
}  