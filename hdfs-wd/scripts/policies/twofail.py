#!/usr/bin/python
import fInfo

class TwoFail:

	def filter(self, failInfo):
		failType = failInfo.ft
		isCrash = (failType.strip() == "CRASH")
		isBefore = failInfo.isBefore
		nodeId = failInfo.nodeId
		dn1 = (nodeId.find("DataNode-1") != -1)
		dn4 = (nodeId.find("DataNode-4") != -1)
		fst = failInfo.fst
		isTUpdateBlk = (fst.find("tryUpdateBlock") != -1)
		isRecvPkt = (fst.find("receivePacket") != -1)
		isRenameTo = (failInfo.fjp.find("renameTo") != -1)	
		isWrite = (failInfo.fjp.find("write") != -1)	
		notMetaTmp = (failInfo.tIoType.find("Meta") == -1) or (failInfo.tIoType.find("Tmp") == -1)

		#filter1 = (isCrash and (not isBefore) and dn4 and isRecvPkt and isWrite and self.isDataFile(failInfo.fType))
		filter1 = (isCrash and (not isBefore) and dn4 and isRecvPkt and isWrite)
		filter2 = (isCrash and (not isBefore) and dn1 and isTUpdateBlk and isRenameTo and notMetaTmp)
		return (filter1 or filter2)


	def isDataFile(self, fType):
		#return self.isBlockFile(tIoType) and (not self.isMetaFile(tIoType))
		print 'fType = ' + fType
		return ((fType.find("Blk") != -1) and (fType.find("Meta") != -1))

	'''
	def isBlockFile(self, tIoType):
		return ((tIoType.find("/dfs/data") != -1) and (tIoType.find("blk_") != -1))

	def isMetaFile(self, tIoType):
		return self.isBlockFile(tIoType) and (tIoType.find(".meta") != -1)
	'''	
