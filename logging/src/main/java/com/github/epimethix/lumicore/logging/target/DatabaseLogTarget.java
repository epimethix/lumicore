/*
 * Copyright 2022 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.logging.target;

import java.io.File;

import com.github.epimethix.lumicore.logging.AbstractLogTarget;

public class DatabaseLogTarget extends AbstractLogTarget {

	@SuppressWarnings("unused")
	private final File databaseFile;

	public DatabaseLogTarget(File databaseFile) {
		super(databaseFile.getName());
		this.databaseFile = databaseFile;
		throw new RuntimeException("Not implemented yet");
	}

	@Override
	public void log(int level, int threshold,String threadName, Class<?> user, String methodName, Exception e, String channel, String message,
			Object... args) {
		if (shouldLog(level, threshold)) {
			return;
		}
	}
}
