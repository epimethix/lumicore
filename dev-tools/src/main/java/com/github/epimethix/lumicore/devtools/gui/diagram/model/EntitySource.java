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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.epimethix.lumicore.devtools.DevTools;
import com.github.epimethix.lumicore.sourceutil.JavaSource;
import com.github.epimethix.lumicore.sourceutil.JavaSource.ConstructorSource;
import com.github.epimethix.lumicore.sourceutil.JavaSource.FieldSource;
import com.github.epimethix.lumicore.sourceutil.JavaSource.MethodSource;
import com.github.epimethix.lumicore.swing.LumicoreSwingImpl;

public class EntitySource implements DiagramEntity {

	private final JavaSource javaSource;
	private final File javaFile;

	private final List<DiagramEntityField> staticFields;
	private final List<DiagramEntityMethod> staticMethods;
	private final List<DiagramEntityConstructor> constructors;
	private final List<DiagramEntityField> instanceFields;
	private final List<DiagramEntityMethod> instanceMethods;
	private boolean hasChanges;

//	public EntitySource(File sourcesDirectory, File javaFile) throws IOException {
//		this(javaFile, JavaSource.createFile(javaFile));
//	}
	
	public EntitySource(File javaFile) throws IOException {
		this(javaFile, JavaSource.readFile(javaFile, ((DevTools)LumicoreSwingImpl.getApplication()).getJavaCharset()));
	}
	
	public EntitySource(File javaFile, JavaSource javaSource) throws IOException {
		this.javaSource = javaSource;
		this.javaFile = javaFile;
		staticFields = new ArrayList<>();
		instanceFields = new ArrayList<>();
		List<FieldSource> fields = javaSource.getFields();
		for (FieldSource s : fields) {
			if (s.isStatic()) {
				staticFields.add(new DiagramEntitySourceField(s));
			} else {
				instanceFields.add(new DiagramEntitySourceField(s));
			}
		}
		constructors = new ArrayList<>();
		for(ConstructorSource s: javaSource.getConstructors()) {
			constructors.add(new DiagramEntitySourceConstructor(s));
		}
		staticMethods = new ArrayList<>();
		instanceMethods = new ArrayList<>();
		List<MethodSource> methods = javaSource.getMethods();
		for (MethodSource s : methods) {
			if (s.isStatic()) {
				staticMethods.add(new DiagramEntitySourceMethod(s));
			} else {
				instanceMethods.add(new DiagramEntitySourceMethod(s));
			}
		}
	}

	@Override
	public String getName() {
		return javaSource.getClassName();
	}

	@Override
	public List<DiagramEntityField> getInstanceFields() {
		return instanceFields;
	}
	
	@Override
	public List<DiagramEntityField> getStaticFields() {
		return staticFields;
	}
	
	@Override
	public List<DiagramEntityConstructor> getConstructors() {
		return constructors;
	}
	
	@Override
	public List<DiagramEntityMethod> getInstanceMethods() {
		return instanceMethods;
	}
	
	@Override
	public List<DiagramEntityMethod> getStaticMethods() {
		return staticMethods;
	}

	@Override
	public String getSimpleName() {
		return javaSource.getSimpleClassName();
	}

	@Override
	public void persist() throws IOException {
		javaSource.print(javaFile, ((DevTools)LumicoreSwingImpl.getApplication()).getJavaCharset());
		hasChanges = false;
	}

	@Override
	public boolean hasChanges() {
		return hasChanges;
	}

//	@Override
	public void insertField(FieldSource fieldSource) {
		javaSource.insertField(fieldSource);
		if(fieldSource.isStatic()) {
			staticFields.add(new DiagramEntitySourceField(fieldSource));
		} else {
			instanceFields.add(new DiagramEntitySourceField(fieldSource));
		}
		hasChanges = true;
	}

	public String getCode() {
		return javaSource.getSource().toString();
	}

	@Override
	public boolean isAbstract() {
		return javaSource.getModifiers().contains("abstract");
	}

	@Override
	public boolean isInterface() {
		return "interface".equals(javaSource.getClassKeyword());
	}
}
