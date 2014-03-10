#!/bin/bash
java -jar FileArranger.jar MItoMI-2013-11-15.txt MItoMI-2013-11-15.txt.rearranged; sort MItoMI-2013-11-15.txt.rearranged > MItoMI-2013-11-25.txt.sorted; java -Xmx24g -jar HourGraphGenerator.jar MItoMI-2013-11-25.txt.sorted; java -Xmx8g -jar HourGraphSorter.jar *.hr;
