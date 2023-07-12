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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EntityClass implements DiagramEntity {

	private final Class<?> cls;

	private List<DiagramEntityField> staticFields;
	private List<DiagramEntityField> instanceFields;
	private List<DiagramEntityConstructor> constructors;
	private List<DiagramEntityMethod> staticMethods;
	private List<DiagramEntityMethod> instanceMethods;

	public EntityClass(Class<?> cls) {
		this.cls = cls;
	}

	@Override
	public String getName() {
		return cls.getName();
	}

	@Override
	public String getSimpleName() {
		return cls.getSimpleName();
	}

	@Override
	public List<DiagramEntityField> getStaticFields() {
		if (Objects.isNull(staticFields)) {
			staticFields = new ArrayList<>();
			Field[] fields = cls.getDeclaredFields();
			for (Field f : fields) {
				if (Modifier.isStatic(f.getModifiers())) {
					staticFields.add(new DiagramEntityClassField(f));
				}
			}
		}
		return staticFields;
	}

	@Override
	public List<DiagramEntityField> getInstanceFields() {
		if (Objects.isNull(instanceFields)) {
			instanceFields = new ArrayList<>();
			Field[] fields = cls.getDeclaredFields();
			for (Field f : fields) {
				if (!Modifier.isStatic(f.getModifiers())) {
					instanceFields.add(new DiagramEntityClassField(f));
				}
			}
		}
		return instanceFields;
	}

	@Override
	public List<DiagramEntityConstructor> getConstructors() {
		if (Objects.isNull(constructors)) {
			constructors = new ArrayList<>();
			Constructor<?>[] constructorArray = cls.getDeclaredConstructors();
			for (Constructor<?> c : constructorArray) {
				constructors.add(new DiagramEntityClassConstructor(c));
			}
		}
		return constructors;
	}

	@Override
	public List<DiagramEntityMethod> getStaticMethods() {
		if (Objects.isNull(staticMethods)) {
			staticMethods = new ArrayList<>();
			Method[] methods = cls.getDeclaredMethods();
			for (Method m : methods) {
				if (Modifier.isStatic(m.getModifiers())) {
					staticMethods.add(new DiagramEntityClassMethod(m));
				}
			}
		}
		return staticMethods;
	}

	@Override
	public List<DiagramEntityMethod> getInstanceMethods() {
		if (Objects.isNull(instanceMethods)) {
			instanceMethods = new ArrayList<>();
			Method[] methods = cls.getDeclaredMethods();
			for (Method m : methods) {
				if (!Modifier.isStatic(m.getModifiers())) {
					instanceMethods.add(new DiagramEntityClassMethod(m));
				}
			}
		}
		return instanceMethods;
	}

	@Override
	public void persist() throws IOException {

	}

	@Override
	public boolean hasChanges() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAbstract() {
		return Modifier.isAbstract(cls.getModifiers());
	}

	@Override
	public boolean isInterface() {
		return cls.isInterface();
	}

}
