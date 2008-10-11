package org.archive.wayback.resourcestore.resourcefile;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveReaderFactory;

/**
 * Sad but needed subclass of the ArchiveReaderFactory, allows config of
 * timeouts for connect and reads on underlying HTTP connections, and overrides
 * the one getArchiveReader(URL,long) method to enable setting the timeouts.
 * 
 * This functionality should be moved into the ArchiveReaderFactory.
 * 
 * @author brad
 *
 */
public class TimeoutArchiveReaderFactory extends ArchiveReaderFactory {

	private final static int STREAM_ALL = -1;
	private int connectTimeout = 10000;
	private int readTimeout = 10000;
	public TimeoutArchiveReaderFactory(int connectTimeout, int readTimeout) {
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
	}
	
	public TimeoutArchiveReaderFactory(int timeout) {
		this.connectTimeout = timeout;
		this.readTimeout = timeout;		
	}
	public TimeoutArchiveReaderFactory() {
	}	
	protected ArchiveReader getArchiveReader(final URL f, final long offset)
	throws IOException {

        // Get URL connection.
        URLConnection connection = f.openConnection();
        if (connection instanceof HttpURLConnection) {
          addUserAgent((HttpURLConnection)connection);
        }
        if (offset != STREAM_ALL) {
        	// Use a Range request (Assumes HTTP 1.1 on other end). If
        	// length >= 0, add open-ended range header to the request.  Else,
        	// because end-byte is inclusive, subtract 1.
        	connection.addRequestProperty("Range", "bytes=" + offset + "-");
        }
        
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);

        return getArchiveReader(f.toString(), connection.getInputStream(),
            (offset == 0));
    }
}
