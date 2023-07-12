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
package com.github.epimethix.lumicore.ioc.interception;

@SuppressWarnings("serial")
public class InterceptionException extends Exception {
	public static final int ABORT_EXECUTION_SILENTLY = 1;
	public static final int ABORT_EXECUTION_THROW_EXCEPTION = 2;
	public static final int ACCESS_DENIED = 3;
	public static final int RE_RUN_EXECUTION = 4;

	private final int errorCode;

	private final Throwable cause;

	public InterceptionException() {
		this(null);
	}

	public InterceptionException(Throwable cause) {
		this(ABORT_EXECUTION_SILENTLY, cause);
	}

	public InterceptionException(int errorCode, Throwable cause) {
		this.errorCode = errorCode;
		this.cause = cause;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public boolean errorCodeEquals(int errorToTest) {
		return errorCode == errorToTest;
	}
	
	@Override
	public synchronized Throwable getCause() {
		return cause;
	}
}
