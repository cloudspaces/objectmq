package omq.supervisor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import omq.exception.RemoteException;
import omq.server.RemoteObject;
import omq.supervisor.util.HasObject;

import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;

public class SupervisorImpl extends RemoteObject implements Supervisor, Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SupervisorImpl.class.getName());

	private String brokerSet;
	private long sleep;
	private Map<String, OmqSettings> objectSettings;
	private RemoteBroker broker;
	private Set<String> brokers;

	public SupervisorImpl(String brokerSet, long sleep) {
		this.brokerSet = brokerSet;
		this.sleep = sleep;
		brokers = new HashSet<String>();
		objectSettings = new HashMap<String, OmqSettings>();
	}

	@Override
	public void run() {
		try {
			broker = getBroker().lookup(brokerSet, RemoteBroker.class);
			while (true) {

				Set<String> keys = objectSettings.keySet();
				for (String reference : keys) {
					System.out.println("key = " + reference);
					try {
						checkObject(reference);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	@Override
	public void subscribe(String brokerSet, String brokerName) throws Exception {
		if (brokerSet.equals(brokerSet) && !brokers.contains(brokerName)) {
			logger.info("Broker " + brokerName + " subscrived");
			brokers.add(brokerName);
		} else {
			throw new Exception("blablabla");
		}
	}

	@Override
	public void spawnObject(OmqSettings settings) throws Exception {
		String reference = settings.getReference();

		if (objectSettings.containsKey(reference)) {
			throw new Exception("JAJAJAJAJA");
		}

		HasObject[] hasList = broker.hasObjectInfo(reference);

		int minObjects = settings.getMinNumberObjects();
		int numBrokers = hasList.length;
		int numObjects = 0;

		for (HasObject h : hasList) {
			if (h.hasObject()) {
				numObjects++;
			}
		}
		System.out.println("NumObjects " + numObjects + " numBrokers " + numBrokers);

		int i = 0;
		while (numObjects < minObjects && i < numBrokers) {
			HasObject h = hasList[i++];
			if (!h.hasObject()) {
				// Use a single broker
				broker.setUID(h.getBrokerName());
				broker.spawnObject(reference, settings.getClassName());
				// Remove the UID
				broker.setUID(null);
				numObjects++;
			}
		}

		// Once there are some objects created, put objectSettings
		objectSettings.put(reference, settings);

	}

	@Override
	public void spawnObject(OmqSettings settings, HasObject[] hasList, int numObjects) throws Exception {
		System.out.println("FUNCIO SPAWN");
		String reference = settings.getReference();

		if (!objectSettings.containsKey(reference)) {
			objectSettings.put(reference, settings);
		}

		int numBrokers = hasList.length;

		if (numObjects < numBrokers) {
			for (HasObject h : hasList) {
				if (!h.hasObject()) {
					// Use a single broker
					broker.setUID(h.getBrokerName());
					if (settings.getProps() == null) {
						broker.spawnObject(reference, settings.getClassName());
					} else {
						broker.spawnObject(reference, settings.getClassName(), settings.getProps());
					}
					// Remove the UID
					broker.setUID(null);
					break;
				}
			}
		}
	}

	@Override
	public void unbindObject(OmqSettings settings, HasObject[] hasList, int numObjects) throws Exception {
		System.out.println("FUNCIO UNBIND");

		String reference = settings.getReference();

		int minObjects = settings.getMinNumberObjects();

		if (numObjects > minObjects) {
			for (HasObject h : hasList) {
				if (h.hasObject()) {
					// Use a single broker
					broker.setUID(h.getBrokerName());
					broker.deleteObject(reference);
					// Remove the UID
					broker.setUID(null);
					break;
				}
			}
		}
	}

	private void checkObject(String reference) throws Exception {
		OmqSettings settings = objectSettings.get(reference);

		int minObjects = settings.getMinNumberObjects();
		int maxMessages = settings.getMaxNumQueued();
		int minMessages = settings.getMinNumQueued();

		Channel channel = getBroker().getChannel();
		DeclareOk dok = channel.queueDeclarePassive(reference);

		int numObjects = 0;
		int numMessages = dok.getMessageCount();

		HasObject[] hasList = broker.hasObjectInfo(reference);
		for (HasObject h : hasList) {
			if (h.hasObject()) {
				numObjects++;
			}
		}

		System.out.println("Num Consumers: " + numObjects + ", num Messages: " + numMessages);

		if (maxMessages < numMessages || numObjects < minObjects) {
			logger.info("SPAWN TIME!!");
			System.out.println("SPAAAAAAAAAAAAAAAAAAAAAAAAAAWN TIME!!!!!");
			spawnObject(settings, hasList, numObjects);
		} else if (numMessages < minMessages && minObjects < numObjects) {
			logger.info("Unbinding object!!!");
			System.out.println("UNBINDIN OOOOOOOOOOOOOOOOOOBJECT!!!");
			unbindObject(settings, hasList, numObjects);
		}
	}

	public Map<String, OmqSettings> getObjectSettings() {
		return objectSettings;
	}

	public void setObjectSettings(Map<String, OmqSettings> objectSettings) {
		this.objectSettings = objectSettings;
	}

	public String getBrokerSet() {
		return brokerSet;
	}

	public void setBrokerSet(String brokerSet) {
		this.brokerSet = brokerSet;
	}

}
