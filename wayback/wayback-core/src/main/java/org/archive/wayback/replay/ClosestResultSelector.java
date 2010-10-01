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
package org.archive.wayback.replay;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BetterRequestException;

/**
 * New interface component on Replay to allow customized selection of the 
 * "best" particular search result from a set to return for a particular 
 * request. Also allows specific Replay instances to optionally bounce the user
 * to a better URL, via a BetterRequestException.
 * 
 * @author brad
 *
 */
public interface ClosestResultSelector {
	/**
	 * Locate and return the best matching search result from a set for a given
	 * request.
	 * @param wbRequest The WaybackRequest being handled
	 * @param results the CaptureSeachResults found matching the request
	 * @return the best CaptureSearchResult, which should be replayed to the
	 * 		user.
	 * @throws BetterRequestException if the user should be redirected to a
	 * 		different, better URL to make this request.
	 */
	public CaptureSearchResult getClosest(WaybackRequest wbRequest, 
			CaptureSearchResults results) throws BetterRequestException;
}
