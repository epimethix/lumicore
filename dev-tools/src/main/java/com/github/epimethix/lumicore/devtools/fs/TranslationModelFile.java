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
package com.github.epimethix.lumicore.devtools.fs;

import java.util.HashMap;
import java.util.Map;

public class TranslationModelFile {

	private String name;

	private String constantsFile;

	private Map<String, String> localeFiles = new HashMap<>();

	private Map<String, Boolean> localeLoaded = new HashMap<>();

	public TranslationModelFile() {}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getConstantsFile() {
		return constantsFile;
	}

	public void setConstantsFile(String constantsFile) {
		this.constantsFile = constantsFile;
	}

	public Map<String, String> getLocaleFiles() {
		return localeFiles;
	}

	public void setLocaleFiles(Map<String, String> localeFiles) {
		this.localeFiles = localeFiles;
	}

	public Map<String, Boolean> getLocaleLoaded() {
		return localeLoaded;
	}

	public void setLocaleLoaded(Map<String, Boolean> localeLoaded) {
		this.localeLoaded = localeLoaded;
	}
}
