#!/usr/bin/python
import sys
import os
import math
if len(sys.argv) < 2:
    exit

for ff in range(24):
    logfile = str(ff)
#    print("Apro file: " + sys.argv[1] + "/" + logfile + ".cfc")
    f = open(sys.argv[1] + "/" + logfile + ".cfc", "r")
    w = open(sys.argv[1] + "/" + logfile + ".dat", "w+")
    w.write("#X Y Z\n")
    l = f.readline()
    if (not l):
        w.write("0 0 0\n")
    i = 1
    while(l and len(l) > 0 and not l.isspace()):
#        print("linea :" + l)
        a = l.split(",")
        # remove { and } 
        a[0] = a[0][1:]
        a[len(a)-1] = a[len(a)-1][0:-1]
        for j in a:
#            print(j)
#            if(len(j) > 0):
            y = math.floor(int(j.replace("}","")) / 100)
            x = int(j.replace("}","")) % 100
            w.write(str(x) + " "+ str(y) + " " + str(i * 10) + "\n")
        i = i + 1
        l = f.readline()
#    w.close()
f.close()
