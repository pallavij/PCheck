/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hdfs;

import java.lang.Thread;
import java.io.PrintStream;
import java.lang.*;
import java.io.*;
import java.util.*;

import org.apache.hadoop.net.Node;
import org.apache.hadoop.net.NetworkTopology;
import org.apache.hadoop.net.NodeBase;
import org.apache.hadoop.util.FIUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hdfs.server.namenode.FSNamesystem;
import org.apache.hadoop.hdfs.server.namenode.DatanodeDescriptor;

// These utils cannot be under core because it needs
// HDFS classes (e.g. Node)

public final class HdfsFiUtil {


  // #######################################################################
  // #######################################################################
  // ####                                                               ####
  // ####               R A C K     A W A R E                           ####
  // ####                                                               ####
  // #######################################################################
  // #######################################################################

  // *************************************************
  public static String getRackNetLocation(String originalNL,
					  String hostName, int port) {
				       
    Configuration conf = new Configuration();
    boolean fiRackAware = conf.getBoolean("fi.rack.aware", false);
    
    if (!fiRackAware) 
      return originalNL;
    
    // some setup: 011100, will chose 123 first, and then 
    int rack;
    if      (port == 50011) rack = 0;
    else if (port == 50012) rack = 0;
    else if (port == 50013) rack = 0;
    else if (port == 50014) rack = 1;
    else if (port == 50015) rack = 1;
    else if (port == 50016) rack = 1;
    else if (port == 50017) rack = 0;
    else if (port == 50018) rack = 1;
    else                    rack = 0;
    
    String newNL =  String.format("/rack-%d", rack);
    return newNL;
    
  }

  // #######################################################################
  // #######################################################################
  // ####                                                               ####
  // ####     C O N T R O L    B Y     W O R K L O A D - D R I V E R    ####
  // ####                                                               ####
  // #######################################################################
  // #######################################################################


  public static final String TMP_FI = "/tmp/fi/";
  public static final String CONTROL_DIR = TMP_FI + "control/";
  public static final String REP_MONITOR_FLAG = CONTROL_DIR + "runRepMonitorFlag";
  public static final String BR_FLAG_STR = "blockReportFlag";
  public static final String BLOCK_REPORT_FLAG = CONTROL_DIR + BR_FLAG_STR;
  
  // *************************************************
  public static void removeRepMonitorFlag() {
    FIUtil.debug(">> FIUtil, removing rep monitor flag ");
    File f = new File(REP_MONITOR_FLAG);
    f.delete();
  }

  // *************************************************
  public static boolean isRepMonitorFlagExist() {
    File f = new File(REP_MONITOR_FLAG);
    if (f.exists()) return true;
    else return false;
  }

  // *************************************************
  public static boolean isBlockReportFlagExist(int dnId) {
    File f = new File(BLOCK_REPORT_FLAG + "-" + dnId);
    if (f.exists()) return true;
    else return false;
  }

  // *************************************************
  public static void removeBlockReportFlag(int dnId) {
    File f = new File(BLOCK_REPORT_FLAG + "-" + dnId);
    f.delete();
  }




  // #######################################################################
  // #######################################################################
  // ####                                                               ####
  // ####              R E M O V E    R A N D O M N E S S               ####
  // ####                                                               ####
  // #######################################################################
  // #######################################################################

  static int chooseNodeTryCount = 0;
  static int MAX_CHOOSE_NODE_TRY_COUNT = 10000; // pallavi-17
  static boolean removeRandomness = false;
  static Configuration conf = null;


  // ************************************************
  public static boolean isRemoveRandomness() {
    if (conf == null) {
      conf = new Configuration();
      removeRandomness = conf.getBoolean("fi.choose.nodes.non.random", false);
    }
    return removeRandomness;
  }

  // ************************************************
  // overwrite the returned node, if we do non-random
  // ************************************************
  public static DatanodeDescriptor returnFirstNode
    (DatanodeDescriptor originalRN, DatanodeDescriptor[] nodes) {

    if (nodes == null || originalRN == null)
      return originalRN;
    
    DatanodeDescriptor returnedNode = originalRN;

    FIUtil.debug(String.format("    [%s][randomly chosen by getDatanodeByHost] \n",
			       returnedNode.getName()));
    
    if (!isRemoveRandomness())
      return originalRN;
    
    for (int i = 0; i < nodes.length ; i++) {
      if (nodes[i].getName().compareTo(originalRN.getName()) < 0) {
	returnedNode = nodes[i];
      }
    }
    FIUtil.debug(String.format("    [%s][our deterministic choice, the least port] \n\n",
			       returnedNode.getName()));
    return returnedNode;
  }
  


