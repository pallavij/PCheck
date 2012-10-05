package election;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

public class Client implements Watcher {
	
	private ZooKeeper zk;
	private String cid;
	private String path;

	public Client(String id) throws IOException, KeeperException, InterruptedException{
	
		cid = id; 	

		System.out.println("CLIENT " + cid + " :: STARTING\n");	
		zk = new ZooKeeper("127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183", 3000, this);
		System.out.println("CLIENT " + cid + " :: FINISHED STARTING\n");	
	
		path = "/election";
		createProposal();
		leaderElection();
		
		//int pause = new Scanner(System.in).nextInt();
	}

	private void createProposal() throws KeeperException, InterruptedException{
		
		// If it is the first client, then it should create the znode "/election"
		Stat stat = zk.exists(path, false);
		if(stat == null){
			System.out.println("CLIENT " + cid + " :: Im the first client, creating " + path + ".");
			String r = zk.create(path, new byte[0], Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
			System.out.println("CLIENT " + cid + " :: " + r + " created.");
		}
		
		String childPath = path + "/n_";
		childPath = zk.create(childPath, new byte[0], Ids.OPEN_ACL_UNSAFE,
				CreateMode.EPHEMERAL_SEQUENTIAL);
		System.out.println("CLIENT " + cid + " :: My leader proposal created. Path = " + childPath + ".");

	}

	private void leaderElection() throws KeeperException, InterruptedException{
		
		// Let C be the children of /election, and i be the sequence number of z;
		// Watch for changes on "/election/n_j", where j is the smallest sequence
		// number such that j < i and n_j is a znode in C
		List<String> children = zk.getChildren(path, false);
		String tmp = children.get(0);
		
		for(String s : children){
			if(tmp.compareTo(s) > 0)
				tmp = s;	
		}
		
		// tmp contains the smallest sequence number
		String leader = path + "/" + tmp;
		Stat s = zk.exists(leader, true);
		
		System.out.println("CLIENT " + cid + " :: Leader is the owner of znode: " + leader);
		System.out.println("CLIENT " + cid + " :: Leader id: " + s.getEphemeralOwner());
		
	}
	
	public void process(WatchedEvent event) {
		
		switch (event.getType()){
		
		case NodeChildrenChanged:
			System.out.println("CLIENT " + cid + " :: NodeChildrenChanged | ZNode: " + event.getPath());
			break;
			
		case NodeCreated:
			System.out.println("CLIENT " + cid + " :: NodeCreated | ZNode: " + event.getPath());
			break;
		           
		case NodeDataChanged:
			System.out.println("CLIENT " + cid + " :: NodeDataChanged | ZNode: " + event.getPath());
			break;
		           
		case NodeDeleted:
			System.out.println("CLIENT " + cid + " :: NodeDeleted | ZNode: " + event.getPath());
			System.out.println("CLIENT " + cid + " :: Leader was lost, new leader election started.");
			try {
				leaderElection();
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
			
		case None:
			
			switch (event.getState()){
			
			case Disconnected:
				System.out.println("CLIENT " + cid + " :: Disconnected.");
				break;
				
			case Expired:
				System.out.println("CLIENT " + cid + " :: Expired.");
				break;
		
			case NoSyncConnected:
				System.out.println("CLIENT " + cid + " :: NoSyncConnected - Deprecated.");
				break;
				
			case SyncConnected:
				System.out.println("CLIENT " + cid + " :: SyncConnected.");
				break;
			
			case Unknown:
				System.out.println("CLIENT " + cid + " :: Unknown - Deprecated.");
				break;
			}
			
		}
		
	}


}
