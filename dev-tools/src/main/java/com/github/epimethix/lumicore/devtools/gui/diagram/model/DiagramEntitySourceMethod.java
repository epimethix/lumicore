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

import com.github.epimethix.lumicore.sourceutil.JavaSource.MethodSource;

public class DiagramEntitySourceMethod implements DiagramEntityMethod {

	private final MethodSource methodSource;

	public DiagramEntitySourceMethod(MethodSource methodSource) {
		this.methodSource = methodSource;
	}

	@Override
	public String getVisibility() {
		return methodSource.getVisibility();
	}

	@Override
	public String getName() {
		return methodSource.getIdentifier();
	}

	@Override
	public String getReturnType() {
		return methodSource.getReturnType();
	}

	@Override
	public String getTypeParameters() {
		List<String> typeParameters = methodSource.getTypeParameters();
		if(!typeParameters.isEmpty()) {
			return String.format("<%s>", String.join(", ", typeParameters));
		} else {
			return "";
		}
	}

	@Override
	public String getReturnTypeParameters() {
		List<String> typeParameters = methodSource.getReturnTypeParameters();
		if(!typeParameters.isEmpty()) {
			return String.format("<%s>", String.join(", ", typeParameters));
		} else {
			return "";
		}
	}

	@Override
	public String getReturnTypeArray() {
		return methodSource.getReturnTypeArray();
	}
	
	@Override
	public String getParameters() {
		List<String> parameters = methodSource.getParameters();
		if(!parameters.isEmpty()) {
			return String.format("(%s)", String.join(", ", parameters));
		} else {
			return "()";
		}
	}
	
	@Override
	public String toString() {
		return getString();
	}
}
