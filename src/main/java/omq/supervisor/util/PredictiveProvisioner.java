package omq.supervisor.util;

import java.io.IOException;

import omq.supervisor.Supervisor;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class PredictiveProvisioner extends Provisioner {

	public PredictiveProvisioner(String objReference, String filename, Supervisor supervisor, double avgServiceTime, double varServiceTime, double avgMeanTime)
			throws IOException {
		super(objReference, filename, supervisor, avgServiceTime, varServiceTime, avgMeanTime);
	}

	@Override
	public void run() {
		while (!killed) {
			try {
				double obs = getStatus("%2f", objReference);

				// DO ACTION
				double pred = getPredArrivalRate(day, startAt, windowSize);
				int numServersNeeded = getNumServersNeeded(obs, pred);

				// Ask how many servers are
				int numServersNow = supervisor.getNumServersWithObject();

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
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
