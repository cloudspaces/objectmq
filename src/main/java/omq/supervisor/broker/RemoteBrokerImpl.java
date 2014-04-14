package omq.supervisor.broker;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import omq.exception.RemoteException;
import omq.exception.RetryException;
import omq.server.RemoteObject;
import omq.supervisor.util.HasObject;

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

	@Override
	public Set<String> getRemoteObjects() {
		return getBroker().getRemoteObjs().keySet();
	}

	@Override
	public void spawnObject(String reference, String className, Properties env) throws Exception {
		System.out.println("SPAWN broker = " + this.getUID());
		RemoteObject remote = (RemoteObject) Class.forName(className).newInstance();
		getBroker().bind(reference, remote, env);
	}

	@Override
	public void spawnObject(String reference, String className) throws Exception {
		System.out.println("SPAWN broker = " + this.getUID());
		RemoteObject remote = (RemoteObject) Class.forName(className).newInstance();
		getBroker().bind(reference, remote);
	}

	@Override
	public void deleteObject(String reference) throws RemoteException, IOException {
		getBroker().unbind(reference);
	}

	@Override
	public boolean hasObject(String reference) throws RetryException {
		return getBroker().getRemoteObjs().containsKey(reference);
	}

	@Override
	public HasObject hasObjectInfo(String reference) throws RetryException {
		System.out.println("Hola soc un broker" + getRef() + ", " + getUID() + ", fil: " + Thread.currentThread().getId());
		if (getBroker().getRemoteObjs().containsKey(reference)) {
			RemoteObject r = getBroker().getRemoteObjs().get(reference);
			int numThreads = r.getPool().getWorkers().size();
			return new HasObject(this.getUID(), reference, true);
		}
		return new HasObject(this.getUID(), reference, false);
	}

}
