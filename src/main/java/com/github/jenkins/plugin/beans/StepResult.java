package com.github.jenkins.plugin.beans;

import com.opencsv.bean.CsvBindByName;

public class StepResult {

	@CsvBindByName
	private String label;

	@CsvBindByName
	private Boolean success;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}
}
