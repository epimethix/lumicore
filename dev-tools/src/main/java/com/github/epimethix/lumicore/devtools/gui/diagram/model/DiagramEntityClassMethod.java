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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.epimethix.lumicore.sourceutil.JavaSource;
import com.github.epimethix.lumicore.sourceutil.JavaSource.MethodSource;

public class DiagramEntityClassMethod implements DiagramEntityMethod {

//	private final Method methodSource;
	private final String visibility;
	private final String name;
	private final String typeParameters;
	private final String returnType;
	private final String returnTypeParameters;
	private final String returnTypeArray;
	private final String parameters;

	public DiagramEntityClassMethod(Method method) {
		visibility = DiagramEntity.getVisibility(method.getModifiers());
		name = method.getName();
		returnType = method.getReturnType().getSimpleName();

		List<String> typeParameters = new ArrayList<>();

		TypeVariable<Method>[] tp = method.getTypeParameters();
		for (TypeVariable<Method> var : tp) {
			typeParameters.add(var.getTypeName());
		}
		if (!typeParameters.isEmpty()) {
			this.typeParameters = String.format("<%s>", String.join(", ", typeParameters));
		} else {
			this.typeParameters = "";
		}

		MethodSource ms = JavaSource.readGenericMethodString(method);
		if (Objects.nonNull(ms)) {
			List<String> returnTypeParameters = ms.getReturnTypeParameters();
			if (!returnTypeParameters.isEmpty()) {
//			for(int i = 0; i < returnTypeParameters.size(); i++) {
//				String typeParam =
//			}
				this.returnTypeParameters = String.format("<%s>", String.join(", ", returnTypeParameters));
			} else {
				this.returnTypeParameters = "";
			}
		} else {
			returnTypeParameters = "";
		}

		Class<?> c = method.getReturnType();
		int arrayDimensions = 0;
		while (c.isArray()) {
			arrayDimensions++;
			c = c.getComponentType();
		}
		returnTypeArray = "[]".repeat(arrayDimensions);

		List<String> parameters = new ArrayList<>();
		Parameter[] parameterArray = method.getParameters();
		for (Parameter p : parameterArray) {
			parameters.add(String.format("%s %s", p.getType().getSimpleName(), p.getName()));
		}
		if (!parameters.isEmpty()) {
			this.parameters = String.format("(%s)", String.join(", ", parameters));
		} else {
			this.parameters = "()";
		}
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
	public String getReturnType() {
		return returnType;
	}

	@Override
	public String getTypeParameters() {
		return typeParameters;
	}

	@Override
	public String getReturnTypeParameters() {
		return returnTypeParameters;
	}

	@Override
	public String getReturnTypeArray() {
		return returnTypeArray;
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
