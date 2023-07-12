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
package com.github.epimethix.lumicore.common.ui;

@SuppressWarnings("serial")
public class CryptoException extends Exception{
	
	public static enum Type {
		PASSWORDS_DONT_MATCH, INVALID_CHARACTERS_IN_PASSWORD, WRONG_PASSWORD;
	}
	
	private final Type type;
	
	public CryptoException(Type type) {
		super(getMessage(type));
		this.type = type;
	}

	private static String getMessage(Type type) {
		switch (type) {
		case PASSWORDS_DONT_MATCH:
			return "passwords don't match!";
		case INVALID_CHARACTERS_IN_PASSWORD:
			return "invalid characters in password!";
		default:
			return "wrong password!";
		}
	}

	public Type getType() {
		return type;
	}
}
