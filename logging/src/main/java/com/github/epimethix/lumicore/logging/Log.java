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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.epimethix.lumicore.logging.target.ConsoleLogTarget;
import com.github.epimethix.lumicore.profile.Profile;
import com.github.epimethix.lumicore.properties.LumicoreProperties;
import com.github.epimethix.lumicore.stackutil.AccessCheck;
import com.github.epimethix.lumicore.stackutil.CallerSensitive;
import com.github.epimethix.lumicore.stackutil.StackUtils;

public class Log {

	public static class LoggerImplementation implements Logger {

		private final Class<?> user;

		private int threshold;

		private List<AbstractLogTarget> targets;

		private String channel;

		private LoggerImplementation(Class<?> user) {
			this.user = user;
			this.threshold = DEFAULT;
			this.targets = new ArrayList<>();
		}

//		public String getUserName() {
//			if (Objects.isNull(channel) || channel.trim().length() == 0) {
//				return user.getName();
//			} else {
//				return String.format("%s?%s", user.getName(), channel);
//			}
//		}

//		private void log(int level, String channel, String message, Object... args) {
//			log(level, channel, null, message, args);
//		}

		private void log(int level, String channel, Exception e, String message, Object... args) {
			if (threshold < SILENT) {
				StackTraceElement ste = StackUtils.getCallerStackTraceElement(1);
				String methodName = ste.getMethodName();
				for (AbstractLogTarget target : targets) {
					if (target.getThreshold() < SILENT) {
						target.log(level, threshold, Thread.currentThread().getName(), user, methodName, e, channel,
								message, args);
					}
				}
			}
		}

		@Override
		public void trace(String message, Object... params) {
			log(Log.TRACE, channel, null, message, params);
		}

		@Override
		public void debug(String message, Object... params) {
			log(Log.DEBUG, channel, null, message, params);
		}

		@Override
		public void info(String message, Object... params) {
			log(Log.INFO, channel, null, message, params);
		}

		@Override
		public void warn(String message, Object... params) {
			log(Log.WARN, channel, null, message, params);
		}

		@Override
		public void critical(Exception e, String message, Object... params) {
			log(Log.CRITICAL, channel, e, message, params);
		}

		@Override
		public void error(Exception e, String message, Object... params) {
			log(Log.ERROR, channel, e, message, params);
		}

		@Override
		public Logger setThreshold(int threshold) {
			this.threshold = threshold;
			return this;
		}

		@Override
		public Logger setChannelName(String channelName) {
			if (channelName.startsWith("?")) {
				this.channel = channelName.substring(1);
			} else {
				this.channel = channelName;
			}
			return this;
		}
	}

	public final static class Message {
		private final static AbstractLogTarget STRING_LOG_TARGET = new AbstractLogTarget("STRING_LOG_TARGET") {
			@Override
			public void log(int level, int threshold, String threadName, Class<?> user, String methodName, Exception e,
					String channel, String message, Object... args) {}
		};
		private final int level;
		private final LocalDateTime time;
		private final Class<?> user;
		private final String methodName;
		private final Exception e;
		private final String channel;
		private final String message;
		private final Object[] params;
		private final String threadName;
		private final File file;

		public Message(int level, Class<?> user, String methodName, String threadName, Exception e, String channel,
				String message, Object[] params) {
			this(level, user, methodName, threadName, e, channel, message, params, null);
		}
		
		public Message(int level, Class<?> user, String methodName, String threadName, Exception e, String channel,
				String message, Object[] params, File file) {
			this.level = level;
			this.time = LocalDateTime.now();
			this.user = user;
			this.methodName = methodName;
			this.threadName = threadName;
			this.e = e;
			this.channel = channel;
			this.message = message;
			this.params = params;
			this.file = file;
		}

		public int getLevel() {
			return level;
		}

		public LocalDateTime getTime() {
			return time;
		}

		public Class<?> getUser() {
			return user;
		}

		public String getMethodName() {
			return methodName;
		}

		public Exception getE() {
			return e;
		}

		public String getChannel() {
			return channel;
		}

		public String getMessage() {
			return message;
		}

		public Object[] getParams() {
			return params;
		}

		public String getThreadName() {
			return threadName;
		}

		public File getFile() {
			return file;
		}

		public void printMessage(PrintStream ps) {
//			STRING_LOG_TARGET.printMessage(level, ps, time, "", user, methodName, e, channel, message, params);
			STRING_LOG_TARGET.printMessage(level, ps, threadName, user, methodName, e, channel, message, params);
		}

		public String toString(Charset charset) {
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				String charsetName = charset.name();
				try (PrintStream ps = new PrintStream(out, true, charsetName)) {
					this.printMessage(ps);
				}
				return out.toString(charsetName);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return "Message [level=" + level + ", user=" + user + ", methodName=" + methodName + ", e=" + e
					+ ", channel=" + channel + ", message=" + message + ", params=" + Arrays.toString(params) + "]";
		}

		@Override
		public String toString() {
			return toString(StandardCharsets.UTF_8);
		}
	}

	public static final int DEFAULT = -1;
	public static final int TRACE = 1;
	public static final int DEBUG = 2;
	public static final int INFO = 3;
	public static final int WARN = 4;
	public static final int CRITICAL = 5;
	public static final int ERROR = 6;
	public static final int SILENT = 7;

	public static final String CHANNEL_IOC = "lumicore-ioc";
	public static final String CHANNEL_ORM = "lumicore-orm";
	public static final String CHANNEL_SWING = "lumicore-swing";

