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
package com.github.epimethix.lumicore.profile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.epimethix.lumicore.stackutil.AccessCheck;

public class CustomCredentials {

	private final String domain;

	private final transient Map<String, String> values;

	private final AccessCheck check;

	CustomCredentials(AccessCheck check, String domain) {
		this.check = check;
		this.domain = domain;
		this.values = new HashMap<>();
	}

	void putValue(String key, String value) {
		values.put(key, value);
	}

	public boolean containsKey(String key) throws IllegalAccessException {
		check.checkPermission();
		return values.containsKey(key);
	}

	public Set<String> keySet() throws IllegalAccessException {
		check.checkPermission();
		return values.keySet();
	}

	public String getValue(String key) throws IllegalAccessException {
		check.checkPermission();
		if (Objects.nonNull(domain) && !domain.trim().isEmpty() && !domain.equals(".") && !key.startsWith(domain)) {
			key = domain.concat(".").concat(key);
		}
		return values.get(key);
	}
}
