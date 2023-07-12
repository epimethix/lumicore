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
package com.github.epimethix.lumicore.swing.dialog;

public class DefaultSwingSplashScreen extends SwingSplashScreen {

	private final Class<?> projectClass;
	private final String resourcePath;
	private final String label;

	public DefaultSwingSplashScreen(Class<?> projectClass, String resourcePath, String label) {
		this.projectClass = projectClass;
		this.resourcePath = resourcePath;
		this.label = label;
	}

	@Override
	public void showSplashScreen() {
		showSplashScreen(projectClass.getResourceAsStream(resourcePath), label);
	}
}
