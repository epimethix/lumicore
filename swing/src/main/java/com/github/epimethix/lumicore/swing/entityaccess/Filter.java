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
package com.github.epimethix.lumicore.swing.entityaccess;

public class Filter {
	@FunctionalInterface
	public interface FilterCriterium {
		boolean accept(Object x);
	}
	private int fieldIndex;
	
	private int filterType;
	
	private FilterCriterium filterCriterium;
	
	public Filter(int fieldIndex, int filterType, FilterCriterium filterCriterium) {
		super();
		this.fieldIndex = fieldIndex;
		this.filterType = filterType;
		this.filterCriterium = filterCriterium;
	}
}
