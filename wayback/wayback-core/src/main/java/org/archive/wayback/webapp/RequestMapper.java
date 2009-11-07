/* RequestMapper
 *
 * $Id$
 *
 * Created on 5:36:36 PM Apr 20, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-webapp.
 *
 * wayback-webapp is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-webapp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-webapp; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.webapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.archive.wayback.exception.ConfigurationException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * RequestMapper accepts a request, and maps that request to
 * a WaybackContext suitable for that request.
 * 
 * This object is a singleton, and the class provides methods for constructing
 * and accessing the singleton.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RequestMapper {
	private static final Logger LOGGER = Logger.getLogger(RequestMapper.class
			.getName());
	
	private final static String PORT_SEPARATOR = ":";
	
	private final static String CONFIG_PATH = "config-path";
	
	private XmlBeanFactory factory = null;
	/**
	 * @param configPath
	 * @param servletContext
	 * @throws ConfigurationException 
	 */
	@SuppressWarnings("unchecked")
	public RequestMapper(ServletContext servletContext) throws ConfigurationException {
		
		String configPath = servletContext.getInitParameter(CONFIG_PATH);
		if(configPath == null) {
			throw new ConfigurationException("Missing " + CONFIG_PATH 
					+ " parameter");
		}
		String resolvedPath = servletContext.getRealPath(configPath);
		Resource resource = new FileSystemResource(resolvedPath);
		factory = new XmlBeanFactory(resource);
		Map map = factory.getBeansOfType(PropertyPlaceholderConfigurer.class);
		if(map != null) {
			Collection<PropertyPlaceholderConfigurer> macros = map.values();
			for(PropertyPlaceholderConfigurer macro : macros) {
				macro.postProcessBeanFactory(factory);
			}
		}
		factory.preInstantiateSingletons();
	}
	
	private String getContextID(HttpServletRequest request) {
		String requestPath = request.getRequestURI();
		String contextPath = request.getContextPath();
		if(requestPath.startsWith(contextPath)) {
			requestPath = requestPath.substring(contextPath.length());
		}
		String collection = "";
		if(requestPath.startsWith("/")) {
			int secondSlash = requestPath.indexOf("/",1);
			if(secondSlash != -1) {
				collection = PORT_SEPARATOR + 
					requestPath.substring(1,requestPath.indexOf("/",1));
			} else {
				collection = PORT_SEPARATOR + requestPath.substring(1);
			}
		}
		return String.valueOf(request.getLocalPort()) + collection;
	}
	
	/**
	 * @param request
	 * @return WaybackContext that handles the specific incoming HTTP request
	 */
	public RequestContext mapContext(HttpServletRequest request) {

		RequestContext context = null;
		String portStr = String.valueOf(request.getLocalPort());
		if(factory.containsBean(portStr)) {
			Object o = factory.getBean(portStr);
			if(o instanceof RequestContext) {
				context = (RequestContext) o;
			}
		} else {
			String contextID = getContextID(request);
			if(factory.containsBean(contextID)) {
				Object o = factory.getBean(contextID);
				if(o instanceof RequestContext) {
					context = (RequestContext) o;
				}
			}
		}
		if(context == null) {
			ArrayList<String> names = getAccessPointNamesOnPort(portStr);
			request.setAttribute("AccessPointNames", names);
		}
		return context;
	}

	public ArrayList<String> getAccessPointNamesOnPort(String portStr) {
		ArrayList<String> names = new ArrayList<String>();
		String[] apNames = factory.getBeanNamesForType(AccessPoint.class);
		String portStrColon = portStr + ":";
		for(String apName : apNames) {
			if(apName.startsWith(portStrColon)) {
				names.add(apName.substring(portStrColon.length()));
			}
		}
		return names;
	}
	/**
	 * clean up all WaybackContexts, which should release resources gracefully.
	 */
	@SuppressWarnings("unchecked")
	public void destroy() {
		LOGGER.info("shutting down contexts...");
		Map beanMap = factory.getBeansOfType(AccessPoint.class);
		Collection accessPoints = beanMap.values();
		for(Object o : accessPoints) {
			if(o instanceof AccessPoint) {
				AccessPoint ap = (AccessPoint) o;
				try {
					String apName = ap.getBeanName();
					LOGGER.info("Shutting down AccessPoint " + apName);
					ap.shutdown();
					LOGGER.info("Successfully shut down " + apName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
