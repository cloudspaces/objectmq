package omq.server;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import omq.client.listener.IResponseWrapper;
import omq.common.util.ParameterQueue;

import org.apache.log4j.Logger;

import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

/**
 * An invocationThread waits for requests an invokes them.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class InvocationThread extends AInvocationThread {

	private static final Logger logger = Logger.getLogger(InvocationThread.class.getName());
	private static final String multi = "multi#";

	private String multiExchange;

	// RemoteObject

	public InvocationThread(RemoteObject obj) throws Exception {
		super(obj);
	}

	/**
	 * This method starts the queues using the information got in the
	 * environment.
	 * 
	 * @throws Exception
	 */
	protected void startQueues() throws Exception {
		// Start channel
		channel = broker.getNewChannel();

		// Get which queues will be used
		List<String> queues = startNormalQueues();
		queues.add(startPrivateQueue());

		/*
		 * Consumer
		 */

		// Disable Round Robin behavior
		boolean autoAck = false;

		int prefetchCount = 1;
		channel.basicQos(prefetchCount);

		// Declare a new consumer which will consume all the queues listed
		consumer = new QueueingConsumer(channel);
		for (String queue : queues) {
			channel.basicConsume(queue, autoAck, consumer);
		}
	}

	private List<String> startNormalQueues() throws Exception {
		List<String> queues = new ArrayList<String>();
		List<RabbitProperties> configList = obj.getConfigList();

		if (configList == null) {
			configList = new ArrayList<RabbitProperties>();

			String exchange = env.getProperty(ParameterQueue.RPC_EXCHANGE, ParameterQueue.DEFAULT_EXCHANGE);
			if ("".equals(exchange)) {
				throw new ConnectException("Cannot use default exchange \"\" because it's used to bound all queues by RabbitMQ");
			}
			String queue = reference;
			String routingKey = reference;

			// RemoteObject default queue
			boolean durable = Boolean.parseBoolean(env.getProperty(ParameterQueue.DURABLE_QUEUE, "false"));
			boolean exclusive = Boolean.parseBoolean(env.getProperty(ParameterQueue.EXCLUSIVE_QUEUE, "false"));
			boolean autoDelete = Boolean.parseBoolean(env.getProperty(ParameterQueue.AUTO_DELETE_QUEUE, "false"));

			List<String> routes = new ArrayList<String>();
			routes.add(routingKey);

			RabbitProperties defaultProps = new RabbitProperties(queue, exchange, "direct", routes, durable, exclusive, autoDelete);
			configList.add(defaultProps);
		}

		for (RabbitProperties props : configList) {
			String exchange = props.getExchange();

			String exchangeType = props.getExchangeType();
			// if (!"direct".equals(exchangeType) &&
			// !"topic".equals(exchangeType) && !"fanout".equals(exchangeType))
			// {
			// exchangeType = "direct";
			// }
			String queue = props.getQueue();

			queues.add(queue);

			// Load boolean properties for this particular queue
			boolean durable = props.isDurable();
			boolean exclusive = props.isExclusive();
			boolean autoDelete = props.isAutodelete();

			channel.exchangeDeclare(exchange, exchangeType);

			channel.queueDeclare(queue, durable, exclusive, autoDelete, null);

			for (String routingKey : props.getRoutes()) {
				channel.queueBind(queue, exchange, routingKey);
				logger.info("RemoteObject: " + reference + " declared topic exchange: " + exchange + ", Queue: " + queue + ", Durable: "
						+ durable + ", Exclusive: " + exclusive + ", AutoDelete: " + autoDelete + ", Binding: " + routingKey);
			}
		}

		return queues;
	}

	private String startPrivateQueue() throws Exception {
		// Get info about which exchange and queue will use

		// String exchange =
		// env.getProperty(ParameterQueue.RPC_PRIVATE_EXCHANGE,
		// ParameterQueue.DEFAULT_PRIVATE_EXCHANGE); // not necessary since all
		// queues are bound to the default exchange
		multiExchange = multi + reference;

		String queue = UID;
		// String uidKey = reference + "." + UID;

		// Multi queue (exclusive queue per remoteObject)
		boolean durable = Boolean.parseBoolean(env.getProperty(ParameterQueue.DURABLE_PQUEUE, "false"));
		boolean exclusive = Boolean.parseBoolean(env.getProperty(ParameterQueue.EXCLUSIVE_PQUEUE, "false"));
		boolean autoDelete = Boolean.parseBoolean(env.getProperty(ParameterQueue.AUTO_DELETE_PQUEUE, "true"));

		// Declare the exchanges and queue
		// channel.exchangeDeclare(exchange, "topic");
		channel.exchangeDeclare(multiExchange, "fanout");
		channel.queueDeclare(queue, durable, exclusive, autoDelete, null);

		// Bind both keys to the uid queue
		channel.queueBind(queue, multiExchange, "");
		// channel.queueBind(queue, exchange, uidKey);

		logger.info("RemoteObject: " + reference + " declared fanout exchange: " + multiExchange + ", Queue: " + queue + ", Durable: "
				+ durable + ", Exclusive: " + exclusive + ", AutoDelete: " + autoDelete);

		return queue;
	}

	@Override
	protected String getType(Delivery delivery) {
		String exchange = delivery.getEnvelope().getExchange();
		if (exchange.equals(multiExchange)) {
			return IResponseWrapper.MULTI_TYPE;
		}

		return IResponseWrapper.NORMAL_TYPE;
	}

}
