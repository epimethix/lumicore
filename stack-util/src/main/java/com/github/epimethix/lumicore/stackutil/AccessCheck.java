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
package com.github.epimethix.lumicore.stackutil;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class can be used to Build access checks that restrict access to certain
 * allowed callers.
 * 
 * @author epimethix
 *
 */
public final class AccessCheck {
//	private final static Logger LOGGER = Log.getLogger();
	private final List<String> allowedCallers;
	private final List<String> allowedIntermediateCallers;
	private final boolean selfAllowed;

	private AccessCheck(List<String> allowedCallers, List<String> allowedIntermediateCallers, boolean selfAllowed) {
		this.allowedCallers = allowedCallers;
		this.allowedIntermediateCallers = allowedIntermediateCallers;
		this.selfAllowed = selfAllowed;
	}

	/**
	 * Performs the access check.
	 * 
	 * @return the allowed caller if the access check succeeded.
	 * 
	 * @throws IllegalAccessException when the access check fails
	 */
	@CallerSensitive
	public final synchronized String checkPermission() throws IllegalAccessException {
		try {
			String caller = null;
			String callee = null;
			Class<?> callerClass = null;
			Class<?> calleeClass = null;
			int i = 0;
			{
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				for (StackTraceElement e : stElements) {
					// i==0 = Thread
					// i==1 = AccessCheck
//					System.out.println(e.getClassName() + "::" + e.getMethodName());
					if (i == 2) {
						callee = e.getClassName() + "::" + e.getMethodName();
						calleeClass = Class.forName(e.getClassName());
					} else if (i > 2) {
						boolean isIntermediate = false;
						if (Objects.nonNull(allowedIntermediateCallers) && !allowedIntermediateCallers.isEmpty()) {
							String intermediateCaller = e.getClassName() + "::" + e.getMethodName();
							if (allowListContains(intermediateCaller, allowedIntermediateCallers)) {
								isIntermediate = true;
							}
						}
						if (!isIntermediate) {
							caller = e.getClassName() + "::" + e.getMethodName();
							callerClass = Class.forName(e.getClassName());
							break;
						}
					}
					i++;
				}
			}
			if (selfAllowed) {
				if (caller.startsWith(calleeClass.getName())) {
					return caller;
				}
			}
			if (allowListContains(caller, allowedCallers)) {
				return caller;
			}
			throw new IllegalAccessException(String.format("%s%s is not allowed to call %s%s",
					callerClass.getSimpleName(), caller.substring(caller.indexOf("::")), calleeClass.getSimpleName(),
					callee.substring(callee.indexOf("::"))));
		} catch (ClassNotFoundException e) {
//		TODO	LOGGER.error(e);
		}
		StringBuilder stackStringBuilder = new StringBuilder();
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement e : stackTrace) {
			stackStringBuilder
					.append(String.format("%n%s::%s[%d]", e.getClassName(), e.getMethodName(), e.getLineNumber()));
		}
//	TODO	LOGGER.critical("checkPermission failed%s", stackStringBuilder.toString());

