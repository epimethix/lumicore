/*
 *  Accounting - Lumicore example application
 *  Copyright (C) 2023  epimethix@protonmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.epimethix.accounting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.github.epimethix.lumicore.common.Reflect;
import com.github.epimethix.lumicore.logging.AbstractLogTarget;
import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;
import com.github.epimethix.lumicore.logging.LoggerConfiguration;
import com.github.epimethix.lumicore.logging.target.FileLogTarget;
import com.github.epimethix.lumicore.orm.SQLRepository;
import com.github.epimethix.lumicore.profile.Profile;

public class ExampleLoggerConfiguration extends LoggerConfiguration {
	private final static String LUMICORE_FRAMEWORK_PACKAGE_NAME = "com.github.epimethix.lumicore";
	private final static String APPLICATION_PACKAGE_NAME = Accounting.class.getPackageName();
	private List<AbstractLogTarget> logTargets;

	public ExampleLoggerConfiguration() {}

	@Override
	public List<AbstractLogTarget> createLogTargets() {
		if (Objects.isNull(logTargets)) {
			/*
			 * error.log: capture ALL messages from ALL channels of and above threshold
			 * CRITICAL (+ ERROR).
			 */
			FileLogTarget fltError = new FileLogTarget(AppFiles.ERROR_LOG_FILE, true);
			fltError.captureResponsibility("*?*");
			fltError.setThreshold(Log.CRITICAL);
			/*
			 * diagnostics.log: capture messages from within the app starting from TRACE
			 * including WARN at most. (excluding CRITICAL and ERROR)
			 */
			FileLogTarget fltAppDiagnostics = new FileLogTarget(AppFiles.DIAGNOSTICS_LOG_FILE, true);
			fltAppDiagnostics.captureResponsibility("com.github.epimethix.lumicoreexample");
			fltAppDiagnostics.setThreshold(Log.TRACE);
			fltAppDiagnostics.setCeiling(Log.WARN);
			/*
			 * framework_diagnostics.log
			 */
			FileLogTarget fltFrameworkDiagnostics = new FileLogTarget(AppFiles.FRAMEWORK_DIAGNOSTICS_LOG_FILE, true);
			// base package of all framework classes
			fltFrameworkDiagnostics.captureResponsibility("com.github.epimethix.lumicore.");
			// all channels
			fltFrameworkDiagnostics.captureResponsibility("?*");
			fltFrameworkDiagnostics.setThreshold(Log.TRACE);
			fltFrameworkDiagnostics.setCeiling(Log.WARN);
			/*
			 * channel_orm.log: capture ALL messages from the channel "lumicore-orm"
			 */
			FileLogTarget fltChannelORM = new FileLogTarget(AppFiles.CHANNEL_ORM_LOG_FILE, true);
			fltChannelORM.captureResponsibility("*?" + Log.CHANNEL_ORM);
			/*
			 * return log targets
			 */
			logTargets = Collections.unmodifiableList(
					Arrays.asList(fltError, fltAppDiagnostics, fltFrameworkDiagnostics, fltChannelORM));
		}
		return logTargets;
	}

	@Override
	public void configureDefaultConsoleLogTarget(AbstractLogTarget defaultConsoleLogTarget) {
		defaultConsoleLogTarget.setThreshold(Log.TRACE);
		defaultConsoleLogTarget.captureResponsibility("?"+Log.CHANNEL_ORM);
		if (System.currentTimeMillis() > 0) {
			return;
		}
		/*
		 * by default defaultConsoleLogTarget has the responsibility "*". this code
		 * reduces the responsibilities of the default console log target to the user
		 * project.
		 */
		defaultConsoleLogTarget.removeResponsibility("*");
		defaultConsoleLogTarget.captureResponsibility("com.github.epimethix.lumicoreexample");
	}

	@Override
	public void configureLogger(Logger logger, Class<?> user, String channel) {
		/*
		 * This method is only dead code for example purposes.
		 */
		if (System.currentTimeMillis() > 0) {
			return;
		}
		/*
		 * Set specific to current profile
		 */
		if (Profile.isProfileActive(Profile.DEBUGGING)) {
			logger.setThreshold(Log.TRACE);
		} else if (Profile.getActiveProfile() < Profile.PRODUCTION) {
			logger.setThreshold(Log.WARN);
		} else {
			logger.setThreshold(Log.CRITICAL);
		}
		/*
		 * select lumicore framework loggers
		 */
		if (user.getName().startsWith(LUMICORE_FRAMEWORK_PACKAGE_NAME)) {
			logger.setThreshold(Log.WARN);
		}
		/*
		 * select application loggers
		 */
		if (user.getName().startsWith(APPLICATION_PACKAGE_NAME)) {
			logger.setThreshold(Log.INFO);
		}
		/*
		 * globally set a threshold:
		 */
		logger.setThreshold(Log.WARN);
		/*
		 * set a threshold for a package
		 */
		if (user.getName().startsWith("package.name")) {
			logger.setThreshold(Log.INFO);
		}
		/*
		 * set a threshold for a channel
		 */
		if (Objects.nonNull(channel) && channel.equals("my-channel")) {
			logger.setThreshold(Log.TRACE);
		}
		/*
		 * set a channel name based on the type of user class
		 */
		if (Reflect.isComponent(user)) {
			logger.setChannelName("channel-components");
		} else if (Reflect.isSwingComponent(user)) {
			logger.setChannelName("channel-swing-components");
		}
		/*
		 * set a channel name for a package
		 */
		if (user.getPackageName().startsWith("some.package.name")) {
			logger.setChannelName("some-channel-name");
		} else if (user.getPackageName().startsWith("some.other.package.name")) {
			logger.setChannelName("some-other-channel-name");
		}
		/*
		 * change framework settings
		 */
		/*
		 * silence specific user
		 */
		if (SQLRepository.class.getName().equals(user.getName())) {
			logger.setThreshold(Log.SILENT);
		}
		/*
		 * set specific threshold to framework channel
		 */
		String ormChannelName = Log.CHANNEL_ORM;
		if (Objects.nonNull(channel) && ormChannelName.equals(channel)) {
			logger.setThreshold(Log.WARN);
		}
	}
}
