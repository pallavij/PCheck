package election;

public class Election{

	static Server s1 = null;
	static Server s2 = null;
	static Server s3 = null;

	static Client c1 = null;
	static Client c2 = null;
	static Client c3 = null;

	public static void main(String[] args){
		try{
			s1 = new Server("One", "/Users/pallavi/Research/faultInjection/zook-fi/conf/zoo1.cfg");
			s2 = new Server("Two", "/Users/pallavi/Research/faultInjection/zook-fi/conf/zoo2.cfg");
			s3 = new Server("Three", "/Users/pallavi/Research/faultInjection/zook-fi/conf/zoo3.cfg");

			//s1.start();
			//s2.start();
			//s3.start();

			Thread t1 = new Thread(){public void run(){s1.start();}};
			Thread t2 = new Thread(){public void run(){s2.start();}};
			Thread t3 = new Thread(){public void run(){s3.start();}};

			t1.start(); t2.start(); t3.start();
			//t1.join(); t2.join(); t3.join();

			Thread.sleep(2000);

			c1 = new Client("One");
			Thread.sleep(6000);
			c2 = new Client("Two");
			Thread.sleep(6000);
			c3 = new Client("Three");
			Thread.sleep(6000);
		}
		catch(Exception e){
			System.err.println("Exception in the leader election process");
			e.printStackTrace();
		}
	}

}
