/*
 * Copyright 2021-2022 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.properties;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.Objects;

//import com.github.epimethix.lumicore.orm.annotation.field.BigDecimalScale;
//import com.github.epimethix.lumicore.utility.SQLiteUtils;
//import com.github.epimethix.lumicore.utility.log.LoggerConfiguration;

/**
 * <b>Configurability:</b>
 * <p>
 * put "lumicore.properties" in the default package of your resources
 * directory to configure the following behaviors:
 * <ul>
 * <li>Property "default-bigdecimal-scale" (number of fraction digits to store,
 * the global setting for mapping BigDecimal fields in case @BigDecimalScale is
 * not used) default value: 4
 * <li>Property "hashing-iterations" (the iterations for slow hashing) default
 * value: 64000
 * <li>Property "hashing-key-length" (the bit length of the hash for password
 * hashing) default value: 128
 * <li>Property "connection-policy" ("open" to keep the connection open after
 * operations) default value: "close"
 * <li>Property "allow-multiple-app-instances" ("true" to disable blocking
 * multiple application instances)
 * </ul>
 * 
 * @author epimethix
 *
 */
public final class LumicoreProperties {

	/*
	 * * * Application
	 */

	private static final String APPLICATION_TIME_ZONE_KEY = "application-time-zone";
	/**
	 * Configure this property with the key
	 * '{@value LumicoreProperties#APPLICATION_TIME_ZONE_KEY}'.
	 * <p>
	 * applicable values are valid input values for {@code ZoneId.of(String)}.
	 * <p>
	 * 
	 * @see ZoneId#of(String)
	 */
	public static final ZoneId APPLICATION_TIME_ZONE;

	/*
	 * * * Database
	 */

	/**
	 * The key "default-bigdecimal-scale"
	 */
	private static final String DEFAULT_SCALE_KEY = "default-bigdecimal-scale";
	/**
	 * The default scale used for mapping BigDecimal fields.
	 * <p>
	 * The default value is 4 if not configured SQLiteUtils (core) otherwise.
	 * 
	 */
	public static final int DEFAULT_BIGDECIMAL_SCALE;
	/**
	 * The key "connection-policy"
	 * <p>
	 * value: "open" to always keep the connection open.
	 */
	private static final String CONNECTION_POLICY_KEY = "connection-policy";
	/**
	 * close connection after operation?
	 */
	public static final boolean CLOSE_CONNECTION_AFTER_OPERATION;

	/**
	 * The key "obtain-immutable"
	 * <p>
	 * value: "yes" to always get immutable entity objects if possible.
	 */
	private static final String OBTAIN_IMMUTABLE_KEY = "obtain-immutable";
	/**
	 * obtain immutable entity by default?
	 */
	public static final boolean OBTAIN_IMMUTABLE;
	/**
	 * The key "default-query-limit"
	 * <p>
	 * value: "n" long.
	 */
	private static final String DEFAULT_QUERY_LIMIT_KEY = "default-query-limit";
	/**
	 * Default query limit?
	 */
	public static final long DEFAULT_QUERY_LIMIT;

	/*
	 * * * User management
	 */

	/**
	 * The key "hashing-iterations" ...
	 */
	private static final String HASHING_ITERATIONS_KEY = "hashing-iterations";
	/**
	 * The number of iterations for slow hashing
	 * <p>
	 * The default value is 64000 if not configured SQLiteUtils (core) otherwise.
	 * 
	 */
	public static final int HASHING_ITERATIONS;
	/**
	 * The key "hashing-key-length"
	 */
	private static final String HASHING_KEY_LENGTH_KEY = "hashing-key-length";
	/**
	 * The hash length for password hashing
	 * <p>
	 * The default value is 128 if not configured SQLiteUtils (core) otherwise.
	 * 
	 */
	public static final int HASHING_KEY_LENGTH;

	/*
	 * * * Logging
	 */

	private static final String LOGGER_CONFIGURATION_KEY = "logger-configuration";
	/**
	 * The logger configuration is required.
	 * <p>
	 * Configure this property with the key
	 * '{@value LumicoreProperties#LOGGER_CONFIGURATION_KEY}'.
	 * <p>
	 * applicable values are fully qualified class names pointing to an
	 * implementation of
	 * {@code com.github.epimethix.lumicore.logging.LoggerConfiguration}.
	 */
	public static final String LOGGER_CONFIGURATION;
	/**
	 * The key "ioc-verbose"
	 * <p>
	 * value: "on" to enable auto wire console output.
	 */
	private static final String IOC_VERBOSE_KEY = "ioc-verbose";
	/**
	 * auto wire output enabled?
	 */
	public static final boolean IOC_VERBOSE;
	/**
	 * The key "orm-verbose"
	 * <p>
	 * value: "on" to enable query console output.
	 */
	private static final String ORM_VERBOSE_KEY = "orm-verbose";
	/**
	 * query output enabled?
	 */
	public static final boolean ORM_VERBOSE;
	/**
	 * The key "swing-verbose"
	 * <p>
	 * value: "on" to enable query console output.
	 */
	private static final String SWING_VERBOSE_KEY = "swing-verbose";
	/**
	 * query output enabled?
	 */
	public static final boolean SWING_VERBOSE;

	/*
	 * * * Swing: lumicore:swing/com.github.epimethix.lumicore.swing.UIUtils
	 */

//	private final static ResourceBundle PROPERTIES;

