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
package com.github.epimethix.lumicore.sourceutil;

import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import com.github.epimethix.lumicore.sourceutil.JavaSource.CommentSource;
import com.github.epimethix.lumicore.sourceutil.JavaSource.EmptyLineSource;;

public class GradleSource {

	public static void main(String[] args) {
		try {
			SettingsSource ss = readSettingsGradle("../settings.gradle");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//	public static final BuildSource readBuildGradle(String path) {
//		
//	}

	public static final SettingsSource readSettingsGradle(String path) throws IOException {
		return new SettingsSource(path);
	}

//	public static final class BuildSource {
//
//	}

	public static final class SettingsSource {

		public static final class RootProjectSource implements Source {
			private final String rootProjectName;
			private final String source;

			public RootProjectSource(String rootProjectName, CharSequence source, int start, int end) {
				this.rootProjectName = rootProjectName;
				this.source = source.subSequence(start, end).toString();
			}

			public String getRootProjectName() {
				return rootProjectName;
			}

			@Override
			public void append(StringBuilder out) {
				out.append(source);
			}

			@Override
			public CharSequence getSource() {
				return source;
			}
		}
		
		public static final class ChildProjectSource implements Source {
			private final String childProjectName;
			private final String source;
			
			public ChildProjectSource(String childProjectName, CharSequence source, int start, int end) {
				this.childProjectName = childProjectName;
				this.source = source.subSequence(start, end).toString();
			}
			
			public String getChildProjectName() {
				return childProjectName;
			}
			
			@Override
			public void append(StringBuilder out) {
				out.append(source);
			}
			
			@Override
			public CharSequence getSource() {
				return source;
			}
		}

		private File settingsFile;
		private final List<Source> sources;

		private SettingsSource(String path) throws IOException {
			this.settingsFile = new File(path);
			sources = new ArrayList<>();
			if (!settingsFile.exists()) {
				throw new FileNotFoundException("File '" + path + "' not found!");
			}
			CharSequence source = Files.readString(settingsFile.toPath());
			int i = 0;
			while (i < source.length()) {
				Optional<Matcher> test = testIndex(EMPTY_LINE_PATTERN, source, i);
				if (test.isPresent()) {
					Matcher emptyLineMatcher = test.get();
//					 int start = i;
//					 int end = emptyLineMatcher.end();
					sources.add(EmptyLineSource.newEmptyLine(source, emptyLineMatcher));
					i = emptyLineMatcher.end();
					continue;
				}
				test = testIndex(LINE_COMMENT_PATTERN, source, i);
				if (test.isPresent()) {
					Matcher lineCommentMatcher = test.get();
					sources.add(CommentSource.Builder.newLineComment(source, lineCommentMatcher));
					i = lineCommentMatcher.end();
					continue;
				}
				test = testIndex(MULTI_LINE_COMMENT_START_PATTERN, source, i);
				if (test.isPresent()) {
					Matcher multiLineCommentMatcher = test.get();
					Optional<Matcher> test2 = find(MULTI_LINE_COMMENT_END_PATTERN, source,
							multiLineCommentMatcher.end());
					if (test2.isPresent()) {
						Matcher multiLineCommentEndMatcher = test2.get();
						Optional<Matcher> test3 = find(END_OF_LINE_PATTERN, source, multiLineCommentEndMatcher.end());
						if (test3.isPresent()) {
							Matcher endOfLineMatcher = test3.get();
							sources.add(CommentSource.Builder.newMultiLineComment(source, i, endOfLineMatcher.end()));
							i = endOfLineMatcher.end();
							continue;
						}
					}
				}
				test = testIndex(ROOT_PROJECT_NAME_PATTERN, source, i);
				if (test.isPresent()) {
					Matcher rootProjectNameMatchern = test.get();
					Optional<Matcher> test2 = find(END_OF_LINE_PATTERN, source, rootProjectNameMatchern.end());
				}
				test = testIndex(CHILD_PROJECT_PATTERN, source, i);
				if (test.isPresent()) {

				}

			}
		}

	}
}
