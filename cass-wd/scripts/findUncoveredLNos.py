#!/usr/bin/python
import os
import sys
import re

def stripTags (str):
	strippedStr = str
	p = 0
	p1 = -1
	p2 = -1
	for c in str:
		if c == '>':
			if str[p+1] != '<':
				p1 = p
		if (p1 != -1) and (c == '<'):
			p2 = p
			break
		p = p + 1
	if (p1 != -1) and (p2 != -1):
		strippedStr = str[p1+1:p2]
	return strippedStr		


def processSubStr(fLine, pos1, pos2, lNos):
	s = fLine[pos1:pos2]
	if s.startswith("<TD"):
		lNo = stripTags(s)
		if lNo != "":
			lNos.append(int(lNo))


if len(sys.argv) != 2:
	sys.exit("Must provide the html file name")

fName = sys.argv[1]
rp = re.compile('<TR\sCLASS=\"z\">')

fLines = []
if os.path.exists(fName):
	f = open(fName, "r")
	fLine = f.read()
	f.close()


redLNos = []

itr = rp.finditer(fLine)

pos1 = -1
pos2 = -1
for match in itr:
	#print 'match = ' + match.group()
	mLimits = match.span()
	if pos1 == -1:
		pos1 = mLimits[1]
	if pos2 == -1:
		pos2 = mLimits[0]

	processSubStr(fLine, pos1, pos2, redLNos)

	pos1 = mLimits[1]
	pos2 = -1

if pos1 < len(fLine):
	pos2 = len(fLine)
	processSubStr(fLine, pos1, pos2, redLNos)



'''
lines = fLine.split(redPattern)
for l in lines:
	if l.startswith("<TD"):
		print "l = " + l
		pos = 0
		pos1 = -1
		pos2 = -1
		for c in l:
			if c == '>':
				pos1 = pos
			if (pos1 != -1) and (c == '<'):
				pos2 = pos
				break
			pos = pos + 1
		if (pos1 != -1) and (pos2 != -1):
			lNo = l[pos1+1:pos2]
			if (lNo != ""):
				redLNos.append(int(lNo))
'''

#print "Uncovered line nos :"
for lNo in redLNos:
	print str(lNo)
