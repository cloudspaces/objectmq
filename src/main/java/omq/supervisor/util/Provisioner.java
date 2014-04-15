package omq.supervisor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import omq.supervisor.Supervisor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Provisioner extends Thread {
	// TODO the avgServiceTime, varServiceTime, avgMeanTime and
	// varInterArrivalTime should be measured online...
	protected List<Double> day;
	protected boolean killed = false;

	protected String filename, objReference;
	protected double avgServiceTime, varServiceTime, avgMeanTime;
	protected long sleep;
	protected double startAt, windowSize;
	protected Supervisor supervisor;

	public Provisioner(String objReference, String filename, Supervisor supervisor, double avgServiceTime, double varServiceTime, double avgMeanTime)
			throws IOException {
		this.filename = filename;
		this.objReference = objReference;
		this.supervisor = supervisor;
		this.avgServiceTime = avgServiceTime;
		this.varServiceTime = varServiceTime;
		this.avgMeanTime = avgMeanTime;

		// Get day information
		day = readFile(filename);
	}

	protected int getNumServersNeeded(double obs, double pred) throws IOException {
		pred = pred < obs ? obs : pred;

		double varInterArrivalTime = getVarInterArrivalTime(day, startAt, windowSize);
		double reqArrivalRate = 1 / (avgServiceTime + (varInterArrivalTime + varServiceTime) / (2 * (avgMeanTime - avgServiceTime)));

		return (int) Math.ceil(pred / reqArrivalRate);
	}

	public List<Double> readFile(String fileName) throws IOException {

		List<Double> nums = new ArrayList<Double>();
		String line;
		BufferedReader buff = new BufferedReader(new FileReader(new File(fileName)));

		while ((line = buff.readLine()) != null) {
			nums.add(Double.parseDouble(line.split("\t")[0]));
		}

		buff.close();

		return nums;
	}

	public int getPredArrivalRate(List<Double> list, double startAt, double windowSize) throws IOException {

		int i = 0;
		for (double num : list) {
			if (num >= startAt) {
				break;
			}
			i++;
		}

		int start = i;
		if (i < list.size()) {
			double end = windowSize + startAt;

			while (i < list.size() && list.get(i) < end) {
				i++;
			}
		}

		return i - start;

	}

	public double getVarInterArrivalTime(List<Double> list, double startAt, double windowSize) throws IOException {

		List<Double> xi = new ArrayList<Double>();
		int i = 0;

		for (double num : list) {
			if (num >= startAt) {
				break;
			}
			i++;
		}

		if (i < list.size()) {
			double end = windowSize + startAt;
			double aux = list.get(i);
			double aux2;
			i++;

			while (i < list.size() && (aux2 = list.get(i)) < end) {
				xi.add(aux2 - aux);
				aux = aux2;
				i++;
			}
		}
		return getVariance(xi);

	}

	protected double getVariance(List<Double> list) {
		double mean = 0;
		double sum = 0;

		for (double xi : list) {
			sum += xi;
		}

		mean = sum / list.size();
		sum = 0;

		for (double xi : list) {
			sum += (xi - mean) * (xi - mean);
		}

		return sum / (list.size() - 1);

	}

	protected int getStatus(String vhost, String queue) throws IOException {
		URL url = new URL("http://localhost:15672/api/queues/" + vhost + "/" + queue);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setRequestMethod("GET");

		String userpass = "guest" + ":" + "guest";
		String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());

		connection.setRequestProperty("Authorization", basicAuth);

		connection.connect();

		int status = connection.getResponseCode();
		System.out.println(status);

		BufferedReader buff = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String json = "", line;
		while ((line = buff.readLine()) != null) {
			json += line + "\n";
		}
		buff.close();

		JsonParser parser = new JsonParser();
		JsonObject jsonObj = parser.parse(json).getAsJsonObject();
		try {
			return jsonObj.get("message_stats").getAsJsonObject().get("deliver_get").getAsInt();
		} catch (NullPointerException e) {
			return 0;
		}

	}

	public String getObjReference() {
		return objReference;
	}

	public void setObjReference(String objReference) {
		this.objReference = objReference;
	}

	public String getReference() {
		return filename;
	}

	public void setReference(String reference) {
		this.filename = reference;
	}

	public double getAvgServiceTime() {
		return avgServiceTime;
	}

	public void setAvgServiceTime(double avgServiceTime) {
		this.avgServiceTime = avgServiceTime;
	}

	public double getVarServiceTime() {
		return varServiceTime;
	}

	public void setVarServiceTime(double varServiceTime) {
		this.varServiceTime = varServiceTime;
	}

	public double getAvgMeanTime() {
		return avgMeanTime;
	}

	public void setAvgMeanTime(double avgMeanTime) {
		this.avgMeanTime = avgMeanTime;
	}

	public long getSleep() {
		return sleep;
	}

	public void setSleep(long sleep) {
		this.sleep = sleep;
	}

	public double getStartAt() {
		return startAt;
	}

	public void setStartAt(double startAt) {
		this.startAt = startAt;
	}

	public double getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(double windowSize) {
		this.windowSize = windowSize;
	}

}
