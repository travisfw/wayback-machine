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

import java.net.URL;

import javax.servlet.http.HttpServletResponse;

/**
 * @author brad
 *
 */
public class LiveWebCacheUnavailableException  extends WaybackException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final String ID = "liveWebCacheNotAvailable";
	protected static final String defaultMessage = "LiveWebCache unavailable";
	/**
	 * Constructor
	 * @param url 
	 * @param code 
	 */
	public LiveWebCacheUnavailableException(URL url, int code) {
		super("The URL " + url.toString() + " is not available(HTTP " + code +
				" returned)",defaultMessage);
		id = ID;
	}
	/**
	 * Constructor with message and details
	 * @param url 
	 * @param code 
	 * @param details
	 */
	public LiveWebCacheUnavailableException(URL url, int code, String details){
		super("The URL " + url.toString() + " is not available(HTTP " + code +
				" returned)",defaultMessage,details);
		id = ID;
	}
	/**
	 * @param url
	 */
	public LiveWebCacheUnavailableException(String url){
		super("The URL " + url + " is not available",defaultMessage);
		id = ID;
	}
	
	/**
	 * @return the HTTP status code appropriate to this exception class.
	 */
	public int getStatus() {
		return HttpServletResponse.SC_BAD_GATEWAY;
	}

}
