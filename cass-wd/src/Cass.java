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


import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.NotFoundException;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class Cass {

  Cassandra.Client client;
  Utility u;

  //Jinsu
  Driver driver;

  String keyspace;
  String columnFamily;
  ColumnPath columnPath;
  String encoding;


  // *******************************************
  public Cass(Driver d) {
    this.driver = d;
    this.u = d.getUtility();
  }


  // *******************************************
  // we only want to do this if client is null
  // we cannot create start this in Cass construct
  // because we don't know when namenode is up or down
  // *******************************************
  private void reconnectToCass() {
    //u.sleep(200);

    TTransport tr = new TSocket("localhost", 9160); //this takes .5sec
    TProtocol proto = new TBinaryProtocol(tr);
    waitForNodesToJoin();

    while (client == null) {

      try {

        u.print("- Trying to connect to Cass ... \n");

        // note that if the server is not woken up yet,
        // this will wait forever, because I hacked
        // the proxy a while ago


        client = new Cassandra.Client(proto);

        tr.open();

        u.print(String.format("- Connected to client ... \n"));



      } catch (Exception e) {
        //u.EXCEPTION(" In Cass construct", e);
        client = null;
        u.sleep(500);
      }
    }
  }

  // *******************************************
  private void waitForNodesToJoin() {
    //**********THIS NEEDS IMPROVEMENT.
    //Jinsu:
    for(int i = 0; i < Driver.NUM_OF_CASS_NODES; i++) {
	driver.waitForNodeRegistration("node"+i);
    }
		
    u.createNewFile(Driver.NODES_CONNECTED_FLAG);
    u.print("- All nodes connected\n");
		
  }




  public void setKeyspace(String ks) {
    keyspace = ks;
  }


  public String getKeyspace(){
    return keyspace;
  }


  public void setColumnFamily(String cf) {
    columnFamily = cf;
  }


  public String getColumnFamily() {
    return columnFamily;
  }


  public void setColumnPath(String cp) {
    try {
      columnPath = new ColumnPath(columnFamily);
      columnPath.setColumn(cp.getBytes(encoding));
    } catch(Exception e) {
      u.EXCEPTION("In setColumnPath.Cass ", e);
    }
  }

  public ColumnPath getColumnPath() {
    return columnPath;
  }

  public void setEncoding(String ecd) {
    encoding = ecd;
  }

  public String getEncoding() {
    return encoding;
  }


  // *******************************************
  public void assertConnection() {
    if (client == null) {
    	u.sleep(500);
    	reconnectToCass();
    }
  }


  // *******************************************
  public void insertEntry(String key, String value, Experiment exp) {

    u.print("- Cass.insertEntry : [ " + key + ", " + value + " ]" + "... Consistency => ALL\n");

    try {


        u.println("inserting for key... ");

        // u.sleep(3000);


        long timestamp = System.currentTimeMillis();

        client.insert(keyspace, key, columnPath, value.getBytes(encoding),
                      timestamp, ConsistencyLevel.ALL);
                      


      } catch (Exception e) {

        u.EXCEPTION("Cass.insertEntry fails", e);

        u.ERROR("Cass.insertEntry fails");
				
        // if we get here, the experiment has failed
        exp.markFailFromNonFrog();
        exp.addNonFrogReport("Cass.insertEntry() " + exp.getExpNum() +" FAILS!");
        
        //REMOVE THIS LATER
        //JINSU: I want to see the exception's name..
        exp.addNonFrogReport("--- Exception =>\t" + e.toString());
        
        
        exp.addExceptionToNonFrogReport(e);
				
      }

      u.print("- End of Cass.insertEntry!!! Yay\n");
      
  }





  // *******************************************
  public void getEntry(String key, Experiment exp) {

    u.print("- Cass.getEntry " + key +  "...\n");

    // if the Experiment already fails .. no need to move on
    if (exp.isFail()) {
      return;
    }

    try {

      //read single column

      //String exp_key = key + exp.getExpNum();
      u.println("single column:");
      Column col = client.get(keyspace, key, columnPath,
                              //ConsistencyLevel.QUORUM)
                              ConsistencyLevel.ONE)
        .getColumn();

      u.println("column name: " + new String(col.name, encoding));
      u.println("column value: " + new String(col.value, encoding));
      u.println("column timestamp: " + new Date(col.timestamp));
    } catch (Exception e) {
      u.EXCEPTION("Cass.getEntry fails", e);

      // u.ERROR("Cass.getEntry fails");

      //exp.markFailFromNonFrog();
      //exp.addNonFrogReport("Cass.getEntry(" + key + ") FAILS!");
      //exp.addExceptionToNonFrogReport(e);
    }
  }


  // *******************************************
  public void delete(String key, Experiment exp) {
    try {
      //String exp_key = key + exp.getExpNum();
      long timestamp = System.currentTimeMillis();
      client.remove(keyspace, key, columnPath, timestamp, ConsistencyLevel.ALL);
    } catch (Exception e) {
      u.EXCEPTION("Cass.delete fails", e);
    }
  }

}