	private static final Map<Class<?>, List<LoggerImplementation>> LOGGERS = new HashMap<>();

	private static LoggerConfiguration loggerConfiguration;

	private static final List<AbstractLogTarget> LOG_TARGETS;

	private static final AbstractLogTarget DEFAULT_CONSOLE_LOG_TARGET;

	static {
//		System.out.println("init Log with profile " + Profile.getActiveProfileName());
		DEFAULT_CONSOLE_LOG_TARGET = new ConsoleLogTarget("DEFAULT");
		LOG_TARGETS = new ArrayList<>();
		try {
			if (Objects.nonNull(LumicoreProperties.LOGGER_CONFIGURATION)) {
				try {
					/*
					 * Wrapped in try / catch ClassCastException
					 */
					@SuppressWarnings("unchecked")
					Class<? extends LoggerConfiguration> cls = (Class<? extends LoggerConfiguration>) Class
							.forName(LumicoreProperties.LOGGER_CONFIGURATION);
					LoggerConfiguration c = cls.getConstructor().newInstance();
					loggerConfiguration = c;
				} catch (InstantiationException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException | ClassCastException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			reconfigure();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private final static synchronized void putLogger(LoggerImplementation logger) {
		List<LoggerImplementation> loggers = Log.LOGGERS.get(logger.user);
		if (Objects.isNull(loggers)) {
			loggers = new ArrayList<>();
			Log.LOGGERS.put(logger.user, loggers);
		}
		loggers.add(logger);
	}

	@CallerSensitive
	public static synchronized Logger getLogger() {
		LoggerImplementation logger = new LoggerImplementation(StackUtils.getCallerClass());
		putLogger(logger);
		configureLogger(logger);
		return logger;
	}

	@CallerSensitive
	public static synchronized Logger getLogger(int threshold) {
		LoggerImplementation logger = new LoggerImplementation(StackUtils.getCallerClass());
		putLogger(logger);
		logger.setThreshold(threshold);
		configureLogger(logger);
		return logger;
	}

	@CallerSensitive
	public static synchronized Logger getLogger(String channelName) {
		LoggerImplementation logger = new LoggerImplementation(StackUtils.getCallerClass());
		putLogger(logger);
		logger.setChannelName(channelName);
		configureLogger(logger);
		return logger;
	}

	@CallerSensitive
	public static synchronized Logger getLogger(int threshold, String channelName) {
		LoggerImplementation logger = new LoggerImplementation(StackUtils.getCallerClass());
		putLogger(logger);
		logger.setThreshold(threshold).setChannelName(channelName);
		configureLogger(logger);
		return logger;
	}

	public static void configure(LoggerConfiguration loggerConfiguration) {
		Log.loggerConfiguration = Objects.requireNonNull(loggerConfiguration);
		try {
			reconfigure();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void reconfigure() throws IllegalAccessException {
		AccessCheck.allowCaller(true, "com.github.epimethix.lumicore.ioc.Lumicore::");
		DEFAULT_CONSOLE_LOG_TARGET.clearResponsibilities();
		if (Profile.getActiveProfile() < Profile.PRODUCTION) {
			DEFAULT_CONSOLE_LOG_TARGET.captureResponsibility("*");
			DEFAULT_CONSOLE_LOG_TARGET.setThreshold(DEFAULT);
			if (LumicoreProperties.IOC_VERBOSE) {
				DEFAULT_CONSOLE_LOG_TARGET.captureResponsibility("?" + CHANNEL_IOC);
			}
			if (LumicoreProperties.ORM_VERBOSE) {
				DEFAULT_CONSOLE_LOG_TARGET.captureResponsibility("?" + CHANNEL_ORM);
			}
			if (LumicoreProperties.SWING_VERBOSE) {
				DEFAULT_CONSOLE_LOG_TARGET.captureResponsibility("?" + CHANNEL_SWING);
			}
		} else {
			DEFAULT_CONSOLE_LOG_TARGET.setThreshold(SILENT);
		}
		LOG_TARGETS.clear();
		LOG_TARGETS.add(DEFAULT_CONSOLE_LOG_TARGET);
		if (Objects.nonNull(loggerConfiguration)) {
			loggerConfiguration.configureDefaultConsoleLogTarget(DEFAULT_CONSOLE_LOG_TARGET);
//			LOG_TARGETS.clear();
//			LOG_TARGETS.add(DEFAULT_CONSOLE_LOG_TARGET);
			List<AbstractLogTarget> targets = loggerConfiguration.createLogTargets();
			if (Objects.nonNull(targets)) {
				LOG_TARGETS.addAll(targets);
			}
			Set<Class<?>> keySet = LOGGERS.keySet();
			for (Class<?> key : keySet) {
				List<LoggerImplementation> list = LOGGERS.get(key);
				for (LoggerImplementation logger : list) {
					configureLogger(logger);
				}
			}
		}
	}

	private static void configureLogger(LoggerImplementation logger) {
		logger.targets.clear();
		if (Objects.nonNull(loggerConfiguration)) {
			loggerConfiguration.configureLogger(logger, logger.user, logger.channel);
		}
		for (AbstractLogTarget target : LOG_TARGETS) {
			if (target.isResponsibleFor(logger.user, logger.channel)) {
				logger.targets.add(target);
			}
		}
	}

	public static boolean wasConfigured() {
		return Objects.nonNull(loggerConfiguration);
	}

	private Log() {}
}
