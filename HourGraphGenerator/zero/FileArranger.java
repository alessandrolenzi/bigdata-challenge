package zero;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import customreader.CustomReader;

public class FileArranger {

	public static void main(String[] args) throws IOException {
		CustomReader reader = new CustomReader(args[0], 1024*1024*10L, 1024, new char[]{'\n', '\t'});
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(args[1], false)));
		int i = 0;
		while (!reader.fileEnded()) {
			String postpone = reader.next();
			if (postpone == null) {
				System.out.println("Really??"+i);
				break;
			}
			StringBuffer remaining = new StringBuffer();
			remaining.append(reader.next().trim());
			remaining.append('\t');
			remaining.append(postpone.trim());
			remaining.append('\t');
			remaining.append(reader.next().trim());
			remaining.append('\t');
			remaining.append(reader.next().trim());		
			remaining.append('\n');
			writer.write(remaining.toString());
			i++;
		}
		writer.flush();
		writer.close();
		writer = null;
	}
	
}
