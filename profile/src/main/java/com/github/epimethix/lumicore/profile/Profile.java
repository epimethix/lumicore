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
package com.github.epimethix.lumicore.profile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.epimethix.lumicore.properties.PropertiesFile;
import com.github.epimethix.lumicore.stackutil.AccessCheck;
import com.github.epimethix.lumicore.stackutil.CallerSensitive;
import com.github.epimethix.lumicore.stackutil.StackUtils;

/**
 * Class to manage the execution profile for an application.
 * <p>
 * The class reads a profile properties file to configure itself.
 * <p>
 * loading the profile can be done by calling
 * {@code Lumicore.loadProfile(File)} or by calling
 * {@code Lumicore.startApplication(Application, File)}.
 * <p>
 * Calling the {@code Profile.loadProfile} methods has only an effect on the
 * first call. Any subsequent calls will have no effect.
 * <p>
 * <strong>active-profile</strong>
 * <p>
 * the property "active-profile" can be set to "dev[elopment]", "test[ing]" or
 * "prod[uction]".
 * <p>
 * omitting the properties file, or the property "active-profile" will cause the
 * configuration to default to "production".
 * <p>
 * The initially loaded profile before calling any of the {@code loadProfile}
 * methods is {@link #PRODUCTION}.
 * <p>
 * <strong>credentials</strong>
 * <p>
 * It is possible to store credentials in the profile properties.
 * <p>
 * The types of credentials are "Credentials" (*default*), "EmailCredentials"
 * (email.), "SQLiteCredentials" (sqlite.) and "CustomCredentials" (*custom*).
 * <p>
 * To enable access to the credentials the permissions have to be configured
 * too.
 * <p>
 * <strong>Example profile.properties</strong>
 * 
 * <pre>
 * {@code
 * active-profile=development
 * }
 * </pre>
 * 
 * @author epimethix
 *
 */
@CallerSensitive
public class Profile {
//	private final static Logger LOGGER = Log.getLogger();

	/**
	 * The value for {@code DEVELOPMENT}.
	 */
	public static final int DEVELOPMENT = 1;
	/**
	 * The value for {@code TESTING}.
	 */
	public static final int TESTING = 2;
	/**
	 * The value for {@code DEBUGGING}.
	 */
	public static final int DEBUGGING = 3;
	/**
	 * The value for {@code PRODUCTION}.
	 */
	public static final int PRODUCTION = 4;
	/**
	 * The value for {@code STRESSED}.
	 */
//	public static final int STRESSED = 5;

	private static final String KEY_ACTIVE_PROFILE = "active-profile";

	private static final String KEY_ALLOW_READ_EMAIL_CREDENTIALS = "allow-read-email-credentials";
	private static final String KEY_ALLOW_READ_SQLITE_CREDENTIALS = "allow-read-sqlite-credentials";
	private static final String KEY_ALLOW_READ_DEFAULT_CREDENTIALS = "allow-read-default-credentials";
	private static final String KEY_ALLOW_READ_CUSTOM_CREDENTIALS = "allow-read-custom-credentials";

	private static AccessCheck CK_EMAIL;
	private static AccessCheck CK_SQLITE;
	private static AccessCheck CK_DEFAULT;
	private static AccessCheck CK_CUSTOM;

	private static final Map<String, String[]> callerPermissions = new HashMap<>();

	private static PropertiesFile PROPERTIES_FILE;

	private static int activeProfile = PRODUCTION;

	private static boolean loaded;

	private static String executionPath;

	/**
	 * 
	 */
	@CallerSensitive
	public static void loadProfile() {
		PropertiesFile pf = null;
		loadProfile(pf);
	}

	@CallerSensitive
	public static void loadProfile(File profileProperties) {
		PropertiesFile propertiesFile = null;
		try {
			propertiesFile = new PropertiesFile(profileProperties);
			loadProfile(propertiesFile);
		} catch (IOException e) {
//			LOGGER.error(e);
			e.printStackTrace();
		}
	}

