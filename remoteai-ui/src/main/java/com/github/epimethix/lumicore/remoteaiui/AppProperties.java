/*
 *  RemoteAI UI - Lumicore example application
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
package com.github.epimethix.lumicore.remoteaiui;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import com.github.epimethix.lumicore.properties.PropertiesFile;

public final class AppProperties {

	private final static String DEFAULT_LOCALE = "DEFAULT_LOCALE";

	private final static PropertiesFile PROPERTIES;

	static {
		PropertiesFile pf = null;
		try {
			if (!AppFiles.PROPERTIES_FILE.exists()) {
				AppFiles.PROPERTIES_FILE.createNewFile();
			}
			pf = PropertiesFile.getProperties(AppFiles.PROPERTIES_FILE.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(Objects.isNull(pf)) {
			System.err.println("Fatal: application properties could not be loaded!");
			System.exit(100);
		}
		PROPERTIES = pf;
	}

	AppProperties() {}

	public void setDefaultLocale(Locale locale) {
		try {
			PROPERTIES.setProperty(DEFAULT_LOCALE, locale.toLanguageTag());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Locale getDefaultLocale() {
		return Locale.forLanguageTag(PROPERTIES.getProperty(DEFAULT_LOCALE, Locale.ENGLISH.toLanguageTag()));
	}

	public File getFile() {
		return PROPERTIES.getFile();
	}
}
