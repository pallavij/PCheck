#!/usr/bin/python
import os
import sys

def getRecv(id, dir):
	fpath = os.path.join(dir, "h" + id + ".txt")
	recv = ""
	if os.path.exists(fpath):
		f = open(fpath, "r")
		flines = f.readlines()
		f.close()

		for fl in flines:
			if fl.startswith("QUEUE: nodeId = "):
				l = len("QUEUE: nodeId = ")
				recv = fl[l:]
				recv = recv.strip()

	return recv			


if len(sys.argv) != 4:
	sys.exit("Must provide the file name, the coverage info directory name, and the output file name")

fn = sys.argv[1]
dn = sys.argv[2]
on = sys.argv[3]

f = open(fn, "r")
flines = f.readlines()
f.close()

outlines = []

for fl in flines:
	p = ((fl.split("["))[0]).strip()
	ids = p.split(" ")
	id1 = ids[0]
	id2 = ids[1]
	r1 = getRecv(id1, dn)
	r2 = getRecv(id2, dn)
	outlines.append(r1 + " " + r2 + "\n")

f = open(on, "w")
f.writelines(outlines)
f.close()
