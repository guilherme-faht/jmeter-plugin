package com.github.jenkins.plugin;

import hudson.Extension;
import hudson.model.RootAction;

@Extension
public class JMeterRootAction implements RootAction {

	@Override
	public String getDisplayName() {
		System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "sandbox allow-same-origin allow-scripts; default-src 'self'; script-src * 'unsafe-eval'; img-src *; style-src * 'unsafe-inline'; font-src *");
		return "JMeter Plugin";
	}

	@Override
	public String getIconFileName() {
		return "clipboard.png";
	}

	@Override
	public String getUrlName() {
		return "https://github.com/guilherme-faht/jmeter-plugin";
	}
}
