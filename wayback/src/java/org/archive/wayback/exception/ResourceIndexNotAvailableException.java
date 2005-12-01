package org.archive.wayback.exception;


/**
 * Exception class for queries which fail because the ResourceIndex is
 * presently inaccessible
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResourceIndexNotAvailableException extends WaybackException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public ResourceIndexNotAvailableException(String message) {
		super(message,"Index not available");
	}
	/**
	 * Constructor with message and details
	 * 
	 * @param message
	 * @param details
	 */
	public ResourceIndexNotAvailableException(String message, String details) {
		super(message,"Index not available",details);
	}
}
