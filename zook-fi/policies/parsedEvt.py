from parse import Parse

class ParsedEvt:
	isPollEvent = False
	isSleepEvent = False
	receiver = ""
	leader = long(-1)
	zxid = long(-1)
	epoch = long(-1)
	queue = ""


	def __init__(self, isPollEvent, isSleepEvent, receiver, leader, zxid, epoch, queue):
		self.isPollEvent = isPollEvent
		self.isSleepEvent = isSleepEvent
		self.receiver = receiver
		self.leader = leader
		self.zxid = zxid
		self.epoch = epoch
		self.queue = queue

	
	@staticmethod
	def getParsedEvt(e):
		pe = ParsedEvt(False, False, "", long(-1), long(-1), long(-1), "")
		pe.isPollEvent = Parse.isPollEvent(e)
		pe.isSleepEvent = Parse.isSleepEvent(e)
		pe.receiver = Parse.getNode(e)
		if not pe.isPollEvent:
			pe.leader = Parse.getVoteField(e, "Leader = ", Parse.eventToLeaderMap)
			pe.zxid = Parse.getVoteField(e, "ZxID = ", Parse.eventToZxidMap)
			pe.epoch = Parse.getVoteField(e, "Epoch = ", Parse.eventToEpochMap)
		pe.queue = Parse.getQueue(e)
		return pe
