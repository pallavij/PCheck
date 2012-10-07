#!/usr/bin/python

import sys
import os

f1 = open("tmp1","r")
line1 = f1.readline()
f1.close()

f2 = open("tmp2","r")
line2 = f2.readline()
f2.close()

line1 = line1.rstrip("\n")
line2 = line2.rstrip("\n")

s1 = set(line1.split(" "))
s2 = set(line2.split(" "))

print "s1-s2"
print (s1-s2)

print "s2-s1"
print (s2-s1)
