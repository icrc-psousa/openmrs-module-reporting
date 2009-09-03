/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.reporting.web.renderers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

import org.openmrs.annotation.Handler;
import org.openmrs.module.report.ReportData;
import org.openmrs.module.report.ReportDefinition;
import org.openmrs.module.report.renderer.RenderingException;

/**
 * An abstract Web Renderer implementation that stubs all render methods.
 */
public abstract class AbstractWebReportRenderer implements WebReportRenderer {
	
	public boolean canRender(ReportDefinition reportDefinition) {
		return true;
	}

	public List<String> getDisplayColumns() { return null; }	
	public void render(ReportData results, String argument, OutputStream out) throws IOException, RenderingException {}
	public void render(ReportData reportData, OutputStream out) throws IOException, RenderingException {}
	public void render(ReportData reportData, Writer writer) throws IOException, RenderingException {}
	public void render(ReportData reportData, String argument, Writer writer)throws IOException, RenderingException {}
	public void setDisplayColumns(List<String> displayColumns) {}
}