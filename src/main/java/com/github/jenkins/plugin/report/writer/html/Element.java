package com.github.jenkins.plugin.report.writer.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Element extends BasicWriter {

	private String name;

	private Map<String, String> properties = new HashMap<>();

	private List<Object> children = new ArrayList<>();

	public Element(String name) {
		this.name = name;
	}

	public Element set(String property, String value) {
		properties.put(property, value);
		return this;
	}

	public Element add(Element child) {
		children.add(child);
		return this;
	}

	public Element add(String child) {
		children.add(child);
		return this;
	}
	
	private void open() {

		write("<" + name);

		if (!properties.isEmpty()) {

			for (Map.Entry<String, String> entry : properties.entrySet()) {

				write(" " + entry.getKey() + "=\"" + entry.getValue() + "\"");
			}
		}
		
		write(">");
	}
	
	private void close() {
		write("</"+name+">\n");
	}
	
	@Override
	public String build() {
		
		open();
		write("\n");
		
		if(!children.isEmpty()) {
			
			for(Object child : children) {
				
				if (child instanceof Element) {
					write(((Element) child).build());
				} else {
					write((String) child);
				}
			}
		}
		
		close();
		
		return super.build();
	}
}
