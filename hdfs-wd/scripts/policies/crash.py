#!/usr/bin/python
import fInfo

class Crash:

	def filter(self, failInfo):
		failType = failInfo.ft
		isCrash = (failType.strip() == "CRASH")
		nodeId = failInfo.nodeId
		inDatanode = (nodeId.find("DataNode") != -1)
		isBefore = failInfo.isBefore
		ioType = failInfo.ioType
		isWrite = (ioType.strip() == "WRITE")

		if isCrash and inDatanode and isBefore and isWrite:
			return True
		return False
