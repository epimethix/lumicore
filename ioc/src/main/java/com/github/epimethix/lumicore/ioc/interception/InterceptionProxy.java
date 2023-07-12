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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.github.epimethix.lumicore.common.Reflect;
import com.github.epimethix.lumicore.ioc.annotation.Intercept;
import com.github.epimethix.lumicore.ioc.annotation.InterceptAfterCall;
import com.github.epimethix.lumicore.ioc.annotation.InterceptAllowCaller;
import com.github.epimethix.lumicore.ioc.annotation.InterceptBeforeCall;

// @formatter:off
/**
 * Use cases of an interception proxy (by OpenAI):
 * 
 * 1. Authentication: Ensuring only authorized users have access to the system.
 * 2. Authorization: Restricting the functionality or data a user can access.
 * 3. Data Loss Prevention (DLP): Monitoring traffic for sensitive data and blocking or alerting on any data deemed to be potentially malicious.
 * 4. Network Security: Blocking or alerting on potentially malicious traffic.
 * 5. Malicious Activity Detection: Monitoring and detecting suspicious activity.
 * 6. Traffic Monitoring: Tracking the types and amount of traffic running through the system.
 * 7. Data Analysis: Analyzing traffic to detect patterns or anomalies.
 * 8. Logging: Capturing and storing traffic data for future analysis.
 * 9. Filtering: Allowing or blocking specific types of traffic.
 * 10. Content Filtering: Limiting the types of content that can be accessed.
 * 11. Bandwidth Limiting: Limiting the amount of bandwidth used by certain applications.
 * 12. Network Address Translation (NAT): Allowing multiple devices to share a single public IP address.
 * 13. Caching: Storing frequently accessed data to reduce network traffic.
 * 14. Load Balancing: Distributing network traffic across multiple servers.
 * 15. Network Address Management: Tracking and managing IP addresses.
 * 16. Network Access Control (NAC): Restricting access to the network based on certain criteria.
 * 17. Network Intrusion Detection/Prevention: Detecting and blocking suspicious activity on the network.
 * 18. Network Segmentation: Segmenting the network into multiple sub-networks for security purposes.
 * 19. Network Monitoring: Keeping track of network performance and usage.
 * 20. URL Filtering: Blocking access to certain types of URLs.
 * 21. Encryption: Encrypting data to ensure its privacy and security.
 * 22. Quality of Service (QoS): Ensuring certain types of traffic are given priority over others.
 * 23. Firewall: Blocking access to certain ports and applications.
 * 24. Network Virtualization: Creating virtual networks for applications and services.
 * 25. Network Address Translation (NAT): Allowing multiple devices to share a single public IP address.
 * 
 * @author epimethix
 *
 */
// @formatter:on
public class InterceptionProxy implements InvocationHandler {
	private final Object o;
	private final Map<Method, InterceptionFunction> checkAccess = new HashMap<>();
	private final Map<Method, InterceptionFunction> before = new HashMap<>();
	private final Map<Method, InterceptionFunctionPost> after = new HashMap<>();

	public InterceptionProxy(Object objectToProxy, Intercept.Strategy strategy, InterceptionController ic) {
		this.o = objectToProxy;
		Class<?> cls = objectToProxy.getClass();
		do {
			Method[] methods = cls.getMethods();
			for (Method m : methods) {
				if (strategy == Intercept.Strategy.ANNOTATED) {
					if (m.isAnnotationPresent(InterceptAllowCaller.class)) {
						InterceptionFunction i = ic.registerMethodInterception(objectToProxy, cls, m,
								m.getAnnotation(InterceptAllowCaller.class));
						if (Objects.nonNull(i)) {
							checkAccess.put(m, i);
						}
					}
					if (m.isAnnotationPresent(InterceptBeforeCall.class)) {
						InterceptionFunction i = (InterceptionFunction) ic.registerMethodInterception(objectToProxy,
								cls, m, m.getAnnotation(InterceptBeforeCall.class));
						if (Objects.nonNull(i)) {
							before.put(m, i);
						}
					}
					if (m.isAnnotationPresent(InterceptAfterCall.class)) {
						InterceptionFunctionPost i = (InterceptionFunctionPost) ic.registerMethodInterception(
								objectToProxy, cls, m, m.getAnnotation(InterceptAfterCall.class));
						if (Objects.nonNull(i)) {
							after.put(m, i);
						}
					}
				} else {
					InterceptionFunction iac = ic.registerMethodInterception(objectToProxy, cls, m,
							(InterceptAllowCaller) null);
					if (Objects.nonNull(iac)) {
						checkAccess.put(m, iac);
					}
					InterceptionFunction ib = ic.registerMethodInterception(objectToProxy, cls, m,
							(InterceptBeforeCall) null);
					if (Objects.nonNull(ib)) {
						before.put(m, ib);
					}
					InterceptionFunctionPost ia = ic.registerMethodInterception(objectToProxy, cls, m,
							(InterceptAfterCall) null);
					if (Objects.nonNull(ia)) {
						after.put(m, ia);
					}
				}
			}
		} while (!Reflect.typeEquals(Object.class, (cls = cls.getSuperclass())));
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {// throws Throwable {
		Object returnValue = null;

		try {
			{
				InterceptionFunction access = this.checkAccess.get(method);
				if (Objects.nonNull(access)) {
					access.intercept(o, method, args);
				}
			}
			{
				InterceptionFunction before = this.before.get(method);
				if (Objects.nonNull(before)) {
					Optional<Object> opt;
					opt = before.intercept(o, method, args);
					if (opt.isPresent()) {
						return opt.get();
					}
				}
			}
			boolean reRan = false;
			do {
				try {
					returnValue = method.invoke(o, args);
					{
						InterceptionFunctionPost after = this.after.get(method);
						if (Objects.nonNull(after)) {
							Optional<Object> opt = after.intercept(o, method, args, returnValue);
							if (opt.isPresent()) {
								returnValue = opt.get();
							}
						}
					}
				} catch (InvocationTargetException e) {
					throw e.getCause();
				} catch (InterceptionException e) {
					if (!reRan && e.getErrorCode() == InterceptionException.RE_RUN_EXECUTION) {
						reRan = true;
						continue;
					}
					throw e;
				}
				return returnValue;
			} while (true);
		} catch (InterceptionException e) {
			switch (e.getErrorCode()) {
			case InterceptionException.ABORT_EXECUTION_SILENTLY:
				break;
			case InterceptionException.ABORT_EXECUTION_THROW_EXCEPTION:
				throw e.getCause();
			case InterceptionException.ACCESS_DENIED:
				throw new IllegalAccessException();
			default:
				break;
			}
			e.printStackTrace();
		}
		return returnValue;
	}
}
