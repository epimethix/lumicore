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

public interface DiagramEntityMethod {
	public String getVisibility();

	public String getName();

	public String getReturnType();

	public String getTypeParameters();

	public String getReturnTypeParameters();

	public String getReturnTypeArray();

	public String getParameters();

	public default String getString() {
		return String.format("%s %s%s%s : %s%s%s", DiagramEntity.getVisibilityChar(getVisibility()),
				getTypeParameters(), getName(), getParameters(), getReturnType(), getReturnTypeParameters(),
				getReturnTypeArray());
	}
}
