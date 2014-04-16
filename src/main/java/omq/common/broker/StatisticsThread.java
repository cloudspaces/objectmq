package omq.common.broker;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class StatisticsThread extends Thread {
	private static final Logger logger = Logger.getLogger(StatisticsThread.class.getName());

	private String reference;

	private Object lock = new Object();
	private Object lockArrival = new Object();
	private Object lockService = new Object();

	private long sleep = 15 * 60 * 1000; // 15 minutes
	private boolean killed = false;

	private double avgServiceTime;
	private double varInterArrivalTime;
	private double varServiceTime;

	private List<Long> arrivalList;
	private List<Long> serviceList;

	public StatisticsThread(String reference) {
		this.reference = reference;
	}

	@Override
	public void run() {
		while (!killed) {
			try {
				sleep(sleep);
				// TODO check this please... Now u r hungry and u need a nap and
				// a coffee :'(
				// This happens when u don't have enough caffeine in your blood
				synchronized (lock) {
					synchronized (lockService) {
						calculateServiceTime();
						serviceList = new ArrayList<Long>();
					}
					synchronized (lockArrival) {
						calculateInterArrivalRate();
						arrivalList = new ArrayList<Long>();
					}
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

	public void addArrival(long arrival) {
		synchronized (lockArrival) {
			arrivalList.add(arrival);
		}
	}

	public void addServiceTime(long serviceTime) {
		synchronized (lockService) {
			serviceList.add(serviceTime);
		}
	}

	private void calculateInterArrivalRate() {

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
	}

	private void calculateServiceTime() {

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
	}

	public Measurement getMeasurement() {
		synchronized (lock) {
			return new Measurement(avgServiceTime, varInterArrivalTime, varServiceTime);
		}
	}

}