	@CallerSensitive
	public static void loadProfile(PropertiesFile propertiesFile) {
		if (loaded) {
			return;
		} else {
			loaded = true;
		}
		PROPERTIES_FILE = propertiesFile;
		if (Objects.nonNull(propertiesFile)) {
			String activeProfileString = PROPERTIES_FILE.getProperty(KEY_ACTIVE_PROFILE, "PRODUCTION");
			activeProfile = getProfileCode(activeProfileString);
//			String[] keySet = new String[] { KEY_ALLOW_READ_DEFAULT_CREDENTIALS, KEY_ALLOW_READ_EMAIL_CREDENTIALS,
//					KEY_ALLOW_READ_SQLITE_CREDENTIALS };
//			CK_DEFAULT = accessCheckBuilders[0].build();
			CK_DEFAULT = parsePermissions(KEY_ALLOW_READ_DEFAULT_CREDENTIALS);
			CK_EMAIL = parsePermissions(KEY_ALLOW_READ_EMAIL_CREDENTIALS);
			CK_SQLITE = parsePermissions(KEY_ALLOW_READ_SQLITE_CREDENTIALS);
			CK_CUSTOM = parsePermissions(KEY_ALLOW_READ_CUSTOM_CREDENTIALS);
		} else {
			activeProfile = PRODUCTION;
		}
		Class<?> callerClass = StackUtils.getCallerClass(Profile.class.getName(),
				"com.github.epimethix.lumicore.ioc.Lumicore");
		try {
			executionPath = URLDecoder.decode(callerClass.getProtectionDomain().getCodeSource().getLocation().getPath(),
					"UTF-8");
		} catch (UnsupportedEncodingException e) {}
	}

	private static void checkIfLoaded() {
		if (!loaded) {
			RuntimeException e = new RuntimeException(
					"Profile was not loaded! call Profile::loadProfile before calling Lumicore::startApplication!");
			e.printStackTrace();
			throw e;
		}
	}

	private static final int getProfileCode(String profileString) {
		String test = profileString.trim().toLowerCase();
		if (test.startsWith("dev")) {
			return DEVELOPMENT;
		} else if (test.startsWith("test")) {
			return TESTING;
		} else if (test.startsWith("deb")) {
			return DEBUGGING;
		} else {
			return PRODUCTION;
		}
	}

	/**
	 * Gets the profile name corresponding to the specified code.
	 * 
	 * @param code the profile code to test
	 * @return the profile name
	 */
	public static final String getProfileName(int code) {
		switch (code) {
		case DEVELOPMENT:
			return "DEVELOPMENT";
		case TESTING:
			return "TESTING";
		default:
			return "PRODUCTION";
		}
	}

	/**
	 * Gets the active profile code.
	 * 
	 * @return the active profile
	 */
	public static int getActiveProfile() {
		return activeProfile;
	}

	/**
	 * Gets the name of the active profile.
	 * 
	 * @return either "PRODUCTION", "TESTING" or "DEVELOPMENT"
	 */
	public static String getActiveProfileName() {
		return getProfileName(activeProfile);
	}

	/**
	 * returns the path to the execution binaries.
	 * <p>
	 * this can be the path to the default package or the path to the jar file the
	 * application is running from.
	 * 
	 * @return the path to the execution binaries
	 * @throws RuntimeException if the profile was not loaded.
	 */
	public static String getExecutionPath() {
		checkIfLoaded();
		return executionPath;
	}

	/**
	 * Tests whether the execution path ends with ".jar"
	 * 
	 * @return true if the application classes reside in a jar file
	 */
	public static boolean runsFromJar() {
		checkIfLoaded();
		return executionPath.endsWith(".jar");
	}

	/**
	 * Checks whether the specified profile code is equal to the active profile
	 * code.
	 * 
	 * @param profile the profile code to test
	 * @return true if the specified profile is active
	 */
	public static boolean isProfileActive(int profile) {
		return activeProfile == profile;
	}

	/**
	 * Checks whether {@link #loadProfile()} was called.
	 * 
	 * @return true if {@link #loadProfile()} was called
	 * @see #loadProfile()
	 * @see #loadProfile(File)
	 * @see #loadProfile(PropertiesFile)
	 */
	public static boolean isLoaded() {
		return loaded;
	}

// pragma_table_info
	private static AccessCheck parsePermissions(String method) {
		AccessCheck.Builder acBuilder = AccessCheck.Builder.newBuilder();
		acBuilder.allowIntermediateCaller(Profile.class);
		String allowReadCreds = PROPERTIES_FILE.getProperty(method, "");
		AccessCheck accessCheck = null;
		if (allowReadCreds.trim().length() > 0) {
//			System.out.println(allowReadCreds);
			String[] callers = allowReadCreds.split("[;]");
//				AccessCheck.Builder acb = AccessCheck.Builder.newBuilder();
			for (String caller : callers) {
				caller = caller.trim();
				int domainSpec = caller.indexOf("(");
				String[] allowedDomains = null;
				String allowedName;
				if (domainSpec > -1) {
					String[] domains = caller.substring(domainSpec + 1, caller.length() - 1).split(",");
					for (int j = 0; j < domains.length; j++) {
						domains[j] = domains[j].trim();
					}
					allowedDomains = domains;
					allowedName = caller.substring(0, domainSpec);
				} else {
					allowedName = caller;
					allowedDomains = new String[] { "." };
				}
				acBuilder.allowCaller(allowedName);
				callerPermissions.put(allowedName.concat(".").concat(method), allowedDomains);
			}
			accessCheck = acBuilder.build();
		}
		return accessCheck;
	}

