package com.github.jenkins.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.github.jenkins.plugin.beans.StepResult;
import com.github.jenkins.plugin.report.HtmlReport;
import com.opencsv.bean.CsvToBeanBuilder;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

public class JMeterLogBuilder extends Builder {

	private static final String REPORT_TEMPLATE_PATH = "/report.html";

	private String path;

	@DataBoundConstructor
	public JMeterLogBuilder(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	@Override
	public boolean perform(Build<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {

		listener.getLogger().println("-----[JMeter Plugin Configuration]-----");
		listener.getLogger().println("OPERACIONAL SYSTEM: " + System.getProperty("os.name"));
		listener.getLogger().println("WORKSPACE: " + getWorkspace(build));
		listener.getLogger().println("LOG PATH: " + getPath());
		listener.getLogger().println("-----[JMeter Plugin Configuration]-----");

		listener.getLogger().println("Start JMeter Log");

		if (!isWorkspaceExists(build)) {
			listener.getLogger().println("Error: Workspace not localized");
			return false;
		}

		if (!isPathExists()) {
			listener.getLogger().println("Error: Path not localized");
			return false;
		}

		HtmlReport rep = new HtmlReport(600, 100);
		rep.addStyle("header", "Times", 16, "BI", "#FF0000", "#FFF1B2");
		rep.addStyle("footer", "Times", 16, "BI", "#2B2B2B", "#B5FFB4");
		rep.addStyle("break", "Arial", 12, "B", "#FFFFFF", "#FFFFFF");
		rep.addStyle("title", "Arial", 14, "B", "#FFFFFF", "#000000");
		rep.addStyle("success", "Arial", 10, "B", "#FFFFFF", "green");
		rep.addStyle("fail", "Arial", 10, "B", "#FFFFFF", "red");
		rep.addStyle("ResultSuccess", "Arial", 14, "B", "#FFFFFF", "green");
		rep.addStyle("ResultFail", "Arial", 14, "B", "#FFFFFF", "red");
		rep.addStyle("data", "Arial", 10, "", "#000000", "#FFFFFF");

		rep.addRow();
		rep.addCell("JMETER PLUGIN - TEST´s REPORT", "center", "header", 2);

		rep.addRow();
		rep.addCell("&nbsp", "center", "break", 2);

		rep.addRow();
		rep.addCell("TEST NAME", "left", "title", 1);
		rep.addCell("RESULT", "center", "title", 1);

		File dir = new File(getPath());
		File[] files = dir.listFiles();

		if (files == null) {
			listener.getLogger().println("Error: Not found log files");
			return false;
		}

		for (File file : files) {

			if (!file.getName().endsWith(".log")) {
				continue;
			}

			List<StepResult> steps = new CsvToBeanBuilder<StepResult>(
					new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")))
							.withType(StepResult.class).build().parse();

			int fails = 0;

			for (StepResult step : steps) {

				rep.addRow();
				rep.addCell(step.getLabel(), "left", "data", 1);

				if (step.getSuccess()) {
					rep.addCell("OK", "center", "success", 1);
				} else {
					rep.addCell("FAIL", "center", "fail", 1);
					fails++;
				}
			}

			if (fails > 0) {
				rep.addRow();
				rep.addCell("FAIL (" + fails + ")", "center", "ResultFail", 2);
			} else {
				rep.addCell("SUCCESS", "center", "ResultSuccess", 2);
			}
		}

		File artifactsDir = build.getArtifactsDir();

		if (!artifactsDir.isDirectory()) {

			boolean success = artifactsDir.mkdirs();

			if (!success) {
				listener.getLogger().println("Can't create artifacts directory at " + artifactsDir.getAbsolutePath());
			}
		}

		String path = artifactsDir.getCanonicalPath() + REPORT_TEMPLATE_PATH;
		rep.save(new File(path));

		return true;
	}

	private FilePath getWorkspace(Build<?, ?> build) {

		return build.getModuleRoot();
	}

	public boolean isWorkspaceExists(Build<?, ?> build) {

		return getWorkspace(build) != null;
	}

	public boolean isPathExists() {

		return Files.exists(Paths.get(getPath()));
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return aClass == FreeStyleProject.class;
		}

		@Override
		public String getDisplayName() {
			return "JMeter Log";
		}

		public FormValidation doCheckJmeterHome(@QueryParameter String path) {

			if (path == null || path.isEmpty()) {
				return FormValidation.error(com.github.jenkins.plugin.Messages
						._JMeterBuilder_DescriptorImpl_errors_fieldRequired().toString());
			}

			return FormValidation.ok();
		}
	}
}
