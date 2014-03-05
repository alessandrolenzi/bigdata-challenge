#!/usr/bin/python
import sys
if len(sys.argv) < 2:
    exit

for logfile in sys.argv[1:]:
    f = open(logfile, "r")
    w = open(logfile[0:-3] + "dat", "w+")
    w.write("#X Y Z\n")
    l = f.readline()
    if (not l):
        w.write("0 0 0\n")
    i = 1
    while(l):
#        print(l)	
        a = l.split(",")
	# remove { and } 
	a[0] = a[0][1:]
	a[len(a)-1] = a[len(a)-1][0:-1]
	for j in a:
	    # print(j)
	    if(len(j) > 0):
    	        y = int(j.replace("}","")) / 100
	        x = int(j.replace("}","")) % 100
	        w.write(str(x) + " "+ str(y) + " " + str(i * 10) + "\n")
        i = i + 1
	l = f.readline()
#    w.close()
f.close()
