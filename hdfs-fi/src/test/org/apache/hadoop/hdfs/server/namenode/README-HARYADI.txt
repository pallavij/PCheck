- CreateEditsLog.java
    Pallavi: NameNode restart with lots of corruptions ... 
             and read failures ..
             DataNode restart.
- FSImageAdapter.java
    Inject a edit log spy 
 
- FileNameGenerator.java
    Not for failure, this generates files per directory, and checks
    if that directory will split into two directories or not

- NNThroughputBenchmark.java
- NameNodeAdapter.java

- TestBlockTokenWithDFS.java
    Eli: TestblockTokenWithHDFS, Token creation? what is token?

- TestCheckPointForSecurityTokens.java

- TestCheckpoint.java
    Pallavi/Eli: TestCheckpoint, roll edit log and crash

- TestDFSConcurrentFileOperations.java
    Pallavi/Eli: TestDFSConcurrentFileOperations, test failed append + lease recovery

- TestDatanodeDescriptor.java
    Pallavi/Eli: TestDatanodeDescriptor.java, invalidate blocks in datanode    

- TestDecommissioningStatus.java
    Pallavi/Eli: TestDecommissioningStatus.java, anything 

- TestEditLog.java
    Pallavi: TestEditLog, test write fs operations

- TestEditLogRace.java
- TestFileLimit.java

- TestFsck.java
    Pallavi/Eli: TestFsck, what does this test?

- TestHeartbeatHandling.java
    Pallavi/Eli: TestHeartBeat, test FSNS handles hearbeat right, how??

- TestHost2NodesMap.java

- TestLeaseManager.java
    Pallavi/Eli: TestLeaseManager, two leases for a single holder, ??

- TestMetaSave.java
    Pallavi/Eli: TestMetaSave, meta save, for what?

- TestNNThroughputBenchmark.java
- TestNameEditsConfigs.java
    Pallavi/Eli: TestNameEditsConfigs, simulate failure scenarios (nice!)

- TestNamenodeCapacityReport.java

- TestNodeCount.java
    Pallavi/Eli: TestNodeCount, stall and restart replication monitor work, 

- TestOverReplicatedBlocks.java
    Pallavi/Eli: TestOverReplicatedBlock, don't treat corrupt replicas as good ones (nice!)

- TestPendingReplication.java
    Pallavi/Eli: TestPendingReplication, test internals of pending replication

- TestReplicationPolicy.java
    Pallavi/Eli: TestReplicationPolicy, rack-aware policy

- TestSafeMode.java
- TestSaveNamespace.java
    Pallavi/Eli: TestSaveNamespace, various failure scenarios during saveNamespace() nice!!!!!!

- TestSecurityTokenEditLog.java
- TestStartup.java
    Pallavi/Eli: TestStartup, ???

- TestStorageRestore.java
    Pallavi/Eli: TestStorageRestore, ??

- TestUnderReplicatedBlocks.java
    Pallavi/Eli: TestUnderReplicatedBlocks, ???
