package omq.test.rabbitProperties;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import omq.common.broker.Broker;
import omq.common.util.ParameterQueue;
import omq.common.util.Serializer;
import omq.server.RabbitProperties;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class FooTest {

	private static Broker broker;
	private static Foo a1, a2, c;

	public FooTest(String type) throws Exception {
		Properties env = new Properties();
		env.setProperty(ParameterQueue.USER_NAME, "guest");
		env.setProperty(ParameterQueue.USER_PASS, "guest");

		// Serializer info
		env.setProperty(ParameterQueue.PROXY_SERIALIZER, type);

		// Set host info of rabbimq (where it is)
		env.setProperty(ParameterQueue.RABBIT_HOST, "127.0.0.1");
		env.setProperty(ParameterQueue.RABBIT_PORT, "5672");

		broker = new Broker(env);
		a1 = broker.lookup("a", "global_exchange", "a", Foo.class);
		a2 = broker.lookup("a", "global_exchange", "b", Foo.class);
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { Serializer.JAVA } , { Serializer.GSON }, { Serializer.KRYO } };
		return Arrays.asList(data);
	}

	@BeforeClass
	public static void server() throws Exception {
		Properties env = new Properties();
		env.setProperty(ParameterQueue.USER_NAME, "guest");
		env.setProperty(ParameterQueue.USER_PASS, "guest");

		// Get host info of rabbimq (where it is)
		env.setProperty(ParameterQueue.RABBIT_HOST, "127.0.0.1");
		env.setProperty(ParameterQueue.RABBIT_PORT, "5672");

		//
		env.setProperty(ParameterQueue.MULTIPLE_BINDS, "true");

		FooImpl a1 = new FooImpl('a');
		FooImpl a2 = new FooImpl('b');

		Broker broker = new Broker(env);

		/*
		 * a
		 */
		List<String> routesA = new ArrayList<String>();
		routesA.add("a");
		RabbitProperties aProps = new RabbitProperties("aQueue", "global_exchange", routesA);
		List<RabbitProperties> aRabbit = new ArrayList<RabbitProperties>();
		aRabbit.add(aProps);
		broker.bind("a", a1, aRabbit);

		/*
		 * b
		 */
		List<String> routesA2 = new ArrayList<String>();
		routesA2.add("b");
		RabbitProperties a2Props = new RabbitProperties("bQueue", "global_exchange", routesA2);
		List<RabbitProperties> a2Rabbit = new ArrayList<RabbitProperties>();
		a2Rabbit.add(a2Props);
		broker.bind("a", a2, a2Rabbit);

		System.out.println("Server started");
	}

	@After
	public void stop() throws Exception {
		broker.stopBroker();
	}

	@Test
	public void test() {

		assertEquals('a', a1.getChar());
		assertEquals('b', a2.getChar());

		a1.setChar('A');
		assertEquals('A', a1.getChar());

		a2.setChar('B');
		assertEquals('B', a2.getChar());
		
		a1.setChar('a');
		a2.setChar('b');

	}

}
