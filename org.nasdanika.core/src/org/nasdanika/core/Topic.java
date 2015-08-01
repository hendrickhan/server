package org.nasdanika.core;

import java.util.Map;

/**
 * Topic allows dynamic contributions to the documentation system.
 * @author Pavel
 *
 */
public interface Topic {
	
	/**
	 * @return HTML content
	 */
	String getContent();
	
	Map<String, Topic> getSubTopics();
	
	/**
	 * @return relative path to topic icon or null.
	 */
	String getIcon();
}