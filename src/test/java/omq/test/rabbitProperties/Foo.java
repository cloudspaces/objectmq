package omq.test.rabbitProperties;

import omq.Remote;

public interface Foo extends Remote {
	public void setChar(char c);

	public int getChar();
}
