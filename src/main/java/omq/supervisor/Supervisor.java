package omq.supervisor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import omq.common.broker.Broker;
import omq.exception.RemoteException;
import omq.exception.RetryException;
import omq.supervisor.util.FileWriterSuper;
import omq.supervisor.util.HasObject;
import omq.supervisor.util.PredictiveProvisioner;
import omq.supervisor.util.ReactiveProvisioner;

import org.apache.log4j.Logger;

import startExperiment.Start;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Supervisor extends Thread {

	/**
	 * 
	 */
	private static final Logger logger = Logger.getLogger(Supervisor.class.getName());
	private static final long START_AT = 0;
	private static final long WINDOW_PRED = 900; // 15 minutes
	private static final long WINDOW_REAC = 300; // 5 minutes

	private String brokerSet;
	private String objReference;
	private OmqSettings omqSettings;
	private RemoteBroker remoteBroker;
	private PredictiveProvisioner predProvisioner;
	private boolean killed = false;

	private AtomicInteger numServersNeed;

	 private ReactiveProvisioner reacProvisioner;

	public Supervisor(String brokerSet, String objReference, OmqSettings omqSettings) {
		this.brokerSet = brokerSet;
		this.objReference = objReference;
		this.omqSettings = omqSettings;
	}

	public void startSupervisor(Broker broker, String filename, double tLow, double tHigh) throws RemoteException, IOException {
		remoteBroker = broker.lookup(brokerSet, RemoteBroker.class);

		// create & start Provisioners
		 predProvisioner = new PredictiveProvisioner(objReference, filename,
		 this);
		 predProvisioner.setStartAt(START_AT);
		 predProvisioner.setWindowSize(WINDOW_PRED);
		 predProvisioner.setSleep(WINDOW_PRED * 1000);

		 reacProvisioner = new ReactiveProvisioner(objReference, filename,
		 this, tLow, tHigh);
		 reacProvisioner.setStartAt(START_AT);
		 reacProvisioner.setWindowSize(WINDOW_REAC);
		 reacProvisioner.setSleep(WINDOW_REAC * 1000);

		// check if the file exists
		try {
			getStatus("%2f", objReference);
		} catch (FileNotFoundException e) {
			// Create a new remoteObject
			try {
				HasObject[] hasList = getHasList();
				List<HasObject> list = new ArrayList<HasObject>();
				list.add(hasList[0]);
				createObjects(1, list);
				// wait some seconds until the object is created and the manager
				// plugin knows new changes
				Thread.sleep(5000);
			} catch (Exception e1) {
				System.exit(1);
			}

		}

		/*
		 * START EXPERMIENT!!!! THIS IS BULLSHIT BUT I'LL START THE EXPERIMENT
		 */
		Start start = broker.lookup("start", Start.class);
		start.startExperiment();

		numServersNeed = new AtomicInteger(1);

		// start both thread
		predProvisioner.start();
		reacProvisioner.start();

		this.start();
	}

	@Override
	public void run() {

		while (!killed) {
			try {
				HasObject[] hasList = getHasList();
				int needed = numServersNeed.get();

				List<HasObject> serversWithObject = whoHasObject(hasList, true);

				// Ask how many servers are
				int numServersNow = serversWithObject.size();

				int diff = needed - numServersNow;
				if (diff > 0) {
					logger.info("Creating " + diff + " " + objReference);
					FileWriterSuper.write(new Date(), "CREATE", diff);
					// Calculate servers without object
					List<HasObject> serversWithoutObject = whoHasObject(hasList, false);
					// Create as servers as needed
					createObjects(diff, serversWithoutObject);
					Thread.sleep(5000);
				}
				// At least 1 server should survive
				if (diff < 0 && needed > 0) {
					diff *= -1;
					logger.info("Removing " + diff + " " + objReference);
					FileWriterSuper.write(new Date(), "REMOVE", diff);
					// Remove as servers as said
					removeObjects(diff, serversWithObject);
					Thread.sleep(5000);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void setNumServersNeeded(int numNeeded) {
		numServersNeed.set(numNeeded);
	}

	public HasObject[] getHasList() throws RetryException {
		return remoteBroker.hasObjectInfo(objReference);
	}

	// TODO create an specific exception when it's impossible to create a new
	// object
	public synchronized void createObjects(int numRequired, List<HasObject> serversWithoutObject) throws Exception {
		System.out.println("CreateObjects " + System.currentTimeMillis());
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

	public List<HasObject> whoHasObject(HasObject[] hasList, boolean condition) throws RetryException {
		List<HasObject> list = new ArrayList<HasObject>();
		for (HasObject h : hasList) {
			if (h.hasObject() == condition) {
				list.add(h);
			}
		}

		return list;
	}

	public ReactiveProvisioner getReacProvisioner() {
		return reacProvisioner;
	}

	public void setReacProvisioner(ReactiveProvisioner reacProvisioner) {
		this.reacProvisioner = reacProvisioner;
	}

	private int getStatus(String vhost, String queue) throws IOException {
		URL url = new URL("http://localhost:15672/api/queues/" + vhost + "/" + queue);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setRequestMethod("GET");

		String userpass = "guest" + ":" + "guest";
		String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());

		connection.setRequestProperty("Authorization", basicAuth);

		connection.connect();

		int status = connection.getResponseCode();
		System.out.println(status);

		BufferedReader buff = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String json = "", line;
		while ((line = buff.readLine()) != null) {
			json += line + "\n";
		}
		buff.close();

		JsonParser parser = new JsonParser();
		JsonObject jsonObj = parser.parse(json).getAsJsonObject();
		try {
			return jsonObj.get("message_stats").getAsJsonObject().get("deliver_get").getAsInt();
		} catch (NullPointerException e) {
			return 0;
		}

	}
}
