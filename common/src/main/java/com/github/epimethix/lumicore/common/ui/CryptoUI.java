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

import java.util.Optional;

/*
 * User interface for obtaining and mutating passwords.
 */
public interface CryptoUI {
	public interface Credentials {
		String getUser();
		char[] getPassword();
	}
	/**
	 * The implementation of this method should obtain a new password from the user
	 * by testing if the user is able to enter the same password twice.
	 * 
	 * @return the new password or Optional.empty() if the dialog was not completed.
	 */
	Optional<Credentials> setupSecret();

	/**
	 * The implementation of this method should first confirm that the user has
	 * permission to mutate the password and then test if the user is able to enter
	 * the new password twice.
	 * 
	 * @param oldPassword the old password for authenticating the mutation.
	 * 
	 * @return the new password or Optional.empty() if the dialog was not completed.
	 */
	Optional<Credentials> resetSecret(char[] oldPassword);

	/**
	 * The implementation of this method should obtain a password from the user.
	 * 
	 * @return {@code Optional#empty()} if the dialog was not completed, the entered
	 *         password otherwise.
	 */
	Optional<Credentials> getSecret();
}
