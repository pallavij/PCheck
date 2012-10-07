#!/usr/bin/python
import sys
import runExp
from policies import crash
from policies import newsrc


if len(sys.argv) < 2:
	print('Correct usage: python run-newsrc.py <No. of failures in an execution>')
	sys.exit(0)

MAX = int(sys.argv[1])	
inclPostFIds = False
useEqvCl = False
rIdOption = -1
clsLast = False
policies = []

#Allow only crashes policy
crashPolicy = crash.Crash()
policies.append(crashPolicy)

#New source location policy
p = newsrc.NewSrc()
policies.append(p)
clsLast = True

runExp.runExp(MAX, policies, useEqvCl, clsLast, rIdOption, inclPostFIds)
