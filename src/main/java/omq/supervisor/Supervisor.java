package omq.supervisor;

import omq.Remote;
import omq.supervisor.util.HasObject;

public interface Supervisor extends Remote {

	public void subscribe(String brokerSet, String brokerName) throws Exception;

	public void spawnObject(OmqSettings settings) throws Exception;

	public void spawnObject(OmqSettings settings, HasObject[] hasList, int numObjects) throws Exception;

	public void unbindObject(OmqSettings settings, HasObject[] hasList, int numObjects) throws Exception;

}
