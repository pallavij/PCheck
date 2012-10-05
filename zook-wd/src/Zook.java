

package org.fi;

import java.io.*;
import java.nio.channels.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import java.util.Arrays;


import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;


import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;




public class Zook {


  ZooKeeper zk;
  Utility u;


  // *******************************************
  public Zook(Driver d) {

    this.u = d.getUtility();

  }


  // *******************************************
  // we only want to do this if client is null
  public void reconnectToZooKeeper() {
    try {
      //PALLAVI: changing the second argument (sessionTimeout) to a very large value	    
      //zk = new ZooKeeper("localhost", 30000, null);    
      zk = new ZooKeeper(getConnStr(Driver.NUM_OF_ZK_NODES, 2180), 300000, null);    
    } catch (Exception e) {
      u.FATAL(" Can't create new ZooKeeper");
    }
    
    boolean fail = true;
    
    while (fail) {
      try {
	u.print("- Trying to connect to ZooKeeper ... \n");
	zk.exists("/test", false);
        u.print(String.format("- Connected to zookeeper !! \n"));
	fail = false;
      } catch (Exception e) {
	fail = true;
        u.sleep(500);
      }
    }
  }

  
  // *******************************************
  // we only want to do this if client is null
  public void reconnectToZooKeeper(int zkServer) {
    try {
      //PALLAVI: changing the second argument (sessionTimeout) to a very large value	    
      //zk = new ZooKeeper("localhost", 30000, null);    
      zk = new ZooKeeper("127.0.0.1:" + (2180 + zkServer), 300000, null);    
    } catch (Exception e) {
      u.FATAL(" Can't create new ZooKeeper");
    }
    
    boolean fail = true;
    
    while (fail) {
      try {
	u.print("- Trying to connect to ZooKeeper ... \n");
	zk.exists("/test", false);
        u.print(String.format("- Connected to zookeeper !! \n"));
	fail = false;
      } catch (Exception e) {
	fail = true;
        u.sleep(500);
      }
    }
  }


  private String getConnStr(int n, int port){
	String c = "";
	for(int i = 1; i <= n; i++){
		c += "127.0.0.1:" + (port+i);
		if(i != n){
			c += ",";
		}
	}
	return c;
  }
  
  // *******************************************
  public void assertConnection() {
    if (zk == null)
      reconnectToZooKeeper();
  }
  
  
  // *******************************************
  public void assertConnection(int sid) {
    if (zk == null)
      reconnectToZooKeeper(sid);
  }

  
  // *******************************************
  public void create(String path, Experiment exp) {

    u.print("- Zk.create : " + path + "\n");

    assertConnection();

    try {

      zk.create(path, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
    
    } 
    catch (Exception e) {

      u.EXCEPTION("Zk.create fails", e);
      u.ERROR("Zk.create fails");

    }
    
    u.print("- End of Zk.create\n");

  }



  // *******************************************
  public void exists(String path, Experiment exp) {
    
    u.print("- Zk.exist " + path +  "...\n");
    
    // if the Experiment already fails .. no need to move on
    if (exp.isFail()) {
      return;
    }

    try {
      
      Stat st = zk.exists(path, true);
      u.println("Exist Stat : " + st);
      
    } catch (Exception e) {

      u.EXCEPTION("Zk.exists fails", e);
      u.ERROR("Zk.exists fails");
      
    }

  }


  // *******************************************
  public void delete(String path) {
    
    u.print("- Zk.delete " + path +  "...\n");
    
    try {
      
      zk.delete(path, -1);
      
    } catch (Exception e) {

      u.EXCEPTION("Zk.delete fails", e);
      u.ERROR("Zk.delete fails");
    
    }
  }


  // *******************************************
  public void setdata(String path, byte[] data, int version) {
	u.print("- Zk.setdata " + path +  "...\n");
	try{
		Stat s = zk.setData(path, data, version);	
	}
        catch(Exception e){
	        u.EXCEPTION("Zk.setdata fails", e);
	        u.ERROR("Zk.setdata fails");
	} 	
  }


  // *******************************************
  public void close(){
	try{
		if(zk != null){
			zk.close();
		}
	}
	catch(Exception e){
	        u.EXCEPTION("Zk.close fails", e);
	        u.ERROR("Zk.close fails");
	}
  } 

  public boolean areServersConsistent(int n, int port, String path){
	try{
		byte[] data = null;
		for(int i = 1; i <= n; i++){
			ZooKeeper zkc = new ZooKeeper("127.0.0.1:" + (port+i), 300000, null);
			byte[] cdata = zkc.getData(path, false, null);	
			if(i == 1){
				data = cdata;
			}
			else{
				boolean eq = Arrays.equals(cdata, data);
				if(!eq){
					u.ERROR("Different views of data between servers");
					return false;
				}
			}
		}
		return true;
	}
	catch(Exception e){
				u.EXCEPTION("Zk.areServersConsistent fails", e);
				return false;
	}
  }

}
