package omq.server;

import java.util.List;

public class RabbitProperties {
	private String queue, exchange, exchangeType = "direct";
	private List<String> routes;
	private boolean durable = false, exclusive = false, autodelete = false;

	public RabbitProperties(String queue, List<String> routes) {
		this.queue = queue;
		this.routes = routes;
	}

	public RabbitProperties(String queue, String exchange, List<String> routes) {
		this.queue = queue;
		this.exchange = exchange;
		this.routes = routes;
	}

	public RabbitProperties(String queue, String exchange, List<String> routes, boolean durable, boolean exclusive, boolean autodelete) {
		this.queue = queue;
		this.exchange = exchange;
		this.routes = routes;
		this.durable = durable;
		this.exclusive = exclusive;
		this.autodelete = autodelete;
	}

	public RabbitProperties(String queue, String exchange, String exchangeType, List<String> routes, boolean durable, boolean exclusive,
			boolean autodelete) {
		super();
		this.queue = queue;
		this.exchange = exchange;
		this.exchangeType = exchangeType;
		this.routes = routes;
		this.durable = durable;
		this.exclusive = exclusive;
		this.autodelete = autodelete;
	}

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public String getExchange() {
		return exchange;
	}

	public void setExchange(String exchange) {
		this.exchange = exchange;
	}

	public List<String> getRoutes() {
		return routes;
	}

	public void setRoutes(List<String> routes) {
		this.routes = routes;
	}

	public boolean isDurable() {
		return durable;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	public boolean isExclusive() {
		return exclusive;
	}

	public void setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
	}

	public boolean isAutodelete() {
		return autodelete;
	}

	public void setAutodelete(boolean autodelete) {
		this.autodelete = autodelete;
	}

	public String getExchangeType() {
		return exchangeType;
	}

	public void setExchangeType(String exchangeType) {
		this.exchangeType = exchangeType;
	}

}