	private static void checkDomainPermission(String method, String allowedCaller, String propertyDomain)
			throws IllegalAccessException {
		String[] permissions = callerPermissions.get(allowedCaller.concat(".").concat(method));
		if (Objects.isNull(permissions)) {
			permissions = callerPermissions
					.get(allowedCaller.substring(0, allowedCaller.indexOf("::")).concat(".").concat(method));
			if (Objects.isNull(permissions)) {
				Set<String> keySet = callerPermissions.keySet();
				List<String> foundPackages = new ArrayList<>();
				for (String key : keySet) {
					if (key.endsWith(method)) {
						String packageName = key.replaceAll("[.]".concat(method).concat("$"), "");
						if (allowedCaller.startsWith(packageName)) {
							foundPackages.add(key);
						}
					}
				}
				if (foundPackages.size() == 1) {
					permissions = callerPermissions.get(foundPackages.get(0));
				} else if (foundPackages.size() > 1) {
					Collections.sort(foundPackages, (a, b) -> Integer.compare(a.length(), b.length()));
					permissions = callerPermissions.get(foundPackages.get(foundPackages.size() - 1));
				}
			}
		}
		if (Objects.nonNull(permissions)) {
			for (String permission : permissions) {
				if (permission.equals("*")) {
					return;
				} else if (permission.endsWith("*")
						&& propertyDomain.startsWith(permission.substring(0, permission.length() - 1))) {
					return;
				} else if (method.equals(KEY_ALLOW_READ_DEFAULT_CREDENTIALS) && permission.equals(".")
						&& (Objects.isNull(propertyDomain) || propertyDomain.trim().isEmpty())) {
					return;
				} else if (method.equals(KEY_ALLOW_READ_EMAIL_CREDENTIALS) && permission.equals(".")
						&& propertyDomain.equals("email")) {
					return;
				} else if (method.equals(KEY_ALLOW_READ_SQLITE_CREDENTIALS) && permission.equals(".")
						&& propertyDomain.equals("sqlite")) {
					return;
				} else if (!permission.isEmpty() && permission.equals(propertyDomain)) {
					return;
				}
			}
		}
		throw new IllegalAccessException(
				String.format("caller '%s' does not have the permission to read the property domain '%s'",
						allowedCaller, propertyDomain));
	}

	private static String checkPermission(String method, AccessCheck ck) throws IllegalAccessException {
		checkIfLoaded();
//		if (Objects.isNull(PROPERTIES_FILE)) {
//			throw new RuntimeException("No profile properties file was loaded");
//		}
		if (Objects.isNull(ck)) {
			throw new IllegalAccessException(String
					.format("please configure access permissions in your profile properties file! Key: '%s'", method));
		}
		return ck.checkPermission();
	}

	/**
	 * 
	 * @return
	 * @throws IllegalAccessException
	 */
	@CallerSensitive
	public static Credentials loadCredentials() throws IllegalAccessException {
		return loadCredentials(null);
	}

	@CallerSensitive
	public static Credentials loadCredentials(String propertyDomain) throws IllegalAccessException {
		String allowedCaller = checkPermission(KEY_ALLOW_READ_DEFAULT_CREDENTIALS, CK_DEFAULT);

		String userNameKey;
		String passwordKey;
		if (Objects.isNull(propertyDomain) || propertyDomain.trim().isEmpty() || ".".equals(propertyDomain)) {
			userNameKey = Credentials.KEY_USER_NAME;
			passwordKey = Credentials.KEY_PASSWORD;
		} else {
			propertyDomain = propertyDomain.replaceAll("[.]$", "");
			userNameKey = String.format("%s.%s", propertyDomain, Credentials.KEY_USER_NAME);
			passwordKey = String.format("%s.%s", propertyDomain, Credentials.KEY_PASSWORD);
		}
		checkDomainPermission(Profile.KEY_ALLOW_READ_DEFAULT_CREDENTIALS, allowedCaller, propertyDomain);
		System.err.println("UserName=");
		String userName = PROPERTIES_FILE.getProperty(userNameKey, null);
		String password = PROPERTIES_FILE.getProperty(passwordKey, null);
		return new Credentials(userName, password);
	}

	@CallerSensitive
	public static EmailCredentials loadEmailCredentials() throws IllegalAccessException {
		return loadEmailCredentials(null);
	}

