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
package com.github.exporthelper.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.exporthelper.core.configuration.ColumnConfig;
import com.github.exporthelper.core.html.HtmlColumn;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.exporthelper.core.html.HtmlCaption;
import com.github.exporthelper.core.html.HtmlTable;

/**
 * <p>
 * Builder used to create instances of {@link HtmlTable}. This builder is mainly
 * used as an export utility and for testing.
 * </p>
 * <p>For example, considering the following simple {@code Person} class:
 * <pre>
 * public class Person {
 *    private Long id;
 *    private String firstName;
 *    private String lastName;
 *    private String mail;
 *    private Date birthDate;
 * 
 *    // Accessors...
 * }
 * </pre>
 * </p>
 * The builder allows to create fully configured instance of {@link HtmlTable} as follows:
 * <pre>
 * HtmlTable table = new HtmlTableBuilder&lt;Person&gt;().newBuilder("yourTableId", persons, request)
 *    .column().fillWithProperty("id").title("Id")
 *    .column().fillWithProperty("firstName").title("Firtname")
 *    .column().fillWithProperty("lastName").title("Lastname")
 *    .column().fillWithProperty("mail").title("Mail")
 *    .column().fillWithProperty("birthDate", "{0,date,dd-MM-yyyy}").title("BirthDate")
 *    .build();
 * </pre>
 * where:
 * <ul>
 * <li>{@code yourTableId} is the HTML id that has be assigned to the {@code table} tag</li>
 * <li>{@code persons} is a collection of {@code Person}</li>
 * <li>{@code request} is the current {@link HttpServletRequest}</li>
 * </ul>
 * 
 * @author Thibault Duchateau
 * @since 0.9.0
 */
public class HtmlTableBuilder<T> {

	// Logger
	private static Logger logger = LoggerFactory.getLogger(HtmlTableBuilder.class);

	public ColumnStep newBuilder(String id, List<T> data,
			HttpServletRequest request) {
		return new Steps<T>(id, null, data, request);
	}

	public ColumnStep newBuilder(String id, List<T> data, HttpServletRequest request, ExportConf exportConf) {
		return new Steps<T>(id, null, data, request, exportConf);
	}
	
	public ColumnStep newBuilder(String id, String title, List<T> data,
			HttpServletRequest request) {
		return new Steps<T>(id, title, data, request);
	}

	public ColumnStep newBuilder(String id, String title, List<T> data,
			HttpServletRequest request, ExportConf exportConf) {
		return new Steps<T>(id, title, data, request, exportConf);
	}

	public static interface ColumnStep {
		FirstContentStep column();
	}

	public static interface FirstContentStep {
		SecondContentStep fillWithProperty(String propertyName);
		SecondContentStep fillWithProperty(String propertyName, String pattern);
		SecondContentStep fillWithProperty(String propertyName, String pattern, String defaultContent);
		SecondContentStep fillWith(String content);
	}

	public static interface SecondContentStep {
		SecondContentStep andProperty(String propertyName);
		SecondContentStep andProperty(String propertyName, String pattern);
		SecondContentStep andProperty(String propertyName, String pattern, String defaultContent);
		SecondContentStep and(String content);
		BuildStep title(String title);
	}

	public static interface TitleStep extends ColumnStep {
		ColumnStep title(String title);
	}

	public static interface BuildStep {
		HtmlTable build();
		FirstContentStep column();
	}

	private static class Steps<T> implements ColumnStep, FirstContentStep, SecondContentStep, BuildStep {

		private String id;
		private String title;
		private List<T> data;
		private LinkedList<HtmlColumn> headerColumns = new LinkedList<HtmlColumn>();
		private HttpServletRequest request;
		private HttpServletResponse response;
		private ExportConf exportConf;

		public Steps(String id, String title, List<T> data,
				HttpServletRequest request) {
			this(id, title, data, request, null);
		}

		public Steps(String id, String title, List<T> data,
				HttpServletRequest request, ExportConf exportConf) {
			this.id = id;
			this.title = title;
			this.data = data;
			this.request = request;
			this.exportConf = exportConf;
		}
		
		// Table configuration

		@Override
		public Steps<T> column() {
			HtmlColumn column = new HtmlColumn(true, "");
			headerColumns.add(column);
			return this;
		}

		@Override
		public Steps<T> title(String title) {
			ColumnConfig.TITLE.setIn(headerColumns.getLast().getColumnConfiguration(), title);
			return this;
		}

		/**
		 * Add a new column to the table and complete it using the passed
		 * property. Convenient if you need to display a single property in the
		 * column.
		 * 
		 * @param property
		 *            name of the (possibly nested) property of the bean which
		 *            is part of the collection being iterated on.
		 */
		@Override
		public Steps<T> fillWithProperty(String property) {
			return fillWithProperty(property, null, "");
		}

