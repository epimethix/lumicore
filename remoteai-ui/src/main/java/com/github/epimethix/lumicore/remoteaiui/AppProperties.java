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

import java.io.IOException;

import com.github.epimethix.lumicore.properties.ApplicationProperties;

public final class AppProperties extends ApplicationProperties {

	AppProperties() {
		super(AppFiles.PROPERTIES_FILE.getPath());
		/*
		 * For Example Purposes the development profile is set. This enables the console
		 * output.
		 */
		// TODO Remove setting development profile by default
		if (!containsKey("active-profile")) {
			try {
				setProperty("active-profile", "dev");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
