package omq.test.supervisor;

import omq.Remote;
import omq.server.RemoteObject;

public class FooImpl extends RemoteObject implements Remote {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FooImpl() {
		System.out.println("I'm a new Foo");
	}

}
