#!/usr/bin/python
import fInfo

class OneFail:

	def filter(self, failInfo):
		failType = failInfo.ft
		isCrash = (failType.strip() == "CRASH")
		isBefore = failInfo.isBefore
		nodeId = failInfo.nodeId
		dn1 = (nodeId.find("DataNode-1") != -1)
		fst = failInfo.fst
		isTUpdateBlk = (fst.find("tryUpdateBlock") != -1)
		isRenameTo = (failInfo.fjp.find("renameTo") != -1)	
		notMetaTmp = (failInfo.tIoType.find(".meta_tmp") == -1)

		if isCrash and (not isBefore) and dn1 and isTUpdateBlk and \
				isRenameTo and notMetaTmp:
			return True
		return False
