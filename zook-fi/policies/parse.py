COVERAGE_COMPLETE_DIR = "/tmp/fi/coverageComplete/"

class Parse:
	isPollMap = {}
	isSleepMap = {}
	eventToNodeMap = {}
	eventToLeaderMap = {}
	eventToZxidMap = {}
	eventToEpochMap = {}

	
	@staticmethod
	def isPollEvent(e):
		return Parse.isEvtOfGivenType(e, "TYPE: POLL", Parse.isPollMap)


	@staticmethod
	def isSleepEvent(e):
		return Parse.isEvtOfGivenType(e, "TYPE: SLEEP", Parse.isSleepMap)

	
	@staticmethod
	def isEvtOfGivenType(e, typ, M):
		if typ in M.keys():
			return M[typ]
		
                fName = "h" + e + ".txt"
		f = open(COVERAGE_COMPLETE_DIR + fName, "r")
		flines = f.readlines()
		f.close()

		for fl in flines:
			if typ in fl:
				M[typ] = True
				return True

		M[typ] = False
		return False 
	
	
	@staticmethod
	def getNode(e):
		if e in Parse.eventToNodeMap.keys():
			return Parse.eventToNodeMap[e]

		v = Parse.getFieldVal(e, "nodeId = ")
		if v == None:
			v = Parse.getFieldVal(e, "NodeId: ")

		Parse.eventToNodeMap[e] = v
		return v


	@staticmethod
	def getFieldVal(e, sToFind):
                fName = "h" + e + ".txt"
		f = open(COVERAGE_COMPLETE_DIR + fName, "r")
		flines = f.readlines()
		f.close()
	
		for fl in flines:
			fl = fl.strip()
			if sToFind in fl:
				idx = fl.find(sToFind)
				return fl[idx + len(sToFind):]	

		return None


	@staticmethod
	def getVoteField(e, field, m):	
		if e in m.keys():
			return m[e]

		vf = long(-1)
		v = Parse.getFieldVal(e, field)
		if v != None:
			vf = long(v)

		m[e] = vf
		return vf


	@staticmethod
	def getQueue(e):	
                fName = "h" + e + ".txt"
		f = open(COVERAGE_COMPLETE_DIR + fName, "r")
		flines = f.readlines()
		f.close()
		
		start = False
		q = ""
		n = "QUEUE: "

		if flines == []:
			return None


		for fl in flines:
			fl = fl.strip()
			if n in fl:
				q += fl[len(n):]
				q += "\n"
				start = True	
			else:
				if start:
					if ("MESSAGE: " in fl) or ("STACK: " in fl):				
						break
					q += fl
					q += "\n"


		return q 



	@staticmethod
	def getEvtQueue(s):
		sParts = s.split(":")
		e = sParts[0]
		e = e.strip()

		if len(sParts) > 1:
			q = sParts[1]
			q = q.strip()
			qParts = q.split(" ")
			return (e, qParts)
		else:
			return (e, [])



	@staticmethod
	def getRequestType(s):
		pstr = Parse.getFieldVal(s, "packet = ")
		if pstr != None:
			pstrParts = pstr.split(" ")
			return pstrParts[0]
		return None


			
	@staticmethod
	def getRequestZxid(s):
		pstr = Parse.getFieldVal(s, "packet = ")
		if pstr != None:
			pstrParts = pstr.split(" ")
			return pstrParts[1]
		return None

	
