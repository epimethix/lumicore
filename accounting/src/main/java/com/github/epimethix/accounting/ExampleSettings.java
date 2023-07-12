/*
 *  Accounting - Lumicore example application
 *  Copyright (C) 2023  epimethix@protonmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.epimethix.accounting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.epimethix.lumicore.properties.PropertiesFile;

public class ExampleSettings {

	private final static String FILE_COMMENT = "Example Settings Comment";

	private final String KEY_LOCALE = "locale";

	private final PropertiesFile propertiesFile;

	public ExampleSettings() throws IOException {
		this.propertiesFile = new PropertiesFile(AppFiles.SETTINGS_FILE, StandardCharsets.UTF_8);
	}

	public final Locale getDefaultLocale() {
		Locale defaultLocale = Locale.ENGLISH;
		String property = propertiesFile.getProperty(KEY_LOCALE, null);
		if (Objects.nonNull(property)) {
			defaultLocale = Locale.forLanguageTag(property);
		}
		return defaultLocale;
	}

	public final void setDefaultLocale(Locale defaultLocale) {
		try {
			propertiesFile.setProperty(KEY_LOCALE, defaultLocale.toLanguageTag());
			propertiesFile.store(FILE_COMMENT);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
