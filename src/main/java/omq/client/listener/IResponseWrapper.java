package omq.client.listener;

public interface IResponseWrapper {
	
	public static final String MULTI_TYPE = "multiResponse";
	public static final String NORMAL_TYPE = "normalResponse";
	
	public void setResult(byte[] result);

	public Object getResult();
}