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
package com.github.epimethix.lumicore.sourceutil;

import java.util.Map;
import java.util.Objects;

/**
 * Interface to specify an element of java source code.
 * 
 * @author epimethix
 *
 */
public interface Source {
	/**
	 * Appends this source element to the specified {@code StringBuilder}.
	 * 
	 * @param out the {@code StringBuilder} to append to
	 */
	void append(StringBuilder out);

	/**
	 * gets the source code of this element.
	 * 
	 * @return the elements source
	 */
	CharSequence getSource();

	/**
	 * Gets the length of the source code encapsulated in this {@code Source}.
	 * 
	 * @return the length of the source code
	 */
	default int length() {
		return getSource().length();
	}

	/**
	 * Checks the integrity of this element of source code based on the specified
	 * boundaries.
	 * 
	 * @param previousEnd the ending index of the previous element
	 * @param boundaries  the boundaries of all source elements
	 * @return the ending index of this element if the check was passed or '-1' if
	 *         the check failed
	 */
	default Integer checkIntegrity(Integer previousEnd, Map<Source, Integer[]> boundaries) {
		Integer[] boundary = boundaries.get(this);
		Integer boundaryStart = Objects.nonNull(boundary) ? boundary[0] : -1;
		if (previousEnd.equals(boundaryStart)) {
			return boundary[1];
		}
		System.err.println(toString() + " failed check! this.start: " + String.valueOf(boundaryStart)
				+ " != previous.end: " + String.valueOf(previousEnd));
		return -1;
	}
}
