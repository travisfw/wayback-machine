/* FilterChain
 *
 * $Id$
 *
 * Created on 12:45:54 PM Jan 24, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.cdx.filter;

import java.util.ArrayList;
import org.archive.wayback.cdx.CDXRecord;

/**
 * FilterChain implements AND logic to chain together multiple RecordFilters
 * into a composite. ABORT and EXCLUDE short circuit the chain, all filters
 * must INCLUDE for inclusion.
 * 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FilterChain implements RecordFilter{

	private ArrayList filters = null;

	/**
	 * Constructor
	 */
	public FilterChain() {
		this.filters = new ArrayList();
	}
	
	/**
	 * @param filter to be added to the chain. filters are processed in the 
	 * order they are added to the chain.
	 */
	public void addFilter(RecordFilter filter) {
		filters.add(filter);
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.cdx.filter.RecordFilter#filterRecord(org.archive.wayback.cdx.CDXRecord)
	 */
	public int filterRecord(CDXRecord record) {
		
		int size = filters.size();
		int result = RECORD_ABORT;
		for(int i = 0; i < size; i++) {
			RecordFilter filter = (RecordFilter) filters.get(i);
			result = filter.filterRecord(record);
			if(result == RECORD_ABORT) {
				break;
			} else if(result == RECORD_EXCLUDE) {
				break;
			}
		}
		return result;
	}
}
