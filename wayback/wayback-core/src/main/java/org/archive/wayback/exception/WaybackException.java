/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.exception;

import javax.servlet.http.HttpServletResponse;

/**
 * Base class for Wayback internal exceptions.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class WaybackException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String GENERIC_ID = "wayback";
	protected static final String KEY_PREFIX = "Exception.";
	protected static final String KEY_TITLE_SUFFIX = ".title";
	protected static final String KEY_MESSAGE_SUFFIX = ".message";
	private String message = "";
	private String title = "Wayback Exception";
	private String details = "";
	protected String id = GENERIC_ID;
	
	/**
	 * Constructor with message only
	 * 
	 * @param message
	 */
	public WaybackException(String message) {
		super(message);
		this.message = message;
	}
	
	/**
	 * Constructor with message, and title
	 * 
	 * @param message
	 * @param title
	 */
	public WaybackException(String message, String title) {
		super(message);
		this.message = message;
		this.title= title;
	}
	
	/**
	 * Constructor with message, title, and details
	 * 
	 * @param message
	 * @param title
	 * @param details
	 */
	public WaybackException(String message, String title,  String details) {
		super(message);
		this.message = message;
		this.title= title;
		this.details = details;
	}
	
	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return Returns the message.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return Returns the details.
	 */
	public String getDetails() {
		return details;
	}
	/**
	 * @return The localization key name of the title of this WaybackException
	 */
	public String getTitleKey() {
		return KEY_PREFIX + id + KEY_TITLE_SUFFIX;
	}
	/**
	 * @return The localization key name of the message of this WaybackException
	 */
	public String getMessageKey() {
		return KEY_PREFIX + id + KEY_MESSAGE_SUFFIX;
	}
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	}
	public void setupResponse(HttpServletResponse response) {
		response.setStatus(getStatus());
	}
}
