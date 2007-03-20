/* RemoteExclusionFilterFactory
 *
 * $Id$
 *
 * Created on 8:23:55 PM Mar 5, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-svn.
 *
 * wayback-svn is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-svn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.accesscontrol.remote;

import java.util.Properties;

import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.resourceindex.ExclusionFilterFactory;
import org.archive.wayback.resourceindex.SearchResultFilter;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RemoteExclusionFilterFactory implements ExclusionFilterFactory {

	/**
	 * configuration name for URL prefix to access exclusion service
	 */
	private final static String EXCLUSION_PREFIX = "resourceindex.exclusionurl";

	/**
	 * configuration name for User Agent to send to exclusion service
	 */
	private final static String EXCLUSION_UA = "resourceindex.exclusionua";


	private String exclusionUrlPrefix = null;

	private String exclusionUserAgent = null;


	/* (non-Javadoc)
	 * @see org.archive.wayback.PropertyConfigurable#init(java.util.Properties)
	 */
	public void init(Properties p) throws ConfigurationException {
		// TODO Auto-generated method stub
		exclusionUrlPrefix = (String) p.get(EXCLUSION_PREFIX);

		exclusionUserAgent = (String) p.get(EXCLUSION_UA);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.ExclusionFilterFactory#get()
	 */
	public SearchResultFilter get() {
		return new RemoteExclusionFilter(exclusionUrlPrefix, exclusionUserAgent);
	}


}
