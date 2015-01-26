package omq.client.listener;

/**
 * IResponseWrapper is used to wrap the server responses. There can be either
 * ResponseWrappers or MultiWrapper
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public interface IResponseWrapper {

	public static final String MULTI_TYPE = "multiResponse";
	public static final String NORMAL_TYPE = "normalResponse";
	
	public void setResult(byte[] result);

	public Object getResult();
}