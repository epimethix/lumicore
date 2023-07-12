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

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;

public interface DiagramEntity {
//	private EntitySource entitySource;

//	public DiagramEntity(File javaFile) throws IOException {
//		this(new EntitySource(javaFile));
//	}
//
//	public DiagramEntity(EntitySource entitySource) {
//		this.entitySource = entitySource;
//	}

	public String getName();
//	{
//		return entitySource.getName();
//	}

	public String getSimpleName();
//	{
//		return entitySource.getSimpleName();
//	}

	public List<DiagramEntityField> getStaticFields();
//	{
//		return entitySource.getStaticFields();
//	}

	public List<DiagramEntityField> getInstanceFields();
//	{
//		return entitySource.getInstanceFields();
//	}

	public List<DiagramEntityConstructor> getConstructors();
//	{
//		return entitySource.getConstructors();
//	}

	public List<DiagramEntityMethod> getStaticMethods();
//	{
//		return entitySource.getStaticMethods();
//	}

	public List<DiagramEntityMethod> getInstanceMethods();
//	{
//		return entitySource.getInstanceMethods();
//	}

	public void persist() throws IOException;
//	{
//		entitySource.persist();
//	}

//	@Override
//	public String toString() {
//		return "DiagramEntity: " + getName();
//	}

	public boolean hasChanges();
//	{
//		return entitySource.hasChanges();
//	}

//	public void insertField(FieldSource fieldSource);
//	{
//		entitySource.insertField(fieldSource);
//	}

//	public String getCode();
//	{
//		return entitySource.getCode();
//	}

	public boolean isAbstract();
//	{
//		
//		return entitySource.isAbstract();
//	}

	public boolean isInterface();
//	{
//		
//		return entitySource.isInterface();
//	}

	public static String getVisibility(int modifiers) {
		if(Modifier.isPrivate(modifiers)) {
			return "private";
		} else if(Modifier.isProtected(modifiers)) {
			return "protected";
		} else if(Modifier.isPublic(modifiers)) {
			return "public";
		}
		return "";
		
	}
	public static String getVisibilityChar(String visibility) {
		if ("public".equals(visibility)) {
			return "+";
		} else if ("protected".equals(visibility)) {
			return "#";
		} else if ("private".equals(visibility)) {
			return "-";
		} else {
			return "~";
		}
	}
}
