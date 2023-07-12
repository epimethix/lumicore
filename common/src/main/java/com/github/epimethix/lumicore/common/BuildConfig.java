/*
 * Copyright 2022 epimethix@protonmail.com
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

import java.io.FileNotFoundException;
import java.util.Objects;

import com.github.epimethix.lumicore.properties.PropertiesFile;

public class BuildConfig {

	private final static String NAME_KEY = "name";
	private final static String GROUP_KEY = "group";
	private final static String VERSION_KEY = "version";
	private final static String TIME_KEY = "time";
	private final static String BUILD_KEY = "build";

	public final static String NAME;
	public final static String GROUP;
	public final static String VERSION;
	public final static long TIME;
	public final static long BUILD;

	static {
		PropertiesFile buildConfig = null;
		try {
			buildConfig = PropertiesFile.getProperties("build");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String name = null;
		String group = null;
		String version = null;
		long time = 0;
		long build = 0;
		if (Objects.nonNull(buildConfig)) {
			name = buildConfig.getProperty(NAME_KEY, null);
			group = buildConfig.getProperty(GROUP_KEY, null);
			version = buildConfig.getProperty(VERSION_KEY, null);
			String timeStr = buildConfig.getProperty(TIME_KEY, null);
			if (Objects.nonNull(timeStr)) {
				try {
					time = Long.parseLong(timeStr);
				} catch (NumberFormatException e) {}
			}
			String buildStr = buildConfig.getProperty(BUILD_KEY, null);
			if (Objects.nonNull(buildStr)) {
				try {
					build = Long.parseLong(buildStr);
				} catch (NumberFormatException e) {}
			}
		}
		NAME = name;
		GROUP = group;
		VERSION = version;
		TIME = time;
		BUILD = build;
	}

	private BuildConfig() {}
}
