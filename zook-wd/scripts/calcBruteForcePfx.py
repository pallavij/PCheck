#!/usr/bin/python
import os
import sys

if len(sys.argv) != 3:
	sys.exit("Must provide the file name that has the concurrent events' information and pfx")

fName = sys.argv[1]
pfxName = sys.argv[2]

f = open(fName, "r")
lines = f.readlines()
f.close()

f = open(pfxName, "r")
pfxLs = f.readlines()
f.close()

nPfx = 0
if len(pfxLs) != 0:
	pfx = pfxLs[0].strip()
	if pfx.find(" ") != -1:	
		nPfx = len(pfx.split(" "))	


if nPfx > 0:
	nExps = 0

	count = 0
	for line in lines:
		if (count >= nPfx) and (line.find(":") != -1):
			q = line.split(":")
			alt = q[1].strip()	
			altParts = alt.split(" ")
			nExps = nExps + len(altParts)
		count = count + 1	

	print "nExps = " + str(nExps)
