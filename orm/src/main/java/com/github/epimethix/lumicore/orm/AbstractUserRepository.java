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
package com.github.epimethix.lumicore.orm;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.orm.Database;
import com.github.epimethix.lumicore.common.orm.query.Query.SelectQuery;
import com.github.epimethix.lumicore.properties.LumicoreProperties;

/**
 * Extending {@code AbstractUserRepository} will enable basic user management
 * (WIP)
 * 
 * @author epimethix
 *
 * @param <T> The class that implements {@link AbstractUser}.
 * @param <U> The user id (primary key) type
 */
public abstract class AbstractUserRepository<T extends AbstractUser<U>, U> extends SQLRepository<T, U> {

//	private static final long STRUCTURE_VERSION = 1L;

	private final SelectQuery selectByUsername;

	public AbstractUserRepository(Database db, Class<T> entityClass, Class<U> idClass) throws ConfigurationException {
		super(db, entityClass, idClass);
		selectByUsername = DEFAULT_SELECT_QUERY.builder().withCriteria(this).equals(AbstractUser.USERNAME, "").leave()
				.limit(1L).build();
	}

//
//	@Override
//	public T save(T item, boolean closeConnection, String user) throws SQLException, InterruptedException {
//		T t = super.save(item, closeConnection, user);
//		return t;
//	}
//
////	@Override
////	public T save(T item, boolean closeConnection, String user) throws SQLException, InterruptedException {
////		return save(item, closeConnection, user, true, true);
////	}
//
////	@Override
////	public boolean delete(U id, boolean closeConnection, String user) throws SQLException, InterruptedException {
////		return super.delete(id, closeConnection, user);
////	}
//
////	@Override
////	public boolean exists(U id, boolean closeConnection) throws SQLException, InterruptedException {
////		return super.exists(id, closeConnection);
////	}
//
//	@Override
//	protected T loadBy(Object value, Object type, boolean closeConnection, String user, String... keys)
//			throws SQLException, InterruptedException {
//		T t = super.loadBy(value, type, closeConnection, user, keys);
//		return t;
//	}
//
//	@Override
//	public T load(U id, boolean closeConnection, String user) throws SQLException, InterruptedException {
//		T t = super.load(id, closeConnection, user);
//		return t;
//	}
//
////	private void clearSecrets(List<T> list) {
////		for (T t : list) {
////			t.setSecret(null);
////		}
////	}
//	
////	@Override
////	public List<T> listAll(boolean closeConnection, String userName) throws SQLException {
////		List<T> list = super.listAll(closeConnection, userName);
////		clearSecrets(list);
////		return list;
////	}
//
//	@Override
//	public List<T> listAll(boolean closeConnection, String user) throws SQLException, InterruptedException {
////		return listBy(false, null, null, closeConnection, user, true, true);
//		List<T> list = super.listAll(closeConnection, user);
////		clearSecrets(list);
//		return list;
//	}
//
//	@Override
//	public List<T> listBy(Object value, Class<?> type, String user, boolean closeConnection, String field)
//			throws SQLException, InterruptedException {
////		return listBy(false, value, type, closeConnection, user, true, true, field);
//		List<T> list = super.listBy(value, type, user, closeConnection, field);
////		clearSecrets(list);
//		return list;
//	}
//
//	@Override
//	public List<T> listBy(CompositeKey ck, String user, boolean closeConnection, String... fields) throws SQLException, InterruptedException {
////		return listBy(false, ck, null, closeConnection, user, true, true, fields);
//		List<T> list = super.listBy(ck, user, closeConnection, fields);
////		clearSecrets(list);
//		return list;
//	}
//
//	@Override
//	public List<T> listBy(boolean or, List<Object> values, List<Class<?>> types, String user, boolean closeConnection,
//			String... fields) throws SQLException, InterruptedException {
////		return listBy(or, values, types, closeConnection, user, true, true, fields);
//		List<T> list = super.listBy(or, values, types, user, closeConnection, fields);
////		clearSecrets(list);
//		return list;
//	}

