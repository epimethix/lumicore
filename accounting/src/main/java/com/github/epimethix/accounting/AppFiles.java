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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.epimethix.lumicore.common.ApplicationUtils;

public final class AppFiles {
	public static final File APP_DIR = ApplicationUtils.getNestingDirectory("Lumicore-Accounting");
	public static final File DB_FILE = new File(APP_DIR, "ExampleApp.db");
	public static final File SETTINGS_FILE = new File(APP_DIR, "settings.properties");
	public static final File PROFILE_FILE = new File("profile.properties");
	public static final File LOCK_FILE = new File(APP_DIR, ".ExampleAppLock");
	public static final File LOG_DIR = new File(APP_DIR, "log");
	public static final File ERROR_LOG_FILE = new File(LOG_DIR, "error.log");
	public static final File DIAGNOSTICS_LOG_FILE = new File(LOG_DIR, "diagnostics.log");
	public static final File FRAMEWORK_DIAGNOSTICS_LOG_FILE  = new File(LOG_DIR, "framework_diagnostics.log");
	public static final File CHANNEL_ORM_LOG_FILE = new File(LOG_DIR, "channel_orm.log");
	public static final Path MESSAGES_DIR = Paths.get(APP_DIR.getPath(), "ipc-messages");

	private AppFiles() {}
}
