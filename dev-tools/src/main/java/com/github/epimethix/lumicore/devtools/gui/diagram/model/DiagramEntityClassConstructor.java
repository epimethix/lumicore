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
package com.github.epimethix.lumicore.devtools.gui.diagram.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class DiagramEntityClassConstructor implements DiagramEntityConstructor {

//	private final Constructor<?> constructor;
	private final String visibility;
	private final String name;
	private final String parameters;

	public DiagramEntityClassConstructor(Constructor<?> constructor) {
		visibility = DiagramEntity.getVisibility(constructor.getModifiers());
		name = constructor.getName();
		List<String> parameters = new ArrayList<>();
		Parameter[] parameterArray = constructor.getParameters();
		for(Parameter p: parameterArray) {
			parameters.add(String.format("%s %s", p.getType().getSimpleName(), p.getName()));
		}
		String result;
		if(!parameters.isEmpty()) {
			result = String.format("(%s)", String.join(", ", parameters));
		} else {
			result = "()";
		}
		this.parameters = result;
	}

	@Override
	public String getVisibility() {
		return visibility;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getParameters() {
		return parameters;
	}

	@Override
	public String toString() {
		return getString();
	}
}
