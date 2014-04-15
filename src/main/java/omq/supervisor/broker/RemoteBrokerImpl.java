package omq.supervisor.broker;

import java.util.Properties;
import java.util.Set;

import omq.exception.RetryException;
import omq.server.RemoteObject;
import omq.supervisor.util.HasObject;

import org.apache.log4j.Logger;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class RemoteBrokerImpl extends RemoteObject implements RemoteBroker {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(RemoteBrokerImpl.class.getName());

	@Override
	public Set<String> getRemoteObjects() {
		return getBroker().getRemoteObjs().keySet();
	}

	@Override
	public void spawnObject(String reference, String className, Properties env) throws Exception {

		logger.info("Broker " + this.getUID() + "will spawn " + reference);
		try {
			RemoteObject remote = (RemoteObject) Class.forName(className).newInstance();
			getBroker().bind(reference, remote, env);
		} catch (Exception e) {
			logger.error("Could not spawn object " + reference, e);
			// Throw the exception to the supervisor
			throw e;
		}
	}

	@Override
	public void spawnObject(String reference, String className) throws Exception {
		logger.info("Broker " + this.getUID() + "will spawn " + reference);
		try {
			RemoteObject remote = (RemoteObject) Class.forName(className).newInstance();
			getBroker().bind(reference, remote);
		} catch (Exception e) {
			logger.error("Could not spawn object " + reference, e);
			// Throw the exception to the supervisor
			throw e;
		}
	}

	@Override
	public void deleteObject(String reference) throws Exception {
		logger.info("Broker " + this.getUID() + "will delete " + reference);
		try {
			getBroker().unbind(reference);
		} catch (Exception e) {
			logger.error("Could not delete object " + reference, e);
			// Throw the exception to the supervisor
			throw e;
		}
	}

	@Override
	public boolean hasObject(String reference) throws RetryException {
		return getBroker().getRemoteObjs().containsKey(reference);
	}

	@Override
	public HasObject hasObjectInfo(String reference) throws RetryException {
		System.out.println("Hola soc un broker" + getRef() + ", " + getUID() + ", fil: " + Thread.currentThread().getId());
		if (getBroker().getRemoteObjs().containsKey(reference)) {
			return new HasObject(this.getUID(), reference, true);
		}
		return new HasObject(this.getUID(), reference, false);
	}

}
