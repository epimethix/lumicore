/*
 * Copyright 2021 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.orm;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLException;

public class DBException extends Exception {
	private static final long serialVersionUID = -7332976784406245941L;
	
	public static final int EC_UNKNOWN_ERROR = 1;
	public static final int EC_PK_VIOLATION = 2;
	public static final int EC_FK_VIOLATION = 3;
	public static final int EC_UNIQUE_VIOLATION = 4;
	
	private final SQLException e;

	public DBException(SQLException e) {
		this.e = e;
	}
	
	public final int getErrorCode() {
//		int x = e.getErrorCode();
//		String msg = getMessage();
//		if(msg.contains("")) {
//			return EC_UNIQUE_VIOLATION;
//		} else if(msg.contains("")) {
//			return EC_UNIQUE_VIOLATION;
//		} else {
//			return EC_UNKNOWN_ERROR;
//		}
		return e.getErrorCode();
	}
	
	public SQLException getNextException() {
		return e.getNextException();
	}
	
	public String getSQLState() {
		return e.getSQLState();
	}
	
	@Override
	public String getMessage() {
		return e.getMessage();
	}
	
	@Override
	public synchronized Throwable getCause() {
		return e.getCause();
	}
	
	@Override
	public String getLocalizedMessage() {
		return e.getLocalizedMessage();
	}
	
	@Override
	public StackTraceElement[] getStackTrace() {
		return e.getStackTrace();
	}
	
	@Override
	public void printStackTrace() {
		e.printStackTrace();
	}

	@Override
	public String toString() {
		return e.toString();
	}

	@Override
	public void printStackTrace(PrintStream s) {
		e.printStackTrace(s);
	}

	@Override
	public void printStackTrace(PrintWriter s) {
		e.printStackTrace(s);
	}

	@Override
	public void setStackTrace(StackTraceElement[] stackTrace) {
		e.setStackTrace(stackTrace);
	}
}
