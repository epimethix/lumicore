package com.github.epimethix.lumicore.classscan;

import com.github.epimethix.lumicore.ioc.annotation.ComponentScan;
import com.github.epimethix.lumicore.profile.Profile;

public class ClassScannerFactory {

	public static ClassScanner createClassScanner(Class<?> applicationClass) {
		ClassScanner scanner;
		if (applicationClass.isAnnotationPresent(ComponentScan.class)) {
			ComponentScan cs = applicationClass.getAnnotation(ComponentScan.class);
			if (Profile.runsFromJar()) {
				scanner = new JarFileScanner(Profile.getExecutionPath(), cs.packages());
			} else {
				scanner = new ClasspathScanner(cs.packages());
			}
		} else {
			if (Profile.runsFromJar()) {
				scanner = new JarFileScanner(Profile.getExecutionPath(), applicationClass.getPackageName());
			} else {
				scanner = new ClasspathScanner(applicationClass.getPackageName());
			}
		}
		return scanner;
	}
}
