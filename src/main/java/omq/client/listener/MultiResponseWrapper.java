package omq.client.listener;

import java.util.ArrayList;
import java.util.List;

/**
 * This response wrapper uses an array of response and will be returned once the
 * timeout of a multicall expires
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class MultiResponseWrapper implements IResponseWrapper {
	private List<byte[]> bytes;

	public MultiResponseWrapper(byte[] result) {
		bytes = new ArrayList<byte[]>();
		bytes.add(result);
	}

	@Override
	public void setResult(byte[] result) {
		bytes.add(result);
	}

	@Override
	public List<byte[]> getResult() {
		return bytes;
	}

}