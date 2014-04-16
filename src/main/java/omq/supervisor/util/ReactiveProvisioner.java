package omq.supervisor.util;

import java.io.IOException;

import omq.supervisor.Supervisor;

public class ReactiveProvisioner extends Provisioner {

	private double tLow, tHigh;

	public ReactiveProvisioner(String objReference, String filename, Supervisor supervisor, double tLow, double tHigh) throws IOException {
		super(objReference, filename, supervisor);
		this.tLow = tLow;
		this.tHigh = tHigh;
	}

	@Override
	public void run() {
		while (!killed) {
			try {
				double obs = getStatus("%2f", objReference);
				double pred = getPredArrivalRate(day, startAt, windowSize);

				double ratio = obs / pred;

				if (ratio < tLow || ratio > tHigh) {
					action(obs, pred);
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
