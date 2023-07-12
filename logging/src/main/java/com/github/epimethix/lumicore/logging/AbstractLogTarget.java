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
package com.github.epimethix.lumicore.logging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.github.epimethix.lumicore.logging.Log.Message;

/**
 * {@code AbstractLogTarget}
 * 
 * @author epimethix
 *
 */
public abstract class AbstractLogTarget {
	/**
	 * Message in enqueueable container
	 */
	private static final class MessageStream {
		private final Message message;
		private final PrintStream stream;

		public MessageStream(Message message, PrintStream stream) {
			this.message = message;
			this.stream = stream;
		}
	}

	private static int QI = 1;

	private final class MessageQueue {
		private final BlockingQueue<MessageStream> queue;

		private MessageQueue() {
//			System.err.println("init MessageQueue " + QI++);
			this.queue = new LinkedBlockingQueue<>();
			Thread t = new Thread(() -> {
				do {
					try {
						Thread.sleep(333L);
						MessageStream messageStream;
						if (Objects.nonNull(messageStream = getMessage())) {
//							if(getClass().getName().equals(FileLogTarget.class.getName())) {
							if (Objects.nonNull(messageStream.stream)) {
								doPrintMessage(messageStream.stream, messageStream.message);
								while (Objects.nonNull(messageStream = getMessage())) {
									doPrintMessage(messageStream.stream, messageStream.message);
								}
							} else if (Objects.nonNull(messageStream.message.getFile())) {
								try (FileOutputStream os = new FileOutputStream(messageStream.message.getFile(), true);
										PrintStream ps = new PrintStream(os, true, charset)) {
//									printMessage(messageStream.message.getLevel(), ps,
//											messageStream.message.getThreadName(), messageStream.message.getUser(),
//											messageStream.message.getMethodName(), messageStream.message.getE(),
//											messageStream.message.getChannel(), messageStream.message.getMessage(),
//											messageStream.message.getParams());
									doPrintMessage(ps, messageStream.message);
									while (Objects.nonNull(messageStream = getMessage())) {
										doPrintMessage(ps, messageStream.message);
//										printMessage(messageStream.message.getLevel(), ps,
//												messageStream.message.getThreadName(), messageStream.message.getUser(),
//												messageStream.message.getMethodName(), messageStream.message.getE(),
//												messageStream.message.getChannel(), messageStream.message.getMessage(),
//												messageStream.message.getParams());
									}
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} while (true);
			});
			String name = "Log Printer Thread " + (QI++) + " " + AbstractLogTarget.this.toString();
			t.setName(name);
			t.start();
			System.out.println("Started " + name);
		}

		private boolean putMessage(PrintStream ps, Message message) {
			return this.queue.offer(new MessageStream(message, ps));
		}

		private MessageStream getMessage() {
			return this.queue.poll();
		}
	}

	private final DateTimeFormatter dateTimeFormatter;

	private final List<String> responsibilities;

	private int threshold;

	private int ceiling;

	private final MessageQueue queue;

	private final Charset charset;
	
	private final String name;

	public AbstractLogTarget(String name) {
		this(name, StandardCharsets.UTF_8);
	}

	public AbstractLogTarget(String name, Charset charset) {
		this.name = name;
		this.charset = charset;
		responsibilities = new ArrayList<>();
		threshold = ceiling = Log.DEFAULT;
		dateTimeFormatter = DateTimeFormatter.ofPattern("u/MM/dd HH:mm:ss");
		queue = new MessageQueue();
	}

	/*
	 * 
	 */
	/**
	 * This method can be implemented to create a custom log target.
	 * <p>
	 * call {@link AbstractLogTarget#shouldLog(int, int)} to determine if a message
	 * should actually be logged.
	 * <p>
	 * call
	 * {@code AbstractLogTarget#printMessage(int, PrintStream, Class, Exception, String, String, Object...)}
	 * to print a message.
	 * <p>
	 * use {@link Message} to store messages.
	 * 
	 * @param level     the message level.
	 * @param threshold the threshold as specified by the logger.
	 * @param user      the user class
	 * @param e         the exception (optional / may be null)
	 * @param channel   the channel name (optional / may be null)
	 * @param message   the message (format) (optional / may be null)
	 * @param args      the format arguments for the message.
	 */
	public abstract void log(int level, int threshold, String threadName, Class<?> user, String methodName, Exception e,
			String channel, String message, Object... args);

	/**
	 * The threshold is the minimum required message level for a message to be
	 * included; messages of the specified threshold level will be included. The
	 * actual threshold when examining a message will be either the logger threshold
	 * or the log target threshold, whichever is greater.
	 * <p>
	 * To let the loggers specify the threshold this value is initially
	 * {@link Log#DEFAULT}.
	 * 
	 * @param threshold
	 * @return this {@code AbstractLogTarget}
	 */
	public final AbstractLogTarget setThreshold(int threshold) {
		this.threshold = threshold;
		return this;
	}

	/**
	 * The ceiling is the maximum allowed message level; so messages of the
	 * specified level will be included but no messages with a greater message level
	 * than that.
	 * <p>
	 * To disable the ceiling by default this value is initially
	 * {@link Log#DEFAULT}.
	 * 
	 * @param ceiling
	 * @return this {@code AbstractLogTarget}
	 */
	public final AbstractLogTarget setCeiling(int ceiling) {
		this.ceiling = ceiling;
		return this;
	}

	public final int getThreshold() {
		return threshold;
	}

	public final int getCeiling() {
		return ceiling;
	}

	/**
	 * Capturing Responsibilities
	 * <p>
	 * source can be:
	 * <p>
	 * 1) a package name.
	 * <p>
	 * 2) a (full) class name.
	 * <p>
	 * 3) an asterisk (*) to capture all user classes (except channeled).
	 * <p>
	 * 4) a channel name beginning with question mark (?) to capture the channel
	 * (this will only happen if the user class is captured too).
	 * <p>
	 * 4) a question mark followed by an asterisk (?*) to capture all channels (this
	 * will only happen if the user class is captured too).
	 * <p>
	 * 5) a channel name beginning with asterisk and question mark (*?) to capture
	 * all user classes on this channel.
	 * <p>
	 * 6) an asterisk followed by question mark and asterisk again (*?*) to capture
	 * all messages within threshold and ceiling.
	 * <p>
	 * 7) a full class name followed by question mark (?) and a channel name to
	 * capture a specific channel of a specific user class.
	 * 
	 * @param source the source to capture
	 * @return this {@code AbstractLogTarget}
	 */
	public final AbstractLogTarget captureResponsibility(String source) {
		if (!responsibilities.contains(source)) {
			responsibilities.add(source);
		}
		return this;
	}

	public final AbstractLogTarget removeResponsibility(String source) {
		if (responsibilities.contains(source)) {
			responsibilities.remove(source);
		}
		return this;
	}

	public final void clearResponsibilities() {
		responsibilities.clear();
	}

	final boolean isResponsibleFor(Class<?> user, String channel) {
		if (responsibilities.contains("*?*")) {
			return true;
		}
		String className = user.getName();
		boolean isResponsibleForChannel = false;
		if (Objects.isNull(channel) || responsibilities.contains("?*")) {
			isResponsibleForChannel = true;
		} else {
			String userName = String.format("%s?%s", className, channel);
			if (responsibilities.contains(userName)) {
				return true;
			}
			channel = "?".concat(channel);
			if (responsibilities.contains(channel)) {
				isResponsibleForChannel = true;
			} else {
				String allUsersOfChannel = "*".concat(channel);
				if (responsibilities.contains(allUsersOfChannel)) {
					return true;
				}
			}
		}
		if (isResponsibleForChannel) {
			if (responsibilities.contains("*")) {
				return true;
			}
			for (String responsibility : responsibilities) {
				if (className.startsWith(responsibility)) {
					return true;
				}
			}
		}
		return false;
	}

	protected void printMessage(int level, PrintStream ps, String threadName, Class<?> user, String methodName,
			Exception e, String channel, String message, Object... params) {
		printMessage(ps, new Message(level, user, methodName, threadName, e, channel, message, params));
	}

	protected void printMessage(int level, File logFile, String threadName, Class<?> user, String methodName,
			Exception e, String channel, String message, Object... params) {
		printMessage(null, new Message(level, user, methodName, threadName, e, channel, message, params));
	}

	protected void printMessage(PrintStream ps, Message message) {
		queue.putMessage(ps, message);
//		if (Objects.nonNull(message.getFile())) {
//			try (FileOutputStream os = new FileOutputStream(message.getFile(), true);
//					PrintStream ps1 = new PrintStream(os, true, charset)) {
//				doPrintMessage(ps1, message);
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//		} else {
//			doPrintMessage(ps, message);
//		}
	}

	private final void doPrintMessage(PrintStream ps, Message message) {
//		if (AbstractLogTarget.this instanceof FileLogTarget) {
//			System.err.println("do print file log target");
//		}
		String channel = message.getChannel();
		// @formatter:off
		ps.printf("%s [%s] %s %s::%s%s:", 
				message.getTime().format(dateTimeFormatter),
				message.getThreadName(),
				getLevelLabel(message.getLevel()),
				message.getUser().getSimpleName(), 
				message.getMethodName(),
				Objects.nonNull(channel) && channel.trim().length() > 0 ? "?".concat(channel) : "");
		// @formatter:on
		if (Objects.nonNull(message.getMessage())) {
			ps.printf(" %s", String.format(message.getMessage(), message.getParams()));
		}
		ps.println();
		if (Objects.nonNull(message.getE())) {
			message.getE().printStackTrace(ps);
		}
	}

	private final String getLevelLabel(int level) {
		switch (level) {
		case Log.TRACE:
			return "TRACE";
		case Log.DEBUG:
			return "DEBUG";
		case Log.INFO:
			return "INFO";
		case Log.WARN:
			return "WARN";
		case Log.CRITICAL:
			return "CRITICAL";
		default:
			return "ERROR";
		}
	}

	protected boolean shouldLog(int messageLevel, int loggerThreshold) {
		if (messageLevel < Math.max(threshold, loggerThreshold)) {
			return false;
		}
		if (ceiling != Log.DEFAULT && messageLevel > ceiling) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%s[%s]", getClass().getSimpleName(), name);
	}
}
