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
		while (!killed) {
			try {
				double obs = getStatus("%2f", objReference);
				double pred = getPredArrivalRate(day, startAt, windowSize);

				action(obs, pred);

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
