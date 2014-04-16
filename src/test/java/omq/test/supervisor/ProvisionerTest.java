package omq.test.supervisor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import omq.common.broker.Broker;
import omq.common.util.ParameterQueue;
import omq.supervisor.OmqSettings;
import omq.supervisor.RemoteBroker;
import omq.supervisor.Supervisor;
import omq.supervisor.util.HasObject;
import omq.supervisor.util.PredictiveProvisioner;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProvisionerTest {

	private static String objReference = "Foo";
	private static String brokerSet = "broker";
	private static Broker b1, b2, b3;
	private static Supervisor supervisor;

	@BeforeClass
	public static void createServers() throws Exception {
		Properties env1 = new Properties();
		env1.setProperty(ParameterQueue.USER_NAME, "guest");
		env1.setProperty(ParameterQueue.USER_PASS, "guest");

		// Get host info of rabbimq (where it is)
		env1.setProperty(ParameterQueue.RABBIT_HOST, "127.0.0.1");
		env1.setProperty(ParameterQueue.RABBIT_PORT, "5672");
		env1.setProperty(ParameterQueue.NUM_THREADS, "1");

		b1 = new Broker(env1);

		Properties env2 = new Properties();
		env2.setProperty(ParameterQueue.USER_NAME, "guest");
		env2.setProperty(ParameterQueue.USER_PASS, "guest");

		// Get host info of rabbimq (where it is)
		env2.setProperty(ParameterQueue.RABBIT_HOST, "127.0.0.1");
		env2.setProperty(ParameterQueue.RABBIT_PORT, "5672");
		env2.setProperty(ParameterQueue.NUM_THREADS, "1");

		b2 = new Broker(env2);

		// Allow remtoe allocation
		b1.allowRemoteAllocation(brokerSet, "b1");
		b2.allowRemoteAllocation(brokerSet, "b2");

		// Create a supervisor
		Properties env3 = new Properties();
		env3.setProperty(ParameterQueue.USER_NAME, "guest");
		env3.setProperty(ParameterQueue.USER_PASS, "guest");

		// Get host info of rabbimq (where it is)
		env3.setProperty(ParameterQueue.RABBIT_HOST, "127.0.0.1");
		env3.setProperty(ParameterQueue.RABBIT_PORT, "5672");
		env3.setProperty(ParameterQueue.NUM_THREADS, "1");

		b3 = new Broker(env3);
		RemoteBroker remoteBroker = b3.lookup(brokerSet, RemoteBroker.class);

		OmqSettings omqSettings = new OmqSettings(objReference, FooImpl.class.getCanonicalName(), null);
		supervisor = new Supervisor(brokerSet, objReference, omqSettings);
		// Simulate start supervisor
		supervisor.setBroker(remoteBroker);

	}

	@Test
	public void checkCreateAndDestroyObjects() throws Exception {

		// Create
		
		int numRequired = 2;
		List<HasObject> serversWithoutObject = new ArrayList<HasObject>();

		serversWithoutObject.add(new HasObject("b1", objReference, false, null));
		serversWithoutObject.add(new HasObject("b2", objReference, false, null));

		supervisor.createObjects(numRequired, serversWithoutObject);

		Thread.sleep(5000);
		assertTrue(b1.getRemoteObjs().containsKey(objReference));
		assertTrue(b2.getRemoteObjs().containsKey(objReference));

		// Remove

		int numToDelete = 2;
		List<HasObject> serversWithObject = new ArrayList<HasObject>();

		serversWithObject.add(new HasObject("b1", objReference, true, null));
		serversWithObject.add(new HasObject("b2", objReference, true, null));

		supervisor.removeObjects(numToDelete, serversWithObject);

		Thread.sleep(5000);
		assertFalse(b1.getRemoteObjs().containsKey(objReference));
		assertFalse(b2.getRemoteObjs().containsKey(objReference));
	}

	@Test
	public void predArrivalRatetest() throws IOException {
		PredictiveProvisioner pred = new PredictiveProvisioner("objReference", "workload_up.txt", null);
		int predArrival = pred.getPredArrivalRate(pred.getDay(), 0, 3600);
		System.out.println(predArrival);
	}

}
