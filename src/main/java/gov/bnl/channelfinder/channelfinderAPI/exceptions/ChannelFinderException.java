/**
 * 
 */
package gov.bnl.channelfinder.channelfinderAPI.exceptions;

import javax.xml.ws.ProtocolException;

import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * @author shroffk
 *
 */
public class ChannelFinderException extends ProtocolException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6279865221993808192L;
	
	private Status status;
	
	public ChannelFinderException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ChannelFinderException(Status status, String message) {
		super(message);
		this.setStatus(status);
	}
	
	/**
	 * 
	 * @param status - the http error status code
	 * @param cause - the original UniformInterfaceException 
	 * @param message - additional error information
	 */
	public ChannelFinderException(Status status, Throwable cause ,String message) {
		super(message, cause);
		this.setStatus(status); 
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}


}
