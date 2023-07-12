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
package com.github.epimethix.lumicore.common.orm.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultCriteria implements Criteria {

	private final List<Criterium> criteria;

	public DefaultCriteria() {
		this(Collections.emptyList());
	}

	public DefaultCriteria(List<Criterium> criteria) {
		this.criteria = new ArrayList<>(criteria);
	}

	public DefaultCriteria(DefaultCriteria c) {
		this.criteria = new ArrayList<>(c.criteria);
	}

	@Override
	public String getCriteria() {
		StringBuilder sb = new StringBuilder();
		for(Criterium c:criteria) {
			sb.append(c.getCriterium());
		}
		return sb.toString();
	}

	@Override
	public void addCriterium(Criterium c) {
		criteria.add(c);
	}

	@Override
	public List<Object> getCriteriumValues() {
		List<Object> values = new ArrayList<>();
		for(Criterium c:criteria) {
			values.addAll(c.getValues());
		}
		return values;
	}

	@Override
	public boolean isEmpty() {
		return criteria.isEmpty();
	}

	@Override
	public void clear() {
		criteria.clear();
	}
	
}
