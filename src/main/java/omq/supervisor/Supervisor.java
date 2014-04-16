package omq.supervisor;

import java.io.IOException;
import java.util.List;

import omq.common.broker.Broker;
import omq.exception.RemoteException;
import omq.exception.RetryException;
import omq.supervisor.util.HasObject;
import omq.supervisor.util.PredictiveProvisioner;
import omq.supervisor.util.ReactiveProvisioner;

import org.apache.log4j.Logger;

public class Supervisor {

	/**
	 * 
	 */
	private static final Logger logger = Logger.getLogger(Supervisor.class.getName());
	private static final long START_AT = 0;
	private static final long WINDOW_PRED = 3600; // 1 hour
	private static final long WINDOW_REAC = 300; // 5 minutes

	private String brokerSet;
	private String objReference;
	private OmqSettings omqSettings;
	private RemoteBroker remoteBroker;
	private PredictiveProvisioner predProvisioner;
	private ReactiveProvisioner reacProvisioner;

	public Supervisor(String brokerSet, String objReference, OmqSettings omqSettings) {
		this.brokerSet = brokerSet;
		this.objReference = objReference;
		this.omqSettings = omqSettings;
	}

	public void startSupervisor(Broker broker, String filename, double tLow, double tHigh) throws RemoteException, IOException {
		remoteBroker = broker.lookup(brokerSet, RemoteBroker.class);

		// create & start Provisioners
		predProvisioner = new PredictiveProvisioner(objReference, filename, this);
		predProvisioner.setStartAt(START_AT);
		predProvisioner.setWindowSize(WINDOW_PRED);

		reacProvisioner = new ReactiveProvisioner(objReference, filename, this, tLow, tHigh);
		reacProvisioner.setStartAt(START_AT);
		reacProvisioner.setWindowSize(WINDOW_REAC);

		// start both thread
		predProvisioner.start();
		reacProvisioner.start();
	}

	public HasObject[] getHasList() throws RetryException {
		return remoteBroker.hasObjectInfo(objReference);
	}

	// TODO create an specific exception when it's impossible to create a new
	// object
	public synchronized void createObjects(int numRequired, List<HasObject> serversWithoutObject) throws Exception {

		int i = 0;
		while (i < serversWithoutObject.size() && i < numRequired) {
			String brokerName = serversWithoutObject.get(i).getBrokerName();
			// Use a single broker
			remoteBroker.setUID(brokerName);

			try {
				if (omqSettings.getEnv() == null) {
					remoteBroker.spawnObject(objReference, omqSettings.getClassName());
				} else {
					remoteBroker.spawnObject(objReference, omqSettings.getClassName(), omqSettings.getEnv());
				}
			} catch (Exception e) {
				logger.error("Could not create an object in broker " + brokerName, e);
			}
			// Remove the UID in order to use the brokerSet name
			remoteBroker.setUID(null);
			i++;
		}
		logger.info("Num objects " + objReference + " created = " + i + ", num objects needed = " + numRequired);

	}

	// TODO create an specific exception when it's impossible to remove a new
	// object
	public synchronized void removeObjects(int numToDelete, List<HasObject> serversWithObject) throws Exception {

		int i = 0;
		while (i < serversWithObject.size() && i < numToDelete) {
			String brokerName = serversWithObject.get(i).getBrokerName();
			// Use a single broker
			remoteBroker.setUID(brokerName);

			try {
				remoteBroker.deleteObject(objReference);
			} catch (Exception e) {
				logger.error("Could not delete an object in broker " + brokerName, e);
			}

			// Remove the UID in order to use the brokerSet name
			remoteBroker.setUID(null);
			i++;
		}
		logger.info("Num objects " + objReference + " deleted = " + i + ", num objects needed to delete = " + numToDelete);

	}

	public String getBrokerSet() {
		return brokerSet;
	}

	public void setBrokerSet(String brokerSet) {
		this.brokerSet = brokerSet;
	}

	public String getObjReference() {
		return objReference;
	}

	public void setObjReference(String objReference) {
		this.objReference = objReference;
	}

	public OmqSettings getOmqSettings() {
		return omqSettings;
	}

	public void setOmqSettings(OmqSettings omqSettings) {
		this.omqSettings = omqSettings;
	}

	public RemoteBroker getBroker() {
		return remoteBroker;
	}

	public void setBroker(RemoteBroker broker) {
		this.remoteBroker = broker;
	}

	public PredictiveProvisioner getPredProvisioner() {
		return predProvisioner;
	}

	public void setPredProvisioner(PredictiveProvisioner predProvisioner) {
		this.predProvisioner = predProvisioner;
	}

	public ReactiveProvisioner getReacProvisioner() {
		return reacProvisioner;
	}

	public void setReacProvisioner(ReactiveProvisioner reacProvisioner) {
		this.reacProvisioner = reacProvisioner;
	}

}