	private static final boolean isActive(final String option) {
		switch (option.toLowerCase()) {
		case "yes":
		case "on":
		case "true":
		case "1":
		case "open":
			return true;
		default:
			return false;
		}
	}

	/*
	 * Initialize constants based on possible configuration
	 */
	static {
		PropertiesFile x = null;
		try {
			x = PropertiesFile.getProperties("lumicore");
		} catch (Exception e) {}
		PropertiesFile PROPERTIES = x;
		ZoneId appTimeZone = ZoneId.systemDefault();
		int defaultScale = 4;
		boolean closeConnection = false;
		boolean obtainImmutable = true;
		long defaultQueryLimit = 100L;
		int hashingIterations = 64000;
		int hashingKeyLength = 128;
		String loggerConfiguration = null;
		boolean iocVerbose = false;
		boolean sqliteVerbose = false;
		boolean swingVerbose = false;
		if (Objects.nonNull(PROPERTIES)) {
			/*
			 * 1) Application
			 */
			if (PROPERTIES.containsKey(APPLICATION_TIME_ZONE_KEY)) {
				String zoneId = PROPERTIES.getProperty(APPLICATION_TIME_ZONE_KEY);
				if (Objects.nonNull(zoneId) && zoneId.trim().length() > 0) {
					try {
						appTimeZone = ZoneId.of(zoneId);
					} catch (ZoneRulesException e) {
						e.printStackTrace();
					} catch (DateTimeException e) {
						e.printStackTrace();
					}
				}
			}
			/*
			 * 2) Database
			 */
			if (PROPERTIES.containsKey(DEFAULT_SCALE_KEY)) {
				try {
					defaultScale = Integer.parseInt(PROPERTIES.getProperty(DEFAULT_SCALE_KEY));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			if (PROPERTIES.containsKey(CONNECTION_POLICY_KEY)) {
				String policy = PROPERTIES.getProperty(CONNECTION_POLICY_KEY);
				closeConnection = !isActive(policy);
			}
			if (PROPERTIES.containsKey(OBTAIN_IMMUTABLE_KEY)) {
				String value = PROPERTIES.getProperty(OBTAIN_IMMUTABLE_KEY);
				obtainImmutable = isActive(value);
			}
			if (PROPERTIES.containsKey(DEFAULT_QUERY_LIMIT_KEY)) {
				String value = PROPERTIES.getProperty(DEFAULT_QUERY_LIMIT_KEY);
				try {
					defaultQueryLimit = Long.parseLong(value);
				} catch (NumberFormatException e) {
					System.err.println(e.getMessage());
				}
			}
			/*
			 * 3) User Management
			 */
			if (PROPERTIES.containsKey(HASHING_ITERATIONS_KEY)) {
				try {
					hashingIterations = Integer.parseInt(PROPERTIES.getProperty(HASHING_ITERATIONS_KEY));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			if (PROPERTIES.containsKey(HASHING_KEY_LENGTH_KEY)) {
				try {
					hashingKeyLength = Integer.parseInt(PROPERTIES.getProperty(HASHING_KEY_LENGTH_KEY));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			/*
			 * 4) Logging
			 */
			if (PROPERTIES.containsKey(LOGGER_CONFIGURATION_KEY)) {
				loggerConfiguration = PROPERTIES.getProperty(LOGGER_CONFIGURATION_KEY);
//				swingVerbose = isActive(output);
//				try {
//					/*
//					 * Checked: ClassCastException is caught
//					 */
//					@SuppressWarnings("unchecked")
//					Class<? extends LoggerConfiguration> cls = (Class<? extends LoggerConfiguration>) Class.forName(output);
//					loggerConfiguration = cls;
//				} catch (ClassNotFoundException e) {
//					System.err.printf("Logger configuration class could not be loaded!%n");
//					e.printStackTrace();
//				} catch (ClassCastException e) {
//					System.err.printf("Logger configuration class must extend LoggerConfiguration!%n");
//					e.printStackTrace();
//				}

			} else {
				System.err.println("Please configure the logging system in 'lumicore.properties'");
			}
			if (PROPERTIES.containsKey(IOC_VERBOSE_KEY)) {
				String output = PROPERTIES.getProperty(IOC_VERBOSE_KEY);
				iocVerbose = isActive(output);
			}
			if (PROPERTIES.containsKey(ORM_VERBOSE_KEY)) {
				String output = PROPERTIES.getProperty(ORM_VERBOSE_KEY);
				sqliteVerbose = isActive(output);
			}
			if (PROPERTIES.containsKey(SWING_VERBOSE_KEY)) {
				String output = PROPERTIES.getProperty(SWING_VERBOSE_KEY);
				swingVerbose = isActive(output);
			}
		}

		APPLICATION_TIME_ZONE = appTimeZone;
		DEFAULT_BIGDECIMAL_SCALE = defaultScale;
		CLOSE_CONNECTION_AFTER_OPERATION = closeConnection;
		OBTAIN_IMMUTABLE = obtainImmutable;
		DEFAULT_QUERY_LIMIT = defaultQueryLimit;
		HASHING_ITERATIONS = hashingIterations;
		HASHING_KEY_LENGTH = hashingKeyLength;
		IOC_VERBOSE = iocVerbose;
		ORM_VERBOSE = sqliteVerbose;
		SWING_VERBOSE = swingVerbose;
		LOGGER_CONFIGURATION = loggerConfiguration;

	}

	private LumicoreProperties() {}
}
