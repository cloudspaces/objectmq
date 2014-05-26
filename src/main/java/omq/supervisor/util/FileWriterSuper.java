package omq.supervisor.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class FileWriterSuper {
	private static FileWriterSuper stats;

	private BufferedWriter writer;

	private FileWriterSuper() throws IOException {
		writer = new BufferedWriter(new FileWriter(new File("super.txt")));
	}

	public synchronized static void write(Date date, String action, int num) {
		if (stats == null) {
			try {
				stats = new FileWriterSuper();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		stats.writeStats(date, action, num);

	}

	private void writeStats(Date date, String action, int num) {
		try {
			writer.write(date + "\t" + action + "\t" + num + "\n");
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
