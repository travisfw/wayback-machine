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
 * Exception class for queries which can be better expressed as another URL, or
 * should, for one reason or another, be requested at a different URL. Likely
 * cause would be to redirect the client so the Browser location reflects the
 * exact request being served. 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class BetterRequestException extends WaybackException {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String ID = "betterRequest";
	private String betterURI;
	private int status = HttpServletResponse.SC_FOUND;


	/**
	 * Constructor
	 * @param betterURI 
	 * @param status
	 * 
	 */
	public BetterRequestException(String betterURI, int status) {
		super("Better URI for query");
		this.betterURI = betterURI;
		this.status = status;
		id = ID;
	}
	/**
	 * Constructor
	 * @param betterURI 
	 * 
	 */
	public BetterRequestException(String betterURI) {
		super("Better URI for query");
		this.betterURI = betterURI;
		id = ID;
	}

	/**
	 * @return Returns the betterURI.
	 */
	public String getBetterURI() {
		return betterURI;
	}
	public int getStatus() {
		return status;
	}
	
}
