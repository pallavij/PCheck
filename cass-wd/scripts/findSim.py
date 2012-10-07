#!/usr/bin/python
import os
import sys
from sets import Set

if len(sys.argv) != 4:
	sys.exit("Must provide the two file names and the output file name")

fn1 = sys.argv[1]
fn2 = sys.argv[2]
on = sys.argv[3]

f1 = open(fn1, "r")
flines1 = Set(f1.readlines())
f1.close()

f2 = open(fn2, "r")
flines2 = Set(f2.readlines())
f2.close()

fcommon = list(flines1.intersection(flines2))
fcommon.sort()

f = open(on, "w")
f.writelines(fcommon)
f.close()
