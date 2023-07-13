package com.github.epimethix.lumicore.classscan;

import com.github.epimethix.lumicore.ioc.annotation.ComponentScan;
import com.github.epimethix.lumicore.profile.Profile;

public class ClassScannerFactory {

	public static ClassScanner createClassScanner(Class<?> applicationClass) {
		return createClassScanner(applicationClass, c->true);
	}

	public static ClassScanner createClassScanner(Class<?> applicationClass, ClassCriteria scanCriteria) {
		ClassScanner scanner;
		if (applicationClass.isAnnotationPresent(ComponentScan.class)) {
			ComponentScan cs = applicationClass.getAnnotation(ComponentScan.class);
			if (Profile.runsFromJar()) {
				scanner = new JarFileScanner(Profile.getExecutionPath(), cs.packages(), scanCriteria);
			} else {
				scanner = new ClasspathScanner(cs.packages(), scanCriteria);
			}
		} else {
			if (Profile.runsFromJar()) {
				scanner = new JarFileScanner(Profile.getExecutionPath(), applicationClass.getPackageName(), scanCriteria);
			} else {
				scanner = new ClasspathScanner(applicationClass.getPackageName(), scanCriteria);
			}
		}
		return scanner;
	}
}