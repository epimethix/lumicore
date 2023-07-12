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

import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface LabelsManager {
	String getLabel(Locale locale, String key, Object... args);
	String getLabel(String key, Object... args);
	Set<String> keySet();
	List<Locale> getAvailableLocales();
	String getName();
	void refreshBundle();
}
