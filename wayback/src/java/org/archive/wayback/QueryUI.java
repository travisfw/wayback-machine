package org.archive.wayback;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.core.WMRequest;
import org.archive.wayback.core.WaybackLogic;

public interface QueryUI {
	public void init(final Properties p) throws IOException;

	public void handle(final WaybackLogic wayback, final WMRequest wmRequest,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException;

	public void showQueryResults(WaybackLogic wayback,
			HttpServletRequest request, HttpServletResponse response,
			final WMRequest wmRequest, final ResourceResults results)
			throws IOException, ServletException;

	public void showPathQueryResults(WaybackLogic wayback,
			HttpServletRequest request, HttpServletResponse response,
			final WMRequest wmRequest, final ResourceResults results)
			throws IOException, ServletException;

	public void showNoMatches(final WMRequest wmRequest,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException;

	public void showIndexNotAvailable(final WMRequest wmRequest,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException;
}
