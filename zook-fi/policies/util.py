dName = "/tmp/fi/depComp/"
rFName = dName + "res"

class Util:

	@staticmethod
	def readLineFromFile (fName):
		f = open(fName, "r")
		fline = f.readline()
		f.close()
		fline = fline.strip()
		return fline


	@staticmethod
	def getIdValFromNodeId (nodeId):
		if "Unknown" in nodeId[5:]:
			return 0
		else:
			return int(nodeId[5])


	@staticmethod
	def returnTrue ():
		f = open(rFName, "w")
		f.write("true\n")
		f.close()


	@staticmethod
	def returnFalse ():
		f = open(rFName, "w")
		f.write("false\n")
		f.close()
