package com.github.jenkins.plugin.writer;

import java.io.File;
import java.io.IOException;

public interface TableWriter {

	public void addStyle(String styleName, String fontFace, int fontSize, String fontStyle, String fontColor,
			String fillColor);

	public void addRow();

	public void addCell(String content, String align, String styleName, int colspan);

	public void save(File fileName) throws IOException;
}
