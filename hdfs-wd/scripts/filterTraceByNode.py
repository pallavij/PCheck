#!/usr/bin/python
import os
import sys


if len(sys.argv) != 3:
	sys.exit("Must provide the file name and node ID")

fn = sys.argv[1]
nid = sys.argv[2]

f = open(fn, "r")
flines = f.readlines()
f.close()

outlines = []
evtlines = []
isNodeNID = True

for fl in flines:
	evtlines.append(fl)
	if fl == "\n":
		if isNodeNID:
			outlines.extend(evtlines)
		evtlines = []
		isNodeNID = True
		continue
	if fl.startswith("QUEUE: nodeId = ") or fl.startswith("NodeId: "):
		if fl.find(nid) == -1:
			isNodeNID = False

f = open("traceByNode.txt", "w")
f.writelines(outlines)
f.close()
