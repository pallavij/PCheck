#!/usr/bin/python
import sys
import runExp
from policies import crash

if len(sys.argv) < 2:
	print('Correct usage: python run-brute.py <No. of failures in an execution>')
	sys.exit(0)

MAX = int(sys.argv[1])	

#ignore the following variable. set it always to false.
inclPostFIds = False

#set it to true for equivalent recovery clustering policy.
useEqvCl = False
#set it to use the correct reduced failure injection task 
#for clustering policy. look at getRId(...) in fInfo.py. 
#since we do not need recovery clustering in this policy, 
#this is just set to -1 here.
rIdOption = -1

#set this to true if you want to consider failure experiments 
#with the same last failure injection task as the same.
#ignore this here. this is used in the new source location 
#policy in run-newsrc.py.
clsLast = False

#list of policies to apply
policies = []

#Allow only crashes policy
crashPolicy = crash.Crash()
policies.append(crashPolicy)


runExp.runExp(MAX, policies, useEqvCl, clsLast, rIdOption, inclPostFIds)
