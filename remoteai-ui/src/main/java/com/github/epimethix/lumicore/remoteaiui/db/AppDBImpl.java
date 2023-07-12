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
package com.github.epimethix.lumicore.remoteaiui.db;

import java.util.Optional;

import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.DatabaseApplication;
import com.github.epimethix.lumicore.orm.SQLDatabase;
import com.github.epimethix.lumicore.remoteaiui.RemoteAIUI;
import com.github.epimethix.lumicore.stackutil.AccessCheck;

public final class AppDBImpl extends SQLDatabase implements AppDB {
	private static final String META_API_KEY = "META_API_KEY";
	private static final String META_TEXT_OUTPUT_DIR = "META_TEXT_OUTPUT_DIR";
	private static final String META_IMAGE_OUTPUT_DIR = "META_IMAGE_OUTPUT_DIR";

	public AppDBImpl(DatabaseApplication databaseApplication) throws ConfigurationException {
		super(databaseApplication);
	}

	@Override
	public Optional<String> getApiKey() throws IllegalAccessException {
		AccessCheck.allowCaller(false, RemoteAIUI.class.getName().concat("::getApiKey"));
		return Optional.ofNullable(getMeta(META_API_KEY));
	}

	@Override
	public void setApiKey(String apiKey) {
		saveMeta(META_API_KEY, apiKey);
	}

	@Override
	public void setImageOutputDirectory(String path) {
		saveMeta(META_IMAGE_OUTPUT_DIR, path);
	}

	@Override
	public Optional<String> getImageOutputDirectory() {
		return Optional.ofNullable(getMeta(META_IMAGE_OUTPUT_DIR));
	}

	@Override
	public void setTextOutputDirectory(String path) {
		saveMeta(META_TEXT_OUTPUT_DIR, path);
	}

	@Override
	public Optional<String> getTextOutputDirectory() {
		return Optional.ofNullable(getMeta(META_TEXT_OUTPUT_DIR));
	}
}