	@CallerSensitive
	public static EmailCredentials loadEmailCredentials(String propertyDomain) throws IllegalAccessException {
		String allowedCaller = checkPermission(KEY_ALLOW_READ_EMAIL_CREDENTIALS, CK_EMAIL);

		String userNameKey;
		String passwordKey;
		String mailServerKey;
		String portKey;
		if (Objects.isNull(propertyDomain) || propertyDomain.trim().isEmpty()) {
			propertyDomain = "email";
		}
		checkDomainPermission(KEY_ALLOW_READ_EMAIL_CREDENTIALS, allowedCaller, propertyDomain);
		propertyDomain = propertyDomain.replaceAll("[.]$", "");
		userNameKey = String.format("%s.%s", propertyDomain, EmailCredentials.KEY_USER_NAME);
		passwordKey = String.format("%s.%s", propertyDomain, EmailCredentials.KEY_PASSWORD);
		mailServerKey = String.format("%s.%s", propertyDomain, EmailCredentials.KEY_MAIL_SERVER);
		portKey = String.format("%s.%s", propertyDomain, EmailCredentials.KEY_PORT);

		String userName = PROPERTIES_FILE.getProperty(userNameKey, null);
		if (Objects.isNull(userName)) {
//			LOGGER.warn("Property value '%s' not found!", userNameKey);
		}
		String password = PROPERTIES_FILE.getProperty(passwordKey, null);
		if (Objects.isNull(password)) {
//			LOGGER.warn("Property value '%s' not found!", passwordKey);
		}
		String mailServer = PROPERTIES_FILE.getProperty(mailServerKey, null);
		if (Objects.isNull(mailServer)) {
//			LOGGER.warn("Property value '%s' not found!", mailServerKey);
		}
		String portString = PROPERTIES_FILE.getProperty(portKey, null);
		int port = 0;
		if (Objects.isNull(portString)) {
//			LOGGER.warn("Property value '%s' not found!", portKey);
		} else {
			try {
				port = Integer.parseInt(portString);
			} catch (NumberFormatException e) {
//				LOGGER.critical(e, "Could not read property '%s'! This should be an integer value!", portKey);
			}
		}
		return new EmailCredentials(userName, password, mailServer, port);
	}

	public static SQLiteCredentials loadSQLiteCredentials() throws IllegalAccessException {
		return loadSQLiteCredentials(null);
	}

	public static SQLiteCredentials loadSQLiteCredentials(String propertyDomain) throws IllegalAccessException {
		String allowedCaller = checkPermission(KEY_ALLOW_READ_SQLITE_CREDENTIALS, CK_SQLITE);

		String userNameKey;
		String passwordKey;
		String filePathKey;
		if (Objects.isNull(propertyDomain) || propertyDomain.trim().isEmpty()) {
			propertyDomain = "sqlite";
		}
		checkDomainPermission(KEY_ALLOW_READ_SQLITE_CREDENTIALS, allowedCaller, propertyDomain);
		propertyDomain = propertyDomain.replaceAll("[.]$", "");
		userNameKey = String.format("%s.%s", propertyDomain, SQLiteCredentials.KEY_USER_NAME);
		passwordKey = String.format("%s.%s", propertyDomain, SQLiteCredentials.KEY_PASSWORD);
		filePathKey = String.format("%s.%s", propertyDomain, SQLiteCredentials.KEY_FILE_PATH);

		String userName = PROPERTIES_FILE.getProperty(userNameKey, null);
		String password = PROPERTIES_FILE.getProperty(passwordKey, null);
		String filePath = PROPERTIES_FILE.getProperty(filePathKey, null);
		return new SQLiteCredentials(userName, password, filePath);
	}

	public static CustomCredentials loadCustomCredentials(String domain, String... keys) throws IllegalAccessException {
		String allowedCaller = checkPermission(KEY_ALLOW_READ_CUSTOM_CREDENTIALS, CK_CUSTOM);
		checkDomainPermission(KEY_ALLOW_READ_CUSTOM_CREDENTIALS, allowedCaller, domain);
		CustomCredentials cc = new CustomCredentials(CK_CUSTOM, domain);
		if (Objects.nonNull(domain) && (domain.trim().isEmpty() || domain.trim().equals("."))) {
			domain = null;
		}
		for (String key : keys) {
			if (Objects.nonNull(domain) && !key.startsWith(domain)) {
				key = domain.concat(".").concat(key);
			}
			String val = PROPERTIES_FILE.getProperty(key, null);
			if (Objects.nonNull(val)) {
				cc.putValue(key, val);
			}
		}
		return cc;
	}
}
