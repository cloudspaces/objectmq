package omq.supervisor;

import java.io.Serializable;
import java.util.Properties;

/**
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class OmqSettings implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String reference;
	private String className;
	private Properties env;

	public OmqSettings(String reference, String className, Properties env) {
		this.reference = reference;
		this.className = className;
		this.env = env;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public Properties getEnv() {
		return env;
	}

	public void setEnv(Properties env) {
		this.env = env;
	}

}
