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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.epimethix.lumicore.ioc.annotation.Intercept;
import com.github.epimethix.lumicore.ioc.annotation.InterceptAfterCall;
import com.github.epimethix.lumicore.ioc.annotation.InterceptAllowCaller;
import com.github.epimethix.lumicore.ioc.annotation.InterceptBeforeCall;

public abstract class InterceptionController {
	
	public static final String DEFAULT = "default";

//	private InterceptionFunction getAccessCheck(Method m) {
//		InterceptAllowCaller allow = m.getAnnotation(InterceptAllowCaller.class);
//		AccessCheck.Builder acb = AccessCheck.Builder.newBuilder().allowCaller(allow.value());
//		if (allow.intermediate().length > 0) {
//			for (String intermediate : allow.intermediate()) {
//				acb.allowIntermediateCaller(intermediate);
//			}
//		}
//		acb.allowIntermediateCaller(m.getDeclaringClass());
//		acb.allowIntermediateCaller(InterceptionProxy.class);
//		final AccessCheck ac = acb.build();
//		InterceptionFunction i = (o, method, args) -> {
//			try {
//				ac.checkPermission();
//			} catch (IllegalAccessException e) {
//				throw new InterceptionException(InterceptionException.ACCESS_DENIED, e);
//			}
//			return Optional.empty();
//		};
//		return i;
//	}
//
//	private InterceptionFunction getMethodInterception(InterceptBeforeCall ibc) {
//		return (InterceptionFunction) getMethodInterceptionMulti(ibc);
//	}
//
//	private InterceptionFunctionPost getMethodInterception(InterceptAfterCall iac) {
//		return (InterceptionFunctionPost) getMethodInterceptionMulti(iac);
//	}
//
//	private Object getMethodInterceptionMulti(Annotation a) {
//		String value;
//		boolean before;
//		if (a instanceof InterceptBeforeCall) {
//			before = true;
//			value = ((InterceptBeforeCall) a).value();
//		} else {
//			before = false;
//			value = ((InterceptAfterCall) a).value();
//		}
//		if (value.contains("::")) {
//			try {
//				Class<?> clsX = Class.forName(value.substring(0, value.indexOf("::")));
//				Method method = clsX.getMethod(value.substring(value.indexOf("::") + 2), Object.class, Method.class,
//						Object[].class);
//				if (before) {
//					InterceptionFunction i = (obj, meth, args) -> {
//						try {
//							return (Optional<Object>) method.invoke(null, obj, meth, args);
//						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//							e.printStackTrace();
//						}
//						return Optional.empty();
//					};
////				before.put(method, i);
//					return i;
//				} else {
//					InterceptionFunctionPost i = (obj, meth, args, res) -> {
//						try {
//							return (Optional<Object>) method.invoke(null, obj, meth, args, res);
//						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//							e.printStackTrace();
//						}
//						return Optional.empty();
//					};
////				before.put(method, i);
//					return i;
//				}
//			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
//				e.printStackTrace();
//			}
//		} else {
//			try {
//				Method method = getClass().getMethod(value, Object.class, Method.class, Object[].class);
//				if (before) {
//					InterceptionFunction i = (obj, meth, args) -> {
//						try {
//							return (Optional<Object>) method.invoke(null, obj, meth, args);
//						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						return Optional.empty();
//					};
//					return i;
//				} else {
//					InterceptionFunctionPost i = (obj, meth, args, res) -> {
//						try {
//							return (Optional<Object>) method.invoke(null, obj, meth, args, res);
//						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//							e.printStackTrace();
//						}
//						return Optional.empty();
//					};
//					return i;
//				}
//			} catch (NoSuchMethodException | SecurityException e) {
//				e.printStackTrace();
//			}
//		}
//		return null;
//	}

	public <O> O wrapComponent(Object componentToProxy, Intercept.Strategy strategy, Class<O> interfaceClass) {
		List<Class<?>> interfaces = new ArrayList<>();
		Class<?> cls = componentToProxy.getClass();
		do {
			interfaces.addAll(Arrays.asList(cls.getInterfaces()));
		} while ((cls = cls.getSuperclass()) != Object.class);
		if (!interfaces.contains(interfaceClass)) {
			throw new InvalidParameterException("interface class must be implemented by the object to intercept");
		}
		return interfaceClass.cast(Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
				interfaces.toArray(new Class<?>[] {}), new InterceptionProxy(componentToProxy, strategy, this)));
	}

//	protected abstract void registerComponent();
//	protected abstract InterceptionFunction registerAccessCheck(Method m);
//	protected abstract InterceptionFunction registerMethodInterception(InterceptBeforeCall annotation);
//	protected abstract InterceptionFunctionPost registerMethodInterception(InterceptAfterCall annotation);

	protected abstract InterceptionFunction registerMethodInterception(Object objectToProxy, Class<?> cls, Method m,
			InterceptAllowCaller annotation);

	protected abstract InterceptionFunctionPost registerMethodInterception(Object objectToProxy, Class<?> cls, Method m,
			InterceptAfterCall annotation);

	protected abstract InterceptionFunction registerMethodInterception(Object objectToProxy, Class<?> cls, Method m,
			InterceptBeforeCall annotation);

	public abstract String getName();

}
