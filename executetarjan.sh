#!/bin/bash
java -Xmx15g -javaagent:scc/SizeOf.jar -jar TarjanSCC.jar $1.hrsorted $2 > $1.cfc
