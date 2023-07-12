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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DiagramEntityClassField implements DiagramEntityField {

//	private final Field field;

	private final String visibility;
	private final String name;
	private final String type;
	private final String typeParameters;
	private final String array;

	public DiagramEntityClassField(Field field) {
		visibility = DiagramEntity.getVisibility(field.getModifiers());
		name = field.getName();
		type = field.getType().getSimpleName();

		List<String> typeParameters = new ArrayList<>();
		try {
			ParameterizedType tp = (ParameterizedType) field.getGenericType();
			Type[] actualArgs = tp.getActualTypeArguments();
			if (Objects.nonNull(actualArgs) && actualArgs.length > 0) {
				for (Type arg : actualArgs) {
					try {
						Class<?> c = Class.forName(arg.getTypeName());
						typeParameters.add(c.getSimpleName());
					} catch (ClassNotFoundException e) {
						typeParameters.add(arg.getTypeName());
					}
				}
			}
		} catch (ClassCastException e) {}

		if (!typeParameters.isEmpty()) {
			this.typeParameters = String.format("<%s>", String.join(", ", typeParameters));
		} else {
			this.typeParameters = "";
		}

		Class<?> c = field.getType();
		int arrayDimensions = 0;
		while (c.isArray()) {
			arrayDimensions++;
			c = c.getComponentType();
		}
		array = "[]".repeat(arrayDimensions);
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
	public String getType() {
		return type;
	}

	@Override
	public String getTypeParameters() {
		return typeParameters;
	}

	@Override
	public final String getArray() {
		return array;
	}

	@Override
	public String toString() {
		return getString();
	}
}
