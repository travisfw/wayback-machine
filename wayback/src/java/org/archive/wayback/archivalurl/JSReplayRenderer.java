/* JSRenderer
 *
 * $Id$
 *
 * Created on 1:34:16 PM Nov 8, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
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
package org.archive.wayback.archivalurl;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.ReplayResultURIConverter;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.proxy.RawReplayRenderer;

/**
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class JSReplayRenderer extends RawReplayRenderer {
	/**
	 * MIME type of documents which should be marked up with javascript to
	 * rewrite URLs inside document
	 */
	private final static String TEXT_HTML_MIME = "text/html";

	private final static long MAX_HTML_MARKUP_LENGTH = 1024 * 1024 * 5;
	
	/** test if the SearchResult should be replayed raw, without JS markup
	 * @param result
	 * @return boolean, true if the document should be returned raw.
	 */
	private boolean isRawReplayResult(SearchResult result) {
		if (-1 == result.get(WaybackConstants.RESULT_MIME_TYPE).indexOf(
				TEXT_HTML_MIME)) {
			return true;
		}
		return false;
	}

	/** send the client to a different/better request URL for the document
	 * they asked for.
	 * 
	 * @param httpResponse
	 * @param url
	 * @throws IOException
	 */
	private void redirectToBetterUrl(HttpServletResponse httpResponse,
			String url) throws IOException {

		httpResponse.sendRedirect(url);

	}

	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ReplayResultURIConverter uriConverter) throws ServletException,
			IOException {

		if (resource == null) {
			throw new IllegalArgumentException("No resource");
		}
		if (result == null) {
			throw new IllegalArgumentException("No result");
		}

		// redirect to actual date if diff than request:
		if (!wbRequest.get(WaybackConstants.REQUEST_EXACT_DATE).equals(
				result.get(WaybackConstants.RESULT_CAPTURE_DATE))) {

			String betterURI = uriConverter.makeReplayURI(result);
			redirectToBetterUrl(httpResponse, betterURI);

		} else {

			if (resource.getRecordLength() > MAX_HTML_MARKUP_LENGTH ||
					isRawReplayResult(result)) {
				
				super.renderResource(httpRequest, httpResponse, wbRequest,
						result, resource, uriConverter);
			} else {

				resource.parseHeaders();
				copyRecordHttpHeader(httpResponse, resource, uriConverter,
						result, false);

				// slurp the whole thing into RAM:
				byte[] bbuffer = new byte[4 * 1024];
				StringBuffer sbuffer = new StringBuffer();
				for (int r = -1; 
					(r = resource.read(bbuffer, 0, bbuffer.length)) != -1;) {
					
					sbuffer.append(new String(bbuffer, 0, r));
				}

				markUpPage(sbuffer, result, uriConverter);

				httpResponse.setHeader("Content-Length", "" + sbuffer.length());
				ServletOutputStream out = httpResponse.getOutputStream();
				out.print(new String(sbuffer));
			}
		}
	}

	/** 
	 * add BASE tag and javascript to a page that will rewrite embedded URLs 
	 * to point back into the WM, also attempt to fix up URL attributes in some
	 * tags that must be correct at page load (FRAME, META, LINK, SCRIPT)
	 * 
	 * @param page
	 * @param result
	 * @param uriConverter
	 */
	private void markUpPage(StringBuffer page, SearchResult result,
			ReplayResultURIConverter uriConverter) {

		String wmPrefix = uriConverter.getReplayUriPrefix();
		String pageUrl = result.get(WaybackConstants.RESULT_URL);
		String pageTS = result.get(WaybackConstants.RESULT_CAPTURE_DATE);

		TagMagix.markupTagRE(page, wmPrefix, pageUrl, pageTS, "FRAME", "SRC");
		TagMagix.markupTagRE(page, wmPrefix, pageUrl, pageTS, "META", "URL");
		TagMagix.markupTagRE(page, wmPrefix, pageUrl, pageTS, "LINK", "HREF");
		// TODO: The classic WM added a js_ to the datespec, so NotInArchives
		// can return an valid javascript doc, and not cause Javascript errors.
		TagMagix.markupTagRE(page, wmPrefix, pageUrl, pageTS, "SCRIPT", "SRC");

		insertBaseTag(page, result);
		insertJavascript(page, result, uriConverter);
	}

	/** add a BASE HTML tag to make all path relative URLs map to the right URL
	 * 
	 * @param page
	 * @param result
	 */
	private void insertBaseTag(StringBuffer page, SearchResult result) {
		String resultUrl = result.get(WaybackConstants.RESULT_URL);
		String baseTag = "<BASE HREF=\"http://" + resultUrl + "\">";
		int insertPoint = page.indexOf("<head>");
		if (-1 == insertPoint) {
			insertPoint = page.indexOf("<HEAD>");
		}
		if (-1 == insertPoint) {
			insertPoint = 0;
		} else {
			insertPoint += 6; // just after the tag
		}
		page.insert(insertPoint, baseTag);
	}

	/** insert Javascript into a page to rewrite URLs
	 * @param page
	 * @param result
	 * @param uriConverter
	 */
	private void insertJavascript(StringBuffer page, SearchResult result,
			ReplayResultURIConverter uriConverter) {
		String resourceTS = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
		String nowTS = Timestamp.currentTimestamp().getDateStr();

		// TODO: make this an external script included via SRC tag...
		String contextPath = uriConverter.getReplayUriPrefix() + "/"
				+ resourceTS + "/";

		String scriptInsert = "<SCRIPT language=\"Javascript\">\n"
				+ "<!--\n"
				+ "\n"
				+ "//            FILE ARCHIVED ON "
				+ resourceTS
				+ " AND RETRIEVED FROM THE\n"
				+ "//            INTERNET ARCHIVE ON "
				+ nowTS
				+ ".\n"
				+ "//            JAVASCRIPT APPENDED BY WAYBACK MACHINE, COPYRIGHT INTERNET ARCHIVE.\n"
				+ "//\n"
				+ "// ALL OTHER CONTENT MAY ALSO BE PROTECTED BY COPYRIGHT (17 U.S.C.\n"
				+ "// SECTION 108(a)(3)).\n"
				+ "\n"
				+ "var sWayBackCGI = \""
				+ contextPath
				+ "\";\n"
				+ "                \n"
				+ "function xResolveUrl(url) {\n"
				+ "   var image = new Image();\n"
				+ "   image.src = url;\n"
				+ "   return image.src;\n"
				+ "}\n"
				+ "function xLateUrl(aCollection, sProp) {\n"
				+ "   var i = 0;\n"
				+ "   for(i = 0; i < aCollection.length; i++) {\n"
				+ "      if (typeof(aCollection[i][sProp]) == \"string\") {\n"
				+ "         if (aCollection[i][sProp].indexOf(\"mailto:\") == -1 &&\n"
				+ "            aCollection[i][sProp].indexOf(\"javascript:\") == -1) {\n"
				+ "\n"
				+ "            if(aCollection[i][sProp].indexOf(\"http\") == 0) {\n"
				+ "               aCollection[i][sProp] = sWayBackCGI + aCollection[i][sProp];\n"
				+ "            } else {\n"
				+ "               aCollection[i][sProp] = sWayBackCGI + xResolveUrl(aCollection[i][sProp]);\n"
				+ "            }\n"
				+ "         }\n"
				+ "      }\n"
				+ "   }\n"
				+ "}\n"
				+ "\n"
				+ "xLateUrl(document.getElementsByTagName(\"IMG\"),\"src\");\n"
				+ "xLateUrl(document.getElementsByTagName(\"A\"),\"href\");\n"
				+ "xLateUrl(document.getElementsByTagName(\"AREA\"),\"href\");\n"
				+ "xLateUrl(document.getElementsByTagName(\"OBJECT\"),\"codebase\");\n"
				+ "xLateUrl(document.getElementsByTagName(\"OBJECT\"),\"data\");\n"
				+ "xLateUrl(document.getElementsByTagName(\"APPLET\"),\"codebase\");\n"
				+ "xLateUrl(document.getElementsByTagName(\"APPLET\"),\"archive\");\n"
				+ "xLateUrl(document.getElementsByTagName(\"EMBED\"),\"src\");\n"
				+ "xLateUrl(document.getElementsByTagName(\"BODY\"),\"background\");\n"
				+ "var forms = document.getElementsByTagName(\"FORM\");\n"
				+ "if (forms) {\n" + "		var j = 0;\n"
				+ "		for (j = 0; j < forms.length; j++) {\n"
				+ "			f = forms[j];\n"
				+ "			if (typeof(f.action)  == \"string\") {\n"
				+ "				if(typeof(f.method)  == \"string\") {\n"
				+ "					if(typeof(f.method) != \"post\") {\n"
				+ "				    	f.action = sWayBackCGI + f.action;\n"
				+ "				    }\n"
				+ "				}\n"
				+ "			}\n"
				+ "		}\n"
				+ "}\n"
				+ "\n"
				+ "//           -->\n"
				+ "\n"
				+ "</SCRIPT>\n";

		int insertPoint = page.indexOf("</body>");
		if (-1 == insertPoint) {
			insertPoint = page.indexOf("</BODY>");
		}
		if (-1 == insertPoint) {
			insertPoint = page.length();
		}
		page.insert(insertPoint, scriptInsert);
	}

}
