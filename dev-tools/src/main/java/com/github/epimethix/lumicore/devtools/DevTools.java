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
package com.github.epimethix.lumicore.devtools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.epimethix.lumicore.common.Application;
import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.ui.AbstractApplication;
import com.github.epimethix.lumicore.common.ui.SplashScreenController;
import com.github.epimethix.lumicore.devtools.gui.DevToolsSplashScreen;
import com.github.epimethix.lumicore.ioc.Lumicore;
import com.github.epimethix.lumicore.properties.ApplicationProperties;
import com.github.epimethix.lumicore.properties.PropertiesFile;
import com.github.epimethix.lumicore.remoteai.Generator;
import com.github.epimethix.lumicore.remoteai.OpenAI;
import com.github.epimethix.lumicore.sourceutil.ProjectSource;

public final class DevTools extends AbstractApplication implements Application {
	
	private final static AppProperties PROPERTIES = new AppProperties("dev-tools-profile");
	
	public static void main(String[] args) {
		launchDevTools(args);
	}

	public final static void launchDevTools(String[] args) {
//		ProjectSource.printClassPathElements();
//		System.err.println(ProjectSource.discoverSiblingProjects().toString());
//		System.err.println(siblingProjects.toString());
		SplashScreenController.showSplashScreen(DevToolsSplashScreen.class);
		try {
			Lumicore.startApplication(DevTools.class, args, PROPERTIES.getProperties());
//			Runtime.getRuntime().addShutdownHook(new Thread(()->{
//				IWorkspace workspace = ResourcesPlugin.getWorkspace();
//				if(Objects.nonNull(workspace)) {
//					IWorkspaceRoot root = workspace.getRoot();
//			        try {
//			            root.refreshLocal(IResource.DEPTH_INFINITE, null);
//			            System.err.println("REFRESHED WORKSPACE ROOT");
//			        } catch (CoreException e) {
//			            e.printStackTrace();
//			        }
//
//				}
//			}));
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

//	private final static List<File> siblingProjects = discoverSiblingProjects();

	private final DevToolsProperties properties = DevToolsProperties.getProperties();
	
	public DevTools() {
		ClassPathIndex.startIndexing(false);
	}

//	@Override
//	public Locale getDefaultLocale() {
//		if (DevToolsFiles.LOCALE_JSON.exists()) {
//			ObjectMapper om = new ObjectMapper();
//			Locale locale;
//			try {
//				locale = om.readValue(DevToolsFiles.LOCALE_JSON, Locale.class);
//				return locale;
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return Locale.ENGLISH;
//	}
//
//	@Override
//	public void setDefaultLocale(Locale locale) {
//		ObjectMapper om = new ObjectMapper();
//		try {
//			om.writerWithDefaultPrettyPrinter().writeValue(DevToolsFiles.LOCALE_JSON, locale);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	@Override
	public String getApplicationName() {
		return "DevTools";
	}

	@Override
	public File getNestingDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	public File getSourcesDirectory() {
		return new File("src/main/java").getAbsoluteFile();
//		return properties.getSourcesDirectory();
	}

	public Charset getDefaultCharset() {
		return properties.getDefaultCharset();
	}

	public Charset getJavaCharset() {
		return properties.getJavaCharset();
	}

	public boolean isGeneratorConfigured() {
		return properties.getGeneratorApiKey().isPresent();
	}

	public Generator getGenerator() {
		Generator g = new OpenAI(properties.getGeneratorApiKey().orElse(""));
		return g;
	}
	
	public Generator setGeneratorApiKey(String key) {
		properties.setGeneratorApiKey(key);
		Generator g = new OpenAI(key);
		return g;
	}

	@Override
	public ApplicationProperties getApplicationProperties() {
		return PROPERTIES;
	}
}
