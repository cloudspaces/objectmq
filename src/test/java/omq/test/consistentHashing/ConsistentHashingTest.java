/**
 * 
 */
package omq.test.consistentHashing;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import omq.common.broker.Broker;
import omq.common.message.Request;
import omq.common.util.ParameterQueue;
import omq.common.util.Serializer;
import omq.common.util.Serializers.ISerializer;
import omq.exception.SerializerException;
import omq.server.RabbitProperties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.esotericsoftware.kryo.serializers.JavaSerializer;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */

public class ConsistentHashingTest {
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
	public void test() throws java.io.IOException, SerializerException, InterruptedException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.exchangeDeclare(HASH_EXCHANGE, "x-consistent-hash");

		
		Serializer serializer = new Serializer(null);
		ISerializer ser = serializer.getInstance(Serializer.JAVA);
		
		Request request; 
		

		for (int i = 0; i < 1000000; i++) {
			String client_route = java.util.UUID.randomUUID().toString();
			// String client_route = i + "";
			request = new Request(client_route, "ping", true, null, false);
			
			BasicProperties props = new BasicProperties.Builder().appId("").correlationId(client_route).replyTo("")
					.type(Serializer.JAVA).deliveryMode(null).build();
			
			
			
			channel.basicPublish(HASH_EXCHANGE, client_route, props, ser.serialize(request));

		}
		System.out.println(" [x] Finished");

		channel.close();
		connection.close();
		
		Thread.sleep(60000);
		
		System.out.println(a);
		System.out.println(b);
	}
}
