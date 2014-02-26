package two;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeMap;

import customreader.CustomReader;

public class HourGraphSorter {
	public static void sortHourGraph(String filename) throws IOException {
		CustomReader reader = new CustomReader(filename, 1024*1024*10L, 1024, new char[]{'\n', '\t'});
		TreeMap<Integer, String> fileMap = new TreeMap<Integer, String>();
		while (!reader.fileEnded()) {
			String nodeId = reader.next();
			if (nodeId == null) break;
			StringBuffer buf = new StringBuffer(nodeId.trim());
			buf.append(reader.getLastDelimiter());
			while (!reader.lineEnded()) {
				buf.append(reader.next());
				buf.append(reader.getLastDelimiter());
			}
			int id = Integer.parseInt(nodeId.trim());
			fileMap.put(id, buf.toString());
		}
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename+"sorted", false)));
		while(!fileMap.isEmpty()) {
			Integer k = fileMap.firstKey();
			String toWrite = fileMap.get(k);
			fileMap.remove(k);
			writer.write(toWrite);
		}
		writer.flush();
		writer.close();
	}
	
	public static void main(String[] args) throws IOException {
		for(String file: args) {
			sortHourGraph(file);
		}
	}

}
