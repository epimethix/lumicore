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
package com.github.epimethix.lumicore.common.ui.labels.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Set;

import com.github.epimethix.lumicore.common.Application;
import com.github.epimethix.lumicore.common.ui.labels.displayer.LabelsDisplayerPool;
import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;

public final class LabelsManagerPool {

	private final static Map<String, LabelsManager> MANAGERS_BY_NAME = new HashMap<>();

	private final static Map<String, LabelsManager> MANAGERS_BY_LABEL = new HashMap<>();

	private final static Set<String> ALL_KEYS = new HashSet<>();

	private final static Set<Locale> AVAILABLE_LOCALES = new HashSet<>();

	private final static Logger LOGGER = Log.getLogger();

	private static Locale currentLocale = Locale.getDefault();
//	private static Set<Locale> availableLocales = new HashSet<>();

	public final static void registerLabelsManager(LabelsManager labelsManager) {
		MANAGERS_BY_NAME.put(labelsManager.getName(), labelsManager);
		Set<String> keys = labelsManager.keySet();
		for (String key : keys) {
			MANAGERS_BY_LABEL.put(key, labelsManager);
			if (!ALL_KEYS.add(key)) {
				LOGGER.warn("The key '%s' from the bundle '%s' is being hidden by another LabelsManager", key,
						labelsManager.getName());
			}
		}
		List<Locale> avLoc = labelsManager.getAvailableLocales();
		LOGGER.trace("%s -> %s  REGISTERED in LabelsManagerPool", labelsManager.getName(), avLoc.toString());
		AVAILABLE_LOCALES.addAll(avLoc);
	}

	public final static void setLocale(Locale locale) {
		setLocale(null, locale);
	}

	public final static void setLocale(Application application, Locale locale) {
		if (Objects.isNull(locale)) {
			return;
		}
		if (locale.equals(currentLocale)) {
			return;
		}
		boolean found = false;
		for (Locale l : AVAILABLE_LOCALES) {
			if (l.equals(locale)) {
				found = true;
				break;
			}
		}
		if (!found) {
			LOGGER.critical(
					new IllegalArgumentException(String.format("Locale [%s] was not found in the available locales %s!",
							locale.toString(), getAvailableLocales().toString())));
			return;
		}
		LOGGER.info("Loading locale %s", locale.getDisplayName(Locale.ENGLISH));
		Locale.setDefault(locale);
		if (Objects.nonNull(application)) {
			application.setDefaultLocale(locale);
		}
		boolean wasRTL = RTL.isRTL(currentLocale);
		boolean isRTL = RTL.isRTL(locale);
		currentLocale = locale;
		for (String key : MANAGERS_BY_NAME.keySet()) {
			LabelsManager lm = MANAGERS_BY_NAME.get(key);
			lm.refreshBundle();
		}
		LabelsDisplayerPool.loadLabels(wasRTL != isRTL);
	}

	public final static String getLabel(String key, Object... args) {
		return getLabel(null, key, args);
	}

	public final static String getLabel(Locale locale, String key, Object... args) {
		return getLabel(null, locale, key, args);
	}

	public final static String getLabel(String bundle, Locale locale, String key, Object... args) {
		LabelsManager manager = null;
		if (Objects.nonNull(bundle) && !bundle.trim().isEmpty()) {
			if (MANAGERS_BY_NAME.containsKey(bundle)) {
				manager = MANAGERS_BY_NAME.get(bundle);
			} else {
				throw new MissingResourceException(String.format("bundle '%s' not found!", bundle),
						LabelsManagerPool.class.getName(), key);
			}
		}
		if (Objects.isNull(manager)) {
			manager = MANAGERS_BY_LABEL.get(key);
		}
		if (Objects.isNull(manager)) {
			return key;
		}
		if (Objects.isNull(locale)) {
			return manager.getLabel(key, args);
		} else {
			return manager.getLabel(locale, key, args);
		}
	}

	public static List<Locale> getAvailableLocales() {
		return new ArrayList<>(AVAILABLE_LOCALES);
	}

	public static boolean isRTL() {
		return RTL.isRTL(currentLocale);
	}

	public static Locale getCurrentLocale() {
		return currentLocale;
	}

	private LabelsManagerPool() {}
}
