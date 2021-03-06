package org.archive.hadoop.mapreduce;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.archive.url.URLKeyMaker;
import org.archive.url.WaybackURLKeyMaker;

public class CDXMapper extends Mapper<Object, Text, Text, Text>
		implements Configurable {
	private static final Logger LOG =
		Logger.getLogger(CDXMapper.class.getName());
	
	// Note the (unbelievably) new "S" for "Size in Compressed Bytes..."
	public final static String NEW_CDX_HEADER =
		"CDX N b a m s k r M S V g";
	private static String TEXT_OUTPUT_DELIM_CONFIG = "text.output.delim";
	public static int MODE_GLOBAL = 0;
	public static int MODE_FULL = 1;

	private Configuration conf;
	private Text key = new Text();
	private Text value = new Text();
	private String delim = " ";
	StringBuilder keySB = new StringBuilder();
	StringBuilder valSB = new StringBuilder();

	private boolean omitNoArchive = false;
	private URLKeyMaker keyMaker = new WaybackURLKeyMaker();
	
	public static String DEFAULT_GZ_LEN = "-";
	
	public StringPair convert(String cdxLine) {
		if(cdxLine.startsWith(" CDX ")) {
			return new StringPair("", NEW_CDX_HEADER);
		}
		String[] parts = cdxLine.split(delim);
		int offsetIdx = 8;
		String metaInstructions = "-";
		if(parts.length == 9) {
			offsetIdx = 7;
		} else if(parts.length == 10) {
			metaInstructions = parts[7];
			if(omitNoArchive) {
				if(metaInstructions.contains("A")) {
					return null;
				}
			}
		} else {
			LOG.warning("Skipping line:" + cdxLine);
			return null;
		}
	
		// don't care about the old key:
//		String urlKey = parts[0];
		String timestamp = parts[1];
		String origUrl = parts[2];
		String mime = parts[3];
		String responseCode = parts[4];
		String digest = parts[5];
		String redirect = parts[6];
		String offset = parts[offsetIdx];
		String filename = parts[offsetIdx+1];
		String urlKey = keyMaker.makeKey(origUrl);
		
		keySB.setLength(0);
		keySB.append(urlKey).append(delim).append(timestamp);

		valSB.setLength(0);
		valSB.append(origUrl).append(delim);
		valSB.append(mime).append(delim);
		valSB.append(responseCode).append(delim);
		valSB.append(digest).append(delim);
		valSB.append(redirect).append(delim);
		valSB.append(metaInstructions).append(delim);
		valSB.append(DEFAULT_GZ_LEN).append(delim);
		valSB.append(offset).append(delim);
		valSB.append(filename);
		return new StringPair(keySB.toString(), valSB.toString());
	}
	
	public void map(Object y, Text textLine, Context context) throws IOException,
			InterruptedException {
		String cdxLine = textLine.toString();
		StringPair st = convert(cdxLine);
		if(st != null) {
			key.set(st.first);
			value.set(st.second);
			context.write(key, value);
		}
	}

	public Configuration getConf() {
		return conf;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
		delim = conf.get(TEXT_OUTPUT_DELIM_CONFIG, delim);
	}
	public class StringPair {
		public String first;
		public String second;
		public StringPair(String first, String second) {
			this.first = first;
			this.second = second;
		}
	}
}