		throw new IllegalAccessException("Illegal Access");
	}

	/**
	 * Builder class for {@code AccessCheck} objects.
	 * 
	 * @author epimethix
	 *
	 */
	public final static class Builder {
		private final List<String> allowedCallers = new ArrayList<>();
		private List<String> allowedIntermediateCallers = new ArrayList<>();
		private boolean selfAllowed;

		/**
		 * Creates a new {@code AccessCheck.Builder}.
		 */
		public Builder() {}

		/**
		 * Allows all methods of a class to call the restricted method.
		 * 
		 * @param callerClass the {@code Class} to allow
		 * @return this {@code Builder}
		 */
		public Builder allowCaller(Class<?> callerClass) {
			allowedCallers.add(callerClass.getName());
			return this;
		}

		/**
		 * Allows a caller which may be
		 * <ul>
		 * <li>a package name to allow all calls from within that package,
		 * <li>a full class name to allow all calls from that class
		 * <li>or a class name followed by double colon ("::") and a method name to
		 * allow a specific method only.
		 * </ul>
		 * 
		 * @param caller the caller
		 * @return this {@code Builder}
		 */
		public Builder allowCaller(String caller) {
			allowedCallers.add(checkForConstructor(caller));
			return this;
		}

		private String checkForConstructor(String caller) {
			int indexOfMethod = caller.indexOf("::");
			if (indexOfMethod > -1) {
				String methodName = caller.substring(indexOfMethod + 2);
				int lastIndexOfDot = caller.lastIndexOf(".");
				String className;
				if (lastIndexOfDot > -1) {
					className = caller.substring(lastIndexOfDot + 1, indexOfMethod);
				} else {
					className = caller.substring(0, indexOfMethod);
				}
				if (className.equals(methodName)) {
					caller = caller.substring(0, indexOfMethod).concat("::<init>");
				}
			}
			return caller;
		}

		/**
		 * Allows an intermediate caller class to intermediate between allowed caller
		 * and the called restricted method.
		 * 
		 * @param callerClass the intermediate caller.
		 * @return this {@code Builder}
		 */
		public Builder allowIntermediateCaller(Class<?> callerClass) {
			allowedIntermediateCallers.add(callerClass.getName());
			return this;
		}

		/**
		 * Allows an intermediate caller to intermediate between allowed caller and the
		 * called restricted method.
		 * <p>
		 * The intermediate caller may be
		 * <ul>
		 * <li>a package name to allow intermediate calls from within that package,
		 * <li>a full class name to allow intermediate calls from that class
		 * <li>or a class name followed by double colon ("::") and a method name to
		 * allow a specific intermediate method only.
		 * </ul>
		 * 
		 * @param caller
		 * @return this {@code Builder}
		 */
		public Builder allowIntermediateCaller(String caller) {
			allowedIntermediateCallers.add(checkForConstructor(caller));
			return this;
		}

		/**
		 * Allows all calls from within the class in which the {@code
		 * AccessCheck.checkPermission()} is performed.
		 * 
		 * @return this {@code Builder}
		 */
		public Builder allowSelf() {
			selfAllowed = true;
			return this;
		}

		/**
		 * Builds the {@code AccessCheck}.
		 * 
		 * @return the {@code AccessCheck}
		 */
		public AccessCheck build() {
			return new AccessCheck(allowedCallers, allowedIntermediateCallers, selfAllowed);
		}

		/**
		 * Static shortcut to construct a new {@code Builder}.
		 * 
		 * @return a new {@code Builder}
		 */
		public static Builder newBuilder() {
			return new Builder();
		}

		/**
		 * Creates an {@code AccessCheck} to allow {@code SQLRepository} to a access a
		 * method via reflection.
		 * <p>
		 * This {@code AccessCheck} can be used in getters/setters of
		 * {@code Entity<ID>}s that should only be accessed from {@code SQLRepository}.
		 * 
		 * @return the {@code AccessCheck} to allow {@code SQLRepository} via
		 *         reflection.
		 */
		@CallerSensitive
		public static AccessCheck createAllowAbstractRepositoryViaReflection() {
			return AccessCheck.Builder.newBuilder().allowIntermediateCaller("jdk.internal.reflect")
					.allowIntermediateCaller("java.lang.reflect.Method::invoke")
					.allowCaller("com.github.epimethix.lumicore.orm.SQLRepository::").build();
		}

		/**
		 * Creates an {@code AccessCheck} that allows calls from within the class
		 * performing the access check only.
		 * 
		 * @return an {@code AccessCheck} that blocks all calls from outside the
		 *         checking class.
		 */
		@CallerSensitive
		public static AccessCheck createAllowPrivateAccess() {
			return AccessCheck.Builder.newBuilder().allowSelf().build();
		}
	} // End of class Builder

	/**
	 * checks if a call to a method is allowed. if the caller class/method is not
	 * listed in the allow list an IllegalAccessException is thrown.
	 * 
	 * @param selfAllowed true to allow all calls from inside the class calling this
	 *                    method.
	 * @param allowList   the list of classes that are allowed to call the method
	 *                    (this includes sub classes).
	 * @return the allowed caller
	 * @throws IllegalAccessException if the call is not allowed
	 */
	@CallerSensitive
	public static String allowCaller(boolean selfAllowed, Class<?>... allowList) throws IllegalAccessException {

		String[] allowList1 = new String[allowList.length];
		for (int i = 0; i < allowList.length; i++) {
			allowList1[i] = allowList[i].getName();
		}
//		checkAllowCaller(selfAllowed, allowList1);
		return checkAllowCaller(selfAllowed, allowList1);
	}

	/**
	 * Allows calls from the specified allowList.
	 * 
	 * @param selfAllowed allow calls from within the class that performs the access
	 *                    check.
	 * @param allowList   the list of allowed callers
	 * @return the allowed caller
	 * @throws IllegalAccessException if the call is not allowed
	 */
	@CallerSensitive
	public static String allowCaller(boolean selfAllowed, String... allowList) throws IllegalAccessException {
		return checkAllowCaller(selfAllowed, allowList);
	}
	
	/**
	 * Allows calls from the class calling {@code allowPrivateAccess()} only.
	 * 
	 * @return the allowed caller
	 * @throws IllegalAccessException if the call is not allowed
	 */
	@CallerSensitive
	public static String allowPrivateAccess() throws IllegalAccessException {
		return checkAllowCaller(true);
	}

	@CallerSensitive
	private static synchronized String checkAllowCaller(boolean selfAllowed, String... allowList)
			throws IllegalAccessException {
		try {
			String caller = null;
			String callee = null;
			Class<?> callerClass = null;
			Class<?> calleeClass = null;
			int i = 0;
			{
				StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
				for (StackTraceElement e : stElements) {
					// i==0 = Thread
					// i==1 = AccessCheck
					// i==2 = AccessCheck
					if (i == 3) {
						callee = e.getClassName() + "::" + e.getMethodName();
						calleeClass = Class.forName(e.getClassName());
					} else if (i == 4) {
						caller = e.getClassName() + "::" + e.getMethodName();
						callerClass = Class.forName(e.getClassName());
					} else if (i > 4) {
						break;
					}
					i++;
				}
			}
			if (i > 4) {
				if (selfAllowed) {
					if (caller.startsWith(calleeClass.getName())) {
						return caller;
					}
				}
				if (allowListContains(caller, Arrays.asList(allowList))) {
					return caller;
				}
				throw new IllegalAccessException(String.format("%s%s is not allowed to call %s%s",
						callerClass.getSimpleName(), caller.substring(caller.indexOf("::")),
						calleeClass.getSimpleName(), callee.substring(callee.indexOf("::"))));
			}
		} catch (ClassNotFoundException e) {}
		throw new IllegalAccessException("Illegal Access \"checking operation failed\"");
	}

	private static final synchronized boolean allowListContains(String caller, Iterable<String> allowList) {
		for (String allowedCaller : allowList) {
			if (allowedCaller.contains("::") && !allowedCaller.endsWith("::")) {
				if (caller.equals(allowedCaller)) {
					return true;
				}
			} else if (caller.startsWith(allowedCaller)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Utility method to print the call stack trace to {@link System#out} for
	 * analysis and debugging.
	 */
	@CallerSensitive
	public static synchronized void printCallers() {
		printCallers(System.out);
	}

	/**
	 * Utility method to print the call stack trace to the specified
	 * {@code PrintStream} for analysis and debugging.
	 */
	@CallerSensitive
	public static synchronized void printCallers(PrintStream printStream) {
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		printStream.println("%n" + "list callers of " + stElements[2].getClassName() + "::"
				+ stElements[2].getMethodName() + "%n%n");
		for (StackTraceElement e : stElements) {
			printStream.printf("%s::%s [%s(line #%d)] %s | %s%n", e.getClassName(), e.getMethodName(), e.getFileName(),
					e.getLineNumber(), e.getModuleName(), e.getClassLoaderName());
		}

	}
}
