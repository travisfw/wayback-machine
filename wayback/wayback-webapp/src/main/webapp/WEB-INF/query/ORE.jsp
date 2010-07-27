<%@ page import="java.util.Date" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%@ page import="org.archive.wayback.core.WaybackRequest" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResults" %>
<%@ page import="org.archive.wayback.core.CaptureSearchResult"%>
<%@ page import="org.archive.wayback.ResultURIConverter" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.archive.wayback.util.Timestamp" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List"  %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.dspace.foresite.Aggregation" %>
<%@ page import="org.dspace.foresite.ResourceMap" %>
<%@ page import="org.dspace.foresite.Agent" %>
<%@ page import="org.dspace.foresite.OREFactory" %>
<%@ page import="org.dspace.foresite.AggregatedResource" %>
<%@ page import="org.dspace.foresite.ORESerialiser" %>
<%@ page import="org.dspace.foresite.ORESerialiserFactory" %>
<%@ page import="org.dspace.foresite.ResourceMapDocument" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.net.URI" %>
<%@ page import="org.dspace.foresite.Predicate" %>
<%@ page import="org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter" %>
<%@page import="org.dspace.foresite.Triple"%>
<%@page import="org.dspace.foresite.jena.TripleJena"%>
<%@page import="java.util.UUID"%>  
<%@page import="java.util.Calendar"%>
<%
	UIResults results = UIResults.extractCaptureQuery(request);//nuzno potom perepisat'
	SimpleDateFormat httpformatterl = new SimpleDateFormat(
			"E, dd MMM yyyy HH:mm:ss z");
	WaybackRequest wbRequest = results.getWbRequest();
	CaptureSearchResults cResults = results.getCaptureResults();
	CaptureSearchResult res = cResults.getClosest(wbRequest, true);

	ArchivalUrlResultURIConverter uriconverter = (ArchivalUrlResultURIConverter) results
			.getURIConverter();
	String uriPrefix = uriconverter.getReplayURIPrefix();
	//String p_url = wbRequest.getContextPrefix();
	String u = wbRequest.getRequestUrl();
	String agguri = uriPrefix + "timebundle/" + u;
	//String agguri = results.getContextConfig("Prefix") + "timebundle/" + u;
	//String remuri = p_url +"timemap/" + u;
	//System.out.println(agguri);
	//System.out.println(remuri);
	String redirection = null;
	if (wbRequest.containsKey("redirect")) {
		redirection = wbRequest.get("redirect");
	}
	if (redirection != null) {
		//default poka 
		//skip content negotiation

		// TODO: see comment in TimeBundleParser - could be handled elsewhere
		response.setStatus(303);
		response.setHeader("Location", uriPrefix + "timemap/rdf/" + u);
		//response.sendRedirect(p_url +"timemap/rdf/" + u);
	} else {
		String format = wbRequest.get("format");
		// System.out.println("here");
		Aggregation agg = OREFactory.createAggregation(new URI(agguri));
		//System.out.println("here");
		ResourceMap rem = agg.createResourceMap(new URI(uriPrefix
				+ "timemap/" + format + "/" + u));

		//SimpleDateFormat  formatter_utc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		Date now = new Date();

		rem.setCreated(now);
		Predicate pr_type = new Predicate();
		pr_type.setURI(new URI(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));

		rem.setModified(now);
		rem.createTriple(pr_type, new URI(
				"http://www.mementoweb.org/terms/tb/TimeMap"));
		//rem.addType(new URI("http://www.mementoweb.org/terms/tb/TimeMap"));
		Agent creator = OREFactory.createAgent();
		creator.addName("Foresite Toolkit (Java)");
		//creator.addMbox(new URI("foresite@googlegroups.com"));

		//rem.addAgent(new URI("http://foresite-toolkit.googlecode.com/#javaAgent"),creator);
		rem.addCreator(creator);
		agg.addTitle("Memento Time Bundle for " + u);

		//CaptureSearchResults cResults = results.getCaptureResults();
		//CaptureSearchResult res = cResults.getClosest(wbRequest,true);
		Iterator<CaptureSearchResult> itr = cResults.iterator();
		SimpleDateFormat formatterk = new SimpleDateFormat(
				"yyyyMMddHHmmss");

		Date f = cResults.getFirstResultDate();
		Date l = cResults.getLastResultDate();

		String ArchiveInterval = formatterk.format(f) + " - "
				+ formatterk.format(l);

		//agg.createTriple(new URI("http://www.mementoweb.org/ns/archiveInterval"),ArchiveInterval);
		agg.addType(new URI(
				"http://www.mementoweb.org/terms/tb/TimeBundle"));
		//String mementourl =p_url.replace("ore","memento");
		//include original into aggregation

		AggregatedResource ar_o = agg.createAggregatedResource(new URI(
				u));
		ar_o.createTriple(pr_type, new URI(
				"http://www.mementoweb.org/terms/tb/OriginalResource"));
		//include timegate into aggregation
		AggregatedResource ar_tg = agg
				.createAggregatedResource(new URI(results
						.getContextConfig("Prefix")
						+ "timegate/" + u));
		Predicate pr_format = new Predicate();
		pr_format.setURI(new URI(
				"http://purl.org/dc/elements/1.1/format"));
		ar_tg.createTriple(pr_format, new URI(u));
		ar_tg.createTriple(pr_type, new URI(
				"http://www.mementoweb.org/terms/tb/TimeGate"));

		String previos_digest = null;
		List<String> previos_blancs = new ArrayList<String>();

		Predicate pr = new Predicate();
		pr.setURI(new URI("http://www.mementoweb.org/terms/tb/start"));
		Predicate pre = new Predicate();
		pre.setURI(new URI("http://www.mementoweb.org/terms/tb/end"));
		Calendar cal = Calendar.getInstance();
		AggregatedResource ar = null;

		Date enddate = null;

		// String buffer for special link serialization format
		StringBuffer linkbf = new StringBuffer();

		linkbf.append("<" + u + ">;rel=\"original\"\n");
		linkbf.append(",<" + agguri + ">;rel=\"timebundle\"\n");
		String firstmemento = null;

		while (itr.hasNext()) {
			CaptureSearchResult cur = itr.next();
			//I am not deduping urls (by digest) for the rdf serialization running out of time, extra efforts for me now ;)

			String resurl = results.getContextConfig("Prefix")
					+ formatterk.format(cur.getCaptureDate()) + "/" + u;

			String digest = cur.getDigest();
			if (previos_digest == null) {
				previos_digest = digest;
			}

			ar = agg.createAggregatedResource(new URI(resurl));
			ar.createTriple(pr_format, cur.getMimeType());

			Predicate pr_1 = new Predicate();
			pr_1.setURI(new URI(
					"http://www.mementoweb.org/terms/tb/mementoFor"));
			ar.createTriple(pr_1, new URI(u));
			ar.createTriple(pr_type, new URI(
					"http://www.mementoweb.org/terms/tb/Memento"));

			Date startdate = cur.getDuplicateDigestStoredDate();
			//System.out.println("start:"+startdate);
			enddate = cur.getCaptureDate();
			//System.out.println("end:"+enddate);

			// serialiase it in links format only for unique  digest

			if (startdate == null) {
				if (firstmemento == null) {
					linkbf.append(",<" + resurl
							+ ">;rel=\"first-memento\";datetime=\""
							+ httpformatterl.format(enddate) + "\"\n");
					firstmemento = "firstmemento";
				} else {
					linkbf.append(",<" + resurl
							+ ">;rel=\"memento\";datetime=\""
							+ httpformatterl.format(enddate) + "\"\n");
				}
			}

			// Adding blanc node
			Triple triple = new TripleJena();
			triple.initialise(new URI(resurl));
			Predicate pred = new Predicate();
			UUID a = UUID.randomUUID();
			String blanc = "urn:uuid:" + a.toString();

			//System.out.println(blanc);
			pred.setURI(new URI(
					"http://www.mementoweb.org/terms/tb/validOver"));
			triple.relate(pred, new URI(blanc));
			Triple tr = new TripleJena();
			tr.initialise(new URI(blanc));

			tr.relate(pr_type, new URI(
					"http://www.mementoweb.org/terms/tb/Period"));

			//period difined by [ [ interval [ date first digest recorded  and date of next digest recorded [ 

			String start = null;
			Triple trd = new TripleJena();
			trd.initialise(new URI(blanc));
			//Calendar cal = Calendar.getInstance();

			if (startdate != null) {

				cal.setTime(startdate);
				trd.relate(pr, cal);
				start = httpformatterl.format(startdate);
			} else {
				cal.setTime(enddate);
				trd.relate(pr, cal);
				start = httpformatterl.format(enddate);
			}

			//System.out.println("type" +trd.getLiteralType());

			ar.addTriple(triple);
			ar.addTriple(tr);
			ar.addTriple(trd);

			if (!digest.equals("previos_digest")) {

				Iterator<String> it = previos_blancs.iterator();
				while (it.hasNext()) {
					String blanc_ = (String) it.next();
					Triple tre = new TripleJena();
					tre.initialise(new URI(blanc_));

					//Calendar cal = Calendar.getInstance();
					cal.setTime(enddate);
					tre.relate(pre, cal);
					ar.addTriple(tre);
				}

				previos_blancs.clear();
				previos_digest = digest;
			}

			previos_blancs.add(blanc);

		}

		Iterator it = previos_blancs.iterator();
		while (it.hasNext()) {
			String blanc_ = (String) it.next();
			Triple tre = new TripleJena();
			tre.initialise(new URI(blanc_));

			cal.setTime(now); //or date of archive stop archiving
			tre.relate(pre, cal);

			ar.addTriple(tre);
		}

		// additional logic for link format
		int m_index = linkbf.lastIndexOf("\"memento\"");
		//System.out.println(m_index);
		linkbf.insert(m_index + 1, "last-");
		//System.out.println("here");

		//String format = wbRequest.get("format");
		ORESerialiser serial = null;
		if (format.equals("rdf")) {
			serial = ORESerialiserFactory.getInstance("RDF/XML");
			response.setContentType("application/rdf+xml");
		}
		//else if (format.equals("atom")) {
		//	serial = ORESerialiserFactory.getInstance("ATOM-1.0");
		//}
		//else if (format.equals ("html")) {
		//	serial = ORESerialiserFactory.getInstance("RDFa");
		//}
		//removed n3 because serialization of the date to the String type
		//else if (format.equals("n3")) {
		//serial = ORESerialiserFactory.getInstance("N3");

		//response.setContentType("text/n3");
		//}
		else if (format.equals("link")) {
			PrintWriter pw = response.getWriter();
			//System.out.println(linkbf.toString());
			
			// TODO: are we sure this is right? We want to flush *before* 
			//       setting content-type?
			pw.print(linkbf.toString());
			pw.flush();
			response.setContentType("text/csv");
		} else {
			// response.setStatus(404);
			// TODO: this should be handled in TimeBundleParser to allow
			//       usual Exception rendering to happen.
			response.sendError(404, "Unknown TimeMap serialization");
		}
		if (serial != null) {
			ResourceMapDocument doc = serial.serialise(rem);
			// TODO: this could get really big. Any way to stream the data out
			//       so we don't need another copy beyond the ResourceMap, 
			//       and other helper objects?
			String serialisation = doc.toString();
			if (format.equals("rdf")) {
				//bug in jena? did not serialise date to date type but to string type // stupid fix will need investigate it 
				serialisation = serialisation
						.replaceAll(
								"end rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string",
								"end rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime");
				serialisation = serialisation
						.replaceAll(
								"start rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string",
								"start rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime");
			}
			PrintWriter pw = response.getWriter();
			pw.print(serialisation);
			pw.flush();
		}

	}
%>