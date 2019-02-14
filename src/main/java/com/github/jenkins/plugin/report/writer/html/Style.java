package com.github.jenkins.plugin.report.writer.html;

import java.util.HashMap;
import java.util.Map;

public class Style extends BasicWriter {

	private String name;

	private Map<String, String> properties = new HashMap<>();

	private static Map<String, Boolean> loaded = new HashMap<>();

	public Style(String name) {
		this.name = name;
		loaded.put(this.name, false);
	}

	public Style set(String property, String value) {
		properties.put(property, value);
		return this;
	}
	
	@Override
	public String build() {

		if (!loaded.get(name)) {

			write("<style type='text/css' media='screen'>\n");
			write("."+name + "\n");
			write("{\n");

			if (!properties.isEmpty()) {

				for (Map.Entry<String, String> entry : properties.entrySet()) {

					write("\t " + entry.getKey() + ": " + entry.getValue() + ";\n");
				}
			}

			write("}\n");
			write("</style>\n");

			loaded.put(name, true);
		}
		
		return super.build();
	}
}
