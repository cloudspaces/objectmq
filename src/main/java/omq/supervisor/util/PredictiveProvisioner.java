package omq.supervisor.util;

import java.io.IOException;
import java.util.Date;

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

				double varInterArrivalTime = getVarInterArrivalTime(day, startAt, windowSize);
				double pred = getPredArrivalRate(day, startAt, windowSize);

				action(pred, varInterArrivalTime);

				Thread.sleep(sleep);
				startAt += windowSize;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void action(double pred, double varInterArrivalTime) {
		try {
			int numServersNeeded;

			numServersNeeded = getNumServersNeeded(pred, varInterArrivalTime, null);
			logger.info("Pred param: " + pred + ", NumServersNeeded: " + numServersNeeded);
			FileWriterProvisioner.write(new Date(), "Predictive", 0, pred, reqArrivalRate, avgServiceTime, varServiceTime, varInterArrivalTime,
					numServersNeeded, -1);

			super.setNumServersNeeded(numServersNeeded);
		} catch (Exception e1) {
			logger.error("Object: " + objReference, e1);
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public int getNumServersNeeded(double pred, double varInterArrivalTime, HasObject[] hasList) throws Exception {
		// There are no servers available
		// if (hasList.length == 0) {
		// throw new Exception("Cannot find any server available");
		// }

		// TODO change this!!!! use the historical avgServiceTime and
		// varServiceTime -> change logs
		double avgServiceTime = 50, varServiceTime = 200;

		double reqArrivalRate = 1 / (avgServiceTime + ((varInterArrivalTime + varServiceTime) / (2 * (responseTime - avgServiceTime))));

		// reqArrival rate is measured in miliseconds but provisioners work with
		// minutes
		reqArrivalRate *= sleep;

		logger.info("ReqArrivalRate: " + reqArrivalRate + " ,AvgServiceTime: " + avgServiceTime + ", VarServiceTime: " + varServiceTime + ", VarInterATime: "
				+ varInterArrivalTime);

		this.reqArrivalRate = reqArrivalRate;
		return (int) Math.ceil(pred / reqArrivalRate);
	}
}
