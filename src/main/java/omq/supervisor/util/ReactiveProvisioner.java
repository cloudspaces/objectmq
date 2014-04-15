package omq.supervisor.util;

import java.io.IOException;

import omq.supervisor.Supervisor;

public class ReactiveProvisioner extends Provisioner {

	private double tLow, tHigh;

	public ReactiveProvisioner(String objReference, String filename, Supervisor supervisor, double avgServiceTime, double varServiceTime, double avgMeanTime,
			double tLow, double tHigh) throws IOException {
		super(objReference, filename, supervisor, avgServiceTime, varServiceTime, avgMeanTime);
		this.tLow = tLow;
		this.tHigh = tHigh;
	}

	@Override
	public void run() {
		while (!killed) {
			try {
				double obs = getStatus("%2f", objReference);
				double pred = getPredArrivalRate(day, startAt, windowSize);

				double ratio = pred / obs;

				if (ratio < tLow || ratio > tHigh) {
					int numServersNeeded = getNumServersNeeded(obs, pred);

					// Ask how many servers are
					int numServersNow = 0;

					int diff = numServersNeeded - numServersNow;

					if (diff > 0) {
						// Create as servers as needed
						try {
							supervisor.createObjects(diff);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (diff < 0) {
						// Remove as servers as said
						try {
							supervisor.removeObjects(diff);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					Thread.sleep(sleep);
					startAt += windowSize;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
