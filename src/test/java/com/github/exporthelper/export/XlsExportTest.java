package com.github.exporthelper.export;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.github.exporthelper.core.ExportConf;
import com.github.exporthelper.core.ExportUtils;
import com.github.exporthelper.core.HtmlTableBuilder;
import com.github.exporthelper.core.html.HtmlTable;
import com.github.exporthelper.extras.poi.XlsExport;
import com.github.exporthelper.mock.Mock;
import com.github.exporthelper.mock.Person;

/**
 * @author chufucun chufucun@cyou-inc.com
 * 
 * version <br>
 * Copyright (C) 2013-2014 boco <br>
 *           This program is protected by copyright laws. <br>
 *           Program Name:MOKA导出.
 * <br>
 *
 * Description: 导出测试.
 * 
 *
 * CreateTime: 2014年8月27日下午4:48:53
 *
 * Change History:
 *
 *        Date             CR Number              Name              Description of change
 *
 *
 */
public class XlsExportTest {

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	protected HtmlTable table;

	@Before
	public void setup() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}

	@Test
	public void testExport() {

		// Get data to export
		List<Person> persons = Mock.persons;

		// Build the export configuration
		ExportConf myExportConf = new ExportConf.Builder("xls")
				.header(true).exportClass(new XlsExport())
				.build();

		// Build the table to export from the data and the export configuration
		HtmlTable table = new HtmlTableBuilder<Person>()
				.newBuilder("tableId", persons, null, myExportConf).column()
				.fillWithProperty("id").title("Id").column()
				.fillWithProperty("firstName").title("Firtname").column()
				.fillWithProperty("lastName").title("Lastname").column()
				.fillWithProperty("address.town.name").title("City").column()
				.fillWithProperty("mail").title("Mail").column()
				.fillWithProperty("birthDate", "{0,date,yyyy-mm-DD}")
				.title("BirthDate").build();

		// Render the export in the browser
		try {
			File file = ExportUtils.renderExport(table, myExportConf, new File(
					"D://"));
			System.out.println(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}