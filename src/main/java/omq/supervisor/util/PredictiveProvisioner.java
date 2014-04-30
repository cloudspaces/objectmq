package omq.supervisor.util;

import java.io.IOException;

import omq.supervisor.Supervisor;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class PredictiveProvisioner extends Provisioner {

	public PredictiveProvisioner(String objReference, String filename, Supervisor supervisor) throws IOException {
		super(objReference, filename, supervisor);
	}

	@Override
	public void run() {

		double prevStatus = 0;

		while (!killed) {
			try {

				double status = getStatus("%2f", objReference);
				double obs = status - prevStatus;
				prevStatus = status;

				double pred = getPredArrivalRate(day, startAt, windowSize);

				action(obs, pred, "PREDICTIVE");

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
