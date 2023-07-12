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

import java.util.List;
import java.util.Objects;

import com.github.epimethix.lumicore.sourceutil.JavaSource.ConstructorSource;

public class DiagramEntitySourceConstructor implements DiagramEntityConstructor {

	private final ConstructorSource constructorSource;

	public DiagramEntitySourceConstructor(ConstructorSource constructorSource) {
		this.constructorSource = Objects.requireNonNull(constructorSource);
	}

	@Override
	public String getVisibility() {
		return constructorSource.getVisibility();
	}

	@Override
	public String getName() {
		return constructorSource.getIdentifier();
	}

	@Override
	public String getParameters() {
		List<String> parameters = constructorSource.getParameters();
		String result;
		if(!parameters.isEmpty()) {
			result = String.format("(%s)", String.join(", ", parameters));
		} else {
			result = "()";
		}
		return result;
	}

	@Override
	public String toString() {
		return getString();
	}
}
