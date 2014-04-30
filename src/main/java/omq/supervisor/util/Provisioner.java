package omq.supervisor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import omq.common.broker.Measurement;
import omq.exception.RetryException;
import omq.supervisor.Supervisor;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Provisioner extends Thread {
	private static final double prune = 1000; // 1000 ms - 1 second
	private static final double responseTime = 800; // 800 ms 0,8 seconds
	private static final Logger logger = Logger.getLogger(Provisioner.class.getName());

	protected List<Double> day;
	protected boolean killed = false;

	protected String filename, objReference;
	protected long sleep;
	protected double startAt, windowSize;
	protected Supervisor supervisor;

	private double avgServiceTime = 0, varServiceTime = 0, varInterArrivalTime = 0, reqArrivalRate = 0;

	public Provisioner(String objReference, String filename, Supervisor supervisor) throws IOException {
		this.filename = filename;
		this.objReference = objReference;
		this.supervisor = supervisor;

		// Get day information
		day = readFile(filename);
	}

	public HasObject[] getHasList() {
		try {
			return supervisor.getHasList();
		} catch (RetryException e) {
			return null;
		}
	}

	public void action(double obs, double pred, String type) {
		try {
			HasObject[] hasList = getHasList();
			int numServersNeeded;

			numServersNeeded = getNumServersNeeded(obs, pred, hasList);

			List<HasObject> serversWithObject = whoHasObject(hasList, true);

			// Ask how many servers are
			int numServersNow = serversWithObject.size();

			int diff = numServersNeeded - numServersNow;

			logger.info("Obs param: " + obs + ", Pred param: " + pred + ", NumServersNeeded: " + numServersNeeded + " NumSerNow: " + numServersNow);
			FileWriterProvisioner.write(new Date(), type, obs, pred, reqArrivalRate, avgServiceTime, varServiceTime, varInterArrivalTime, numServersNeeded,
					numServersNow);
			if (diff > 0) {
				// Calculate servers without object
				List<HasObject> serversWithoutObject = whoHasObject(hasList, false);
				// Create as servers as needed
				supervisor.createObjects(diff, serversWithoutObject, obs, pred);
			}
			// At least 1 server should survive
			if (diff < 0 && numServersNeeded > 0) {
				diff *= -1;
				// Remove as servers as said
				supervisor.removeObjects(diff, serversWithObject, obs, pred);
			}
		} catch (Exception e1) {
			logger.error("Object: " + objReference, e1);
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	protected int getNumServersNeeded(double obs, double pred, HasObject[] hasList) throws Exception {
		// There are no servers available
		if (hasList.length == 0) {
			throw new Exception("Cannot find any server available");
		}

		double avgServiceTime = 0, varServiceTime = 0, varInterArrivalTime = 0;
		pred = pred < obs ? obs : pred;

		// Calculate avgServiceTime, varServiceTime, varInterArrivalTime
		int i = 0;
		for (HasObject h : hasList) {
			Measurement m = h.getMeasurement();
			if (h.hasObject() && m != null) {
				avgServiceTime += m.getAvgServiceTime();
				varServiceTime += m.getVarServiceTime();
				varInterArrivalTime += m.getVarInterArrivalTime();
				i++;
			}
		}

		// There are no servers with the required object, at least 1 server is
		// needed
		if (i == 0) {
			return 1;
		}

		// Calculate mean times among servers
		avgServiceTime /= i;
		varServiceTime /= i;
		varInterArrivalTime /= i;

		this.avgServiceTime = avgServiceTime;
		this.varServiceTime = varServiceTime;
		this.varInterArrivalTime = varInterArrivalTime;

		// reqArrivalRate = 1 / (avgServiceTime + (varInterArrivalTime +
		// varServiceTime) / (2 * (avgMeanTime - avgServiceTime)))

		double reqArrivalRate = 1 / (avgServiceTime + ((varInterArrivalTime + varServiceTime) / (2 * (responseTime * avgServiceTime))));

		// reqArrival rate is measured in miliseconds but provisioners work with
		// minutes
		reqArrivalRate *= sleep * 1000;

		logger.info("ReqArrivalRate: " + reqArrivalRate + " ,AvgServiceTime: " + avgServiceTime + ", VarServiceTime: " + varServiceTime + ", VarInterATime: "
				+ varInterArrivalTime);

		this.reqArrivalRate = reqArrivalRate;
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
		List<Double> laux = new ArrayList<Double>();
		for (double xi : list) {
			if (xi < prune) {
				laux.add(xi);
			}
		}
		list = laux;

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

	public List<HasObject> whoHasObject(HasObject[] hasList, boolean condition) throws RetryException {
		List<HasObject> list = new ArrayList<HasObject>();
		for (HasObject h : hasList) {
			if (h.hasObject() == condition) {
				list.add(h);
			}
		}

		return list;
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
		if (200 >= status && status < 300) {

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
		} else {
			throw new IOException("Queue does not exist");
		}

	}

	public List<Double> getDay() {
		return day;
	}

	public void setDay(List<Double> day) {
		this.day = day;
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
