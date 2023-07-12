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

import com.github.epimethix.lumicore.sourceutil.JavaSource.FieldSource;

public class DiagramEntitySourceField implements DiagramEntityField {

	private final FieldSource fieldSource;

	public DiagramEntitySourceField(FieldSource fieldSource) {
		this.fieldSource = Objects.requireNonNull(fieldSource);
	}

	@Override
	public String getVisibility() {
		return fieldSource.getVisibility();
	}

	@Override
	public String getName() {
		return fieldSource.getIdentifier();
	}

	@Override
	public String getType() {
		return fieldSource.getType();
	}

	public String getTypeParameters() {
		List<String> typeParameters = fieldSource.getTypeParameters();
		if (!typeParameters.isEmpty()) {
			return String.format("<%s>", String.join(", ", typeParameters));
		} else {
			return "";
		}
	}

	public final String getArray() {
		return fieldSource.getArray();
	}

	@Override
	public String toString() {
		return getString();
	}
}
