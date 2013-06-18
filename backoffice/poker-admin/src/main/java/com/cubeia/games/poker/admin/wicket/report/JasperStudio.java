/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cubeia.games.poker.admin.wicket.report;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.*;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

@Component
@SuppressWarnings("deprecation")
public class JasperStudio implements ReportStudio {

	private static final String REPORT_EXTESION = ".jrxml";
	
	private DataSource dataSource;
	private Collection<?> colDataSource;
	
	private final Logger log = Logger.getLogger(getClass());
	
	@Override
	public ReportGenerator getGenerator(RequestBean b) {
		return new Generator(b);
	}
	
	
	// --- PRIVATE MEMBERS --- //
	
	private Connection getConnection() throws SQLException {		
		return dataSource == null ? null : dataSource.getConnection();
	}
	
	
	@Override
	public void setCollectionSource(Collection<?> source) {
		this.colDataSource = source;
	}

	private class Generator implements ReportGenerator {

		private final RequestBean req;
		private JasperReport report;
		
		private Generator(RequestBean req) {
			readDefenition(req);
			this.req = req;
		}
		
		private void readDefenition(RequestBean req) {
			String name = "/reports/" + req.getName() + REPORT_EXTESION;			
			InputStream in = getClass().getClassLoader().getResourceAsStream(name);
			if(in == null) throw new IllegalArgumentException("Could not find report with name '" + name + "'");
			else {
				try {
					JasperDesign design = JasperManager.loadXmlDesign(in);
					design.setIgnorePagination(!req.getFormat().isPaged());
					report = JasperCompileManager.compileReport(design);
				} catch (JRException e) {
					throw new IllegalStateException("Failed to compile report", e);
				} finally {
					try {
						in.close();
					} catch(IOException e) { }
				}
			}
		}

		@Override
		public void generate(OutputStream out) throws IOException, ReportException {
			Connection con = null;
			try {
				con = getConnection();
				if(log.isDebugEnabled()) {
					String tmp = "PARAMS: ";
					for (String key : req.getParams().keySet()) {
						tmp += key + "=" + req.getParams().get(key) + "; ";
					}
					log.debug(tmp);
				}
				JasperPrint print = colDataSource == null ? JasperFillManager.fillReport(report, req.getParams(), con)
														  : JasperFillManager.fillReport(report, req.getParams(), new JRBeanCollectionDataSource(colDataSource));
				JRExporter exporter = getExporterForFormat(req.getFormat());
				exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
				exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
				// exporter.setParameter(JRExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
				exporter.exportReport();
			} catch (Exception e) {
				throw new ReportException("Failed to create report", e);
			} finally {
				try {
					colDataSource = null;
					if(con != null) {
						con.close();
					}
				} catch(SQLException e) {
					Logger.getLogger(getClass()).error("Failed to close SQL connection", e);
				}
			}
		}

		private JRExporter getExporterForFormat(Format format) {
			if(format == Format.CSV) {
				return new JRCsvExporter();
			} else if(format == Format.XML) {
				return new JRXmlExporter();
			} else if(format == Format.PDF) {
				return new JRPdfExporter();
			} else if(format == Format.XLS) {
				return new JRXlsExporter();
			} else {
				return new JRHtmlExporter();
			} 
		}

		@Override
		public String getContentType() {
			return req.getFormat().getContentType();
		}

		@Override
		public String getReportName() {
			return req.getName();
		}		
		
		@Override
		public Disposition getContentDisposition() {
			return req.getFormat().isAttachment() ? Disposition.ATTACHMENT : Disposition.INLINE;
		}
		
		@Override
		public String getContentName() {
			return req.getName() + "." + req.getFormat().name().toLowerCase();
		}
	}
}
