#!/usr/bin/python
import sys
import runExp
from policies import crash


if len(sys.argv) < 2:
	print('Correct usage: python run-rec-all.py <No. of failures in an execution>')
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

#Recovery clustering policy (src + stack + node + target node + target IO type + 
#RPC context + file type involved in IO)
useEqvCl = True
rIdOption = 4

runExp.runExp(MAX, policies, useEqvCl, clsLast, rIdOption, inclPostFIds)
