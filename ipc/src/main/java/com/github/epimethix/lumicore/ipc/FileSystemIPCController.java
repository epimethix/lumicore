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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * This implementation of {@code IPCController} (Inter-Process Communication)
 * uses a file system directory as shared memory. the messages are stored in
 * temporary files that are deleted upon delivery.
 * <p>
 * There can be one {@code FileSystemIPCController} per directory.
 * 
 * @author epimethix
 *
 */
public class FileSystemIPCController implements IPCController {

	private static Map<String, IPCController> ipcControllers = new HashMap<>();

	/**
	 * Creates a {@code FileSystemIPCController} for the specified directory or
	 * retrieves the one already registered under this directory.
	 * <p>
	 * The IPC Messages will be encoded using UTF-8.
	 * <p>
	 * there can only be one {@code FileSystemIPCController} per directory.
	 * 
	 * @param messagesDirectory the shared directory
	 * @param mode              either {@code IPCController.Mode#SENDER} or
	 *                          {@code IPCController.Mode#RECEIVER}
	 * @return the {@code IPCController}
	 * @throws IOException
	 */
	public static final IPCController getIPCController(Path messagesDirectory, IPCController.Mode mode)
			throws IOException {
		return getIPCController(messagesDirectory, mode, StandardCharsets.UTF_8);
	}

	/**
	 * Creates a {@code FileSystemIPCController} for the specified directory or
	 * retrieves the one already registered under this directory.
	 * <p>
	 * there can only be one {@code FileSystemIPCController} per directory.
	 * 
	 * @param messagesDirectory the shared directory
	 * @param mode              either {@code IPCController.Mode#SENDER} or
	 *                          {@code IPCController.Mode#RECEIVER}
	 * @param charset           The Charset to use for writing the IPC Messages
	 * @return the {@code IPCController}
	 * @throws IOException
	 */
	public static final IPCController getIPCController(Path messagesDirectory, IPCController.Mode mode, Charset charset)
			throws IOException {
		IPCController ipcController = ipcControllers.get(messagesDirectory.toString());
		if (Objects.isNull(ipcController)) {
			ipcController = new FileSystemIPCController(messagesDirectory, charset, mode);
			ipcControllers.put(messagesDirectory.toString(), ipcController);
		}
		return ipcController;
	}

	private final Set<MessageListener> messageListeners = new HashSet<>();

	private final Path messagesDirectory;

	private final WatchService service;

	private final Charset charset;

	private final IPCController.Mode mode;

	private FileSystemIPCController(Path messagesDirectory, IPCController.Mode mode) throws IOException {
		this(messagesDirectory, StandardCharsets.UTF_8, mode);
	}

	private FileSystemIPCController(Path messagesDirectory, Charset charset, IPCController.Mode mode)
			throws IOException {
		this.messagesDirectory = Objects.requireNonNull(messagesDirectory);
		if (!Files.exists(messagesDirectory)) {
			Files.createDirectories(messagesDirectory);
		}
		if (!Files.isDirectory(messagesDirectory)) {
			throw new IllegalArgumentException("messagesDirectory must be a directory!");
		}
		this.charset = charset;
		this.mode = mode;
		if (mode == Mode.RECEIVER) {
			service = FileSystems.getDefault().newWatchService();
			messagesDirectory.register(service, StandardWatchEventKinds.ENTRY_MODIFY);
			startMessageReceiverThread();
		} else {
			service = null;
		}
	}

	private void startMessageReceiverThread() {
		Thread t = new Thread(() -> {
			do {
				WatchKey wk;
				try {
					wk = service.take();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					continue;
				}
				for (WatchEvent<?> e : wk.pollEvents()) {
					Kind<?> kind = e.kind();
					if (kind == StandardWatchEventKinds.OVERFLOW) {
						continue;
					}
					/*
					 * Oracle example does it like that...
					 */
					@SuppressWarnings("unchecked")
					WatchEvent<Path> wep = (WatchEvent<Path>) e;
					Path name = wep.context();
					Path file = messagesDirectory.resolve(name);
					StringBuilder messageBuilder = new StringBuilder();
					try {
						for (String m : Files.readAllLines(file, charset)) {
							messageBuilder.append(m).append("\n");
						}
						String message = messageBuilder.toString().trim();
						for (MessageListener ml : messageListeners) {
							ml.receive(message);
						}
						Files.delete(file);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				if (!wk.reset()) {
					break;
				}
			} while (true);
			System.err.println("FileSystemIPCController Exited!");
		});
		t.setName("FileSystem IPC Controller Message Receiver Thread");
		t.start();
	}

	@Override
	public final boolean addMessageListener(MessageListener messageListener) {
		return messageListeners.add(messageListener);
	}

	@Override
	public final boolean removeMessagesListener(MessageListener messageListener) {
		return messageListeners.remove(messageListener);
	}

	@Override
	public final void putMessage(String message) {
		if (mode != Mode.RECEIVER) {
			try {
				Files.writeString(messagesDirectory.resolve(UUID.randomUUID().toString()), message, charset);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.printf("Message to '%s' not sent because currently in RECEIVER mode:%n%s",
					messagesDirectory.toString(), message);
		}
	}
}
