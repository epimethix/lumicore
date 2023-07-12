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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import com.github.epimethix.lumicore.properties.PropertiesFile;

final class DevToolsProperties {

	private static DevToolsProperties devToolsProperties;

	static DevToolsProperties getProperties() {
		if (Objects.isNull(devToolsProperties)) {
			devToolsProperties = new DevToolsProperties();
		}
		return devToolsProperties;
	}

	private final static String KEY_SOURCES_DIRECTORY = "sources-directory";
//	private final static String KEY_ENTITIES_PACKAGE = "entities-package";
	private final static String KEY_LABELS_CLASS = "labels-class";
	private final static String KEY_DEFAULT_CHARSET = "default-charset";
	private final static String KEY_PROPERTIES_CHARSET = "properties-charset";
	private final static String KEY_JAVA_CHARSET = "java-charset";
	private final static String KEY_GENERATOR_API_KEY = "generator-api-key";

	private final PropertiesFile propertiesFile;

	private final File sourcesDirectory;

//	private final String entitiesPackage;

	private final Class<?> labelsClass;

	private final Charset defaultCharset;
	private final Charset propertiesCharset;
	private final Charset javaCharset;

	private boolean loaded;

	private DevToolsProperties() {
		PropertiesFile propertiesFile = null;
		File srcDirFile = null;
//		String entitiesPkg = null;
		String labelsClassName = null;
		Class<?> labelsCls = null;
		Charset defCS, propCS, javaCS;
		defCS = propCS = javaCS = StandardCharsets.UTF_8;
		try {
			propertiesFile = new PropertiesFile(new File("lumicore-dev-tools.properties"));
			String srcDir = propertiesFile.getProperty(KEY_SOURCES_DIRECTORY, null);
			if (Objects.nonNull(srcDir) && srcDir.trim().length() > 0) {
				srcDirFile = new File(srcDir);
				loaded = true;
			}
//			entitiesPkg = propertiesFile.getProperty(KEY_ENTITIES_PACKAGE, null);
			labelsClassName = propertiesFile.getProperty(KEY_LABELS_CLASS, null);
//			labelsClassName.rep
			if (Objects.nonNull(labelsClassName) && !labelsClassName.trim().isEmpty()) {
				try {
					labelsCls = Class.forName(labelsClassName);
				} catch (ClassNotFoundException e1) {}
			}
			if (propertiesFile.containsKey(KEY_DEFAULT_CHARSET)) {
				try {
					defCS = Charset.forName(propertiesFile.getProperty(KEY_DEFAULT_CHARSET));
				} catch (Exception e) {
					e.printStackTrace();
					defCS = StandardCharsets.UTF_8;
				}
			} else {
				defCS = StandardCharsets.UTF_8;
			}
			if (propertiesFile.containsKey(KEY_PROPERTIES_CHARSET)) {
				try {
					propCS = Charset.forName(propertiesFile.getProperty(KEY_PROPERTIES_CHARSET));
				} catch (Exception e) {
					e.printStackTrace();
					propCS = defCS;
				}
			} else {
				propCS = defCS;
			}
			if (propertiesFile.containsKey(KEY_JAVA_CHARSET)) {
				try {
					javaCS = Charset.forName(propertiesFile.getProperty(KEY_JAVA_CHARSET));
				} catch (Exception e) {
					e.printStackTrace();
					javaCS = defCS;
				}
			} else {
				javaCS = defCS;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.propertiesFile = propertiesFile;
		this.sourcesDirectory = srcDirFile;
		this.defaultCharset = defCS;
		this.propertiesCharset = propCS;
		this.javaCharset = javaCS;
//		if (Objects.nonNull(entitiesPkg) && entitiesPkg.trim().length() > 0) {
//			this.entitiesPackage = entitiesPkg;
//		} else {
//			this.entitiesPackage = null;
//		}
		this.labelsClass = labelsCls;
	}

	boolean wasLoaded() {
		return loaded;
	}

	File getSourcesDirectory() {
		return sourcesDirectory;
	}

//	String getEntitiesPackage() {
//		return entitiesPackage;
//	}

	Class<?> getLabelsClass() {
		return labelsClass;
	}

	boolean fileExists() {
		return propertiesFile.exists();
	}

	void deploy() {
		try {
			propertiesFile.setProperty(KEY_SOURCES_DIRECTORY, "");
//		propertiesFile.setProperty(KEY_ENTITIES_PACKAGE, "");
			propertiesFile.setProperty(KEY_LABELS_CLASS, "");
//			propertiesFile.store();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Charset getDefaultCharset() {
		return defaultCharset;
	}

	Charset getJavaCharset() {
		return javaCharset;
	}

	Charset getPropertiesCharset() {
		return propertiesCharset;
	}
	
	Optional<String> getGeneratorApiKey(){
		return Optional.ofNullable(propertiesFile.getProperty(KEY_GENERATOR_API_KEY, null));
	}
	
	void setGeneratorApiKey(String key) {
		try {
			propertiesFile.setProperty(KEY_GENERATOR_API_KEY, key);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
