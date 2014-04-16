package omq.common.broker;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class StatisticsThread extends Thread {
	private static final Logger logger = Logger.getLogger(StatisticsThread.class.getName());

	private String reference;

	private Object lock = new Object();

	private long sleep = 15 * 60 * 1000; // 15 minutes
	private boolean killed = false;

	private double avgServiceTime;
	private double varInterArrivalTime;
	private double varServiceTime;

	private List<Long> arrivalList;
	private List<Long> serviceList;

	public StatisticsThread(String reference) {
		this.reference = reference;
		serviceList = new ArrayList<Long>();
		arrivalList = new ArrayList<Long>();
	}

	@Override
	public void run() {
		while (!killed) {
			try {
				sleep(sleep);
				synchronized (lock) {

					calculateServiceTime();
					serviceList = new ArrayList<Long>();

					calculateInterArrivalRate();
					arrivalList = new ArrayList<Long>();

					logger.info("Object: " + reference + " statistics = {avgServiceTime : " + avgServiceTime + ", varInterArrivalTime : " + varInterArrivalTime
							+ " , varServiceTime : " + varServiceTime);
				}
			} catch (InterruptedException e) {
				logger.error(e);
				e.printStackTrace();
			}
		}
	}

	public double getAvgServiceTime() {
		synchronized (lock) {
			return avgServiceTime;
		}
	}

	public double getVarInterArrivalTime() {
		synchronized (lock) {
			return varInterArrivalTime;
		}
	}

	public double getVarServiceTime() {
		synchronized (lock) {
			return varServiceTime;
		}
	}

	public void setInfo(long arrival, long serviceTime) {
		synchronized (lock) {
			arrivalList.add(arrival);
			serviceList.add(serviceTime);
		}
	}

	public void calculateInterArrivalRate() {
		if (arrivalList.size() > 0) {
			List<Long> interList = new ArrayList<Long>();

			int i = 0;
			while (i + 2 < arrivalList.size()) {
				interList.add(arrivalList.get(i + 1) - arrivalList.get(i));
				i += 2;
			}

			double mean = 0;
			double sum = 0;

			for (double xi : interList) {
				sum += xi;
			}

			mean = sum / interList.size();
			sum = 0;

			for (double xi : interList) {
				sum += (xi - mean) * (xi - mean);
			}

			varInterArrivalTime = sum / (interList.size() - 1);
		} else {
			varInterArrivalTime = 0;
		}
	}

	public void calculateServiceTime() {
		if (serviceList.size() > 0) {
			double mean = 0;
			double sum = 0;

			for (double xi : serviceList) {
				sum += xi;
			}

			mean = sum / serviceList.size();
			sum = 0;

			for (double xi : serviceList) {
				sum += (xi - mean) * (xi - mean);
			}

			avgServiceTime = mean;
			varServiceTime = sum / (serviceList.size() - 1);
		} else {
			avgServiceTime = 0;
			varServiceTime = 0;
		}
	}

	public Measurement getMeasurement() {
		synchronized (lock) {
			return new Measurement(avgServiceTime, varInterArrivalTime, varServiceTime);
		}
	}

}
