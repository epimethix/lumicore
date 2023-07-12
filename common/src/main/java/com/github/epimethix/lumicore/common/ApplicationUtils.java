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
package com.github.epimethix.lumicore.common;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.lang3.SystemUtils;

import com.github.epimethix.lumicore.stackutil.StackUtils;

public final class ApplicationUtils {
	public static final String RUNNING_DIRECTORY;

	static {
		RUNNING_DIRECTORY = getRunningDirectory(Application.class);
//		System.err.println(RUNNING_DIRECTORY);
	}

	public static final String createBanner(String text) {

		String bar = "#############################################";
		String nameBar = "###";

		String format = "%s%n";
		StringBuilder sb = new StringBuilder();

		sb.append(String.format(format, bar));

		sb.append(nameBar);

		int space = bar.length() - 2 * nameBar.length();

		int rest = space - text.length();

		if (rest > 0) {
			if (rest % 2 != 0) {
				rest--;
				space--;
				sb.append(" ");
			}
			for (int i = 0; i < 2; i++) {
//				for (int j = 0; j < rest / 2; j++) {
//					sb.append(" ");
//				}
				sb.append(" ".repeat(rest / 2));
				if (i == 0) {
					sb.append(text);
				}
			}
			sb.append(nameBar).append(String.format("%n"));
		} else {
			sb.append(" ").append(String.format(format, text));
		}

		sb.append(bar);

		return sb.toString();
	}

	public static String getRunningDirectory(Class<?> cls) {
		String runningDir = null;
		try {
			runningDir = URLDecoder.decode(cls.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
			File runningDirFile = new File(runningDir);
			if (runningDirFile.isFile()) {
				runningDir = runningDirFile.getParentFile().getPath();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return runningDir;
	}

	public static boolean lockSingleInstance(File lockFile) {
		try {
			if (!lockFile.getParentFile().exists()) {
				lockFile.getParentFile().mkdirs();
			}
			final RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
			final FileChannel channel = raf.getChannel();
			final FileLock lock = channel.tryLock();
			if (Objects.isNull(lock)) {
				try {
					channel.close();
					raf.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
//				SwingUtilities.invokeAndWait(() -> {
//					String message = String.format(A.getLabel(A.ALREADY_RUNNING_MESSAGE, applicationName));
//					JOptionPane optionPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE,
//							JOptionPane.DEFAULT_OPTION);
//					JDialog dialog = optionPane.createDialog("Error");
//					dialog.setVisible(true);
//					System.exit(1);
//				});
				return false;
			} else {
				Runtime.getRuntime().addShutdownHook(new ReleaseApplicationLockThread(lock, channel, raf));
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO revise decision to exit?
			System.exit(1);
		}
		return true;
	}

	private final static class ReleaseApplicationLockThread extends Thread {
		private final FileLock lock;
		private final Channel channel;
		private final RandomAccessFile raf;

		public ReleaseApplicationLockThread(FileLock lock, Channel channel, RandomAccessFile raf) {
			this.lock = lock;
			this.channel = channel;
			this.raf = raf;
		}

		@Override
		public void run() {
			try {
				lock.release();
				channel.close();
				raf.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String classNamesToString(Collection<? extends Class<?>> classes) {
		StringBuilder classNamesStringBuilder = new StringBuilder();
		int i = 0;
		for (Class<?> cls : classes) {
			classNamesStringBuilder.append(cls.getSimpleName());
			if (++i < classes.size()) {
				classNamesStringBuilder.append(", ");
			}
		}
		return classNamesStringBuilder.toString();
	}

	public static File getNestingDirectory(String nestingDirName) {
		File nestingDir = null;
		try {
			Class<?> callerClass = StackUtils.getCallerClass();
			File location = new File(
					URLDecoder.decode(callerClass.getProtectionDomain().getCodeSource().getLocation().getPath(),
							StandardCharsets.UTF_8.name()));
			File roamingNestingDir;
			if (location.getName().endsWith(".jar")) {
				roamingNestingDir = new File(location.getParentFile(), nestingDirName);
			} else {
				roamingNestingDir = new File(location, nestingDirName);
			}
			if (roamingNestingDir.exists()) {
				nestingDir = roamingNestingDir;
			}
		} catch (UnsupportedEncodingException e) {}

		if (Objects.isNull(nestingDir)) {
			File osNestingDir;
			if (nestingDirName.startsWith(".")) {
				osNestingDir = new File(SystemUtils.USER_HOME, nestingDirName);
			} else {
				osNestingDir = new File(SystemUtils.USER_HOME, "." + nestingDirName);
			}
			if (SystemUtils.IS_OS_WINDOWS) {
				String appData = System.getenv("AppData");
				if (Objects.nonNull(appData) && appData.trim().length() > 0) {
					osNestingDir = Paths.get(appData, nestingDirName).toFile();
				}
			} else if (SystemUtils.IS_OS_MAC) {
				File appSupport = Paths.get(SystemUtils.USER_HOME, "Library/ApplicationSupport").toFile();
				if (appSupport.exists() && appSupport.isDirectory()) {
					osNestingDir = new File(appSupport, nestingDirName);
				} else {
					appSupport = Paths.get(SystemUtils.USER_HOME, "Library/Application Support").toFile();
					if (appSupport.exists() && appSupport.isDirectory()) {
						osNestingDir = new File(appSupport, nestingDirName);
					}
				}
			} else if (SystemUtils.IS_OS_LINUX) {
				String dataHome = System.getenv("XDG_DATA_HOME");
				if (Objects.isNull(dataHome) || dataHome.trim().isEmpty()) {
					dataHome = SystemUtils.getUserHome().getPath() + "/.local/share";
				}
				osNestingDir = Paths.get(dataHome, nestingDirName).toFile();
			}
			nestingDir = osNestingDir;
		}
		if (!nestingDir.exists()) {
			nestingDir.mkdirs();
		}
		return nestingDir;
	}

	private ApplicationUtils() {}
}
