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

package com.cubeia.backoffice.report;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.cubeia.backoffice.report.ReportGenerator.Disposition;

/**
 * This servlet should be called with a URI where the path info matches a 
 * report JRXML file on the class path, for example:
 * 
 * <pre>
 *    /all-users/?para:param1=value1
 * </pre>
 * 
 * The above request will look for an "all-users.jrxml" file on the class path and
 * compile a report with the parameter "param1" set to "value1".
 * 
 * <p>All parameters prefixed with "para:" will be stripped of the prefix and used as 
 * parameters when the report it compiled. Other than this, the following parameters
 * are recognized:
 * 
 * <pre>
 *   format = [ 'xml' | 'csv' | 'xls' | 'html' | 'pdf' ]
 * </pre>
 * 
 * @author larsan
 */
public class ReportServlet extends HttpServlet {

	public static final String REPORTS_COLLECTION_DATA_SOURCE = "reports.collection.dataSource";

	private static final long serialVersionUID = -2123131036634418948L;

	private ReportStudio reports;
	private Logger log;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
		reports = (ReportStudio) context.getBean("jasperStudio");
		log = Logger.getLogger(getClass());
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Collection<?> colDataSource = (Collection<?>)req.getSession().getAttribute(REPORTS_COLLECTION_DATA_SOURCE);
		reports.setCollectionSource(colDataSource);
		
		RequestBean b = RequestBean.parse(req);
		log.debug("Received request: " + b);
		ReportGenerator gen = reports.getGenerator(b);
		resp.setContentType(gen.getContentType());
		resp.setHeader("content-disposition", getDisposition(gen) + "; filename=" + gen.getContentName());
		OutputStream out = resp.getOutputStream();
		try {
			gen.generate(out);
		} catch (Exception e) {
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.error("Report generation failed", e);
		} finally {
			req.getSession().setAttribute(REPORTS_COLLECTION_DATA_SOURCE, null);
			out.close();
		}
	}

	private String getDisposition(ReportGenerator gen) {
		return gen.getContentDisposition() == Disposition.INLINE ? "inline" : "attachment";
	}
}
