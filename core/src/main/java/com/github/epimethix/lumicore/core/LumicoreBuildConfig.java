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
package com.github.epimethix.lumicore.core;

import java.util.ResourceBundle;

public class LumicoreBuildConfig {

	public static final String NAME;
	public static final String VERSION;
	public static final long BUILD;
	public static final long BUILD_MILLIS;
	
	static {
		ResourceBundle buildProperties = ResourceBundle.getBundle("lumicore-build");
//		project-name=sqlite-lumicore
		NAME = buildProperties.getString("project-name");
//		project-version=0.3a
		VERSION = buildProperties.getString("project-version");
//		build-number=6
		BUILD = Long.parseLong(buildProperties.getString("build-number"));
//		build-time=1655304923111
		BUILD_MILLIS = Long.parseLong(buildProperties.getString("build-time"));
//		System.err.printf("");
//		System.err.println("");
	}
}
