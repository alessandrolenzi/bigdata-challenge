#!/bin/bash
mkdir scc;
scp carilenzi@node3:/home/carilenzi/StronglyConnectedComponents/generated/1.hrsorted /home/carilenzi/scc/;
scp carilenzi@node3:/home/carilenzi/StronglyConnectedComponents/generated/2.hrsorted /home/carilenzi/scc/;
scp carilenzi@node3:/home/carilenzi/StronglyConnectedComponents/generated/3.hrsorted /home/carilenzi/scc/;
scp carilenzi@node3:/home/carilenzi/StronglyConnectedComponents/generated/4.hrsorted /home/carilenzi/scc/;
scp carilenzi@node3:/home/carilenzi/StronglyConnectedComponents/generated/5.hrsorted /home/carilenzi/scc/;
scp carilenzi@novello:/home/carilenzi/scc/TarjanSCC.jar /home/carilenzi/scc/;
scp carilenzi@novello:/home/carilenzi/scc/SizeOf.jar /home/carilenzi/scc/;
scp carilenzi@novello:/home/carilenzi/scc/execute.sh /home/carilenzi/scc/execute.sh;
