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


public final class AccessCheck {
//	private final static Logger LOGGER = Log.getLogger();
	
	private final List<String> allowedCallers;
	private final List<String> allowedIntermediateCallers;
	private final boolean selfAllowed;

	private AccessCheck(List<String> allowedCallers, List<String> allowedIntermediateCallers, boolean selfAllowed) {
		super();
		this.allowedCallers = allowedCallers;
		this.allowedIntermediateCallers = allowedIntermediateCallers;
		this.selfAllowed = selfAllowed;
	}

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
//			LOGGER.error(e);
		}
		StringBuilder stackStringBuilder = new StringBuilder();
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for(StackTraceElement e : stackTrace) {
			stackStringBuilder.append(String.format("%n%s::%s[%d]", e.getClassName(), e.getMethodName(), e.getLineNumber()));
		}
//		LOGGER.critical("checkPermission failed%s", stackStringBuilder.toString());
		throw new IllegalAccessException("Illegal Access");
	}

	public final static class Builder {
		private final List<String> allowedCallers = new ArrayList<>();
		private List<String> allowedIntermediateCallers = new ArrayList<>();
		private boolean selfAllowed;

//		public Builder(Class<?>... allowList) {
//
//			String[] allowList1 = new String[allowList.length];
//			for (int i = 0; i < allowList.length; i++) {
//				allowList1[i] = allowList[i].getName();
//			}
////			checkAllowCaller(selfAllowed, allowList1);
//			this.allowedCallers = allowList1;
//		}
//		
//		public Builder(String... allowedCallers) {
//			this.allowedCallers = allowedCallers;
//		}
		public Builder() {}

		public Builder allowCaller(Class<?> callerClass) {
			allowedCallers.add(callerClass.getName());
			return this;
		}

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
				if(className.equals(methodName)) {
					caller = caller.substring(0, indexOfMethod).concat("::<init>");
				}
			}
			return caller;
		}

		public Builder allowIntermediateCaller(Class<?> callerClass) {
			allowedIntermediateCallers.add(callerClass.getName());
			return this;
		}

		public Builder allowIntermediateCaller(String caller) {
			allowedIntermediateCallers.add(checkForConstructor(caller));
			return this;
		}

		public Builder allowSelf() {
			selfAllowed = true;
			return this;
		}

		public AccessCheck build() {
			return new AccessCheck(allowedCallers, allowedIntermediateCallers, selfAllowed);
		}

		public static Builder newBuilder() {
			return new Builder();
		}
	}

	/**
	 * checks if a call to a method is allowed. if the caller class/method is now
	 * listed in the allow list an IllegalAccessException is thrown.
	 * 
	 * @param selfAllowed true to allow all calls from inside this class.
	 * @param allowList   the list of classes that are allowed to call the method
	 *                    (this includes sub classes).
	 * @throws IllegalAccessException
	 */
	@CallerSensitive
	public static void allowCaller(boolean selfAllowed, Class<?>... allowList) throws IllegalAccessException {

		String[] allowList1 = new String[allowList.length];
		for (int i = 0; i < allowList.length; i++) {
			allowList1[i] = allowList[i].getName();
		}
//		checkAllowCaller(selfAllowed, allowList1);
	}

	@CallerSensitive
	public static void allowCaller(boolean selfAllowed, String... allowList) throws IllegalAccessException {
		checkAllowCaller(selfAllowed, allowList);
	}

//	@CallerSensitive
//	public static synchronized String getCallers() {
//		try {
//			return PrintStreamString.toString(AccessCheck::printCallers);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return "";
//	}
	
	@CallerSensitive
	public static synchronized void printCallers() {
		printCallers(System.out);
	}
	
	@CallerSensitive
	public static synchronized void printCallers(PrintStream printStream) {
		StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
		printStream.println("%n" +
				"list callers of " + stElements[2].getClassName() + "::" + stElements[2].getMethodName() + "%n%n");
		for (StackTraceElement e : stElements) {
			printStream.printf("%s::%s [%s(line #%d)] %s | %s%n", e.getClassName(), e.getMethodName(), e.getFileName(),
					e.getLineNumber(), e.getModuleName(), e.getClassLoaderName());
		}

	}

	@CallerSensitive
	private static synchronized void checkAllowCaller(boolean selfAllowed, String... allowList)
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
						return;
					}
				}
				if (allowListContains(caller, Arrays.asList(allowList))) {
					return;
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

	public static AccessCheck allowAbstractRepositoryViaReflection() {
		return AccessCheck.Builder.newBuilder()
//				.allowIntermediateCaller("jdk.internal.reflect.NativeMethodAccessorImpl::invoke0")
//				.allowIntermediateCaller("jdk.internal.reflect.NativeMethodAccessorImpl::invoke")
//				.allowIntermediateCaller("jdk.internal.reflect.DelegatingMethodAccessorImpl::invoke")
				.allowIntermediateCaller("jdk.internal.reflect")
				.allowIntermediateCaller("java.lang.reflect.Method::invoke")
				.allowCaller("com.github.epimethix.lumicore.orm.AbstractRepository::").build();
	}
}
