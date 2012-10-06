/**
 * Copyright (c) 2012,
 * Thanh Do  <thanhdo@cs.wisc.edu>
 * Haryadi S. Gunawi  <haryadi@cs.uchicago.edu>
 * Pallavi Joshi  <pallavi@cs.berkeley.edu>
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p/>
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p/>
 * 3. The names of the contributors may not be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */



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
