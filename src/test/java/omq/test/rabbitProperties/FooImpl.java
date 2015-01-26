package omq.test.rabbitProperties;

import omq.server.RemoteObject;

public class FooImpl extends RemoteObject implements Foo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private char c;

	public FooImpl(char c) {
		this.c = c;
	}

	public int getChar() {
		System.out.println("SOC getChar "+ c);
		return c;
	}

	public void setChar(char c) {
		System.out.println("SOC setChar "+ this.c);
		this.c = c;
	}

}
