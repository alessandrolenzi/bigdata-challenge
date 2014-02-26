package one;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Date;
import java.util.TreeSet;
import java.util.ArrayList;
/** Class to generate hour graphs*/
import customreader.CustomReader;
public class HourGraphGenerator {

	public HourGraphGenerator() {
		
	}
	
	private static void writeOut(ArrayList<HourlyArc> list) throws IOException {
		
		
		PrintWriter writer = null;
		Collections.sort(list);
		if (list.size() == 0) return;
		int currentStartNode = list.get(0).startID;
		int currentHour = -1;
		
		for (HourlyArc cur: list) {
			if (currentHour != cur.hourID) {
				currentHour = cur.hourID;
				if(writer != null){ writer.write('\n'); writer.flush(); writer.close();}
				writer = new PrintWriter(new BufferedWriter(new FileWriter(cur.hourID+".hr", true)));
				writer.write(String.format("%d",cur.startID));
			}
			if (currentHour == cur.hourID && currentStartNode == cur.startID) { //just write
				writer.write("\t"+cur.endID+":"+String.format("%s", cur.strength));
				continue;
			} else {
				System.err.println(currentHour);
			}
		}
		writer.write('\n'); writer.flush(); writer.close();
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static HourlyArc parseFirstRow(CustomReader c) {
		int startNode = Integer.parseInt(c.next().trim());
		Date d = new Date(Long.parseLong(c.next().trim()));
		int endNode = Integer.parseInt(c.next());
		double directionalStrength = Double.parseDouble(c.next().trim());
		return new HourlyArc(startNode, endNode, d.getHours(), directionalStrength);
	}
	
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
		
		CustomReader custom = new CustomReader(args[0], 1024*1024*10L, 1024, new char[]{'\n', '\t'});
		HourlyArc curArc = parseFirstRow(custom); //scan first row
		//TreeSet<HourlyArc> allArcs = new TreeSet<HourlyArc>();
		ArrayList<HourlyArc> list = new ArrayList<HourlyArc>();
		//Scan the whole file.
		int i = 0;
		int subsequent = 0;
		int rows = 0;
		while (!custom.fileEnded()) {
			String str = custom.next();
			//System.err.println(str);
			if (str == null) break;
			int startNode = Integer.parseInt(str);
			Date d = new Date(Long.parseLong(custom.next()));
			int endNode = Integer.parseInt(custom.next());
			double directionalStrength = Double.parseDouble(custom.next());
			
			rows++;
			try {
				curArc.update(startNode, endNode, d.getHours(), directionalStrength);
			} catch (NotSameArcException e) {
				list.add(curArc);
				if (curArc.startID != startNode) {
					writeOut(list);
					list.clear();
					subsequent = 1;
				}
				curArc = new HourlyArc(startNode, endNode, d.getHours(), directionalStrength);
			}
			float percentage = custom.currentPosition() / custom.fileLength();
			if (percentage > i/4) System.err.println(i++);
		}
		if (list.size() == 0 || !curArc.equals(list.get(list.size()-1))) list.add(curArc);
		writeOut(list);
	}

}
