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
	 * 1. the properties file relative to the project directory (R/W)
	 * <p>
	 * 2. the properties files full path (R/W)
	 * <p>
	 * 3. the properties file in any of the relative upstream directories (R/W)
	 * <p>
	 * 4. the properties file in the execution directory
	 * {@code callerClass.getProtectionDomain().getCodeSource().getLocation()} (R/W)
	 * <p>
	 * 5. the properties file in the same package as the caller class (R)
	 * <p>
	 * 6. the properties file in the default package on the class path (R)
	 * <p>
	 * 7. in case the specified propertiesName contains a {@code File.separator}
	 * {@code getProperties(String, Charset)} is called with the file name only (?)
	 * 
	 * @param propertiesName the properties file name to search for, with or without
	 *                       the file extension ".properties", may also be a
	 *                       relative or full path.
	 * @return the {@code PropertiesFile}
	 * @throws FileNotFoundException
	 */
	public static PropertiesFile getProperties(String propertiesName) throws FileNotFoundException {
		return getProperties(propertiesName, StandardCharsets.UTF_8);
	}

	/**
	 * searches a properties file.
	 * <p>
	 * propertiesName search is looking for:
	 * <p>
	 * 1. the properties file relative to the project directory (R/W)
	 * <p>
	 * 2. the properties files full path (R/W)
	 * <p>
	 * 3. the properties file in any of the relative upstream directories (R/W)
	 * <p>
	 * 4. the properties file in the execution directory
	 * {@code callerClass.getProtectionDomain().getCodeSource().getLocation()} (R/W)
	 * <p>
	 * 5. the properties file in the same package as the caller class (R)
	 * <p>
	 * 6. the properties file in the default package on the class path (R)
	 * <p>
	 * 7. in case the specified propertiesName contains a {@code File.separator}
	 * {@code getProperties(String, Charset)} is called with the file name only (?)
	 * 
	 * @param propertiesName the properties file name to search for, with or without
	 *                       the file extension ".properties", may also be a
	 *                       relative or full path.
	 * @param charset        the charset to use.
	 * @return the {@code PropertiesFile}
	 * @throws FileNotFoundException
	 */
	public static PropertiesFile getProperties(String propertiesName, Charset charset) throws FileNotFoundException {
		String fileName;
		if (!propertiesName.endsWith(".properties")) {
			fileName = propertiesName + ".properties";
		} else {
			fileName = propertiesName;
		}
		{
			/*
			 * Search relative or absolute paths
			 */
			File f = new File(fileName);
			if (f.exists()) {
				try {
					PropertiesFile p = new PropertiesFile(f, charset);
//					LOGGER.trace("Loaded from specified (absolute or relative) file path");
					return p;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Class<?> callerClass = StackUtils.getCallerClass(true);

		{
			/*
			 * Search in execution directory
			 */
			String executionPath = callerClass.getProtectionDomain().getCodeSource().getLocation().getPath();
			File f = new File(executionPath, fileName);
			if (f.exists()) {
				try {
					PropertiesFile p = new PropertiesFile(f, charset);
//					LOGGER.trace("Loaded '%s' from execution directory", fileName);
					return p;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		{
			/*
			 * Search in relative parent directories
			 */
			int upstreamDirs;
			String parent = ".." + File.separator;
			String separatorRegex = File.separator.equals("/") ? "[/]" : "[\\\\]";
			upstreamDirs = new File("").getAbsolutePath().split(separatorRegex).length;
			for (int i = 1; i < upstreamDirs; i++) {
				File fTest = new File(parent.repeat(i) + fileName);
				if (fTest.exists()) {
					try {
						PropertiesFile p = new PropertiesFile(fTest, charset);
						return p;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		/*
		 * Search for resource relative to caller class (may be in jar)
		 */
		try (InputStream in = callerClass.getResourceAsStream(fileName)) {
			if (Objects.nonNull(in)) {
				PropertiesFile p = new PropertiesFile(in, charset);
//					LOGGER.trace("Loaded '%s' from relative stream", fileName);
				return p;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		 * Search for absolute resource path
		 */
		try (InputStream in = callerClass.getResourceAsStream("/" + fileName)) {
			if (Objects.nonNull(in)) {
				PropertiesFile p = new PropertiesFile(in);
//					LOGGER.trace("Loaded '%s' from absolute stream", fileName);
				return p;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (fileName.contains(File.separator)) {
			/*
			 * Search relative path of absolute specification
			 */
			return getProperties(new File(fileName).getName(), charset);
		}
		throw new FileNotFoundException(String.format("Properties file '%s' not found", new File(fileName).getName()));
	}

	private final Properties properties;

	private final File propertiesFile;

	private final Charset charset;

	private final String fileComment;

	private final boolean readOnly;

	private final Properties runtimeProperties = new Properties();

	/**
	 * Constructs a writable PropertiesFile using the charset
	 * {@code StandardCharsets.UTF_8}, without defaults or comment.
	 * 
	 * @param propertiesFile the *.properties file to load
	 * @throws IOException
	 */
	public PropertiesFile(File propertiesFile) throws IOException {
		this(propertiesFile, StandardCharsets.UTF_8, null);
	}

	/**
	 * Constructs a writable PropertiesFile with the specified comment using the
	 * charset {@code StandardCharsets.UTF_8}, without defaults.
	 * 
	 * @param propertiesFile the *.properties file to load
	 * @param fileComment    the file comment
	 * @throws IOException
	 */
	public PropertiesFile(File propertiesFile, String fileComment) throws IOException {
		this(propertiesFile, StandardCharsets.UTF_8, fileComment);
	}

	/**
	 * Constructs a writable PropertiesFile using the specified charset, without
	 * defaults or comment.
	 * 
	 * @param propertiesFile the *.properties file to load
	 * @param charset        the charset
	 * @throws IOException
	 */
	public PropertiesFile(File propertiesFile, Charset charset) throws IOException {
		this(propertiesFile, charset, null);
	}

	/**
	 * Constructs a writable PropertiesFile using the specified charset and the
	 * specified comment, without defaults.
	 * 
	 * @param propertiesFile the *.properties file to load
	 * @param charset        the charset
	 * @param fileComment    the comment
	 * @throws IOException
	 */
	public PropertiesFile(File propertiesFile, Charset charset, String fileComment) throws IOException {
		this(propertiesFile, charset, fileComment, null);
	}

	/**
	 * Constructs a writable PropertiesFile using the specified charset, the
	 * specified comment and the specified defaults.
	 * 
	 * @param propertiesFile the *.properties file to load
	 * @param charset        the charset
	 * @param fileComment    the comment
	 * @param defaults       the defaults
	 * @throws IOException
	 */
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

	/**
	 * Constructs a read-only {@code PropertiesFile} using the specified stream with
	 * the charset UTF_8.
	 * 
	 * @param in the stream to the properties file
	 * @throws IOException
	 */
	public PropertiesFile(InputStream in) throws IOException {
		this(in, StandardCharsets.UTF_8);
	}

	/**
	 * Constructs a read-only {@code PropertiesFile} using the specified stream with
	 * the specified charset.
	 * 
	 * @param in      the stream to the properties file
	 * @param charset the charset to use
	 * @throws IOException
	 */
	public PropertiesFile(InputStream in, Charset charset) throws IOException {
		this.properties = new Properties();
		this.fileComment = null;
		this.propertiesFile = null;
		this.charset = charset;
		this.readOnly = true;
//		LOGGER.trace("Properties loaded from InputStream: %s", in.toString());
		load(in);
	}

	/**
	 * Gets all property keys.
	 * 
	 * @return the properties key set.
	 */
	public final Set<Object> keySet() {
		Set<Object> keySet = properties.keySet();
		keySet.addAll(runtimeProperties.keySet());
		return keySet;
	}

	/**
	 * Sets a String property.
	 * 
	 * @param key   the key
	 * @param value the value
	 * @throws IOException
	 */
	public final void setProperty(String key, String value) throws IOException {
		if (!readOnly) {
			properties.setProperty(key, value);
			store();
		} else {
			runtimeProperties.setProperty(key, value);
		}
	}

	/**
	 * Gets a String property.
	 * 
	 * @param key the key
	 * @return the value or null by default
	 */
	public final String getProperty(String key) {
		return getProperty(key, null);
	}

	/**
	 * Gets a String property.
	 * 
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the value or the specified defaultValue by default
	 */
	public final String getProperty(String key, String defaultValue) {
		if (runtimeProperties.containsKey(key)) {
			return runtimeProperties.getProperty(key, defaultValue);
		}
		return properties.getProperty(key, defaultValue);
	}

	/**
	 * Sets an int property.
	 * 
	 * @param key   the key
	 * @param value the value
	 * @throws IOException
	 */
	public final void setProperty(String key, int value) throws IOException {
		setProperty(key, String.valueOf(value));
	}

	/**
	 * Gets an int property.
	 * 
	 * @param key the key
	 * @return the value or 0 by default
	 */
	public int getIntProperty(String key) {
		return getIntProperty(key, 0);
	}

	/**
	 * Gets an int property.
	 * 
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the value or the specified defaultValue by default
	 */
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

	/**
	 * Sets a long property.
	 * 
	 * @param key   the key
	 * @param value the value
	 * @throws IOException
	 */
	public final void setProperty(String key, long value) throws IOException {
		setProperty(key, String.valueOf(value));
		store();
	}

	/**
	 * Gets a long property.
	 * 
	 * @param key the key
	 * @return the value or 0L by default
	 */
	public long getLongProperty(String key) {
		return getLongProperty(key, 0L);
	}

	/**
	 * Gets a long property.
	 * 
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the value or the specified defaultValue by default
	 */
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

	/**
	 * Sets a double property.
	 * 
	 * @param key   the key
	 * @param value the value
	 * @throws IOException
	 */
	public final void setProperty(String key, double value) throws IOException {
		setProperty(key, String.valueOf(value));
		store();
	}

	/**
	 * Gets a double property.
	 * 
	 * @param key the key
	 * @return the value or 0.0d by default
	 */
	public double getDoubleProperty(String key) {
		return getDoubleProperty(key, 0.0d);
	}

	/**
	 * Gets a double property.
	 * 
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the value or the specified defaultValue by default
	 */
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

	/**
	 * Sets a float property.
	 * 
	 * @param key   the key
	 * @param value the value
	 * @throws IOException
	 */
	public final void setProperty(String key, float value) throws IOException {
		setProperty(key, String.valueOf(value));
		store();
	}

	/**
	 * Gets a float property.
	 * 
	 * @param key the key
	 * @return the value or 0.0f by default
	 */
	public float getFloatProperty(String key) {
		return getFloatProperty(key, 0.0f);
	}

	/**
	 * Gets a float property.
	 * 
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the value or the specified defaultValue by default
	 */
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

	/**
	 * Sets a Locale property.
	 * 
	 * @param key   the key
	 * @param value the value
	 * @throws IOException
	 */
	public final void setProperty(String key, Locale value) throws IOException {
		setProperty(key, value.toLanguageTag());
		store();
	}

	/**
	 * Gets a Locale property.
	 * 
	 * @param key the key
	 * @return the value or Locale.ENGLISH by default
	 */
	public Locale getLocaleProperty(String key) {
		return getLocaleProperty(key, Locale.ENGLISH);
	}

	/**
	 * Gets a Locale property.
	 * 
	 * @param key          the key
	 * @param defaultValue the default value
	 * @return the value or the specified defaultValue by default
	 */
	public Locale getLocaleProperty(String key, Locale defaultValue) {
		String val = getProperty(key, defaultValue.toLanguageTag());
		Locale result = defaultValue;
		try {
			result = Locale.forLanguageTag(val);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Sets an enum property.
	 * 
	 * @param <T>   the enum type
	 * @param key   the property key
	 * @param value the enum value to set
	 */
	public <T extends Enum<T>> void setProperty(String key, T value) {
		try {
			setProperty(key, value.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets an enum property.
	 * 
	 * @param <T>       the enum type
	 * @param key       the property key
	 * @param enumClass the enum class
	 * @return the enum value or null by default
	 */
	public <T extends Enum<T>> T getEnumProperty(String key, Class<T> enumClass) {
		String value = getProperty(key);
		if (Objects.isNull(value)) {
			return null;
		}
		return Enum.valueOf(enumClass, value);
	}

	/**
	 * Gets an enum property.
	 * 
	 * @param <T>          the enum type
	 * @param key          the property key
	 * @param defaultValue the default value
	 * @return the enum value or null by default
	 */
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T getEnumProperty(String key, T defaultValue) {
		Objects.requireNonNull(defaultValue);
		try {
			String val = getProperty(key);
			if (Objects.nonNull(val)) {
				return (T) Enum.valueOf(defaultValue.getClass(), val);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	/**
	 * Checks if the loaded properties contain the specified key.
	 * 
	 * @param key the key to seek
	 * @return true if the key exists, false otherwise.
	 */
	public boolean containsKey(String key) {
		return properties.containsKey(key) || runtimeProperties.contains(key);
	}

	/**
	 * Loads the properties from the specified input stream.
	 * 
	 * @param in the stream
	 * @throws IOException
	 */
	public final void load(InputStream in) throws IOException {
		this.properties.load(in);
	}

	/**
	 * reload the properties from file.
	 * 
	 * @throws IOException
	 */
	public final void load() throws IOException {
		try (Reader in = new FileReader(propertiesFile, charset)) {
			this.properties.load(in);
		} catch (FileNotFoundException e) {}
	}

	/**
	 * Stores the loaded properties to the known file.
	 * <p>
	 * This method is automatically called by the setProperty methods.
	 * 
	 * @throws IOException if any reading error occurs or the {@code PropertiesFile}
	 *                     is in read-only mode
	 */
	public final void store() throws IOException {
		store(fileComment);
	}

	/**
	 * Stores the loaded properties to the known file using the specified comment.
	 * <p>
	 * This method is automatically called by the setProperty methods.
	 * 
	 * @param fileComment the file comment
	 * @throws IOException if any reading error occurs or the {@code PropertiesFile}
	 *                     is in read-only mode
	 */
	public final void store(String fileComment) throws IOException {
		if (readOnly) {
			throw new ReadOnlyFileSystemException();
		}
		try (Writer out = new FileWriter(propertiesFile, charset)) {
			this.properties.store(out, fileComment);
		}
	}

	/**
	 * tests if the file exists.
	 * 
	 * @return true if the file exists, false otherwise.
	 */
	public boolean exists() {
		if (Objects.nonNull(propertiesFile)) {
			return propertiesFile.exists();
		} else {
			return false;
		}
	}

	/**
	 * gets the {@code File} of the {@code PropertiesFile}.
	 * 
	 * @return the {@code File}
	 */
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

	public boolean isReadOnly() {
		return readOnly;
	}
}
