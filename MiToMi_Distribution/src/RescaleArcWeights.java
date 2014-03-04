import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Scanner;


public class RescaleArcWeights {

	public static String next(FileReader i) {
		String res = "";
		char c = 0;
		try {
			c = (char) i.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(true){
			// System.out.println("ciclo " + ( (int) c));
			if(c == -1) return "EOL";
			if(c == 65535) return "EOL";
			if(c == '\n') {
				// System.out.println(res); 
				return res;}
			if(c == '\t') {
				//System.out.println(res); 
				return res;}
			res += c;
			try {
				c = (char) i.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args) {
		
		FileReader f = null;
		Writer w, t;
		Statistics stat = new Statistics();
		
		try {
			f = new FileReader(args[0]);
			w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[0] + ".scaled")));
			t = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[0] + ".sums")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
//		try {
//			f.mark(1000000000);
//		} catch (IOException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
		
		String tok = next(f);
		try {
			// System.out.println("found source "+ tok);
			t.write(tok);
		} catch (IOException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}
		
		double sum = 0;
		while(!tok.equals("EOL")){
			tok = next(f);
			if(tok.contains(":")){
				String[] vals = tok.split(":");
				sum += Double.parseDouble(vals[1]);
			} else if(!tok.equals("EOL")) {
				try {
					// System.out.println("found source "+ tok);
					t.write(":" + sum + "\n" + tok);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sum = 0;
			} else {
				try {
					t.write(":" + sum + "\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}		
		
		
		try {
			t.flush();
			t.close();
			f.close();
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		
		FileInputStream fT = null;
		try {
			f = new FileReader(args[0]);
		} catch (FileNotFoundException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		try {
			fT = new FileInputStream(args[0] + ".sums");
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		BufferedReader rT = new BufferedReader(new InputStreamReader(fT));
		Scanner sT = new Scanner(rT);
		
		
		tok = next(f);
		sum = Double.parseDouble((sT.nextLine().split(":"))[1]);
		try {
			w.write(tok);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(!tok.equals("EOL")){
			tok = next(f);
			if(tok.contains(":")){
				String[] vals = tok.split(":");
				double scaled = Double.parseDouble(vals[1]) / sum;
				stat.update(scaled);
				try {
					w.write(String.format("\t%s:%f", vals[0], scaled));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if(!tok.equals("EOL")) {
				try {
					w.write("\n" + tok);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sum = Double.parseDouble((sT.nextLine().split(":"))[1]);
			} else {
				try {
					w.write("\n");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		
		try {
			w.flush();
			w.close();
			f.close();
			sT.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] k = args[0].split("\\.");
		String[] v = k[0].split("/");
		System.out.println(v[v.length - 1] + "\t" + stat.asRecord());
		
	}

}
