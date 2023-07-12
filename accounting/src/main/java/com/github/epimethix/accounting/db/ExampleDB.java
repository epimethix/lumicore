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
package com.github.epimethix.accounting.db;

import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.DatabaseApplication;
import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;
import com.github.epimethix.lumicore.orm.SQLDatabase;

public class ExampleDB extends SQLDatabase {

	Logger LOGGER = Log.getLogger();

	public final static long STRUCTURE_VERSION = 1L;

	/**
	 * Custom Meta data field "run-count".
	 */
	private static final String META_RUN_COUNT = "run-count";

	public ExampleDB(DatabaseApplication databaseApplication) throws ConfigurationException {
		super(databaseApplication);
		/*
		 * the first time `incrementRunCount()` is called it will set run-count to `1L`.
		 * since the value does not exist yet `getRunCount()` will return `0L`.
		 */
		incrementRunCount();
		long runCount = getRunCount();
		if (runCount < 2) {
			LOGGER.info("ExampleDB is being loaded for the first time");
			/*
			 * The next time the database will be loaded will be the 2nd time
			 */
		} else {
			LOGGER.info("ExampleDB is being loaded for the %d. time", runCount);
		}
	}

	/*
	 * getter/setters for custom meta data field "run-count"
	 */

	public final long getRunCount() {
		return getIntegerMeta(META_RUN_COUNT);
	}

	public final void setRunCount(long runCount) {
		saveIntegerMeta(META_RUN_COUNT, runCount);
	}

	public final void incrementRunCount() {
		setRunCount(getRunCount() + 1);
	}
}