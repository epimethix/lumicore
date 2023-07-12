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
package com.github.epimethix.lumicore.common.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class SplashScreenController {

	private static SplashScreen splashScreen;

	public static final void showSplashScreen(SplashScreen splashScreen) {
		splashScreen.showSplashScreen();
		SplashScreenController.splashScreen = splashScreen;
	}

	public static final void showSplashScreen(Class<? extends SplashScreen> splashScreenClass) {
		if (Objects.isNull(splashScreen)) {
			try {
				splashScreen = (SplashScreen) splashScreenClass.getConstructor().newInstance();
				splashScreen.showSplashScreen();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}
	}

	public static final void hideSplashScreen() {
		if (Objects.nonNull(splashScreen)) {
			splashScreen.hideSplashScreen();
			splashScreen = null;
		}
	}

}
