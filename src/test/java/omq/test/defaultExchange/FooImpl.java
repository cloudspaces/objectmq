package omq.test.defaultExchange;

import omq.server.RemoteObject;

public class FooImpl extends RemoteObject implements Foo{

	/**
	 * 
	 */
	private static final long serialVersionUID = -9203192139599843528L;

	@Override
	public int returnZero() {
		return 0;
	}

}
