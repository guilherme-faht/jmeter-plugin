package com.github.jenkins.plugin.report;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.github.jenkins.plugin.report.writer.html.Element;
import com.github.jenkins.plugin.report.writer.html.Style;
import com.github.jenkins.plugin.report.writer.html.TableWriter;
import com.google.common.io.Files;

public class HtmlReport implements TableWriter {

	private List<Style> styles = new ArrayList<>();

	private int[] widths;

	private int colCounter;

	private Element table;

	private Element currentRow;

	public HtmlReport(int... widths) {
		this.widths = widths;

		table = new Element("table");
		table.set("cellspacing", "0").set("cellpadding", "0").set("style", "border-collapse:collapse;margin:0 auto");
	}

	@Override
	public void addStyle(String styleName, String fontFace, int fontSize, String fontStyle, String fontColor,
			String fillColor) {

		Style style = new Style(styleName);
		style.set("font-family", fontFace).set("color", fontColor).set("background-color", fillColor)
				.set("border-top", "1px solid #000000").set("border-bottom", "1px solid #000000")
				.set("border-left", "1px solid #000000").set("border-right", "1px solid #000000")
				.set("font-size", fontSize + "pt").set("border-left", "1px solid #000000");

		if (fontStyle != null && fontStyle.indexOf('B') > -1) {
			style.set("font-weight", "bold");
		} else if (fontStyle != null && fontStyle.indexOf('I') > -1) {
			style.set("font-style", "italic");
		}

		styles.add(style);
	}

	@Override
	public void addRow() {
		currentRow = new Element("tr");
		table.add(currentRow);
		colCounter = 0;
	}

	@Override
	public void addCell(String content, String align, String styleName, int colSpan) {

		int calcWidth = 0;

		for (int i = colCounter; i < colCounter + colSpan; i++) {
			calcWidth += widths[i];
		}

		Element td = new Element("td");
		td.set("align", align).set("width", String.valueOf(calcWidth - 2)).set("colspan", String.valueOf(colSpan));
		td.add(content);

		if (styleName != null) {
			td.set("class", styleName);
		}

		currentRow.add(td);

		colCounter++;
	}

	@Override
	public void save(File fileName) throws IOException {

		StringBuilder html = new StringBuilder();
		html.append("<html>\n");

		html.append("\t<head>\n");
		html.append("\t<meta name=\"hudson.model.DirectoryBrowserSupport.CSP\" ");
		html.append("content=\"sandbox allow-same-origin allow-scripts; default-src 'self'; script-src * 'unsafe-eval'; img-src *; style-src * 'unsafe-inline'; font-src *\">");
		for (Style style : styles) {
			html.append(style.build());
		}
		html.append("\t</head>\n");

		html.append("\t<body>\n");
		html.append(table.build());
		html.append("\t</body>\n");
		html.append("</html>");

		Files.write(html.toString(), fileName, Charset.forName("UTF-8"));
	}
}
