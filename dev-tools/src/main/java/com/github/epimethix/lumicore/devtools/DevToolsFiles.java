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
package com.github.epimethix.lumicore.devtools;

import java.io.File;
import java.nio.file.Path;

import com.github.epimethix.lumicore.common.ApplicationUtils;

public class DevToolsFiles {

	public static final File NESTING_DIR = ApplicationUtils.getNestingDirectory("LumicoreDevTools");

	public static final String RESOURCES_RELATIVE = "src/dev/resources";
	public static final String MAIN_RESOURCES_RELATIVE = "src/main/resources";
	public static final File SRC_ZIP = new File(Path.of(System.getProperty("java.home"), "lib", "src.zip").toString());
	public static final File LOCALE_JSON = new File(NESTING_DIR, "locale.json");

}
