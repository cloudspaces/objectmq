package omq.test.supervisor;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import omq.common.broker.Measurement;
import omq.common.broker.StatisticsThread;

import org.junit.Test;

//TODO re-do this test

public class StatisticsThreadTest {

	@Test
	public void test() throws InterruptedException {
		List<Long> arrivalList = new ArrayList<Long>();
		List<Long> serviceList = new ArrayList<Long>();

		StatisticsThread thread = new StatisticsThread(null);

		for (int i = 0; i < 5; i++) {
			long arrival = System.currentTimeMillis();
			Thread.sleep(500);
			long end = System.currentTimeMillis();

			long serviceTime = end - arrival;
			arrivalList.add(arrival);
			serviceList.add(serviceTime);
			// thread.setInfo(arrival, serviceTime);
		}

		thread.calculateInterArrivalRate();
		thread.calculateServiceTime();
		Measurement m1 = thread.getMeasurement();

		Measurement m2 = calculate(arrivalList, serviceList);

		assertEquals(m1, m2);

	}

	public Measurement calculate(List<Long> arrivalList, List<Long> serviceList) {

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

		double varInterArrivalTime = sum / (interList.size() - 1);

		mean = 0;
		sum = 0;

		for (double xi : serviceList) {
			sum += xi;
		}

		mean = sum / serviceList.size();
		sum = 0;

		for (double xi : serviceList) {
			sum += (xi - mean) * (xi - mean);
		}

		double avgServiceTime = mean;
		double varServiceTime = sum / (serviceList.size() - 1);

		return new Measurement(avgServiceTime, varInterArrivalTime, varServiceTime);
	}

}
