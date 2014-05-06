package omq.supervisor.util;

import java.io.IOException;
import java.util.Date;
import java.util.List;

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

				// Thread.sleep(sleep);
				Thread.sleep(0);
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
			HasObject[] hasList = getHasList();
			int numServersNeeded;

			numServersNeeded = getNumServersNeeded(pred, varInterArrivalTime, hasList);

			List<HasObject> serversWithObject = whoHasObject(hasList, true);

			// Ask how many servers are
			int numServersNow = serversWithObject.size();

			int diff = numServersNeeded - numServersNow;

			logger.info("Pred param: " + pred + ", NumServersNeeded: " + numServersNeeded + " NumSerNow: " + numServersNow);
			FileWriterProvisioner.write(new Date(), "Predictive", 0, pred, reqArrivalRate, avgServiceTime, varServiceTime, varInterArrivalTime,
					numServersNeeded, numServersNow);
			if (diff > 0) {
				// Calculate servers without object
				List<HasObject> serversWithoutObject = whoHasObject(hasList, false);
				// Create as servers as needed
				supervisor.createObjects(diff, serversWithoutObject);
			}
			// At least 1 server should survive
			if (diff < 0 && numServersNeeded > 0) {
				diff *= -1;
				// Remove as servers as said
				supervisor.removeObjects(diff, serversWithObject);
			}
		} catch (Exception e1) {
			logger.error("Object: " + objReference, e1);
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public int getNumServersNeeded(double pred, double varInterArrivalTime, HasObject[] hasList) throws Exception {
		// There are no servers available
		if (hasList.length == 0) {
			throw new Exception("Cannot find any server available");
		}

		// TODO change this!!!! use the historical avgServiceTime and
		// varServiceTime -> change logs
		double avgServiceTime = 23, varServiceTime = 20;

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
