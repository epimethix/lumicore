/*
 *  Accounting - Lumicore example application
 *  Copyright (C) 2023  epimethix@protonmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.epimethix.accounting;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.github.epimethix.lumicore.ioc.annotation.InterceptAfterCall;
import com.github.epimethix.lumicore.ioc.annotation.InterceptAllowCaller;
import com.github.epimethix.lumicore.ioc.annotation.InterceptBeforeCall;
import com.github.epimethix.lumicore.ioc.interception.InterceptionController;
import com.github.epimethix.lumicore.ioc.interception.InterceptionFunction;
import com.github.epimethix.lumicore.ioc.interception.InterceptionFunctionPost;
import com.github.epimethix.lumicore.logging.Log;
import com.github.epimethix.lumicore.logging.Logger;

public class LoggingInterceptionController extends InterceptionController {

	private final static Logger LOGGER = Log.getLogger();

//	private final InterceptionFunction logAccessCheck = (o, m, args) -> {
//		LOGGER.info("ACCESS: %s::%s args=%s", o.getClass(), m.getName(), Arrays.asList(args).toString());
//		return Optional.empty();
//	};

	private final InterceptionFunction logBefore = (o, m, args) -> {
		LOGGER.info("BEFORE: %s::%s args=%s", o.getClass(), m.getName(), Arrays.asList(args).toString());
		return Optional.empty();
	};

	private final InterceptionFunctionPost logAfter = (o, m, args, result) -> {
		LOGGER.info("AFTER: %s::%s args=%s result=[%s]", o.getClass(), m.getName(), Arrays.asList(args).toString(),
				Objects.isNull(result) ? "null" : result.toString());
		return Optional.empty();
	};

	@Override
	protected InterceptionFunction registerMethodInterception(Object objectToProxy, Class<?> cls, Method m,
			InterceptAllowCaller annotation) {
		return this::access;
	}
	
	private final Optional<Object> access(Object o, Method m, Object[] args){
		LOGGER.info("ACCESS: %s::%s args=%s", o.getClass(), m.getName(), Arrays.asList(args).toString());
		return Optional.empty();
		
	}

	@Override
	protected InterceptionFunctionPost registerMethodInterception(Object objectToProxy, Class<?> cls, Method m,
			InterceptAfterCall annotation) {
		return logAfter;
	}

	@Override
	protected InterceptionFunction registerMethodInterception(Object objectToProxy, Class<?> cls, Method m,
			InterceptBeforeCall annotation) {
		return logBefore;
	}

	@Override
	public String getName() {
		return InterceptionController.DEFAULT;
	}

}
