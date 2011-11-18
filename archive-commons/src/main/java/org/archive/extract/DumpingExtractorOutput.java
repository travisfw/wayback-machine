package org.archive.extract;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Logger;

import org.archive.resource.Resource;
import org.archive.util.StreamCopy;
import org.json.JSONException;

import com.google.common.io.CountingOutputStream;
import com.google.common.io.NullOutputStream;

public class DumpingExtractorOutput implements ExtractorOutput {
	private static final Logger LOG = 
		Logger.getLogger(DumpingExtractorOutput.class.getName());
	
	private PrintStream out;
	public DumpingExtractorOutput(OutputStream out) {
		this.out = new PrintStream(out);
	}

	public void output(Resource resource) throws IOException {
		NullOutputStream nullo = new NullOutputStream();
		CountingOutputStream co = new CountingOutputStream(nullo);
		StreamCopy.copy(resource.getInputStream(), co);
		long bytes = co.getCount();
		if(bytes > 0) {
			LOG.info(bytes + " unconsumed bytes in Resource InputStream.");
		}
		try {
			out.println(resource.getMetaData().getTopMetaData().toString(1));
		} catch (JSONException e) {
			LOG.warning(e.getMessage());
		}		
	}
}
