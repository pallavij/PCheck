#!/usr/bin/python

from parsedEvt import ParsedEvt
from util import Util
import sys


class P2:

	@staticmethod
	def policy(e1, e2, stPairs, qstate):

		pe1 = ParsedEvt.getParsedEvt(e1)
		pe2 = ParsedEvt.getParsedEvt(e2)

		if pe1.isPollEvent or pe2.isPollEvent:
			Util.returnFalse()
			sys.exit(0)

		sameRecrs = (pe1.receiver == pe2.receiver)

		if sameRecrs and (pe1.leader != pe2.leader) and (pe1.zxid != pe2.zxid) and (pe1.epoch != pe2.epoch):
			Util.returnTrue()
		else:
			Util.returnFalse() 

