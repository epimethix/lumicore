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

import com.github.epimethix.lumicore.common.ui.CryptoUI.Credentials;

public class DefaultCredentials implements Credentials {
	private final transient String user;
	private final transient char[] password;

	public DefaultCredentials(String user, char[] password) {
		this.user = user;
		this.password = password;
	}

	public String getUser() {
		return user;
	}

	public char[] getPassword() {
		return password;
	}
}
