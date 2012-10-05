package election;

import java.io.IOException;

import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig.ConfigException;

public class Server {

	String id;
	String conf;

	public Server(String id, String conf){
		this.id = id;
		this.conf = conf;
	}

	public void start(){
		ZooKeeperServerMain zkserver = new ZooKeeperServerMain();
		ServerConfig sc = new ServerConfig();
		//QuorumPeerConfig qpc = new QuorumPeerConfig(); 
		
		try {
			sc.parse(conf);
		} catch (ConfigException e) {
			e.printStackTrace();
		}
		
		System.out.println("SERVER " + id + " :: STARTED\n");
		
		try {
			zkserver.runFromConfig(sc);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("SERVER " + id + " :: AFTER runFromConfig. Something is very wrong.\n");
	}
	
	
}
