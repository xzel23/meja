/*
 * Copyright 2015 Axel Howind <axel@dua3.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.meja.model.poi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Workbook;

import com.dua3.meja.io.FileFormatException;
import com.dua3.meja.model.WorkbookFactory;
import com.dua3.meja.model.poi.PoiWorkbook.PoiHssfWorkbook;
import com.dua3.meja.model.poi.PoiWorkbook.PoiXssfWorkbook;

/**
 *
 * @author axel
 */
public final class PoiWorkbookFactory extends WorkbookFactory {

	private static final PoiWorkbookFactory INSTANCE = new PoiWorkbookFactory();

	public static PoiWorkbookFactory instance() {
		return INSTANCE;
	}

	private PoiWorkbookFactory() {
	}

	@Override
	public PoiWorkbook<?, ?, ?, ?, ?, ?> open(File file) throws IOException {
		try {
			Locale locale = Locale.getDefault();
			final Workbook poiWorkbook = org.apache.poi.ss.usermodel.WorkbookFactory
					.create(file);
			return createWorkbook(poiWorkbook, locale);
		} catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException ex) {
			throw new FileFormatException(ex.getMessage());
		}
	}

	public PoiWorkbook<?, ?, ?, ?, ?, ?> open(InputStream in)
			throws IOException {
		return open(in, Locale.getDefault());
	}

	public PoiWorkbook<?, ?, ?, ?, ?, ?> open(InputStream in, Locale locale)
			throws IOException {
		try {
			final Workbook poiWorkbook = org.apache.poi.ss.usermodel.WorkbookFactory
					.create(in);
			return createWorkbook(poiWorkbook, locale);
		} catch (org.apache.poi.openxml4j.exceptions.InvalidFormatException ex) {
			throw new FileFormatException(ex.getMessage());
		}
	}

	public PoiWorkbook<?, ?, ?, ?, ?, ?> open(URI uri) throws IOException {
		return open(uri, Locale.getDefault());
	}

	public PoiWorkbook<?, ?, ?, ?, ?, ?> open(URI uri, Locale locale)
			throws IOException {
		return open(uri.toURL(), locale);
	}

	public PoiWorkbook<?, ?, ?, ?, ?, ?> open(URL url) throws IOException {
		return open(url, Locale.getDefault());
	}

	public PoiWorkbook<?, ?, ?, ?, ?, ?> open(URL url, Locale locale)
			throws IOException {
		try (InputStream in = new BufferedInputStream(url.openStream())) {
			return open(in, locale);
		}
	}

	@Override
	public PoiWorkbook<?, ?, ?, ?, ?, ?> create() {
		return createXls(Locale.getDefault());
	}

	public PoiHssfWorkbook createXls() {
		return createXls(Locale.getDefault());
	}

	public PoiHssfWorkbook createXls(Locale locale) {
		return new PoiHssfWorkbook(
				new org.apache.poi.hssf.usermodel.HSSFWorkbook(), locale);
	}

	public PoiXssfWorkbook createXlsx() {
		return createXlsx(Locale.getDefault());
	}

	public PoiXssfWorkbook createXlsx(Locale locale) {
		return new PoiXssfWorkbook(
				new org.apache.poi.xssf.usermodel.XSSFWorkbook(), locale);
	}

	private PoiWorkbook<?, ?, ?, ?, ?, ?> createWorkbook(
			final Workbook poiWorkbook, Locale locale) {
		if (poiWorkbook instanceof org.apache.poi.hssf.usermodel.HSSFWorkbook) {
			return new PoiHssfWorkbook(
					(org.apache.poi.hssf.usermodel.HSSFWorkbook) poiWorkbook,
					locale);
		} else if (poiWorkbook instanceof org.apache.poi.xssf.usermodel.XSSFWorkbook) {
			return new PoiXssfWorkbook(
					(org.apache.poi.xssf.usermodel.XSSFWorkbook) poiWorkbook,
					locale);
		} else {
			throw new IllegalStateException();
		}
	}

}
