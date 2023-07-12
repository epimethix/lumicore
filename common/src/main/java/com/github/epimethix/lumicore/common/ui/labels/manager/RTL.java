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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class RTL {

	private final static Locale LOCALE_AR = new Locale("ar");

	private final static Locale LOCALE_FA = new Locale("fa");

	private final static Locale LOCALE_IW = new Locale("iw");

	private final static Locale LOCALE_UR = new Locale("ur");

	private final static Set<Locale> RTL_LOCALES = new HashSet<>();

	static {
		RTL_LOCALES.add(LOCALE_AR);
		RTL_LOCALES.add(LOCALE_FA);
		RTL_LOCALES.add(LOCALE_IW);
		RTL_LOCALES.add(LOCALE_UR);
	}

	public final static boolean registerRTLLocale(Locale l) {
		return RTL_LOCALES.add(l);
	}

	public final static boolean isRTL(Locale locale) {
		for (Locale rtl : RTL_LOCALES) {
			if (rtl.getLanguage().equals(locale.getLanguage())) {
				return true;
			}
		}
		return false;
	}

	private RTL() {}
}
