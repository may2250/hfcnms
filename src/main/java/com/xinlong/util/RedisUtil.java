package com.xinlong.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


/**  
 * 连接和使用redis资源的工具类    
 * @author watson   
 * @version 0.5   
 */ 
public class RedisUtil {
	
	/**       
	 * 数据�?      
	 */     
	private JedisPool jedisPool;
	
	/**      
	 * 获取数据库连�?       
	 * @return conn       
	 */     
	public Jedis getConnection() {
		Jedis jedis=null;          
		try {          			
			jedis=jedisPool.getResource();          
		} catch (Exception e) {              
			e.printStackTrace();   
			jedisPool.returnBrokenResource(jedis);
			jedis = null;
		}          
		return jedis;      
	}   
	
	/**       
	 * 关闭数据库连�?       
	 * @param conn       
	 */     
	public void closeConnection(Jedis jedis) {          
		if (null != jedis) {              
			try {                  
				jedisPool.returnResource(jedis);              
			} catch (Exception e) {
					e.printStackTrace();              
			}          
		}      
	}  
	
	/**       
	 * 设置数据       
	 * @param conn       
	 */     
	public boolean setData(String key,String value) {
		try {                  
			Jedis jedis=jedisPool.getResource();                  
			jedis.set(key,value);                  
			jedisPool.returnResource(jedis);                  
			return true;              
		} catch (Exception e) {
			e.printStackTrace();                                
		}          
		return false;      
	}
	
	/**       
	 * 获取数据       
	 * @param conn       
	 */     
	public String getData(String key) {
		String value=null;              
		try {
			Jedis jedis=jedisPool.getResource();
			value=jedis.get(key);                  
			jedisPool.returnResource(jedis);                  
			return value;              
		} catch (Exception e) {
			e.printStackTrace();                                
		}          
		return value;      
	}            
	
	/**       
	 * 设置连接�?       
	 * @param 数据�?      
	 */     
	public void setJedisPool(JedisPool JedisPool) {
		this.jedisPool = JedisPool;      
	}       
	
	/**       
	 * 获取连接�?       
	 * @return 数据�?       
	 */     
	public JedisPool getJedisPool() {
		return jedisPool;      
	}     
} 

