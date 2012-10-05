#!/usr/bin/python
import os
import sys

if len(sys.argv) != 2:
	sys.exit("Must provide the file name that has the concurrent events' information")

fName = sys.argv[1]

f = open(fName, "r")
lines = f.readlines()
f.close()

nExps = 0

for line in lines:
	if line.find(":") != -1:
		q = line.split(":")
		alt = q[1].strip()	
		altParts = alt.split(" ")
		nExps = nExps + len(altParts)

print "nExps = " + str(nExps)
