/*
 * [The "BSD licence"]
 * Copyright (c) 2012 Dandelion
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Dandelion nor the names of its contributors 
 * may be used to endorse or promote products derived from this software 
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.exporthelper.export;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Scanner;

import org.junit.Test;
import org.springframework.util.StringUtils;

import com.github.exporthelper.core.CsvExport;
import com.github.exporthelper.core.ExportConf;
import com.github.exporthelper.mock.Mock;

/**
 * Test the CSV export.
 *
 * @author Thibault Duchateau
 */
public class CsvExportTest extends ExportTest {

	@Test
	public void should_generate_table_with_header() {
		
		initDefaultTable();
		configureExport(new ExportConf.Builder("csv").header(true).build());
		processExport(new CsvExport());
		
		System.err.println(new String(baos.toByteArray()));
		// The header must exist
		String firstLine = new Scanner(new String(baos.toByteArray())).nextLine();
		assertThat(firstLine).contains("Id;FirstName;LastName;City;Mail");
		
		// There must be as many line as entries in Mock.persons (+1 for the header row)
		assertThat(new String(baos.toByteArray()).split("\n")).hasSize(Mock.persons.size() + 1);
	}
	
	@Test
	public void should_generate_table_without_header() {
		
		initDefaultTable();
		configureExport(new ExportConf.Builder("csv").header(false).build());
		processExport(new CsvExport());
		
		// The header must exist
		String firstLine = new Scanner(new String(baos.toByteArray())).nextLine();
		assertThat(firstLine).doesNotContain("Id;FirstName;LastName;City;Mail");
		
		// There must be as many line as entries in Mock.persons (+1 for the header row)
		assertThat(new String(baos.toByteArray()).split("\n")).hasSize(Mock.persons.size());
	}
	
	@Test
	public void should_generate_table_with_4_columns() {
		
		initTable();
		configureExport(new ExportConf.Builder("csv").header(false).build());
		processExport(new CsvExport());
		
		String firstLine = new Scanner(new String(baos.toByteArray())).nextLine();
		assertThat(StringUtils.countOccurrencesOf(firstLine, ";")).isEqualTo(4);
	}
}