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
import java.util.Arrays;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;

public final class DefaultLabelsManager implements LabelsManager {

	private Locale currentLocale;

	private final String bundleName;

	private final String fallBackBundleName;

	private ResourceBundle labels;

	private final List<Locale> availableLocales;

//	private final BiConsumer<LabelsDisplayer, Boolean> addingAction;
//	private final BiConsumer<LabelsDisplayer, Boolean> loadingAction;

	private final static Logger LOGGER = Log.getLogger();

	public DefaultLabelsManager() {
		this("labels");
	}

	public DefaultLabelsManager(String bundleName) {
		this(bundleName, bundleName.concat("_en"));
	}

	public DefaultLabelsManager(String bundleName, String fallBackBundleName) {
		this.bundleName = bundleName;
		this.fallBackBundleName = fallBackBundleName;
//		this.addingAction = addingAction;
//		this.loadingAction = loadingAction;
		this.availableLocales = Collections.unmodifiableList(loadAvailableLocales());
		refreshBundle();
		LabelsManagerPool.registerLabelsManager(this);
	}

	@Override
	public final void refreshBundle() {
		ResourceBundle x = null;
		try {
			x = ResourceBundle.getBundle(bundleName);
		} catch (Exception e) {
			LOGGER.critical(e.getMessage());
			x = ResourceBundle.getBundle(fallBackBundleName);
		}
		labels = x;
		currentLocale = labels.getLocale();
	}

	@Override
	public String getLabel(Locale locale, String key, Object... args) {
		if(Objects.isNull(key) || key.trim().isEmpty()) {
			LOGGER.critical("Key is empty or null");
			return "";
		}
		String label = key;
		ResourceBundle rb = labels;
		if (Objects.nonNull(locale) && !locale.equals(currentLocale)) {
			rb = ResourceBundle.getBundle(bundleName, locale);
		}
		try {
			label = rb.getString(key);
			label = String.format(label, args);
		} catch (MissingResourceException e) {
			try {
				label = LabelsManagerPool.getLabel(locale, key, args);
			} catch (MissingResourceException e1) {
				LOGGER.critical("Label for key '%s' in locale '%s' not found", key,
						Locale.getDefault().getDisplayName(Locale.ENGLISH));
				LOGGER.error(e1);
			}
		} catch (IllegalFormatException e) {
			LOGGER.error(e, "formatting the String '%s' with args '%s' failed", label, Arrays.toString(args));
		}
		return label;
	}

	@Override
	public String getLabel(String key, Object... args) {
		return getLabel(null, key, args);
	}

	@Override
	public Set<String> keySet() {
		return labels.keySet();
	}

	@Override
	public List<Locale> getAvailableLocales() {
		return availableLocales;
	}

	private List<Locale> loadAvailableLocales() {
		List<Locale> availableLocales = new ArrayList<>();
		for (Locale locale : Locale.getAvailableLocales()) {
			try {
				ResourceBundle rb = ResourceBundle.getBundle(bundleName, locale);
				if (rb.getLocale().equals(locale)) {
					availableLocales.add(locale);
				}
			} catch (MissingResourceException e) {}
		}
		return availableLocales;
	}

	@Override
	public String getName() {
		return bundleName;
	}
}
