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
package com.github.epimethix.lumicore.swing.control;

import java.util.Objects;

public class IntegerComponentValidation {
	public static enum IntegerError {
		NONE, VALUE_IS_NULL, VALUE_IS_ZERO, VALUE_IS_NEGATIVE, VALUE_IS_POSITIVE, VALUE_IS_GREATER_THAN_MAX, VALUE_IS_LESS_THAN_MIN
	}

	private boolean allowNull;
	private boolean allowZero;
	private boolean allowNegative = true;
	private boolean allowPositive = true;
	private Long maxValue;
	private Long minValue;

	public IntegerComponentValidation() {}

	public boolean isAllowNull() {
		return allowNull;
	}

	public void setAllowNull(boolean allowNull) {
		this.allowNull = allowNull;
	}

	public boolean isAllowZero() {
		return allowZero;
	}

	public void setAllowZero(boolean allowZero) {
		this.allowZero = allowZero;
	}

	public boolean isAllowNegative() {
		return allowNegative;
	}

	public void setAllowNegative(boolean allowNegative) {
		this.allowNegative = allowNegative;
	}

	public boolean isAllowPositive() {
		return allowPositive;
	}

	public void setAllowPositive(boolean allowPositive) {
		this.allowPositive = allowPositive;
	}

	public Long getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Long maxValue) {
		this.maxValue = maxValue;
	}

	public Long getMinValue() {
		return minValue;
	}

	public void setMinValue(Long minValue) {
		this.minValue = minValue;
	}

	public IntegerError validate(Long l) {
		if (Objects.isNull(l)) {
			if (allowNull) {
				return IntegerError.NONE;
			} else {
				return IntegerError.VALUE_IS_NULL;
			}
		}
		if (l.equals(Long.valueOf(0L))) {
			if (allowZero) {
				return IntegerError.NONE;
			} else {
				return IntegerError.VALUE_IS_ZERO;
			}
		}
		if (!allowNegative && l.longValue() < 0L) {
			return IntegerError.VALUE_IS_NEGATIVE;
		}
		if (!allowPositive && l.longValue() > -1L) {
			return IntegerError.VALUE_IS_POSITIVE;
		}
		if(Objects.nonNull(minValue) && minValue.longValue() > l.longValue()) {
			return IntegerError.VALUE_IS_LESS_THAN_MIN;
		}
		if(Objects.nonNull(maxValue) && maxValue.longValue() < l.longValue()) {
			return IntegerError.VALUE_IS_GREATER_THAN_MAX;
		}
		return IntegerError.NONE;
	}
}
