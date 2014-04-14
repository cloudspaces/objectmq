package omq.supervisor.broker;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import omq.Remote;
import omq.exception.RemoteException;
import omq.exception.RetryException;
import omq.supervisor.util.HasObject;

public interface RemoteBroker extends Remote {
	public Set<String> getRemoteObjects();

	public void spawnObject(String reference, String className, Properties env) throws Exception;

	public void spawnObject(String reference, String className) throws Exception;

	public void deleteObject(String reference) throws RemoteException, IOException;

	public boolean hasObject(String reference) throws RetryException;

	public HasObject hasObjectInfo(String reference) throws RetryException;

}