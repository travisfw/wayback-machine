/* ResourceStore
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback;

import java.io.IOException;

import org.archive.wayback.core.Resource;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.exception.ResourceNotAvailableException;

/**
 * Transforms a CaptureSearchResult into a Resource.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public interface ResourceStore {
	/**
	 * Transform a CaptureSearchResult into a Resource
	 * 
	 * @param result CaptureSearchResult which should be retrieved
	 * @return Resource object retrieved for the SearchResult
	 * @throws ResourceNotAvailableException if the resource was unavailable
	 * 			(down host, corrupted files, etc)
	 */
	public Resource retrieveResource(CaptureSearchResult result) 
		throws ResourceNotAvailableException;

	/**
	 * Release any resources used by this ResourceIndex cleanly
	 * @throws IOException for usual causes
	 */
	public void shutdown() throws IOException;
}
