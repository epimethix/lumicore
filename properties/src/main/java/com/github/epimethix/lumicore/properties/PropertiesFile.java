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
package com.github.epimethix.lumicore.properties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.ReadOnlyFileSystemException;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import com.github.epimethix.lumicore.stackutil.StackUtils;

/**
 * This class tries to emulate a similar experience of obtaining properties
 * files like ResourceBundle does but for a single properties file.
 */
public class PropertiesFile {

//	private final static Logger LOGGER = Log.getLogger();

	/**
	 * searches a properties file.
	 * <p>
	 * propertiesName search is looking for:
	 * <p>
	 * 1. the properties file relative to the project directory
	 * <p>
	 * 2. the properties files full path
	 * <p>
	 * 3. the properties file in the same package as the caller class
	 * <p>
	 * 3. the properties file in the default package on the classpath
	 * 
	 * 
	 * @param propertiesName the properties file name to search for, with or without
	 *                       the file extension ".properties", may also be a
	 *                       relative or full path.
	 * @return the {@code PropertiesFile}
	 * @throws FileNotFoundException
	 */
	public static PropertiesFile getProperties(String propertiesName) throws FileNotFoundException {
		String fileName;
		if (!propertiesName.endsWith(".properties")) {
			fileName = propertiesName + ".properties";
		} else {
			fileName = propertiesName;
		}
		{
			File f = new File(fileName);
			if (f.exists()) {
				try {
					PropertiesFile p = new PropertiesFile(f);
//					LOGGER.trace("Loaded from specified (absolute or relative) file path");
					return p;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Class<?> callerClass = StackUtils.getCallerClass();
		{
			String executionPath = callerClass.getProtectionDomain().getCodeSource().getLocation().getPath();
			File f = new File(executionPath, fileName);
			if (f.exists()) {
				try {
					PropertiesFile p = new PropertiesFile(f);
//					LOGGER.trace("Loaded '%s' from execution directory", fileName);
					return p;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try (InputStream in = callerClass.getResourceAsStream(fileName)) {
			if (Objects.nonNull(in)) {
				PropertiesFile p = new PropertiesFile(in);
//					LOGGER.trace("Loaded '%s' from relative stream", fileName);
				return p;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try (InputStream in = callerClass.getResourceAsStream("/" + fileName)) {
			if (Objects.nonNull(in)) {
				PropertiesFile p = new PropertiesFile(in);
//					LOGGER.trace("Loaded '%s' from absolute stream", fileName);
				return p;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new FileNotFoundException(String.format("Properties file '%s' not found", new File(fileName).getName()));
	}

	private final Properties properties;

	private final File propertiesFile;

	private final Charset charset;

	private final String fileComment;

	private final boolean readOnly;

	public PropertiesFile(File propertiesFile) throws IOException {
		this(propertiesFile, StandardCharsets.UTF_8, null);
	}

	public PropertiesFile(File propertiesFile, String fileComment) throws IOException {
		this(propertiesFile, StandardCharsets.UTF_8, fileComment);
	}

	public PropertiesFile(File propertiesFile, Charset charset) throws IOException {
		this(propertiesFile, charset, null);
	}

	public PropertiesFile(File propertiesFile, Charset charset, String fileComment) throws IOException {
		this(propertiesFile, charset, fileComment, null);
	}

	public PropertiesFile(File propertiesFile, Charset charset, String fileComment, Properties defaults)
			throws IOException {
		this.fileComment = fileComment;
		if (Objects.nonNull(defaults)) {
			this.properties = new Properties(defaults);
		} else {
			this.properties = new Properties();
		}
		this.propertiesFile = propertiesFile;
		this.charset = charset;
		this.readOnly = false;
//		LOGGER.trace("Properties File Path: %s", propertiesFile.getAbsolutePath());
		load();
	}

	public PropertiesFile(InputStream in) throws IOException {
		this.properties = new Properties();
		this.fileComment = null;
		this.propertiesFile = null;
		this.charset = null;
		this.readOnly = true;
//		LOGGER.trace("Properties loaded from InputStream: %s", in.toString());
		load(in);
	}

	public final Set<Object> keySet() {
		return properties.keySet();
	}

	public final void setProperty(String key, String value) throws IOException {
		properties.setProperty(key, value);
		store();
	}

	public final String getProperty(String key) {
		return getProperty(key, null);
	}

	public final String getProperty(String key, String defaultValue) {
		return properties.getProperty(key, defaultValue);
	}

	public final void setProperty(String key, int value) throws IOException {
		properties.setProperty(key, String.valueOf(value));
		store();
	}

	public int getIntProperty(String key) {
		return getIntProperty(key, 0);
	}

	public int getIntProperty(String key, int defaultValue) {
		String val = getProperty(key, String.valueOf(defaultValue));
		int result = defaultValue;
		try {
			result = Integer.parseInt(val);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return result;
	}

	public final void setProperty(String key, long value) throws IOException {
		properties.setProperty(key, String.valueOf(value));
		store();
	}

	public long getLongProperty(String key) {
		return getLongProperty(key, 0L);
	}

	public long getLongProperty(String key, long defaultValue) {
		String val = getProperty(key, String.valueOf(defaultValue));
		long result = defaultValue;
		try {
			result = Long.parseLong(val);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return result;
	}

	public final void setProperty(String key, double value) throws IOException {
		properties.setProperty(key, String.valueOf(value));
		store();
	}

	public double getDoubleProperty(String key) {
		return getDoubleProperty(key, 0.0d);
	}

	public double getDoubleProperty(String key, double defaultValue) {
		String val = getProperty(key, String.valueOf(defaultValue));
		double result = defaultValue;
		try {
			result = Double.parseDouble(val);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return result;
	}

	public final void setProperty(String key, float value) throws IOException {
		properties.setProperty(key, String.valueOf(value));
		store();
	}

	public float getFloatProperty(String key) {
		return getFloatProperty(key, 0.0f);
	}

	public float getFloatProperty(String key, float defaultValue) {
		String val = getProperty(key, String.valueOf(defaultValue));
		float result = defaultValue;
		try {
			result = Float.parseFloat(val);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return result;
	}

	public final void setProperty(String key, Locale value) throws IOException {
		properties.setProperty(key, value.toLanguageTag());
		store();
	}

	public Locale getLocaleProperty(String key) {
		return getLocaleProperty(key, Locale.ENGLISH);
	}

	public Locale getLocaleProperty(String key, Locale defaultValue) {
		String val = getProperty(key, defaultValue.toLanguageTag());
		Locale result = defaultValue;
		try {
			result = Locale.forLanguageTag(val);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return result;
	}

	public boolean containsKey(String key) {
		return properties.containsKey(key);
	}

	public String getString(String key) {
		return properties.getProperty(key);
	}

	public final void load(InputStream in) throws IOException {
		this.properties.load(in);
	}

	public final void load() throws IOException {
		try (Reader in = new FileReader(propertiesFile, charset)) {
			this.properties.load(in);
		} catch (FileNotFoundException e) {}
	}

	public final void store() throws IOException {
		store(fileComment);
	}

	public final void store(String fileComment) throws IOException {
		if (readOnly) {
			throw new ReadOnlyFileSystemException();
		}
		try (Writer out = new FileWriter(propertiesFile, charset)) {
			this.properties.store(out, fileComment);
		}
	}

	public boolean exists() {
		if (Objects.nonNull(propertiesFile)) {
			return propertiesFile.exists();
		} else {
			return false;
		}
	}

	public File getFile() {
		return propertiesFile;
	}

	@Override
	public String toString() {
		return String.format("Properties file '%s' (%sexisting) using charset '%s'", propertiesFile.getPath(),
				propertiesFile.exists() ? "" : "not ", charset.toString());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((charset == null) ? 0 : charset.hashCode());
		result = prime * result + ((fileComment == null) ? 0 : fileComment.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((propertiesFile == null) ? 0 : propertiesFile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PropertiesFile other = (PropertiesFile) obj;
		if (charset == null) {
			if (other.charset != null)
				return false;
		} else if (!charset.equals(other.charset))
			return false;
		if (fileComment == null) {
			if (other.fileComment != null)
				return false;
		} else if (!fileComment.equals(other.fileComment))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (propertiesFile == null) {
			if (other.propertiesFile != null)
				return false;
		} else if (!propertiesFile.equals(other.propertiesFile))
			return false;
		return true;
	}
}
