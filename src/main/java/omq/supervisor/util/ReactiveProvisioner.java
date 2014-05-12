package omq.supervisor.util;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import omq.common.broker.Measurement;
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

		double prevStatus = 0;

		while (!killed) {
			try {
				Thread.sleep(sleep);

				// GetStatus returns the number of messages queued along the
				// time. For this reason, it's necessary to save the previous
				// state
				double status = getStatus("%2f", objReference);
				double obs = status - prevStatus;
				prevStatus = status;

				double pred = getPredArrivalRate(day, startAt, windowSize);

				double ratio = obs / pred;

				if (ratio < tLow || ratio > tHigh) {
					action(obs, pred);
				}

				startAt += windowSize;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void action(double obs, double pred) {
		try {
			HasObject[] hasList = getHasList();
			int numServersNeeded;

			numServersNeeded = getNumServersNeeded(obs, pred, hasList);

			List<HasObject> serversWithObject = whoHasObject(hasList, true);

			// Ask how many servers are
			int numServersNow = serversWithObject.size();

			int diff = numServersNeeded - numServersNow;

			logger.info("Obs param: " + obs + ", Pred param: " + pred + ", NumServersNeeded: " + numServersNeeded + " NumSerNow: " + numServersNow);
			FileWriterProvisioner.write(new Date(), "REACTIVE", obs, pred, reqArrivalRate, avgServiceTime, varServiceTime, varInterArrivalTime,
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

	private int getNumServersNeeded(double obs, double pred, HasObject[] hasList) throws Exception {
		// There are no servers available
		if (hasList.length == 0) {
			throw new Exception("Cannot find any server available");
		}

		double avgServiceTime = 0, varServiceTime = 0, varInterArrivalTime = 0;

		// Calculate avgServiceTime, varServiceTime, varInterArrivalTime
		int i = 0;
		for (HasObject h : hasList) {
			Measurement m = h.getMeasurement();
			if (h.hasObject() && m != null) {
				avgServiceTime += m.getAvgServiceTime();
				varServiceTime += m.getVarServiceTime();
				varInterArrivalTime += m.getVarInterArrivalTime();
				i++;
			}
		}

		// There are no servers with the required object, at least 1 server is
		// needed
		if (i == 0) {
			return 1;
		}

		// Calculate mean times among servers
		avgServiceTime /= i;
		varServiceTime /= i;
		varInterArrivalTime /= i;

		this.avgServiceTime = avgServiceTime;
		this.varServiceTime = varServiceTime;
		this.varInterArrivalTime = varInterArrivalTime;

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
