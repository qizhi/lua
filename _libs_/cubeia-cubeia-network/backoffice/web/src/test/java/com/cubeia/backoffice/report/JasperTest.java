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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Assert;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.junit.Test;

public class JasperTest {

    public class TestUser {
        private long id;
        private String name;
        
        public TestUser(long id, String name) {
            super();
            this.id = id;
            this.name = name;
        }
        
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }

    public class KeyValue {
        private String key;
        private Integer value;

        public KeyValue(String key, Integer value) {
            super();
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }
        
        public void setKey(String key) {
            this.key = key;
        }
        
        public Integer getValue() {
            return value;
        }
        
        public void setValue(Integer value) {
            this.value = value;
        }
    }
    
    @SuppressWarnings("rawtypes")
	@Test
    public void generateReport() throws JRException {
        InputStream inReport = getClass().getResourceAsStream("/reports/test.jrxml");
        Assert.assertNotNull(inReport);
        
        JasperReport jasperReport = JasperCompileManager.compileReport(inReport);

        ArrayList<TestUser> users = new ArrayList<TestUser>();
        for (int i = 0; i < 1000; i++) {
            users.add(new TestUser(1234 + i, "snubbe-" + i));
        }
        
        JRDataSource jasperDS = new JRBeanCollectionDataSource(users);
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap(), jasperDS);
        JasperExportManager.exportReportToPdfFile(jasperPrint, "target/test_report.pdf");
//        JasperExportManager.exportReportToHtmlFile(jasperPrint, "target/test_report.html");
        
        Assert.assertTrue(new File("target/test_report.pdf").exists());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void generateReportWithGraph() throws JRException {
        InputStream inReport = getClass().getResourceAsStream("/reports/test-graph.jrxml");
        Assert.assertNotNull(inReport);
        
        JasperReport jasperReport = JasperCompileManager.compileReport(inReport);

        ArrayList<KeyValue> values = new ArrayList<KeyValue>();
        for (int i = 0; i < 10; i++) {
            values.add(new KeyValue("key-" + i, (int) (100 * Math.random())));
        }
        
        JRDataSource jasperDS = new JRBeanCollectionDataSource(values);
        
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, new HashMap(), jasperDS);
        JasperExportManager.exportReportToPdfFile(jasperPrint, "target/test_graph_report.pdf");
        
        Assert.assertTrue(new File("target/test_graph_report.pdf").exists());
    }
    
    
}
