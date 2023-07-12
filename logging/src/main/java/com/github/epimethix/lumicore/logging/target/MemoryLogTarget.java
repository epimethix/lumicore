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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.github.epimethix.lumicore.logging.AbstractLogTarget;
import com.github.epimethix.lumicore.logging.Log.Message;

public class MemoryLogTarget extends AbstractLogTarget {
	private final List<Message> messages;

	public MemoryLogTarget(String name) {
		super(name);
		messages = new ArrayList<>();
	}

	@Override
	public void log(int level, int threshold, String threadName, Class<?> user, String methodName, Exception e,
			String channel, String message, Object... args) {
		if (shouldLog(level, threshold)) {
			messages.add(new Message(level, user, methodName, threadName, e, channel, message, args));
		}
	}

	public final int size() {
		return messages.size();
	}

	public final void clear() {
		messages.clear();
	}

	public final Message getMessage(int index) {
		return messages.get(index);
	}

	public final List<Message> getMessages() {
		return new ArrayList<>(messages);
	}

	public final void printMessages(PrintStream printStream) {
		for (Message message : messages) {
			message.printMessage(printStream);
		}
	}
}
