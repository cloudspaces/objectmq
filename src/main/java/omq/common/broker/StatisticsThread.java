package omq.common.broker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import omq.server.AInvocationThread;
import omq.server.RemoteObject;

import org.apache.log4j.Logger;

public class StatisticsThread extends Thread {
	private static final Logger logger = Logger.getLogger(StatisticsThread.class.getName());

	private String reference;

	private Object lock = new Object();

	private long sleep = (4 * 60 * 1000) + (30 * 1000); // 4,5 minutes
	private boolean killed = false;
	private RemoteObject remoteObj;

	private double avgServiceTime;
	private double varInterArrivalTime;
	private double varServiceTime;

	private List<Long> arrivalList;
	private List<Long> serviceList;

	public StatisticsThread(RemoteObject remoteObj) {
		this.remoteObj = remoteObj;
		reference = remoteObj.getRef();
		serviceList = new ArrayList<Long>();
		arrivalList = new ArrayList<Long>();
	}

	@Override
	public void run() {
		while (!killed) {
			try {
				Thread.sleep(sleep);
				askData();
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

	public void askData() {
		for (AInvocationThread thread : remoteObj.getPool().getWorkers()) {
			StatisticList aux = thread.getAndRemoveStatsLists();
			serviceList.addAll(aux.getServiceList());
			arrivalList.addAll(aux.getArrivalList());
		}
	}

	public void calculateInterArrivalRate() {

		if (arrivalList.size() > 2) {
			List<Long> interList = new ArrayList<Long>();

			Collections.sort(arrivalList);
			System.out.println("calculateInterArrivalRate: " + arrivalList);

			long prev = arrivalList.get(0);

			int i = 1;
			while (i < arrivalList.size()) {
				long aux = arrivalList.get(i);
				interList.add(aux - prev);
				prev = aux;
				i++;
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
			System.out.println("calculateServiceTime: " + serviceList);

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
			System.out.println(avgServiceTime + " " + varInterArrivalTime + " " + varServiceTime);
			return new Measurement(avgServiceTime, varInterArrivalTime, varServiceTime);
		}
	}

}
