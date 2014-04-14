package omq.supervisor.util;

import java.io.IOException;

public class ReactiveProvisioner extends Provisioner {

	private double tLow, tHigh;

	public ReactiveProvisioner(String reference, double avgServiceTime, double varServiceTime, double avgMeanTime, double tLow, double tHigh)
			throws IOException {
		super(reference, avgServiceTime, varServiceTime, avgMeanTime);
		this.tLow = tLow;
		this.tHigh = tHigh;
	}

	@Override
	public void run() {
		while (!killed) {
			try {
				double obs = getStatus("%2f", reference);
				double pred = getPredArrivalRate(day, startAt, windowSize);

				double ratio = pred / obs;

				if (ratio < tLow || ratio > tHigh) {
					int numServersNeeded = getNumServersNeeded(obs, pred);

					// Ask how many servers are
					int numServersNow = 0;

					int diff = numServersNeeded - numServersNow;

					if (diff > 0) {
						// Create as servers as needed
					}
					if (diff < 0) {
						// Remove as servers as said
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
