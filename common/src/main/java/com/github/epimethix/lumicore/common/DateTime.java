/*
 * Copyright 2023 epimethix@protonmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.epimethix.lumicore.common;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateTime {
	public static final String FORMAT_DD_MM_YYYY_HH_MM = "dd.MM.yyyy HH:mm";
	public static final String FORMAT_DD_MM_YYYY = "dd.MM.yyyy";
	public static final String FORMAT_MM_DD_YYYY_HH_MM = "MM.dd.yyyy HH:mm";
	public static final String FORMAT_MM_DD_YYYY = "MM.dd.yyyy";
	public static final String FORMAT_YYYY_MM_DD_HH_MM = "yyyy.MM.dd HH:mm";
	public static final String FORMAT_YYYY_MM_DD = "yyyy.MM.dd";
	public static final String DEFAULT_DATE_TIME_FORMAT = FORMAT_DD_MM_YYYY_HH_MM;
	public static final String DEFAULT_DATE_FORMAT = FORMAT_DD_MM_YYYY;
	public static final String[] DATE_FORMATS = { FORMAT_DD_MM_YYYY, FORMAT_MM_DD_YYYY, FORMAT_YYYY_MM_DD };
	public static final String[] DATE_TIME_FORMATS = { FORMAT_DD_MM_YYYY_HH_MM, FORMAT_MM_DD_YYYY_HH_MM,
			FORMAT_YYYY_MM_DD_HH_MM };

	public static DateTimeFormatter getDefaultDateTimeFormatter() {
		return DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
	}

	public static DateTimeFormatter getDefaultDateFormatter() {
		return DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
	}

	public static String formatDefaultMillis(long t, DateTimeFormatter formatter) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(t), ZoneId.systemDefault()).format(formatter);
	}

	public static LocalDate localDateOfMillis(long t) {
		return LocalDate.ofInstant(Instant.ofEpochMilli(t), ZoneId.systemDefault());
	}
	public static LocalDateTime localDateTimeOfMillis(long t) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(t), ZoneId.systemDefault());
	}

	public static long toMillis(LocalDateTime t) {
		return t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	public static long toMillis(LocalDate date) {
		return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	private DateTime() {}
}
