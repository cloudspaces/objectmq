package omq.common.broker;

import java.util.ArrayList;
import java.util.List;

public class StatisticList {
	private List<Long> arrivalList;
	private List<Long> serviceList;

	public StatisticList() {
		arrivalList = new ArrayList<Long>();
		serviceList = new ArrayList<Long>();
	}

	public void setInfo(long arrival, long serviceTime) {
		arrivalList.add(arrival);
		serviceList.add(serviceTime);
	}

	public List<Long> getArrivalList() {
		return arrivalList;
	}

	public List<Long> getServiceList() {
		return serviceList;
	}

}
