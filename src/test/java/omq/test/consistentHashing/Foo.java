/**
 * 
 */
package omq.test.consistentHashing;

import omq.Remote;
import omq.client.annotation.AsyncMethod;
import omq.client.annotation.RemoteInterface;
import omq.client.annotation.SyncMethod;

/**
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
@RemoteInterface
public interface Foo extends Remote {
	@AsyncMethod
	public void ping();
	
	@SyncMethod
	public int getCounter();
}