  // ************************************************
  // haryadi: remove non-determinism, remove randomness (Place 2b)
  // return true, if pass, or we give up
  // return false, if HDFS should find new nodes
  // ************************************************
  public static boolean areChosenNodesSequential
    (NetworkTopology clusterMap,
     String choosenNode, String scope, List<Node> excludedNodes) {
    
    if (!isRemoveRandomness()) return true; // caller could proceed
    
    // (7) -- something is wrong                                                      
    if (!isPassDeterministic(clusterMap, choosenNode, scope, excludedNodes)) {
      if (chooseNodeTryCount++ > MAX_CHOOSE_NODE_TRY_COUNT)  {
	FIUtil.debug("\n\n    ERROR trycount exceeds many tries \n\n");
	chooseNodeTryCount = 0;
	return true; // give up, caller coould proceed
      }
      return false; // caller should continue looping, and choose new nodes
    }
 
    chooseNodeTryCount = 0;  // arggghhh, reset count, pallavi-17

    return true; // this is sorted, caller could proceed
  }
  
  
  // ************************************************
  public static boolean isPassDeterministic(NetworkTopology clusterMap,
					    String choosenNode, String scope,
					    List<Node> excludedNodes) {


    // ******************************************************
    // haryadi, remove randomness setup
    // ******************************************************
    String cm1 = clusterMap.toString();  // NetworkTopology clusterMap
    String [] cm2 = cm1.split("\n");
    List<String> cm3 = new ArrayList<String>(Arrays.asList(cm2));
    Collections.sort(cm3);
    cm3.remove(cm3.size()-1); // remove the last two entries "Expected ..." and
    cm3.remove(cm3.size()-1); // "Number ..." which comes from clusterMap.toString()
    List<String> sortedNetTopList = cm3;
    String[] sortedNetTopArray = (String[]) cm3.toArray(new String[cm3.size()]);
    // copied from chooseRandom
    // then it means we want the scope to be in excluded scope
    String excludedScope;
    if (scope.startsWith("~")) {
      excludedScope = scope.substring(1);
      scope = NodeBase.ROOT;
    }
    // else we have a specific scope in mind
    else {
      scope = scope;
      excludedScope = null;
    }


    // deepDebug
    if (false) {
      FIUtil.debug("");
      FIUtil.debug(">> RTC.isPassDeterministic (1) ");
      FIUtil.debug(">>     scope      = [" + scope + "]");
      FIUtil.debug(">>     exScope    = [" + excludedScope + "]");
      FIUtil.debug(">>     choosenN   = " + choosenNode);
      FIUtil.debug(">>     excludedN  = " + excludedNodes);
      FIUtil.debug(">>     sortedTopo = " + sortedNetTopList);
    }

    // define what we expect .. what we expec is given the nodes(rack#)
    // we want to get the next available node, not in the excluded nodes
    String expectedDeterministicNode = null;
    for (int i = 0; i < sortedNetTopArray.length; i++) {

      String curNode = sortedNetTopArray[i];

      // 1) make sure the rack requirement pass
      // possibilities <scope, excludedScope>:
      // <""      , null>      --> passRack = true
      // <"/rackX", null>      --> passRack = true (IF curRack == rackX)
      // <""      , "/rackX">  --> passRack = true (IF curRack != rackX)
      // <"/rackX", "/rackY">  --> do we ever get here??
      boolean passRack;
      // any rack is fine
      if (excludedScope == null && scope.equals(NodeBase.ROOT)) {
        passRack = true;
      }
      // rack must be in scope
      else if (excludedScope == null && !scope.equals(NodeBase.ROOT)) {
        if (curNode.contains(scope)) passRack = true;
        else                         passRack = false;
      }
      // rack must not be in ex scope
      else if (excludedScope != null && scope.equals(NodeBase.ROOT)) {
        if (!curNode.contains(excludedScope)) passRack = true;
        else                                  passRack = false;
      }
      // rack must be in scope but not in ex scope
      else if (excludedScope != null && !scope.equals(NodeBase.ROOT)) {
        if (curNode.contains(scope) && !curNode.contains(excludedScope))
          passRack = true;
        else
          passRack = false;
      }
      else {
        passRack = false;
        FSNamesystem.LOG.info("ERROR: We should not get here .. see nn.out!! kill me");
        FIUtil.debug("ERROR: We should not get here .. see nn.out!! kill me");
        FIUtil.printStackTrace();
        System.out.flush();
        System.exit(-13);
      }
      if (excludedScope != null && curNode.contains(excludedScope)) {
        passRack = false;
      }
      // excludeScope is null
      else {
        if      (scope.equals(NodeBase.ROOT)) passRack = true;
        else if (curNode.contains(scope))     passRack = true;
        else                                  passRack = false;
      }
      if (!passRack)
        continue;

      // 2) make sure it's not in the excluded nodes
      boolean passExclude = true;
      for (int j = 0; j < excludedNodes.size() ; j++) {
        String excludeAddr = excludedNodes.get(j).getName();
        if (curNode.contains(excludeAddr))  {
          passExclude = false;
          break;
        }
      }
      if (!passExclude)
        continue;

      // if so, we get the next available expected node in the rack
      // then let's just break
      expectedDeterministicNode = curNode ;
      break;
    }

    if (expectedDeterministicNode == null) {
      FIUtil.debug("## ERROR RepTargetChooser does not find the ");
      FIUtil.debug("## expected deterministic node \n" + FIUtil.getStackTrace());
      return false;
    }
    
    FIUtil.debug(String.format("    [chosen: %s][expected: %s] ", 
			       choosenNode,
			       expectedDeterministicNode));
    


    // now compare choosen node with expected one
    if (expectedDeterministicNode.contains(choosenNode)) {
      return true;
    }

    return false;
  }
  // ****************************************************** end of issPassDeterministic

  //this is used so that the last datanode in the pipeline and the other datanodes use the same 
  //function for replyOut.flush()
  public static void commonReplyOutFlush(DataOutputStream replyOut) throws IOException {
	       replyOut.flush();
  }

}
