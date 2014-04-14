package omq.supervisor.util;

import java.io.IOException;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class PredictiveProvisioner extends Provisioner {

	public PredictiveProvisioner(String reference, double avgServiceTime, double varServiceTime, double avgMeanTime) throws IOException {
		super(reference, avgServiceTime, varServiceTime, avgMeanTime);
	}

	@Override
	public void run() {
		while (!killed) {
			try {
				double obs = getStatus("%2f", reference);

				// DO ACTION
				double pred = getPredArrivalRate(day, startAt, windowSize);
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
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
