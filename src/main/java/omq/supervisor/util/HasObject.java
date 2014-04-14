package omq.supervisor.util;

import java.io.Serializable;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class HasObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String brokerName;
	private String reference;
	private boolean hasObject;

	public HasObject(String brokerName, String reference, boolean hasObject) {
		this.brokerName = brokerName;
		this.reference = reference;
		this.hasObject = hasObject;
	}

	public String getBrokerName() {
		return brokerName;
	}

	public void setBrokerName(String brokerName) {
		this.brokerName = brokerName;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public boolean hasObject() {
		return hasObject;
	}

	public void setHasObject(boolean hasObject) {
		this.hasObject = hasObject;
	}

}