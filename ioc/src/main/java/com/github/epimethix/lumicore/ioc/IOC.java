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
package com.github.epimethix.lumicore.ioc;

import java.lang.reflect.Modifier;

import com.github.epimethix.lumicore.common.swing.EntityEditor;
import com.github.epimethix.lumicore.ioc.annotation.Component;
import com.github.epimethix.lumicore.ioc.annotation.Service;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;

public final class IOC {
	public static boolean isImplementationClass(Class<?> cls) {
		if (cls.isInterface()) {
			return false;
		} else if (Modifier.isAbstract(cls.getModifiers())) {
			return false;
		}
		return true;
	}

	public static boolean isSingleton(Class<?> componentClass) {
		if (componentClass.isAnnotationPresent(Component.class)) {
			return componentClass.getAnnotation(Component.class).singleton();
		} else if (componentClass.isAnnotationPresent(Service.class)) {
			return componentClass.getAnnotation(Service.class).singleton();
		} else if (componentClass.isAnnotationPresent(SwingComponent.class)) {
			return componentClass.getAnnotation(SwingComponent.class).singleton();
		} else if (EntityEditor.class.isAssignableFrom(componentClass)) {
			return false;
		} else {
			return true;
		}
	}

	private IOC() {}
}
