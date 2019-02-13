package com.github.jenkins.plugin;

import hudson.Extension;
import hudson.model.RootAction;

@Extension
public class JMeterRootAction implements RootAction {

	@Override
	public String getDisplayName() {
		return "JMeter Plugin";
	}

	@Override
	public String getIconFileName() {
		return "clipboard.png";
	}

	@Override
	public String getUrlName() {
		return "https://www.facebook.com";
	}
}
