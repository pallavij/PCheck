#!/usr/bin/python
import os
import sys

if len(sys.argv) != 2:
	sys.exit("Must provide the file name")

fn = sys.argv[1]

f = open(fn, "r")
flines = f.readlines()
f.close()

outlines = []

add = True
for fl in flines:
	isComment = fl.startswith("=")
	isStartOfQ = fl.strip().startswith(":")
	if (not isComment) and (not isStartOfQ) and add:
		outlines.append(fl)
	else:	
		if isStartOfQ:
			add = False
		if isComment:
			add = True

f = open("trace.txt", "w")
f.writelines(outlines)
f.close()
