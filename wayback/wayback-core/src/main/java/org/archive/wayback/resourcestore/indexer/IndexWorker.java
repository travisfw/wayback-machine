/* IndexWorker
 *
 * $Id$
 *
 * Created on 2:58:51 PM Jun 23, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
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
package org.archive.wayback.resourcestore.indexer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.logging.Logger;

import org.archive.wayback.Shutdownable;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.cdx.CDXFormatIndex;
import org.archive.wayback.resourceindex.cdx.SearchResultToCDXFormatAdapter;
import org.archive.wayback.resourceindex.cdx.format.CDXFormat;
import org.archive.wayback.resourceindex.cdx.format.CDXFormatException;
import org.archive.wayback.resourceindex.updater.IndexClient;
import org.archive.wayback.resourcestore.locationdb.ResourceFileLocationDB;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;
import org.archive.wayback.util.url.IdentityUrlCanonicalizer;

/**
 * Simple worker, which gets tasks from an IndexQueue, in the case, the name
 * of ARC/WARC files to be indexed, retrieves the ARC/WARC location from a
 * ResourceFileLocationDB, creates the index, which is serialized into a file,
 * and then hands that file off to a ResourceIndex for merging, using an 
 * IndexClient.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class IndexWorker implements Shutdownable {
	private static final Logger LOGGER =
        Logger.getLogger(IndexWorker.class.getName());

	public final static String ARC_EXTENSION = ".arc";
	public final static String ARC_GZ_EXTENSION = ".arc.gz";
	public final static String WARC_EXTENSION = ".warc";
	public final static String WARC_GZ_EXTENSION = ".warc.gz";
	
	private ArcIndexer arcIndexer = new ArcIndexer();
	private WarcIndexer warcIndexer = new WarcIndexer();
	
	private UrlCanonicalizer canonicalizer = new IdentityUrlCanonicalizer();
//	private UrlCanonicalizer canonicalizer = new AggressiveUrlCanonicalizer();

	private long interval = 120000;
	private IndexQueue queue = null;
	private ResourceFileLocationDB db = null;
	private IndexClient target = null;
	private WorkerThread thread = null;
	
	public void init() {
		arcIndexer.setCanonicalizer(canonicalizer);
		warcIndexer.setCanonicalizer(canonicalizer);
		if(interval > 0) {
			thread = new WorkerThread(this,interval);
			thread.start();
		}
	}

	public void shutdown() {
		if(thread != null) {
			thread.interrupt();
			try {
				thread.join(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean doWork() throws IOException {
		boolean worked = false;
		String name = queue.dequeue();
		if(name != null) {
			worked = true;
			String[] pathsOrUrls = null;
			try {
				pathsOrUrls = db.nameToUrls(name);
			} catch(IOException e) {
				LOGGER.severe("FAILED TO LOOKUP(" + name + ")" + 
						e.getLocalizedMessage());
				return false;
			}
			try {
				if(pathsOrUrls != null) {
					for(String pathOrUrl : pathsOrUrls) {
						LOGGER.info("Indexing " + name + " from " + pathOrUrl);
						CloseableIterator<CaptureSearchResult> itr = indexFile(pathOrUrl);
						target.addSearchResults(name, itr);
						itr.close();
						break;
					}
				}
			} catch(IOException e) {
				LOGGER.severe("FAILED to index or upload (" + name + ")");
				e.printStackTrace();
			}
		}
		return worked;
	}
	
	public CloseableIterator<CaptureSearchResult> indexFile(String pathOrUrl) 
	throws IOException {

		CloseableIterator<CaptureSearchResult> itr = null;
		
		if(pathOrUrl.endsWith(ARC_EXTENSION)) {
			itr = arcIndexer.iterator(pathOrUrl);
		} else if(pathOrUrl.endsWith(ARC_GZ_EXTENSION)) {
			itr = arcIndexer.iterator(pathOrUrl);			
		} else if(pathOrUrl.endsWith(WARC_EXTENSION)) {
			itr = warcIndexer.iterator(pathOrUrl);
		} else if(pathOrUrl.endsWith(WARC_GZ_EXTENSION)) {
			itr = warcIndexer.iterator(pathOrUrl);
		}		
		return itr;
	}

	private static void USAGE() {
		System.err.println("USAGE:");
		System.err.println("");
		System.err.println("cdx-indexer [-format FORMAT|-identity] FILE");
		System.err.println("cdx-indexer [-format FORMAT|-identity] FILE CDXFILE");
		System.err.println("");
		System.err.println("Create a CDX format index from ARC or WARC file");
		System.err.println("FILE at CDXFILE or to STDOUT.");
		System.err.println("With -identity, perform no url canonicalization.");
		System.err.println("With -format, output CDX in format FORMAT.");
		System.exit(1);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String cdxSpec = CDXFormatIndex.CDX_HEADER_MAGIC;
		PrintWriter pw = new PrintWriter(System.out);
		UrlCanonicalizer canonicalizer = new AggressiveUrlCanonicalizer();
		boolean setFormat = false;
		boolean isIdentity = false;
		String path = null;
		for(int idx = 0; idx < args.length; idx++) {
			if(args[idx].equals("-identity")) {
				canonicalizer = new IdentityUrlCanonicalizer();
				isIdentity = true;
			} else if(args[idx].equals("-format")) {
				idx++;
				if(idx >= args.length) {
					USAGE();
				}
				cdxSpec = args[idx];
				setFormat = true;
			} else {
				// either input filename:
				if(path == null) {
					path = args[idx];
				} else {
					// or if that's already been specified, then target file:
					if(idx+1 != args.length){
						USAGE();
					}
					try {
						pw = new PrintWriter(args[idx]);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						System.exit(1);
					}
					break;
				}
			}
		}
		if(!setFormat && isIdentity) {
			cdxSpec = cdxSpec.replace(" N ", " a ");
		}
		IndexWorker worker = new IndexWorker();
		worker.canonicalizer = canonicalizer;
		worker.interval = 0;
		worker.init();
		try {
			CloseableIterator<CaptureSearchResult> itr = worker.indexFile(path);
			CDXFormat cdxFormat = new CDXFormat(cdxSpec);
			Iterator<String> lines = 
				SearchResultToCDXFormatAdapter.adapt(itr, cdxFormat);
			pw.println(cdxSpec);
			while(lines.hasNext()) {
				pw.println(lines.next());
			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (CDXFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
	}	
	
	private class WorkerThread extends Thread {
		private long runInterval = 120000;
		private IndexWorker worker = null;
		
		public WorkerThread(IndexWorker worker, long runInterval) {
			this.worker = worker;
			this.runInterval = runInterval;
		}

		public void run() {
			LOGGER.info("alive.");
			long sleepInterval = runInterval;
			while (true) {
				try {
					boolean worked = worker.doWork();
					
					if(worked) {
						sleepInterval = 0;
					} else {
						sleepInterval += runInterval;
					}
					if(sleepInterval > 0) {
						sleep(sleepInterval);
					}
				} catch (InterruptedException e) {
					LOGGER.info("Shutting Down.");
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	
	/**
	 * @return the interval
	 */
	public long getInterval() {
		return interval;
	}
	/**
	 * @param interval the interval to set
	 */
	public void setInterval(long interval) {
		this.interval = interval;
	}
	/**
	 * @return the queue
	 */
	public IndexQueue getQueue() {
		return queue;
	}
	/**
	 * @param queue the queue to set
	 */
	public void setQueue(IndexQueue queue) {
		this.queue = queue;
	}
	/**
	 * @return the db
	 */
	public ResourceFileLocationDB getDb() {
		return db;
	}
	/**
	 * @param db the db to set
	 */
	public void setDb(ResourceFileLocationDB db) {
		this.db = db;
	}
	/**
	 * @return the target
	 */
	public IndexClient getTarget() {
		return target;
	}
	/**
	 * @param target the target to set
	 */
	public void setTarget(IndexClient target) {
		this.target = target;
	}
	/**
	 * @return the canonicalizer
	 */
	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}
	/**
	 * @param canonicalizer the canonicalizer to set
	 */
	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}
}
