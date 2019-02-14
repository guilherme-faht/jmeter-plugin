package com.github.jenkins.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

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

public class JMeterBuilder extends Builder {

	private String jmeterHome;

	private String command;

	@DataBoundConstructor
	public JMeterBuilder(String jmeterHome, String command) {
		this.jmeterHome = jmeterHome;
		this.command = command;
	}

	public String getJmeterHome() {
		return jmeterHome;
	}

	public void setJmeterHome(String jmeterHome) {
		this.jmeterHome = jmeterHome;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public boolean perform(Build<?, ?> build, Launcher launcher, BuildListener listener)
			throws InterruptedException, IOException {

		listener.getLogger().println("-----[JMeter Plugin Configuration]-----");
		listener.getLogger().println("OPERACIONAL SYSTEM: " + System.getProperty("os.name"));
		listener.getLogger().println("WORKSPACE: " + getWorkspace(build));
		listener.getLogger().println("JMETER_HOME: " + getJmeterHome());
		listener.getLogger().println("COMMAND: " + getCommand());
		listener.getLogger().println("-----[JMeter Plugin Configuration]-----");

		listener.getLogger().println("Start JMeter Test");

		if (!isWorkspaceExists(build)) {
			listener.getLogger().println("Error: Workspace not localized");
			return false;
		}

		if (!isJMeterExists()) {
			listener.getLogger().println("Error: JMeter not localized");
			return false;
		}

		if (!isTestFileExists(build)) {
			listener.getLogger().println("Error: JMeter file '.jmx' not localized");
			return false;
		}

		ProcessBuilder processBuilder = new ProcessBuilder(
				prepareProcessComand(getJMeterCommand(), "-n", "-t", getTestCommand(build)));
		processBuilder.directory(new File(getJMeterDirectory()));
		processBuilder.environment().put("JMETER_HOME", getJmeterHome());
		Process process = processBuilder.start();

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), Charset.forName("UTF-8")));
		String line = null;

		while ((line = reader.readLine()) != null) {
			listener.getLogger().println(line);
		}

		reader.close();
		process.waitFor();

		return true;
	}

	private FilePath getWorkspace(Build<?, ?> build) {

		return build.getModuleRoot();
	}

	private String getJMeterDirectory() {

		StringBuilder sb = new StringBuilder();
		sb.append(getJmeterHome()); // JMETER_HOME
		sb.append(File.separator); // PATH SEPARATOR
		sb.append("bin"); // JMETER BIN PATH

		return sb.toString();
	}

	private String getJMeterCommand() {

		StringBuilder sb = new StringBuilder(getJMeterDirectory());
		sb.append(File.separator); // PATH SEPARATOR

		if (isWindows()) {
			sb.append("jmeter.bat"); // JMETER WINDOWS BAT
		} else {
			sb.append("jmeter.sh"); // JMETER LINUX SHELLSCRIPT
		}

		return sb.toString();
	}

	private String getTestFile(Build<?, ?> build) {

		Pattern pattern = Pattern.compile("(.{1,}\\.jmx)");
		Matcher matcher = pattern.matcher(getCommand());

		String testFile = null;

		if (matcher.find()) {

			if (Files.exists(Paths.get(matcher.group(1)))) {
				testFile = matcher.group(1);
			} else if (Files.exists(Paths.get(getWorkspace(build) + File.separator + matcher.group(1)))) {
				testFile = getWorkspace(build) + File.separator + matcher.group(1);
			}
		}

		return testFile;
	}

	private String getTestCommand(Build<?, ?> build) {

		Pattern pattern = Pattern.compile("(.{1,}\\.jmx)");
		Matcher matcher = pattern.matcher(getCommand());

		StringBuilder sb = new StringBuilder();

		if (matcher.find()) {

			if (Files.exists(Paths.get(matcher.group(1)))) {
				sb.append(getCommand());
			} else if (Files.exists(Paths.get(getWorkspace(build) + File.separator + matcher.group(1)))) {
				sb.append(getWorkspace(build) + File.separator + getCommand());
			}
		}

		return sb.toString();
	}

	private String[] prepareProcessComand(String jMeterCommand, String prop1, String prop2, String testCommand) {

		List<String> arrayList = new ArrayList<>();
		arrayList.add(jMeterCommand);
		arrayList.add(prop1);
		arrayList.add(prop2);

		String[] props = testCommand.split(" ");
		for (String prop : props) {

			prop = prop.trim();

			if (!prop.isEmpty()) {
				arrayList.add(prop);
			}
		}

		return arrayList.toArray(new String[0]);
	}

	private boolean isWindows() {

		return System.getProperty("os.name").contains("Windows");
	}

	public boolean isWorkspaceExists(Build<?, ?> build) {

		return getWorkspace(build) != null;
	}

	public boolean isJMeterExists() {

		return Files.exists(Paths.get(getJMeterCommand()));
	}

	private boolean isTestFileExists(Build<?, ?> build) {

		return getTestFile(build) != null;
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return aClass == FreeStyleProject.class;
		}

		@Override
		public String getDisplayName() {
			return "JMeter Test";
		}

		public FormValidation doCheckJmeterHome(@QueryParameter String jmeterHome) {

			Path jmeterHomePath = Paths.get(jmeterHome);

			if (jmeterHome == null || jmeterHome.isEmpty()) {
				return FormValidation.error(com.github.jenkins.plugin.Messages
						._JMeterBuilder_DescriptorImpl_errors_fieldRequired().toString());
			}

			if (!Files.isDirectory(jmeterHomePath)) {
				return FormValidation.error(com.github.jenkins.plugin.Messages
						._JMeterBuilder_DescriptorImpl_errors_invalidJMeterHome().toString());
			}

			return FormValidation.ok();
		}

		public FormValidation doCheckCommand(@QueryParameter String command) {

			if (command == null || command.isEmpty()) {
				return FormValidation.error(com.github.jenkins.plugin.Messages
						._JMeterBuilder_DescriptorImpl_errors_fieldRequired().toString());
			}

			return FormValidation.ok();
		}
	}
}
