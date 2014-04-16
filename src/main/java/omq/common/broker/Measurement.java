package omq.common.broker;

import java.io.Serializable;

public class Measurement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double avgServiceTime;
	private double varInterArrivalTime;
	private double varServiceTime;

	public Measurement(double avgServiceTime, double varInterArrivalTime, double varServiceTime) {
		this.avgServiceTime = avgServiceTime;
		this.varInterArrivalTime = varInterArrivalTime;
		this.varServiceTime = varServiceTime;
	}

	public double getAvgServiceTime() {
		return avgServiceTime;
	}

	public void setAvgServiceTime(double avgServiceTime) {
		this.avgServiceTime = avgServiceTime;
	}

	public double getVarInterArrivalTime() {
		return varInterArrivalTime;
	}

	public void setVarInterArrivalTime(double varInterArrivalTime) {
		this.varInterArrivalTime = varInterArrivalTime;
	}

	public double getVarServiceTime() {
		return varServiceTime;
	}

	public void setVarServiceTime(double varServiceTime) {
		this.varServiceTime = varServiceTime;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Measurement) {
			Measurement m = (Measurement) obj;
			return (avgServiceTime == m.getAvgServiceTime()) && (varInterArrivalTime == m.getVarInterArrivalTime())
					&& (varServiceTime == m.getVarServiceTime());
		}
		return false;
	}

}
