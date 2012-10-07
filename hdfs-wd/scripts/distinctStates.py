#!/usr/bin/python
import os
import sys

if len(sys.argv) != 2:
	sys.exit("Must provide the directory name")

dirpath = sys.argv[1]
states = set([])

for file in os.listdir(dirpath):
	f = open(os.path.join(dirpath, file), 'r')
	fstates = f.readlines()
	for fs in fstates:
		states.add(fs)

print "Number of distinct states is " + str(len(states))
print "Distinct states are :"
for s in states:
	print s.strip()
