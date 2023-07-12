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
package com.github.epimethix.lumicore.common.orm;

/**
 * Interface for mapping enum constants to INTEGER codes.
 * <p>
 * When implementing this interface for an enum that is used as an
 * {@code Entity} field value then the integer value obtained through
 * {@link IntegerEnum#toInteger()} will be stored in the database.
 * <p>
 * It is important that the INTEGER code is unique for the set of enum
 * constants.
 * 
 * @author epimethix
 */
public interface IntegerEnum {
	/**
	 * Seeks the enum constant for the specified INTEGER code.
	 * <p>
	 * this method is called when setting database values to their java
	 * {@code Entity} fields.
	 * 
	 * @param integer   the INTEGER code of the enum constant to seek.
	 * @param enumClass the {@code IntegerEnum} to analyze.
	 * @return the first enum constant found for the specified code or the last enum
	 *         element by default.
	 *         <p>
	 *         Note that {@code null} is returned by default when...
	 *         <p>
	 *         ... the parameter {@code integer} equals 0 (zero)
	 *         <p>
	 *         ... the provided enum class has 0 (zero) constants
	 *         <p>
	 *         ... the specified enum class is not assignable to {@code IntegerEnum}
	 */
	public static Enum<?> ofInteger(long integer, Class<? extends Enum<?>> enumClass) {
		if (IntegerEnum.class.isAssignableFrom(enumClass)) {
			IntegerEnum[] c = (IntegerEnum[]) enumClass.getEnumConstants();
			if (c.length > 0 && integer != 0L) {
				IntegerEnum i = c[c.length - 1];
				for (IntegerEnum ie : c) {
					if (ie.toInteger() == integer) {
						i = ie;
						break;
					}
				}
				return enumClass.cast(i);
			}
		}
		return null;
	}

	/**
	 * This method returns the associated INTEGER value for this enum constant.
	 * <p>
	 * The implementation of this method should consider that the value 0 (zero) is
	 * interpreted as null value so it should most probably always return a non 0
	 * (zero) value in most cases.
	 * 
	 * @return the unique INTEGER value of this enum constant.
	 */
	long toInteger();
}
