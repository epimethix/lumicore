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
import java.util.Objects;

public class StackUtils {

	public static final StackTraceElement getCallerStackTraceElement() {
		return getCallerStackTraceElement(1);
	}

	public static final StackTraceElement getCallerStackTraceElement(int skip) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		if (stackTrace.length < 4 + skip) {
			throw new RuntimeException(
					"ApplicationUtils::getCallerStackTraceElement cannot be called from the main method!");
		}
		return stackTrace[3 + skip];
	}

	public static final void printStackTrace() {
		printStackTrace(System.out, 1);
	}

	public static final void printStackTrace(PrintStream printStream) {
		printStackTrace(printStream, 1);
	}

	public static final void printStackTrace(PrintStream printStream, int skip) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		int i = 0;
		skip = skip < 0 ? 0 : skip;
		for (StackTraceElement element : stackTrace) {
			if (i < 2 + skip) {
				i++;
			} else {
				printStream.printf("%s::%s[%d]%n", element.getClassName(), element.getMethodName(), element.getLineNumber());
			}
		}
	}

	public static final void printStackTraceToCaller(Class<?> callerClass) {
		printStackTraceToCaller(callerClass, System.out, 1);
	}

	public static final void printStackTraceToCaller(Class<?> callerClass, PrintStream printStream, int skip) {
		String callerClassName = callerClass.getName();
		String thisClassName = null;
		StringBuilder stackTraceSelection = new StringBuilder();
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		int i = 0;
		skip = skip < 0 ? 0 : skip;
		boolean callerFound = false;
		for (StackTraceElement element : stackTrace) {
			if (i < 2 + skip) {
				i++;
			} else {
				if (i == 2 + skip) {
					thisClassName = element.getClassName();
				}
				stackTraceSelection.append(String.format("%s::%s%n", element.getClassName(), element.getMethodName()));
			}
			if (callerClass.getName().equals(element.getClassName())) {
				callerFound = true;
				break;
			}
		}
		if (callerFound) {
			printStream.print(stackTraceSelection.toString());
		} else {
			printStream.printf("%s did not call %s%n", callerClassName, thisClassName);
		}
//		System.out.println(element.getClassName() + "::" + element.getMethodName());
	}

	public static final Class<?> getCallerClass(boolean skipSelf) {
		if (skipSelf) {
			return getCallerClass(-1);
		} else {
			return getCallerClass(1);
		}
	}

	public static final Class<?> getCallerClass() {
		return getCallerClass(1);
	}

	public static final Class<?> getCallerClass(int skip) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		if (stackTrace.length < 4) {
			throw new RuntimeException(
					"ApplicationUtils::getCallerClass this method can not be called from the main method");
		}
		int indexToLookFor;
		if (skip < 0) {
			indexToLookFor = -1;
		} else {
			indexToLookFor = 3 + skip;
			if (stackTrace.length <= indexToLookFor) {
				throw new RuntimeException(
						"ApplicationUtils::getCallerClass stack trace is smaller than the index to look for!");
			}
		}
		try {
			if (indexToLookFor > -1) {
				return Class.forName(stackTrace[indexToLookFor].getClassName());
			} else {
				String askerClassName = null;
				for (int i = 3; i < stackTrace.length; i++) {
					if (Objects.nonNull(askerClassName) && !askerClassName.equals(stackTrace[i].getClassName())) {
						return Class.forName(stackTrace[i].getClassName());
					}
					if (Objects.isNull(askerClassName)
							&& !stackTrace[i].getClassName().equals(StackUtils.class.getName())) {
						askerClassName = stackTrace[i].getClassName();
					}
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("ApplicationUtils::getCallerClass operation failed!");
	}

	public static final Class<?> getCallerClass(String... intermediates) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		if (stackTrace.length < 4) {
			throw new RuntimeException(
					"ApplicationUtils::getCallerClass this method can not be called from the main method");
		}
		try {
			String askerClassName = null;
			outer: for (int i = 3; i < stackTrace.length; i++) {
				if (Objects.nonNull(askerClassName)) {
					String currentClassName = stackTrace[i].getClassName();
					for (String intermediate : intermediates) {
						if (intermediate.equals(currentClassName)) {
							continue outer;
						}
					}
					return Class.forName(stackTrace[i].getClassName());
				}
				if (Objects.isNull(askerClassName)
						&& !stackTrace[i].getClassName().equals(StackUtils.class.getName())) {
					askerClassName = stackTrace[i].getClassName();
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("ApplicationUtils::getCallerClass operation failed!");
	}
}
