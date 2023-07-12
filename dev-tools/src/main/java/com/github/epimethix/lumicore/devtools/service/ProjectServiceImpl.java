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
package com.github.epimethix.lumicore.devtools.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.epimethix.lumicore.devtools.gui.translation.TranslationModel;
import com.github.epimethix.lumicore.ioc.annotation.Service;
import com.github.epimethix.lumicore.sourceutil.ProjectSource;

@Service
public final class ProjectServiceImpl implements ProjectService {

	private final Map<String, ProjectSource> projectSourcesByProjectPath;

	private final Set<File> siblingProjects;

	public ProjectServiceImpl() {
		projectSourcesByProjectPath = new HashMap<>();
		siblingProjects = new HashSet<>(ProjectSource.discoverSiblingProjects());
	}

	@Override
	public final Set<File> getSiblingProjects() {
		return new HashSet<>(siblingProjects);
	}

	@Override
	public final ProjectSource loadProjectSource(File project) {
		File srcDir = new File(project, "src/main/java");
		if (!srcDir.exists()) {
			throw new IllegalArgumentException(
					String.format("The File %s has no sources Directory!", project.getPath()));
		}
		ProjectSource projectSource = projectSourcesByProjectPath.get(project.getPath());
		if (Objects.isNull(projectSource)) {
			projectSource = ProjectSource.scan(srcDir, ".java");
			projectSourcesByProjectPath.put(project.getPath(), projectSource);
		}
		return projectSource;
	}

	@Override
	public final List<TranslationModel> getTranslationModels(File project) {
		File srcDevResources = new File(project, "src/dev/resources");
		List<TranslationModel> translationModels = new ArrayList<>();
		if (srcDevResources.exists()) {
			File[] i18nFiles = srcDevResources
					.listFiles(f -> f.isFile() && f.getName().toLowerCase(Locale.ENGLISH).endsWith(".i18n"));
			for (File i18n : i18nFiles) {
				try {
					translationModels.add(new TranslationModel(project, i18n));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return translationModels;
	}
}
