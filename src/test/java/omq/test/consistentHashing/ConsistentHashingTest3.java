/**
 * 
 */
package omq.test.consistentHashing;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import omq.common.broker.Broker;
import omq.common.message.Request;
import omq.common.util.ParameterQueue;
import omq.common.util.Serializer;
import omq.common.util.Serializers.ISerializer;
import omq.exception.SerializerException;
import omq.server.RabbitProperties;
import omq.test.calculator.Calculator;

import org.junit.BeforeClass;
import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 *
 */
public class ConsistentHashingTest3 {

	private static final String HASH_EXCHANGE = "consistent_hash", EXCHANGE_TYPE = "x-consistent-hash";

	private static Broker broker;
	private static FooImpl a, b;

	@BeforeClass
	public static void server() throws Exception {
		Properties env = new Properties();
		env.setProperty(ParameterQueue.USER_NAME, "guest");
		env.setProperty(ParameterQueue.USER_PASS, "guest");

		// Get host info of rabbimq (where it is)
		env.setProperty(ParameterQueue.RABBIT_HOST, "127.0.0.1");
		env.setProperty(ParameterQueue.RABBIT_PORT, "5672");

		a = new FooImpl();
		b = new FooImpl();

		Broker broker = new Broker(env);

		/*
		 * a
		 */
		List<String> routesA = new ArrayList<String>();
		routesA.add("100");
		RabbitProperties aProps = new RabbitProperties("aQueue", HASH_EXCHANGE, EXCHANGE_TYPE, routesA, false, false, false);
		List<RabbitProperties> aRabbit = new ArrayList<RabbitProperties>();
		aRabbit.add(aProps);
		broker.bind("a", a, aRabbit);

		/*
		 * b
		 */
		List<String> routesB = new ArrayList<String>();
		routesB.add("100");
		RabbitProperties bProps = new RabbitProperties("bQueue", HASH_EXCHANGE, EXCHANGE_TYPE, routesB, false, false, false);
		List<RabbitProperties> bRabbit = new ArrayList<RabbitProperties>();
		bRabbit.add(bProps);
		broker.bind("b", b, bRabbit);

		System.out.println("Server started");
	}

	@Test
	public void test() throws Exception {
		int sum = 100;
		Properties env = new Properties();
		env.setProperty(ParameterQueue.USER_NAME, "guest");
		env.setProperty(ParameterQueue.USER_PASS, "guest");

		// Get host info of rabbimq (where it is)
		env.setProperty(ParameterQueue.RABBIT_HOST, "127.0.0.1");
		env.setProperty(ParameterQueue.RABBIT_PORT, "5672");
		env.setProperty(ParameterQueue.RPC_EXCHANGE, HASH_EXCHANGE);
		
		broker = new Broker(env);
		
		for(int i = 0; i < sum; i++){
			Foo foo = broker.lookup("routing_key"+i, Foo.class);
			foo.ping();
		}
		
		Thread.sleep(5000);
		
		broker.stopBroker();
		
		
		System.out.println(a);
		System.out.println(b);
		
		assertEquals(sum, a.getCounter() + b.getCounter());
		assertTrue(a.getCounter() > 0);

	}

}
