package omq.supervisor.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class FileWriterProvisioner {
	private static FileWriterProvisioner stats;

	private BufferedWriter writer;

	private FileWriterProvisioner() throws IOException {
		writer = new BufferedWriter(new FileWriter(new File("statistics.txt")));
		writer.write("date \t type \t obs \t pred \t reqArrivalRate \t avgServiceTime \t varServiceTime \t varInterArrivalTime \t numServersNeeded \t numServersNow \n");
		writer.flush();
	}

	public synchronized static void write(Date date, String type, double obs, double pred, double reqArrivalRate, double avgServiceTime, double varServiceTime,
			double varInterArrivalTime, int numServersNeeded, int numServersNow) {
		if (stats == null) {
			try {
				stats = new FileWriterProvisioner();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		stats.writeStats(date, type, obs, pred, reqArrivalRate, avgServiceTime, varServiceTime, varInterArrivalTime, numServersNeeded, numServersNow);

	}

	public synchronized static void write(Date date, String type, double obs, double pred, double tLow, double tHigh) {
		if (stats == null) {
			try {
				stats = new FileWriterProvisioner();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		stats.writeStats(date, type, obs, pred, tLow, tHigh);

	}

	private void writeStats(Date date, String type, double obs, double pred, double tLow, double tHigh) {
		try {
			writer.write(date + "\t" + type + "\t" + obs + "\t" + pred + "\t" + tLow + "\t" + tHigh + "\n");
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void writeStats(Date date, String type, double obs, double pred, double reqArrivalRate, double avgServiceTime, double varServiceTime,
			double varInterArrivalTime, int numServersNeeded, int numServersNow) {
		try {
			writer.write(date + "\t" + type + "\t" + obs + "\t" + pred + "\t" + reqArrivalRate + "\t" + avgServiceTime + "\t" + varServiceTime + "\t"
					+ varInterArrivalTime + "\t" + numServersNeeded + "\t" + numServersNow + "\n");
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
