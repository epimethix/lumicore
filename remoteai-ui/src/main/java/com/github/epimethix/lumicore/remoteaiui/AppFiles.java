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

import com.github.epimethix.lumicore.common.ApplicationUtils;

public final class AppFiles {
	public static final File NESTING_DIR = ApplicationUtils.getNestingDirectory("RemoteAI-UI");
	public static final File RUNNING_DIR = new File(ApplicationUtils.getRunningDirectory(AppFiles.class));
	public static final File PROPERTIES_FILE = new File(NESTING_DIR, "profile.properties");
	public static final File DATABASE_FILE = new File(NESTING_DIR, "db.sqlite");

	private AppFiles() {}
}
