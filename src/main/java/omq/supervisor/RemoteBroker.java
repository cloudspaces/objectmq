package omq.supervisor;

import java.io.IOException;
import java.util.Properties;

import omq.Remote;
import omq.client.annotation.AsyncMethod;
import omq.client.annotation.MultiMethod;
import omq.client.annotation.SyncMethod;
import omq.exception.RemoteException;
import omq.exception.RetryException;
import omq.supervisor.util.HasObject;

public interface RemoteBroker extends Remote {

	@AsyncMethod
	public void spawnObject(String reference, String className, Properties env) throws Exception;

	@AsyncMethod
	public void spawnObject(String reference, String className) throws Exception;

	@AsyncMethod
	public void deleteObject(String reference) throws RemoteException, IOException;

	@MultiMethod
	@SyncMethod(retry = 1, timeout = 1000)
	public boolean[] hasObject(String reference) throws RetryException;

	@MultiMethod
	@SyncMethod(retry = 1, timeout = 1000)
	public HasObject[] hasObjectInfo(String reference) throws RetryException;

}