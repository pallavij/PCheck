#!/usr/bin/python
import os
import sys
from sets import Set

def getEvtType(ln):
	type = ""
	if ln.find("POLL") != -1:
		type = "POLL"
	if ln.find("NETREAD") != -1:
		type = "NETREAD"
	return type	



def getNodeId(ln):
	nodeId = ""
	if ln.find("Node-1") != -1:
		nodeId = "Node-1"
	if ln.find("Node-2") != -1:
		nodeId = "Node-2"
	if ln.find("Node-3") != -1:	
		nodeId = "Node-3"
	return nodeId



def getQueueType(ln):
	qType = ""
	if ln.find("QuorumCnxManager.java(129)") != -1:
		qType = "Q1"
	if ln.find("FastLeaderElection.java(527)") != -1:
		qType = "Q2"
	return qType



def getQToPollFrom(nodeId, qType, ql):
	defaultVal = ""
	if (not nodeId.startswith("Node-")) or (not qType.startswith("Q")):
		return defaultVal
	nid = int(nodeId[5:])
	qid = int(qType[1:])
	idx = 2*(nid-1) + (qid-1)
	return ql[idx]



def getQToAppendTo(nodeId, qType, ql):
	defaultVal = ""
	if (not nodeId.startswith("Node-")) or (not qType.startswith("Q")):
		return defaultVal
	nid = int(nodeId[5:])
	qid = int(qType[1:])
	if qid == 2:
		return ql[6]
	else:
		idx = 2*nid - 1
		return ql[idx]
	



def pollFromQ(qp, qa):
	if len(qp) != 0:
		id = qp.pop(0)
		qa.append(id)



def putMsg(nMsg, nodeId, ql):
	if nodeId.startswith("Node-"):
		nid = int(nodeId[5:])
		idx = 2*(nid - 1)
		q = ql[idx] 
		q.append(nMsg)


def printEvtOrder(idToContent, qf):
	'''
	wlines = []
	for mid in qf:
		wlines.extend(idToContent[mid])
		wlines.append("\n")
	
	'''
	wlines = []
	ridLn = "QUEUE: nodeId = Node-"
	sidLn = "othrNodeId = Node-"
	lidLn = "Leader = "
	for mid in qf:
		mparts = idToContent[mid]
		s = ""
		r = ""
		l = ""
		for mp in mparts:
			if mp.startswith(ridLn):
				r = (mp[len(ridLn):]).strip()
			if mp.startswith(sidLn):
				s = (mp[len(sidLn):]).strip()
			if mp.startswith(lidLn):
				l = (mp[len(lidLn):]).strip()
		wlines.append(s + " - " + r + " (" + l + ")\n")		

	f = open("mOrder.txt", "w")
	f.writelines(wlines)
	f.close()
		
			



if len(sys.argv) != 2:
	sys.exit("Must provide the file name")

fn = sys.argv[1]

f = open(fn, "r")
flines = f.readlines()
f.close()

evtlines = []
nRead = 0
nMsg = 0
idToContent = {}

q11 = []
q12 = []
q21 = []
q22 = []
q31 = []
q32 = []
qf = []

ql = [q11, q12, q21, q22, q31, q32, qf]

for i in range(len(flines)):
	ln = flines[nRead]
	nRead = nRead + 1
	if ln != "\n":
		evtlines.append(ln)
	else:
		type = getEvtType(evtlines[0])
		
		nodeId = getNodeId(evtlines[2])

		if type == "POLL":
			qType = getQueueType(evtlines[3])
			qp = getQToPollFrom(nodeId, qType, ql)
			qa = getQToAppendTo(nodeId, qType, ql)
			pollFromQ(qp, qa)


		if type == "NETREAD":
			nMsg = nMsg + 1
			idToContent[nMsg] = evtlines
			putMsg(nMsg, nodeId, ql)
		

		evtlines = []


printEvtOrder(idToContent, qf)	
