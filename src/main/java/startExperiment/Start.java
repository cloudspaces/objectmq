package startExperiment;

import omq.Remote;
import omq.client.annotation.AsyncMethod;
import omq.client.annotation.MultiMethod;

public interface Start extends Remote {
	@AsyncMethod
	@MultiMethod
	public void startExperiment();
}
