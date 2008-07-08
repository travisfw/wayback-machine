/* PathPrefixDatePrefixQueryRequestParser
 *
 * $Id$
 *
 * Created on 6:42:18 PM Apr 24, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.archivalurl.requestparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.requestparser.PathRequestParser;

/**
 * RequestParser implementation that extracts request info from an Archival Url
 * representing an url prefix and a date prefix.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class PathPrefixDatePrefixQueryRequestParser extends PathRequestParser {
	/**
	 * Regex which parses Archival URL queries into timestamp + URL for URLs
	 * beginning with the URL prefix
	 */
	private final static Pattern WB_PATH_QUERY_REGEX = Pattern
			.compile("^(\\d{0,13})\\*/(.*)\\*$");

	public WaybackRequest parse(String requestPath) {
		WaybackRequest wbRequest = null;
		Matcher matcher = WB_PATH_QUERY_REGEX.matcher(requestPath);
		if (matcher != null && matcher.matches()) {

			wbRequest = new WaybackRequest();
			String dateStr = matcher.group(1);
			String urlStr = matcher.group(2);

			String startDate;
			String endDate;
			if(dateStr.length() == 0) {
				startDate = getEarliestTimestamp();
				endDate = getLatestTimestamp();
			} else {
				startDate = Timestamp.parseBefore(dateStr).getDateStr();
				endDate = Timestamp.parseAfter(dateStr).getDateStr();
			}

			wbRequest.setStartTimestamp(startDate);
			wbRequest.setEndTimestamp(endDate);

			wbRequest.setUrlQueryRequest();
			wbRequest.setRequestUrl(urlStr);
		}
		return wbRequest;
	}
}
