package com.github.jenkins.plugin.report.writer.html;

public abstract class BasicWriter {

	private StringBuilder output = new StringBuilder();

	public void write(String value) {
		output.append(value);
	}

	public String build() {
		return output.toString();
	}
}
