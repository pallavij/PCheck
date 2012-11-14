#!/usr/bin/python

from parsedEvt import ParsedEvt
from util import Util
import sys


class P1:

	@staticmethod
	def policy(e1, e2, stPairs, qstate):

		pe1 = ParsedEvt.getParsedEvt(e1)
		pe2 = ParsedEvt.getParsedEvt(e2)

		if pe1.isPollEvent or pe2.isPollEvent:
			Util.returnFalse()
			sys.exit(0)

		sameRecrs = (pe1.receiver == pe2.receiver)

		l = long(-1)
		z = long(-1)
		idx = Util.getIdValFromNodeId(pe1.receiver) - 1
		if idx <= (len(stPairs) - 1):
			lz = stPairs[idx]	
			l = lz[0]
			z = lz[1]


		if sameRecrs and (pe1.leader >= l) and (pe2.leader >= l) and (pe1.leader != pe2.leader):
			Util.returnTrue()
		else:
			Util.returnFalse() 

