package org.archive.wayback.accesspoint;

import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.BeanNameAware;

public class AccessPointConfig implements BeanNameAware {
	
	private Properties configs = null;
	private List<String> fileIncludePrefixes = null;
	private List<String> fileExcludePrefixes = null;
	private String beanName;
	
	public Properties getConfigs() {
		return configs;
	}
	public void setConfigs(Properties configs) {
		this.configs = configs;
	}
	public List<String> getFileIncludePrefixes() {
		return fileIncludePrefixes;
	}
	public void setFileIncludePrefixes(List<String> fileIncludePrefixes) {
		this.fileIncludePrefixes = fileIncludePrefixes;
	}
	public List<String> getFileExcludePrefixes() {
		return fileExcludePrefixes;
	}
	public void setFileExcludePrefixes(List<String> fileExcludePrefixes) {
		this.fileExcludePrefixes = fileExcludePrefixes;
	}
	
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
	public String getBeanName() {
		return this.beanName;
	}
}
