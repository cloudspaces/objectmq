/**
 * 
 */
package omq.test.consistentHashing;

import omq.server.RemoteObject;

/**
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class FooImpl extends RemoteObject implements Foo {

	private int counter = 0;

	@Override
	public synchronized void ping() {
		counter++;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Counter = " + counter;
	}

}
