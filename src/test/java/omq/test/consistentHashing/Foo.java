/**
 * 
 */
package omq.test.consistentHashing;

import omq.Remote;
import omq.client.annotation.AsyncMethod;
import omq.client.annotation.RemoteInterface;

/**
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
@RemoteInterface
public interface Foo extends Remote {
	@AsyncMethod
	public void ping();
}
