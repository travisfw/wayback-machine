/**
 * 
 */
package org.archive.wayback.util.webapp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Helper static methods to implement registration of a RequestHandler with a
 * RequestMapper, based on the beanName() method.
 * 
 * @author brad
 *
 */
public class BeanNameRegistrar {

	private static final Logger LOGGER = Logger.getLogger(
			BeanNameRegistrar.class.getName());

	private static final String PORT_PATTERN_STRING = 
		"([0-9]+):?";
	private static final String PORT_PATH_PATTERN_STRING = 
		"([0-9]+):([0-9a-zA-Z_.-]+)";
	private static final String HOST_PORT_PATTERN_STRING = 
		"([0-9a-z_.-]+):([0-9]+):?";
	private static final String HOST_PORT_PATH_PATTERN_STRING = 
		"([0-9a-z_.-]+):([0-9]+):([0-9a-zA-Z_.-]+)";
	
	private static final Pattern PORT_PATTERN = 
		Pattern.compile(PORT_PATTERN_STRING);
	private static final Pattern PORT_PATH_PATTERN = 
		Pattern.compile(PORT_PATH_PATTERN_STRING);
	private static final Pattern HOST_PORT_PATTERN = 
		Pattern.compile(HOST_PORT_PATTERN_STRING);
	private static final Pattern HOST_PORT_PATH_PATTERN = 
		Pattern.compile(HOST_PORT_PATH_PATTERN_STRING);
	
	/*
	 * matches:
	 *   8080
	 *   8080:
	 */
	private static boolean registerPort(String name, RequestHandler handler, 
			RequestMapper mapper) {
		Matcher m = null;
		m = PORT_PATTERN.matcher(name);
		if(m.matches()) {
			int port = Integer.parseInt(m.group(1)); 
			mapper.addRequestHandler(port, null, null, handler);
			return true;
		}
		return false;
	}
	/*
	 * matches:
	 *   8080:blue
	 *   8080:fish
	 */
	private static boolean registerPortPath(String name, RequestHandler handler, 
			RequestMapper mapper) {
		Matcher m = null;
		m = PORT_PATH_PATTERN.matcher(name);
		if(m.matches()) {
			int port = Integer.parseInt(m.group(1)); 
			mapper.addRequestHandler(port, null, m.group(2), handler);
			return true;
		}
		return false;
	}
	/*
	 * matches:
	 *   localhost.archive.org:8080
	 *   static.localhost.archive.org:8080
	 */
	private static boolean registerHostPort(String name, RequestHandler handler, 
			RequestMapper mapper) {
		Matcher m = null;
		m = HOST_PORT_PATTERN.matcher(name);
		if(m.matches()) {
			int port = Integer.parseInt(m.group(2));
			mapper.addRequestHandler(port, m.group(1), null, handler);
			return true;
		}
		return false;
	}
	/*
	 * matches:
	 *   localhost.archive.org:8080:two
	 *   static.localhost.archive.org:8080:fish
	 */
	private static boolean registerHostPortPath(String name, 
			RequestHandler handler,	RequestMapper mapper) {
		Matcher m = null;
		m = HOST_PORT_PATH_PATTERN.matcher(name);
		if(m.matches()) {
			int port = Integer.parseInt(m.group(2)); 
			mapper.addRequestHandler(port, m.group(1), m.group(3), handler);
			return true;
		}
		return false;
	}
	
	/**
	 * Extract the RequestHandler objects beanName, parse it, and register the
	 * RequestHandler with the RequestMapper according to the beanNames 
	 * semantics.
	 * @param handler The RequestHandler to register
	 * @param mapper the RequestMapper where the RequestHandler should be 
	 * registered.
	 */
	public static void registerHandler(RequestHandler handler, 
			RequestMapper mapper) {
		String name = handler.getBeanName();
		if(name != null) {
			if(name.equals(RequestMapper.GLOBAL_PRE_REQUEST_HANDLER)) {
				LOGGER.info("Registering Global-pre request handler:" +
						handler);
				mapper.addGlobalPreRequestHandler(handler);
				
			} else if(name.equals(RequestMapper.GLOBAL_POST_REQUEST_HANDLER)) {
	
				LOGGER.info("Registering Global-post request handler:" + 
						handler);
				mapper.addGlobalPostRequestHandler(handler);
				
			} else {
				try {
	
					boolean registered = 
						registerPort(name, handler, mapper) ||
						registerPortPath(name, handler, mapper) ||
						registerHostPort(name, handler, mapper) ||
						registerHostPortPath(name, handler, mapper);
	
					if(!registered) {
						LOGGER.error("Unable to register (" + name + ")");
					}
				} catch(NumberFormatException e) {
					LOGGER.error("FAILED parseInt(" + name + ")");
				}
			}
		} else {
			LOGGER.info("Unable to register RequestHandler - null beanName");
		}
		if(handler instanceof ShutdownListener) {
			ShutdownListener s = (ShutdownListener) handler;
			mapper.addShutdownListener(s);
		}
	}
}
