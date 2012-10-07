#!/usr/bin/python
import fInfo

class NewSrc:

	def coverageId(self, failInfo):
		jpStr = failInfo.jpStr
		fjpSrcLoc = failInfo.fjpSrcLoc
		h = hash(jpStr+"\n"+fjpSrcLoc+"\n")
		return str(h)