	/**
	 * 
	 * @param username
	 * @param password
	 * 
	 * @return
	 * 
	 * @throws SQLException
	 * @throws InterruptedException
	 */
	public T loadByUsername(String username, char[] password) throws SQLException, InterruptedException {
		List<T> userLs = select(selectByUsername.withCriteriumValues(username));
		if (userLs.size() == 1) {
			T user = userLs.get(0);
//			if (Objects.nonNull(user)) {
			try {
				if (passwordMatches(password, user.getSecret())) {
					user.setSecret(null);
					return user;
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
//			}
		}
		return null;
	}

//	public T createUser(T user, char[] password1, char[] password2) throws SQLException, InterruptedException {
//		if(!Arrays.equals(password1, password2)) {
//			return null;
//		}
//		try {
//			user.setSecret(password1);
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		}
//		return save(user);
//	}

	boolean resetSecret(String username, String oldPassword, String newPassword, String newPassword2)
			throws SQLException, InterruptedException {
		return resetSecret(username, oldPassword.toCharArray(), newPassword.toCharArray(), newPassword2.toCharArray());
	}

	boolean resetSecret(String username, char[] oldPassword, char[] newPassword1, char[] newPassword2)
			throws SQLException, InterruptedException {
		if (!Arrays.equals(newPassword1, newPassword2)) {
			return false;
		}
		T user = loadByUsername(username, oldPassword);
		if (Objects.nonNull(user)) {
			try {
				user.setSecret(hashPassword(newPassword1));
				Optional<T> userOpt;
				if ((userOpt = save(user)).isPresent()) {
//					user.setSecret(null);
					return true;
				}
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * hashes a password
	 * 
	 * @param password the plain text password
	 * 
	 * @return the hashed secret
	 */
	private String hashPassword(String password) {
		return hashPassword(password.toCharArray());
	}

	/**
	 * Hashes the plain text password and compares it to the supplied secret.
	 * 
	 * @param password the plain text password
	 * @param secret   the hashed password from storage memory
	 * 
	 * @return true if the secret was reconstructed successfully
	 */
	private boolean passwordMatches(String password, String secret) {
		return passwordMatches(password.toCharArray(), secret);
	}

	/**
	 * Hashes a password using PBKDF2WithHmacSHA1.
	 * 
	 * @param password   the plain text password
	 * @param salt       the salt
	 * @param iterations the number of iterations
	 * @param keyLength  the bit length of the key
	 * 
	 * @return the hashed secret like this: iterations#secret#salt
	 */
	// TODO return secret as byte[]
	private String hashPassword(char[] password, byte[] salt, int iterations, int keyLength) {
		try {
			KeySpec keySpec = new PBEKeySpec(password, salt, iterations, keyLength);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWithHmacSHA512AndAES_256");
			String hexSecret = Hex.encodeHexString(factory.generateSecret(keySpec).getEncoded());
			String hexSalt = Hex.encodeHexString(salt);
//			return String.format("%d#%s#%s", iterations, hexSecret, hexSalt).toCharArray();
//			PBEWithHmacSHA512AndAES_128
//			PBEWithHmacSHA512AndAES_256
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
//		return null;
		throw new RuntimeException("WIP");
	}

	/**
	 * hashes a password
	 * 
	 * @param password the plain text password
	 * 
	 * @return the hashed secret
	 */
	private String hashPassword(char[] password) {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[16];
		random.nextBytes(salt);
		return hashPassword(password, salt, LumicoreProperties.HASHING_ITERATIONS,
				LumicoreProperties.HASHING_KEY_LENGTH);
	}

	/**
	 * Hashes the plain text password and compares it to the supplied secret.
	 * 
	 * @param password the plain text password
	 * @param secret   the hashed password from storage memory
	 * 
	 * @return true if the secret was reconstructed successfully
	 */
	private boolean passwordMatches(char[] password, String secret) {
		String[] secretArray = secret.split("#");
		if (secretArray.length == 3) {
			try {
				int iterations = Integer.parseInt(secretArray[0]);
				int keyLength = Hex.decodeHex(secretArray[1]).length * 8;
				byte[] salt = Hex.decodeHex(secretArray[2]);
				return hashPassword(password, salt, iterations, keyLength).equals(secret);
			} catch (DecoderException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private boolean passwordMatches(char[] password, char[] secret) {
		// TODO Auto-generated method stub
		return false;
	}
}
