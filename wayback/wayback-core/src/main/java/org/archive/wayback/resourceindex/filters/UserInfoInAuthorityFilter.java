/* UserInfoInAuthorityFilter
 *
 * $Id$:
 *
 * Created on Apr 16, 2010.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.resourceindex.filters;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.url.UrlOperations;

/**
 * Class which omits CaptureSearchResults that have and '@' in the original URL
 * field, if that '@' is after the scheme, and before the first '/' or ':'
 *  
 * @author brad
 *
 */
public class UserInfoInAuthorityFilter implements ObjectFilter<CaptureSearchResult> {
	private boolean wantUserInfo = false;
	public int filterObject(CaptureSearchResult o) {
		boolean hasUserInfo = 
			(UrlOperations.urlToUserInfo(o.getOriginalUrl()) != null);
		return hasUserInfo == wantUserInfo
			? ObjectFilter.FILTER_INCLUDE : ObjectFilter.FILTER_EXCLUDE;
	}
	/**
	 * @return the wantUserInfo
	 */
	public boolean isWantUserInfo() {
		return wantUserInfo;
	}
	/**
	 * @param wantUserInfo the wantUserInfo to set
	 */
	public void setWantUserInfo(boolean wantUserInfo) {
		this.wantUserInfo = wantUserInfo;
	}
}
