package Log;

import java.io.FileOutputStream;

public class HtmlFileLog {
	public static void htmlFileLog(String htmlString, String fileName)
	{
		try {
			FileOutputStream fout = new FileOutputStream(fileName);
			fout.write(htmlString.getBytes());
			fout.close();
		} catch (Exception e) {
				// TODO: handle exception
			e.printStackTrace();
		}
	}
}
