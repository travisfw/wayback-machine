<?xml version="1.0" encoding="UTF-8"?>
<%@ page contentType="text/xml" %>
<%@ page import="org.archive.wayback.exception.WaybackException" %>
<%@ page import="org.archive.wayback.core.UIResults" %>
<%@ page import="org.archive.wayback.util.StringFormatter" %>
<%

WaybackException e = (WaybackException) request.getAttribute("exception");
UIResults results = UIResults.getFromRequest(request);
StringFormatter fmt = results.getFormatter();

%>
<wayback>
	<error>
		<title><%= UIQueryResults.encodeXMLContent(fmt.format(e.getTitleKey())) %></title>
		<message><%= UIQueryResults.encodeXMLContent(fmt.format(e.getMessageKey())) %></message>
	</error>
</wayback>
