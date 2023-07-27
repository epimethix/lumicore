package com.github.epimethix.lumicore.properties;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import com.github.epimethix.lumicore.common.ui.Theme;

public abstract class ApplicationProperties {

	private final static String DEFAULT_LOCALE = "default-locale";

	private static final String THEME = "theme";

	private final PropertiesFile PROPERTIES;

	public ApplicationProperties(String propertiesName) {
		PropertiesFile pf = null;
		try {
			pf = PropertiesFile.getProperties(propertiesName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (Objects.isNull(pf)) {
			System.err.printf("Fatal: application properties '%s' could not be loaded!", propertiesName);
			System.exit(100);
		}
		PROPERTIES = pf;
	}

	public PropertiesFile getProperties() {
		return PROPERTIES;
	}

	public void setDefaultLocale(Locale locale) {
		try {
			PROPERTIES.setProperty(DEFAULT_LOCALE, locale.toLanguageTag());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Locale getDefaultLocale() {
		return Locale.forLanguageTag(PROPERTIES.getProperty(DEFAULT_LOCALE, Locale.ENGLISH.toLanguageTag()));
	}

	public File getFile() {
		return PROPERTIES.getFile();
	}

	public Theme getTheme() {
		return PROPERTIES.getEnumProperty(THEME, Theme.class);
	}

	public void setTheme(Theme t) {
		PROPERTIES.setProperty(THEME, t);
	}

	public final Set<Object> keySet() {
		return PROPERTIES.keySet();
	}

	public final void setProperty(String key, String value) throws IOException {
		PROPERTIES.setProperty(key, value);
	}

	public final String getProperty(String key) {
		return PROPERTIES.getProperty(key);
	}

	public final String getProperty(String key, String defaultValue) {
		return PROPERTIES.getProperty(key, defaultValue);
	}

	public final void setProperty(String key, int value) throws IOException {
		PROPERTIES.setProperty(key, value);
	}

	public int getIntProperty(String key) {
		return PROPERTIES.getIntProperty(key);
	}

	public int getIntProperty(String key, int defaultValue) {
		return PROPERTIES.getIntProperty(key, defaultValue);
	}

	public final void setProperty(String key, long value) throws IOException {
		PROPERTIES.setProperty(key, value);
	}

	public long getLongProperty(String key) {
		return PROPERTIES.getLongProperty(key);
	}

	public long getLongProperty(String key, long defaultValue) {
		return PROPERTIES.getLongProperty(key, defaultValue);
	}

	public final void setProperty(String key, double value) throws IOException {
		PROPERTIES.setProperty(key, value);
	}

	public double getDoubleProperty(String key) {
		return PROPERTIES.getDoubleProperty(key);
	}

	public double getDoubleProperty(String key, double defaultValue) {
		return PROPERTIES.getDoubleProperty(key, defaultValue);
	}

	public final void setProperty(String key, float value) throws IOException {
		PROPERTIES.setProperty(key, value);
	}

	public float getFloatProperty(String key) {
		return PROPERTIES.getFloatProperty(key);
	}

	public float getFloatProperty(String key, float defaultValue) {
		return PROPERTIES.getFloatProperty(key, defaultValue);
	}

	public final void setProperty(String key, Locale value) throws IOException {
		PROPERTIES.setProperty(key, value);
	}

	public Locale getLocaleProperty(String key) {
		return PROPERTIES.getLocaleProperty(key);
	}

	public Locale getLocaleProperty(String key, Locale defaultValue) {
		return PROPERTIES.getLocaleProperty(key, defaultValue);
	}

	public <T extends Enum<T>> void setProperty(String key, T value) {
		PROPERTIES.setProperty(key, value);
	}

	public <T extends Enum<T>> T getEnumProperty(String key, Class<T> enumClass) {
		return PROPERTIES.getEnumProperty(key, enumClass);
	}

	public <T extends Enum<T>> T getEnumProperty(String key, T defaultValue) {
		return PROPERTIES.getEnumProperty(key, defaultValue);
	}

	public boolean containsKey(String key) {
		return PROPERTIES.containsKey(key);
	}
	
}
