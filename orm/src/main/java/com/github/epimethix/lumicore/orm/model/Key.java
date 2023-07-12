/*
 * Copyright 2021 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.orm.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Container class The implementation of the {@link CompositeKey#toString()}
 * method puts together the key values in a String ordered by the corresponding
 * field names to create a unique string key when necessary.
 * <p>
 * The escape sequence is a triple colon (:::), do not use this character
 * sequence in TEXT keys!
 */
public final class Key {
	/**
	 * The implementation of the {@link #toString()} method puts together the key
	 * values in a String ordered by the corresponding field names to create a
	 * unique string key when necessary.
	 * <p>
	 * The escape sequence is a triple colon (:::), do not use this character
	 * sequence in TEXT keys!
	 */
	public static abstract class CompositeKey {
		public static int KEY_SIZE = -1;
		public static final String KEY_ESCAPE_SEQUENCE = ":::";
		/**
		 * The implementation of the {@link #toString()} method puts together the key
		 * values in a String ordered by the corresponding field names to create a
		 * unique string key when necessary.
		 * <p>
		 * The escape sequence is a triple colon (:::), do not use this character
		 * sequence in TEXT keys!
		 */
		private Map<String, Object> keys;
		private Map<String, Class<?>> types;

		/**
		 * The implementation of the {@link #toString()} method puts together the key
		 * values in a String ordered by the corresponding field names to create a
		 * unique string key when necessary.
		 * <p>
		 * The escape sequence is a triple colon (:::), do not use this character
		 * sequence in TEXT keys!
		 */
		private CompositeKey() {
			this.keys = new HashMap<>();
			this.types = new HashMap<>();
		}

		/**
		 * The implementation of the {@link #toString()} method puts together the key
		 * values in a String ordered by the corresponding field names to create a
		 * unique string key when necessary.
		 * <p>
		 * The escape sequence is a triple colon (:::), do not use this character
		 * sequence in TEXT keys!
		 */
		public final void put(String field, Object value, Class<?> type) {
			if (value instanceof String) {
				if (((String) value).contains(KEY_ESCAPE_SEQUENCE)) {
					throw new IllegalArgumentException(String.format(
							"%s >> illegal String key content, String keys may not contain the escape sequence '%s'",
							getClass().getSimpleName(), KEY_ESCAPE_SEQUENCE));
				}
			}
			keys.put(field, value);
			types.put(field, type);
		}

		public final Object get(String field) {
			return keys.get(field);
		}

		public final Class<?> getType(String field) {
			return types.get(field);
		}

		public final Set<String> keySet() {
			return keys.keySet();
		}

		public final int size() {
			return keys.size();
		}

		/**
		 * puts together the key values in a String ordered by the corresponding field
		 * names to create a unique string key when necessary.
		 * <p>
		 * The escape sequence is a triple colon (:::), do not use this character
		 * sequence in TEXT keys!
		 */
		@JsonIgnore
		@Override
		public String toString() {
			List<String> keys = new ArrayList<>(keySet());
			Collections.sort(keys);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < keys.size(); i++) {
				sb.append(String.valueOf(get(keys.get(i))));
				if (i + 1 < keys.size()) {
					sb.append(KEY_ESCAPE_SEQUENCE);
				}
			}
			return sb.toString();
		}
	}

	/**
	 * The implementation of the {@link #toString()} method puts together the key
	 * values in a String ordered by the corresponding field names to create a
	 * unique string key when necessary.
	 * <p>
	 * The escape sequence is a triple colon (:::), do not use this character
	 * sequence in TEXT keys!
	 */
	public final static class DualKey extends CompositeKey {
		public static final int KEY_SIZE = 2;

		public DualKey() {}

		public DualKey(String fieldNameX, Object x, Class<?> typeX, String fieldNameY, Object y, Class<?> typeY) {
			put(fieldNameX, x, typeX);
			put(fieldNameY, y, typeY);
		}
	}

	/**
	 * The implementation of the {@link #toString()} method puts together the key
	 * values in a String ordered by the corresponding field names to create a
	 * unique string key when necessary.
	 * <p>
	 * The escape sequence is a triple colon (:::), do not use this character
	 * sequence in TEXT keys!
	 */
	public final static class TripleKey extends CompositeKey {
		public static final int KEY_SIZE = 3;

		public TripleKey() {}

		public TripleKey(String fieldNameX, Object x, Class<?> typeX, String fieldNameY, Object y, Class<?> typeY,
				String fieldNameZ, Object z, Class<?> typeZ) {
			put(fieldNameX, x, typeX);
			put(fieldNameY, y, typeY);
			put(fieldNameZ, z, typeZ);
		}
	}

	/**
	 * The implementation of the {@link #toString()} method puts together the key
	 * values in a String ordered by the corresponding field names to create a
	 * unique string key when necessary.
	 * <p>
	 * The escape sequence is a triple colon (:::), do not use this character
	 * sequence in TEXT keys!
	 */
	public final static class QuadrupleKey extends CompositeKey {
		public static final int KEY_SIZE = 4;

		public QuadrupleKey() {}

		public QuadrupleKey(String fieldNameW, Object w, Class<?> typeW, String fieldNameX, Object x, Class<?> typeX,
				String fieldNameY, Object y, Class<?> typeY, String fieldNameZ, Object z, Class<?> typeZ) {
			put(fieldNameW, w, typeW);
			put(fieldNameX, x, typeX);
			put(fieldNameY, y, typeY);
			put(fieldNameZ, z, typeZ);
		}
	}

	/**
	 * The implementation of the {@link #toString()} method puts together the key
	 * values in a String ordered by the corresponding field names to create a
	 * unique string key when necessary.
	 * <p>
	 * The escape sequence is a triple colon (:::), do not use this character
	 * sequence in TEXT keys!
	 */
	public final static class QuintupleKey extends CompositeKey {
		public static final int KEY_SIZE = 5;

		public QuintupleKey() {}

		public QuintupleKey(String fieldNameV, Object v, Class<?> typeV, String fieldNameW, Object w, Class<?> typeW,
				String fieldNameX, Object x, Class<?> typeX, String fieldNameY, Object y, Class<?> typeY,
				String fieldNameZ, Object z, Class<?> typeZ) {
			put(fieldNameV, v, typeV);
			put(fieldNameW, w, typeW);
			put(fieldNameX, x, typeX);
			put(fieldNameY, y, typeY);
			put(fieldNameZ, z, typeZ);
		}
	}

	private Key() {}
}