		@Override
		public Steps<T> fillWithProperty(String property, String pattern) {
			return fillWithProperty(property, pattern, "");
		}

		@Override
		public Steps<T> fillWithProperty(String property, String pattern, String defaultContent) {
			if (headerColumns.getLast().getColumnConfiguration().getColumnElements() == null) {
				headerColumns.getLast().getColumnConfiguration().setColumnElements(new ArrayList<ColumnElement>());
			}
			headerColumns.getLast().getColumnConfiguration().getColumnElements()
					.add(new ColumnElement(property, pattern, "", defaultContent));
			return this;
		}

		@Override
		public Steps<T> fillWith(String content) {
			if (headerColumns.getLast().getColumnConfiguration().getColumnElements() == null) {
				headerColumns.getLast().getColumnConfiguration().setColumnElements(new ArrayList<ColumnElement>());
			}
			headerColumns.getLast().getColumnConfiguration().getColumnElements()
					.add(new ColumnElement(null, null, content, null));
			return this;
		}

		@Override
		public Steps<T> andProperty(String property) {
			return andProperty(property, null, "");
		}

		@Override
		public Steps<T> andProperty(String property, String pattern) {
			return andProperty(property, pattern, "");
		}

		@Override
		public Steps<T> andProperty(String property, String pattern, String defaultContent) {
			if (headerColumns.getLast().getColumnConfiguration().getColumnElements() == null) {
				headerColumns.getLast().getColumnConfiguration().setColumnElements(new ArrayList<ColumnElement>());
			}
			headerColumns.getLast().getColumnConfiguration().getColumnElements()
					.add(new ColumnElement(property, pattern, null, defaultContent));

			return this;
		}

		@Override
		public Steps<T> and(String content) {
			if (headerColumns.getLast().getColumnConfiguration().getColumnElements() == null) {
				headerColumns.getLast().getColumnConfiguration().setColumnElements(new ArrayList<ColumnElement>());
			}
			headerColumns.getLast().getColumnConfiguration().getColumnElements()
					.add(new ColumnElement(null, null, content, null));
			return this;
		}

		@Override
		public HtmlTable build() {
			HtmlTable table = new HtmlTable(id, request, response);
			if (StringUtils.isNotBlank(title)) {
				HtmlCaption caption = new HtmlCaption();
				caption.setTitle(title);
				table.setCaption(caption);
			}
			
			table.getExportConfiguration().put(exportConf.getFormat(),
					exportConf);
			// 把文件扩展名也加入到导出配置
			//			if (StringUtils.isNotBlank(exportConf.getFileExtension())
			//					&& !exportConf.getFormat().equals(
			//							exportConf.getFileExtension())) {
			//				table.getExportConfiguration().put(
			//						exportConf.getFileExtension(), exportConf);
			//			}

			//			if (data != null && data.size() > 0) {
			//				TableConfig.INTERNAL_OBJECTTYPE.setIn(table.getTableConfiguration(), data.get(0).getClass().getSimpleName());
			//			} else {
			//				TableConfig.INTERNAL_OBJECTTYPE.setIn(table.getTableConfiguration(), "???");
			//			}

			table.addHeaderRow();

			for (HtmlColumn column : headerColumns) {
				String title = ColumnConfig.TITLE.valueFrom(column.getColumnConfiguration());
				if (StringUtils.isNotBlank(title)) {
					column.setContent(new StringBuilder(title));
				} else {
					column.setContent(new StringBuilder(""));
				}
				table.getLastHeaderRow().addColumn(column);
			}

			if (data != null) {

				for (T o : data) {

					table.addRow();
					for (HtmlColumn column : headerColumns) {

						String content = "";
						for (ColumnElement columnElement : column.getColumnConfiguration().getColumnElements()) {

							if (StringUtils.isNotBlank(columnElement.getPropertyName())) {
								try {
									Object tmpObject = PropertyUtils.getNestedProperty(o, columnElement
											.getPropertyName().trim());

									if (tmpObject != null && StringUtils.isNotBlank(columnElement.getPattern())) {
										MessageFormat messageFormat = new MessageFormat(columnElement.getPattern());
										content += messageFormat.format(new Object[] { tmpObject });
									} else {
										content += tmpObject == null ? ""
												: String.valueOf(tmpObject);
									}
								} catch (Exception e) {
									logger.warn("Something went wrong with the property {}. Check that an accessor method for this property exists in the bean.");
									content += columnElement.getDefaultValue();
								}
							} else if (columnElement.getContent() != null) {
								content += columnElement.getContent();
							} else {
								content += columnElement.getDefaultValue();
							}
						}

						table.getLastBodyRow().addColumn(String.valueOf(content));
					}
				}
			}

			return table;
		}
	}
}