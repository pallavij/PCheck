#!/usr/bin/python

from util import Util
from p1 import P1
from p2 import P2

dName = "/tmp/fi/depComp/"

e1 = Util.readLineFromFile(dName + "e1")
e2 = Util.readLineFromFile(dName + "e2")
state = Util.readLineFromFile(dName + "state")
qstate = Util.readLineFromFile(dName + "qstate")


stPairs = []

if state.strip() != "":
	stElems = state.split(":")
	for se in stElems:
		seElems = se.split(",") 
		seFst = seElems[0]
		seSnd = seElems[1]
		sePair = (long(seFst[1:]), long(seSnd[:-1]))
		stPairs.append(sePair)


P1.policy(e1, e2, stPairs, qstate)
#P2.policy(e1, e2, stPairs, qstate)
