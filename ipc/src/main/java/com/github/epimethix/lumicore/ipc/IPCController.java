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
package com.github.epimethix.lumicore.ipc;

/**
 * IPC Controller stands for Inter-Process Communication Controller. It can be
 * used for sending Messages from one application instance to another. An IPC
 * Controller can either be in receiver (server) mode or in sender (client)
 * mode.
 * <p>
 * An IPC Controller could be used for an application that enforces a single
 * instance. Any additionally started instances would then pass the startup
 * arguments (files to open) to the already running instance via the
 * {@code IPCController} which then will act upon the message.
 * <p>
 * The first application instance should be using {@code Mode#RECEIVER} and all
 * instances that are started additionally should use {@code Mode#SENDER}, send
 * the message and then terminate.
 * <p>
 * For bi-directional IPC use two {@code IPCController}s.
 * 
 * @author epimethix
 * @see Mode
 * @see FileSystemIPCController
 */
public interface IPCController {
	public static enum Mode {
		SENDER, RECEIVER;// , SENDER_RECEIVER;
	}

	/**
	 * Adds the specified {@code MessageListener} to the controllers set of
	 * listeners to be notified of new messages.
	 * 
	 * @param messageListener the listener to be notified.
	 * 
	 * @return true if the listener was added, false if it was known already
	 */
	boolean addMessageListener(MessageListener messageListener);

	/**
	 * Removes the specified {@code MessageListener} from the set of listeners.
	 * 
	 * @param messageListener the listener to remove.
	 * 
	 * @return true if the listener was removed, false if it was not in the set.
	 */
	boolean removeMessagesListener(MessageListener messageListener);

	/**
	 * putMessage puts a sends a message into the shared memory. If the
	 * {@code IPCController} is in receiverMode this method should do nothing.
	 */
	void putMessage(String message);

}
