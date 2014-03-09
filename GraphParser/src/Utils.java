import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


public class Utils {

	/**
	 * 
	 * @param filename the name of a file (format [anything]-yyyy-MM-dd.[ext])
	 * @return Date of the first millisecond of the day specified in the filename
	 */
	@SuppressWarnings("unused")
	public static Date dateFromFileName (String filename){

		String[] split = filename.split("-",-1);
		String dateString = split[3].substring(0, 2) + "-" + split[2] + "-" + split[1] + ",00:00:00 AM";
		
		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy,HH:mm:ss aaa");
		Date date = null;
		try {
			date = formatter.parse(dateString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	public static Integer[] parseIntArray(String s){
		List<Integer> res = new LinkedList<Integer>();
		Integer k = 0;
		String[] items = s.split(",");
		for(String i : items){
			k = Integer.parseInt(i);
			res.add(k);
		}
		return (Integer[])(res.toArray());
	}
	 
	@SuppressWarnings("unused")
	public static String datestringFromFilename (String filename) {
			String[] split = filename.split("-",-1);
			return split[1] + "-" + split[2] + "-" + split[3];
	}
}
