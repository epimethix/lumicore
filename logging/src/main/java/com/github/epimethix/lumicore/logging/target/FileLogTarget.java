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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.github.epimethix.lumicore.logging.AbstractLogTarget;
import com.github.epimethix.lumicore.logging.Log.Message;

public class FileLogTarget extends AbstractLogTarget {

	private final File logFile;

	public FileLogTarget(File logFile) {
		this(logFile, false);
	}

	public FileLogTarget(File logFile, boolean archiveOnStart) {
		this(logFile, archiveOnStart, StandardCharsets.UTF_8);
	}

	public FileLogTarget(File logFile, boolean archiveOnStart, Charset charset) {
		super(logFile.getName(), charset);
		if (archiveOnStart && logFile.exists()) {
			File parentFile = logFile.getParentFile();
			String name = logFile.getName();
			int indexOfExtension = name.lastIndexOf(".");
			String extension = "";
			if (indexOfExtension > -1) {
				extension = name.substring(indexOfExtension);
				name = name.substring(0, indexOfExtension);
			}
			File archivedFile;
			int i = 1;
			do {
				archivedFile = new File(parentFile, String.format("%s.%03d%s", name, i, extension));
				i++;
			} while (archivedFile.exists());
			File fileToRename = new File(logFile.getPath());
			fileToRename.renameTo(archivedFile);
		}
		this.logFile = logFile;
	}

	private synchronized void printToFile(int level, Class<?> user, String methodName, File logFile, Exception e,
			String channel, String message, Object... params) {
		if (!logFile.getParentFile().exists()) {
			logFile.getParentFile().mkdirs();
		}
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		printMessage(null, new Message(level, user, methodName, Thread.currentThread().getName(), e, channel, message,
				params, logFile));
	}

	@Override
	public synchronized void log(int level, int threshold, String threadName, Class<?> user, String methodName,
			Exception e, String channel, String message, Object... args) {
		if (shouldLog(level, threshold)) {
			printToFile(level, user, methodName, logFile, e, channel, message, args);
		}
	}
}
