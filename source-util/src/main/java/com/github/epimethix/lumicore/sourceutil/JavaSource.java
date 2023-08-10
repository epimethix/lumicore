/*
 * Copyright 2022-2023 epimethix@protonmail.com
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

import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.ANNOTATION_SHORT_OPEN_BRACKET;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.ANNOTATION_SHORT_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.ANNOTATION_SHORT_TYPE_NAME;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.ARRAY_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.CLASS_EXTENDS_KEYWORD_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.CLASS_EXTENDS_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.CLASS_EXTENDS_SUPER_TYPE;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.CLASS_IMPLEMENTS_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.CLASS_SIGNATURE_IDENTIFIER;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.CLASS_SIGNATURE_KEYWORD;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.CLASS_SIGNATURE_MODIFIERS;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.CLASS_SIGNATURE_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.CONSTRUCTOR_IDENTIFIER;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.CONSTRUCTOR_SIGNATURE_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.CONSTRUCTOR_VISIBILITY;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.EMPTY_LINE_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.END_OF_CLASS_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.FIELD_SIGNATURE_IDENTIFIER;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.FIELD_SIGNATURE_IDENTIFIER_ARRAY;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.FIELD_SIGNATURE_MODIFIERS;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.FIELD_SIGNATURE_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.FIELD_SIGNATURE_TERMINATOR;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.FIELD_SIGNATURE_TYPE;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.FIELD_SIGNATURE_TYPE_ARRAY;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.FIELD_SIGNATURE_TYPE_NAME;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.FIELD_SIGNATURE_TYPE_PARAMETERS_ARRAY;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.IDENTIFIER_NAME;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.IDENTIFIER_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.IMPORT_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.IMPORT_STATIC;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.IMPORT_TYPE;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.INITIALIZER_BLOCK_SIGNATURE_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.INITIALIZER_BLOCK_STATIC;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.JAVA_DOC_COMMENT_START_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.LINE_COMMENT_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MEMBER_CLASS_SIGNATURE_IDENTIFIER;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MEMBER_CLASS_SIGNATURE_KEYWORD;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MEMBER_CLASS_SIGNATURE_MODIFIERS;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MEMBER_CLASS_SIGNATURE_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.METHOD_SIGNATURE_IDENTIFIER;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.METHOD_SIGNATURE_MODIFIERS;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.METHOD_SIGNATURE_RETURN_TYPE;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.METHOD_SIGNATURE_RETURN_TYPE_ARRAY;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.METHOD_SIGNATURE_RETURN_TYPE_NAME;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.METHOD_SIGNATURE_RETURN_TYPE_PARAMETERS_ARRAY;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.METHOD_SIGNATURE_SHORT_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.METHOD_SIGNATURE_TYPE_PARAMETERS;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.METHOD_SIGNATURE_TYPE_PARAMETERS_WITH_BRACKETS;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MULTI_LINE_COMMENT_START_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MYSTERY_IDENTIFIER_ARRAY;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MYSTERY_IDENTIFIER_NAME;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MYSTERY_IDENTIFIER_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MYSTERY_IDENTIFIER_TERMINATOR;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MYSTERY_SIGNATURE_METHOD_OR_FIELD_MODIFIERS;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MYSTERY_SIGNATURE_METHOD_OR_FIELD_OPEN_TYPE_PARAMETERS;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_NAME;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_PARAMETERS_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_MODIFIERS;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_TYPE_PARAMS_OPEN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.PACKAGE_NAME;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.PACKAGE_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.TYPE_NAME_ARRAY;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.TYPE_NAME_NAME;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.TYPE_NAME_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.TYPE_PARAMETERS_START_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.find;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.getVisibilityFromModifiers;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.nextEndOfComment;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.nextNewLine;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.skipWhitespace;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.readExceptions;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.readParameters;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.readSignatureList;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.readTypeParameters;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.seekEndOfBlock;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.seekEndOfClosingBracket;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.seekEndOfOpenBlock;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.seekEndOfOpenBracket;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.seekEndOfStatement;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.seekEndOfTypeParameters;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.splitModifiers;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.testIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;

// @formatter:off
/**
 * Container- and Builder class for java source code (elements).
 * <p>
 * <pre>
 * !   Not implemented yet are   !
 * ! -reading enum constants     !
 * ! -reading annotation members !
 * </pre>
 * <strong>Compatibility Requirements</strong>
 * <p>
 * -only the first top level class is parsed: one top level class per .java file
 * <p>
 * -package/identifier names may consist only of java characters.
 * <p>
 * <strong>From the specification v 1.8:</strong>
 * <p>
 * A "Java letter" is a character for which the method {@code Character.isJavaIdentifierStart(int)} returns true.
 * <p>
 * A "Java letter-or-digit" is a character for which the method {@code Character.isJavaIdentifierPart(int)} returns true.
 * <p>
 * The "Java letters" include uppercase and lowercase ASCII Latin letters A-Z (\u0041-\u005a), and a-z (\u0061-\u007a), 
 * and, for historical reasons, the ASCII underscore (_, or \u005f) and dollar sign ($, or \u0024). The $ sign should 
 * be used only in mechanically generated source code or, rarely, to access pre-existing names on legacy systems.
 * <p>
 * The "Java digits" include the ASCII digits 0-9 (\u0030-\u0039).
 * 
 * @author epimethix
 * @see Character#isJavaIdentifierStart(char)
 * @see Character#isJavaIdentifierPart(char)
 */
// @formatter:on
public final class JavaSource implements Source {
	/**
	 * tries to parse a {@code MethodSource} from the result of the method
	 * {@code Method.toGenericString()}.
	 * 
	 * @param method the method to analyze
	 * @return a {@code MethodSource} representing the method signature of the
	 *         supplied method.
	 */
	public static MethodSource readGenericMethodString(Method method) {
		String className = method.getDeclaringClass().getName().replaceAll("\\.", "\\.") + "\\.";
		String genericString = method.toGenericString().replaceAll(className, "");
		genericString = genericString + ";";
		JavaSource js = new JavaSource("");
		MethodSource ms = null;
		Optional<Matcher> opt = testIndex(METHOD_SIGNATURE_SHORT_PATTERN, genericString, 0);
		try {
			if (opt.isPresent()) {
				ms = js.parseMethod(0, genericString, opt.get(), null, Collections.emptyList());
			} else {
				opt = testIndex(MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_PATTERN, genericString, 0);
				if (opt.isPresent()) {
					ms = js.parseMethod2(0, genericString, opt.get(), null, Collections.emptyList());
				} else {
					opt = testIndex(MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_PARAMETERS_PATTERN, genericString, 0);
					if (opt.isPresent()) {
						ms = (MethodSource) js.parseMethodOrField(0, genericString, opt.get(), null,
								Collections.emptyList());
					}
				}
			}
		} catch (IndexOutOfBoundsException e) {
			System.err
					.println("JavaSource.readGenericMethodString failed: " + method.getName() + " :: " + genericString);
		}
		return ms;
	}

	public static JavaSource readFile(String path) throws IOException {
		return readFile(new File(path));
	}

	/**
	 * Reads a java source code file using UTF-8 as {@code Charset}.
	 * 
	 * @param sourceJava the .java file
	 * @return the {@code JavaSource} that was parsed from the specified file
	 * @throws IOException if a proplem arises while reading the file
	 */
	public final static JavaSource readFile(File sourceJava) throws IOException {
		return readFile(sourceJava, StandardCharsets.UTF_8);
	}

	/**
	 * Reads a java source code file using the specified {@code Charset}.
	 * 
	 * @param sourceJava the .java file
	 * @param charset    the charset to use
	 * @return the {@code JavaSource} that was parsed from the specified file
	 * @throws IOException if a proplem arises while reading the file
	 */
	public final static JavaSource readFile(File sourceJava, Charset charset) throws IOException {
		StringBuilder source = new StringBuilder();
		try (FileReader reader = new FileReader(sourceJava, charset);
				BufferedReader bufferedReader = new BufferedReader(reader)) {
			CharBuffer buffer = CharBuffer.allocate(2048);
			while (bufferedReader.read(buffer) != -1) {
				buffer.flip();
				source.append(buffer);
				buffer.clear();
			}
		}
		return readSource(source);
	}

	/**
	 * Reads the specified java source code.
	 * 
	 * @param sourceCode the source code to read
	 * @return the {@code JavaSource} that was parsed from the specified code
	 */
	public final static JavaSource readSource(CharSequence sourceCode) {
		return new JavaSource(sourceCode);
	}

	/**
	 * Source code element containing an empty line.
	 * 
	 * @author epimethix
	 *
	 */
	public final static class EmptyLineSource implements Source {
		private final CharSequence emptyLine;
		private final String name;

		private EmptyLineSource(CharSequence sourceCode, int start, int end) {
			this.emptyLine = sourceCode.subSequence(start, end);
			this.name = UUID.randomUUID().toString();
		}

		private EmptyLineSource() {
			this.emptyLine = String.format("%n");
			this.name = UUID.randomUUID().toString();
		}

		/**
		 * Creates a new {@code EmptyLineSource} consisting of a single new line.
		 * 
		 * @return an {@code EmptyLineSource}
		 */
		public static final EmptyLineSource newEmptyLine() {
			return new EmptyLineSource();
		}

		@Override
		public void append(StringBuilder out) {
			out.append(emptyLine);
		}

		@Override
		public CharSequence getSource() {
			return emptyLine;
		}

		@Override
		public String toString() {
			return "empty line";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EmptyLineSource other = (EmptyLineSource) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		public static Source newEmptyLine(CharSequence source, Matcher emptyLineMatcher) {
			return new EmptyLineSource(source, emptyLineMatcher.start(), emptyLineMatcher.end());
		}
	} // End of class EmptyLineSource

	/**
	 * Source code element containing a comment.
	 */
	public final static class CommentSource implements Source {
		private final String name;
		private final CharSequence comment;

		private CommentSource(CharSequence sourceCode) {
			this(sourceCode, 0, sourceCode.length());
		}

		private CommentSource(CharSequence sourceCode, int start, int end) {
			this.name = UUID.randomUUID().toString();
			this.comment = sourceCode.subSequence(start, end);
		}

		@Override
		public void append(StringBuilder out) {
			out.append(comment);
		}

		@Override
		public CharSequence getSource() {
			return comment;
		}

		@Override
		public String toString() {
			return "comment";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CommentSource other = (CommentSource) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		/**
		 * Builder for creating a {@code CommentSource}.
		 * 
		 * @author epimethix
		 *
		 */
		public static final class Builder {
			private enum CommentType {
				LINE, MULTI_LINE, JAVA_DOC
			}

			private final int indent;
			private final String commentText;
			private final CommentType type;
			private int lineWidth = 85;

			private Builder(int indent, String commentText, CommentType type) {
				this.indent = indent;
				this.commentText = commentText;
				this.type = type;
			}

			public static CommentSource newLineComment(CharSequence source, Matcher lineCommentMatcher) {
				return new CommentSource(source, lineCommentMatcher.start(), lineCommentMatcher.end());
			}

			public static CommentSource newMultiLineComment(CharSequence source, int start, int end) {
				return new CommentSource(source, start, end);
			}

			/**
			 * Obtains a new {@code Builder} for building a line comment.
			 * 
			 * @param indent      the number of tabs to indent the comment
			 * @param commentText the comment text
			 * @return a new {@code Builder}
			 */
			public static final Builder newLineComment(int indent, String commentText) {
				return new Builder(indent, commentText, CommentType.LINE);
			}

			/**
			 * Obtains a new {@code Builder} for building a multi-line comment.
			 * 
			 * @param indent      the number of tabs to indent the comment
			 * @param commentText the comment text
			 * @return a new {@code Builder}
			 */
			public static final Builder newMultiLineComment(int indent, String commentText) {
				return new Builder(indent, commentText, CommentType.MULTI_LINE);
			}

			/**
			 * Obtains a new {@code Builder} for building a java-doc comment.
			 * 
			 * @param indent      the number of tabs to indent the comment
			 * @param commentText the comment text
			 * @return a new {@code Builder}
			 */
			public static final Builder newJavaDocComment(int indent, String commentText) {
				return new Builder(indent, commentText, CommentType.JAVA_DOC);
			}

			/**
			 * set the maximum of characters a single line may have.
			 * 
			 * @param width the line width in characters
			 * @return this {@code Builder}
			 */
			public final Builder setLineWidth(int width) {
				this.lineWidth = width;
				return this;
			}

			/**
			 * Builds the comment source code and {@code CommentSource}.
			 * 
			 * @return the new {@code CommentSource}
			 */
			public final CommentSource build() {
				StringBuilder sourceCode = new StringBuilder();
				int indentWidth = indent * 4 + 3;
				int textWidth = lineWidth - indentWidth;
				if (type.equals(CommentType.LINE)) {
					for (int i = 0; i < commentText.length();) {
						String appendString;
						if (commentText.length() - i > textWidth) {
							appendString = commentText.substring(i, i + textWidth);
						} else {
							appendString = commentText.substring(i);
						}
						sourceCode.append("\t".repeat(indent)).append("// ").append(appendString)
								.append(String.format("%n"));
						i += textWidth;
					}
				} else {
					if (type.equals(CommentType.MULTI_LINE)) {
						sourceCode.append("\t".repeat(indent)).append(String.format("/*%n"));
					} else {
						sourceCode.append("\t".repeat(indent)).append(String.format("/**%n"));
					}

					for (int i = 0; i < commentText.length();) {
						String appendString;
						if (commentText.length() - i > textWidth) {
							appendString = commentText.substring(i, i + textWidth);
						} else {
							appendString = commentText.substring(i);
						}
						sourceCode.append("\t".repeat(indent)).append(" * ").append(appendString)
								.append(String.format("%n"));
						i += textWidth;
					}
					sourceCode.append("\t".repeat(indent)).append(" */").append(String.format("%n"));
				}
				return new CommentSource(sourceCode);
			}
		} // End of class Builder
	} // End of class CommentSource

	/**
	 * Source code element containing an annotation.
	 * 
	 * @author epimethix
	 *
	 */
	public final static class AnnotationSource implements Source {
		private final String name;
		private final CharSequence annotation;
		private final String typeName;

		private AnnotationSource(Matcher matcher, CharSequence sourceCode, int start, int end) {
			this.annotation = sourceCode.subSequence(start, end);
			this.typeName = matcher.group(ANNOTATION_SHORT_TYPE_NAME);
			this.name = UUID.randomUUID().toString();
		}

		private AnnotationSource(String typeName, CharSequence sourceCode, int start, int end) {
			this.annotation = sourceCode.subSequence(start, end);
			this.typeName = typeName;
			this.name = UUID.randomUUID().toString();
		}

		/**
		 * Get the annotation type name.
		 * 
		 * @return the type name
		 */
		public String getTypeName() {
			return typeName;
		}

		@Override
		public void append(StringBuilder out) {
			out.append(annotation);
		}

		@Override
		public CharSequence getSource() {
			return annotation;
		}

		@Override
		public String toString() {
			return String.format("annotation %s", typeName);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AnnotationSource other = (AnnotationSource) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		/**
		 * Builder class for creating a new {@code AnnotationSource}.
		 * 
		 * @author epimethix
		 *
		 */
		public static final class Builder {

			private final int indent;
			private final String typeName;
			private List<String> values = new ArrayList<>();

			private Builder(int indent, String typeName) {
				this.indent = indent;
				this.typeName = typeName;
			}

			/**
			 * Obtain a new {@code Builder} to create an {@code AnnotationSource}.
			 * 
			 * @param indent   the number of tabs to indent the annotation
			 * @param typeName the annotation type name
			 * @return a new {@code Builder}
			 */
			public static final Builder newAnnotation(int indent, String typeName) {
				return new Builder(indent, typeName);
			}

			/**
			 * adds a value to the annotation.
			 * 
			 * @param value the value to add
			 * @return this {@code Builder}
			 */
			public Builder addValue(String value) {
				values.add(value);
				return this;
			}

			/**
			 * adds a value to the annotation.
			 * 
			 * @param value the value to add
			 * @return this {@code Builder}
			 */
			public Builder setStringValue(String value) {
				values.add("\"".concat(value).concat("\""));
				return this;
			}

			/**
			 * Builds the annotation source code and {@code AnnotationSource}.
			 * 
			 * @return a new {@code AnnotationSource}
			 */
			public AnnotationSource build() {
				StringBuilder sourceCode = new StringBuilder();
				if (!typeName.startsWith("@")) {
					sourceCode.append("\t".repeat(indent)).append("@").append(typeName);
				} else {
					sourceCode.append("\t".repeat(indent)).append(typeName);
				}
				if (!values.isEmpty()) {
					sourceCode.append("(").append(String.join(", ", values)).append(")");
				}
				sourceCode.append(String.format("%n"));
				return new AnnotationSource(typeName, sourceCode, 0, sourceCode.length());
			}
		} // End of class Builder
	} // End of class AnnotationSource

	/**
	 * Source code element containing the package declaration.
	 * 
	 * @author epimethix
	 *
	 */
	public final static class PackageSource implements Source {
		private final CharSequence packageDeclaration;
		private final String packageName;

		private PackageSource(String packageName, CharSequence sourceCode) {
			this(packageName, sourceCode, 0, sourceCode.length());
		}

		private PackageSource(String packageName, CharSequence sourceCode, int start, int end) {
			this.packageName = packageName;
			this.packageDeclaration = sourceCode.subSequence(start, end);
		}

		private PackageSource(Matcher matcher, CharSequence sourceCode, int start, int end) {
			this.packageName = matcher.group(PACKAGE_NAME);
			this.packageDeclaration = sourceCode.subSequence(start, end);
		}

		/**
		 * Creates a new {@code PackageSource} containing the specified package name.
		 * 
		 * @param packageName the package name
		 * @return the new {@code PackageSource}
		 */
		public static final PackageSource newPackageSource(String packageName) {
			return new PackageSource(packageName,
					new StringBuilder("package ").append(packageName).append(";").append(String.format("%n")));
		}

		/**
		 * Gets the package name of the package declaration.
		 * 
		 * @return the package name
		 */
		public String getPackageName() {
			return packageName;
		}

		@Override
		public void append(StringBuilder out) {
			out.append(packageDeclaration);
		}

		@Override
		public CharSequence getSource() {
			return packageDeclaration;
		}

		@Override
		public String toString() {
			return "package " + packageName + ";";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((packageDeclaration == null) ? 0 : packageDeclaration.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PackageSource other = (PackageSource) obj;
			if (packageDeclaration == null) {
				if (other.packageDeclaration != null)
					return false;
			} else if (!packageDeclaration.equals(other.packageDeclaration))
				return false;
			return true;
		}
	} // End of class PackageSource

	/**
	 * Source code element containing an import statement.
	 * 
	 * @author epimethix
	 *
	 */
	public final static class ImportSource implements Source {
		private final CharSequence importDeclaration;
		private final boolean staticImport;
		private final String importType;

		private ImportSource(boolean staticImport, String importType, CharSequence sourceCode, int start, int end) {
			this.staticImport = staticImport;
			this.importType = importType;
			this.importDeclaration = sourceCode.subSequence(start, end);
		}

		private ImportSource(Matcher matcher, CharSequence sourceCode, int start, int end) {
			staticImport = Objects.nonNull(matcher.group(IMPORT_STATIC));
			importType = matcher.group(IMPORT_TYPE);
			this.importDeclaration = sourceCode.subSequence(start, end);
		}

		/**
		 * Creates a new {@code ImportSource} containing the non-static import of the
		 * specified type name.
		 * 
		 * @param typeName the type name to import
		 * @return the new {@code ImportSource}
		 */
		public static ImportSource newImport(String typeName) {
			return newImport(false, typeName);
		}

		/**
		 * Creates a new {@code ImportSource} containing the import of the specified
		 * type name.
		 * 
		 * @param staticImport true to add the 'static' keyword
		 * @param typeName     the type name to import
		 * @return the new {@code ImportSource}
		 */
		public static ImportSource newImport(boolean staticImport, String typeName) {
			StringBuilder sourceCode = new StringBuilder();
			sourceCode.append("import ");
			if (staticImport) {
				sourceCode.append("static ");
			}
			sourceCode.append(typeName).append(";").append(String.format("%n"));
			return new ImportSource(staticImport, typeName, sourceCode, 0, sourceCode.length());
		}

		/**
		 * Check if import is static.
		 * 
		 * @return true if the static keyword occurs
		 */
		public boolean isStaticImport() {
			return staticImport;
		}

		/**
		 * Gets the type name portion of the import statement.
		 * 
		 * @return the type name
		 */
		public String getImportType() {
			return importType;
		}

		@Override
		public void append(StringBuilder out) {
			out.append(importDeclaration);
		}

		@Override
		public CharSequence getSource() {
			return importDeclaration;
		}

		@Override
		public String toString() {
			return String.format("import%s %s;", staticImport ? " static" : "", importType);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((importType == null) ? 0 : importType.hashCode());
			result = prime * result + (staticImport ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ImportSource other = (ImportSource) obj;
			if (importType == null) {
				if (other.importType != null)
					return false;
			} else if (!importType.equals(other.importType))
				return false;
			if (staticImport != other.staticImport)
				return false;
			return true;
		}
	} // End of class ImportSource

	/**
	 * Source code element containing the class declaration of the java file.
	 * 
	 * @author epimethix
	 *
	 */
	public final static class ClassDeclarationSource implements Source {
		private final CommentSource javaDoc;
		private final List<AnnotationSource> annotations;
		private final String classKeyword;
		private final String className;
		private final CharSequence classDeclaration;

		private ClassDeclarationSource(Matcher matcher, CommentSource javaDoc, List<AnnotationSource> annotations,
				CharSequence sourceCode, int start, int end) {
			this.classKeyword = matcher.group(CLASS_SIGNATURE_KEYWORD);
			this.className = matcher.group(CLASS_SIGNATURE_IDENTIFIER);
			this.javaDoc = javaDoc;
			this.annotations = annotations;
			this.classDeclaration = sourceCode.subSequence(start, end);
		}

		private ClassDeclarationSource(MemberClassSource memberClassSource, int startClass) {
			this.javaDoc = memberClassSource.comment;
			this.annotations = memberClassSource.annotations;
			this.classKeyword = memberClassSource.classKeyword;
			this.className = memberClassSource.className;
			this.classDeclaration = memberClassSource.getSource().subSequence(0, startClass);
		}

		/**
		 * Gets the used keyword.
		 * 
		 * @return {@code class}, {@code interface} or {@code @interface}
		 */
		public String getClassKeyword() {
			return classKeyword;
		}

		/**
		 * Gets the class identifier.
		 * 
		 * @return the (simple) class name
		 */
		public String getClassName() {
			return className;
		}

		/**
		 * Gets the java doc comment of the class if there is one.
		 * 
		 * @return an {@code Optional} containing the java doc comment
		 */
		public Optional<CommentSource> getComment() {
			return Optional.ofNullable(javaDoc);
		}

		/**
		 * Gets the annotations of the class.
		 * 
		 * @return the list of annotations
		 */
		public List<AnnotationSource> getAnnotations() {
			return annotations;
		}

		@Override
		public CharSequence getSource() {
			return classDeclaration;
		}

		@Override
		public int length() {
			int length = 0;
			if (Objects.nonNull(javaDoc)) {
				length += javaDoc.length();
			}
			if (Objects.nonNull(annotations)) {
				for (AnnotationSource as : annotations) {
					length += as.length();
				}
			}
			length += classDeclaration.length();
			return length;
		}

		@Override
		public void append(StringBuilder out) {
			if (Objects.nonNull(javaDoc)) {
				javaDoc.append(out);
			}
			if (Objects.nonNull(annotations)) {
				for (AnnotationSource as : annotations) {
					as.append(out);
				}
			}
			out.append(classDeclaration);
		}

		@Override
		public Integer checkIntegrity(Integer previousEnd, Map<Source, Integer[]> boundaries) {
			Integer end = previousEnd;
			if (Objects.nonNull(javaDoc)) {
				end = javaDoc.checkIntegrity(end, boundaries);
			}
			if (Objects.nonNull(annotations)) {
				for (AnnotationSource as : annotations) {
					end = as.checkIntegrity(end, boundaries);
				}
			}
			return Source.super.checkIntegrity(end, boundaries);
		}

		@Override
		public String toString() {
			return String.format("%s %s", classKeyword, className);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((className == null) ? 0 : className.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ClassDeclarationSource other = (ClassDeclarationSource) obj;
			if (className == null) {
				if (other.className != null)
					return false;
			} else if (!className.equals(other.className))
				return false;
			return true;
		}

	} // End of class ClassDeclarationSource

	/**
	 * Source code element containing an initializer block.
	 * 
	 * @author epimethix
	 *
	 */
	public final static class InitializerBlockSource implements Source {
		private final String name;
		private boolean staticBlock;
		private final CharSequence block;

		private InitializerBlockSource(Matcher matcher, CharSequence sourceCode) {
			this(matcher, sourceCode, 0, sourceCode.length());
		}

		private InitializerBlockSource(Matcher matcher, CharSequence sourceCode, int start, int end) {
			this.name = UUID.randomUUID().toString();
			this.staticBlock = Objects.nonNull(matcher.group(INITIALIZER_BLOCK_STATIC));
			this.block = sourceCode.subSequence(start, end);
		}

		private InitializerBlockSource(boolean staticBlock, CharSequence sourceCode) {
			this(staticBlock, sourceCode, 0, sourceCode.length());
		}

		private InitializerBlockSource(boolean staticBlock, CharSequence sourceCode, int start, int end) {
			this.name = UUID.randomUUID().toString();
			this.staticBlock = staticBlock;
			this.block = sourceCode.subSequence(start, end);
		}

		/**
		 * Tests if the static keyword was used.
		 * 
		 * @return true if the initializer block is {@code static}
		 */
		public boolean isStatic() {
			return staticBlock;
		}

		@Override
		public void append(StringBuilder out) {
			out.append(block);
		}

		@Override
		public CharSequence getSource() {
			return block;
		}

		@Override
		public String toString() {
			if (staticBlock) {
				return "static initializer block";
			} else {
				return "instance initializer block";
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			InitializerBlockSource other = (InitializerBlockSource) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		/**
		 * Builder class to create a new {@code InitializerBlockSource}.
		 * 
		 * @author epimethix
		 *
		 */
		public final static class Builder {
			private boolean staticBlock;
			private final List<String> statements = new ArrayList<>();

			private Builder() {}

			/**
			 * Obtains a new {@code Builder}.
			 * 
			 * @return a {@code Builder} to create a new {@code InitializerBlockSource}
			 */
			public final static Builder newInitializerBlock() {
				return new Builder();
			}

			/**
			 * Toggles the static keyword of the block which is initially off.
			 * 
			 * @return this {@code Builder}
			 */
			public final Builder setStatic() {
				staticBlock = !staticBlock;
				return this;
			}

			/**
			 * Adds a statement to the list of statements.
			 * 
			 * @param statement the statement to add
			 * @return this {@code Builder}
			 */
			public final Builder addStatement(String statement) {
				statements.add(statement);
				return this;
			}

			/**
			 * Builds the initializer block source code and {@code InitializerBlockSource}.
			 * 
			 * @return the new {@code InitializerBlockSource}
			 */
			public final InitializerBlockSource build() {
				StringBuilder sourceCode = new StringBuilder();
				if (staticBlock) {
					sourceCode.append("\tstatic {");
				} else {
					sourceCode.append("\t{");
				}
				if (!statements.isEmpty()) {
					sourceCode.append(String.format("%n"));
					for (String statement : statements) {
						if (statement.endsWith(";")) {
							sourceCode.append("\t".repeat(2)).append(statement).append(String.format("%n"));
						} else {
							sourceCode.append("\t".repeat(2)).append(statement).append(String.format(";%n"));
						}
					}
					sourceCode.append("\t");
				}
				sourceCode.append(String.format("}%n"));
				return new InitializerBlockSource(staticBlock, sourceCode);
			}
		} // End of class Builder
	} // End of class InitializerBlockSource

	/**
	 * Class member source containing a constructor.
	 * 
	 * @author epimethix
	 *
	 */
	public final static class ConstructorSource implements Member {
		private final CommentSource comment;
		private final List<AnnotationSource> annotations;
		private final String visibility;
		private final String identifier;
		private final List<String> parameters;
		private final List<String> exceptions;
		private final CharSequence constructor;

		private ConstructorSource(CommentSource comment, List<AnnotationSource> annotations, String visibility,
				String identifier, List<String> parameters, List<String> exceptions, CharSequence sourceCode, int start,
				int end) {
			this.comment = comment;
			this.annotations = annotations;
			if (Objects.nonNull(visibility)) {
				this.visibility = visibility.trim();
			} else {
				this.visibility = "";
			}
			this.identifier = identifier;
			this.parameters = parameters;
			this.exceptions = exceptions;
			this.constructor = sourceCode.subSequence(start, end);
		}

		@Override
		public Optional<CommentSource> getComment() {
			return Optional.ofNullable(comment);
		}

		@Override
		public List<AnnotationSource> getAnnotations() {
			return annotations;
		}

		@Override
		public String getVisibility() {
			return visibility;
		}

		@Override
		public List<String> getModifiers() {
			return Collections.emptyList();
		}

		@Override
		public String getIdentifier() {
			return identifier;
		}

		/**
		 * Get the constructors parameters.
		 * 
		 * @return the parameters
		 */
		public List<String> getParameters() {
			return parameters;
		}

		/**
		 * Get the constructors thrown exceptions.
		 * 
		 * @return the exceptions
		 */
		public List<String> getExceptions() {
			return exceptions;
		}

		@Override
		public CharSequence getSource() {
			return constructor;
		}

		@Override
		public void append(StringBuilder out) {
			if (Objects.nonNull(comment)) {
				comment.append(out);
			}
			if (Objects.nonNull(annotations)) {
				for (AnnotationSource as : annotations) {
					as.append(out);
				}
			}
			out.append(constructor);
		}

		@Override
		public String toString() {
			return "constructor";
		}

//		@Override
//		public String toString() {
//			return "ConstructorSource [annotations=" + annotations + ", visibility=" + visibility + ", identifier="
//					+ identifier + ", parameters=" + parameters + ", exceptions=" + exceptions + "]";
//		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
			result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ConstructorSource other = (ConstructorSource) obj;
			if (identifier == null) {
				if (other.identifier != null)
					return false;
			} else if (!identifier.equals(other.identifier))
				return false;
			if (parameters == null) {
				if (other.parameters != null)
					return false;
			} else if (!parameters.equals(other.parameters))
				return false;
			return true;
		}

		/**
		 * {@code Builder} class to create a new {@code ConstructorSource}.
		 * 
		 * @author epimethix
		 *
		 */
		public final static class Builder {
			private CommentSource comment;
			private final List<AnnotationSource> annotations = new ArrayList<>();
			private String visibility = "";
			private final String identifier;
			private final List<String> parameters = new ArrayList<>();
			private final List<String> exceptions = new ArrayList<>();
			private final List<String> statements = new ArrayList<>();

			private Builder(String identifier) {
				this.identifier = identifier;
			}

			/**
			 * Obtains a new {@code Builder} to create a new {@code ConstructorSource}.
			 * 
			 * @param identifier the class name
			 * @return a new {@code Builder}
			 */
			public static final Builder newConstructor(String identifier) {
				return new Builder(identifier);
			}

			/**
			 * Sets the java-doc comment for the constructor.
			 * 
			 * @param comment the java-doc comment
			 * @return this {@code Builder}
			 */
			public final Builder setComment(CommentSource comment) {
				this.comment = comment;
				return this;
			}

			/**
			 * 
			 * @param annotation
			 * @return this {@code Builder}
			 */
			public final Builder addAnnotation(AnnotationSource annotation) {
				annotations.add(annotation);
				return this;
			}

			/**
			 * Sets the visibility modifier to 'public'.
			 * 
			 * @return this {@code Builder}
			 */
			public final Builder setPublic() {
				visibility = "public";
				return this;
			}

			/**
			 * Sets the visibility modifier to 'protected'.
			 * 
			 * @return this {@code Builder}
			 */
			public final Builder setProtected() {
				visibility = "protected";
				return this;
			}

			/**
			 * Sets the visibility modifier to 'private'.
			 * 
			 * @return this {@code Builder}
			 */
			public final Builder setPrivate() {
				visibility = "private";
				return this;
			}

			/**
			 * Sets the visibility modifier to ''.
			 * 
			 * @return this {@code Builder}
			 */
			public final Builder setPackagePrivate() {
				visibility = "";
				return this;
			}

			/**
			 * Adds a parameter to the parameter list.
			 * 
			 * @param parameter the parameter to add
			 * @return this {@code Builder}
			 */
			public final Builder addParameter(String parameter) {
				parameters.add(parameter);
				return this;
			}

			/**
			 * Adds an {@code Exception} to the list of thrown {@code Exception}s.
			 * 
			 * @param exception the {@code Exception} to add.
			 * @return this {@code Builder}
			 */
			public final Builder addException(String exception) {
				exceptions.add(exception);
				return this;
			}

			/**
			 * Adds a statement to the constructor block.
			 * 
			 * @param statement the statement to add
			 * @return this {@code Builder}
			 */
			public final Builder addStatement(String statement) {
				statements.add(statement);
				return this;
			}

			/**
			 * Builds the constructor source code and {@code ConstructorSource}.
			 * 
			 * @return the new {@code ConstructorSource}
			 */
			public final ConstructorSource build() {
				StringBuilder sourceCode = new StringBuilder("\t");
				if (visibility.length() > 0) {
					sourceCode.append(visibility).append(" ");
				}
				sourceCode.append(identifier).append("(");
				if (!parameters.isEmpty()) {
					sourceCode.append(String.join(", ", parameters));
				}
				sourceCode.append(")");
				if (!exceptions.isEmpty()) {
					sourceCode.append(" throws ").append(String.join(", ", exceptions));
				}
				sourceCode.append(String.format(" {"));
				if (!statements.isEmpty()) {
					sourceCode.append(String.format("%n"));
					for (String statement : statements) {
						sourceCode.append("\t".repeat(2)).append(statement);
						if (!statement.endsWith(";")) {
							sourceCode.append(";");
						}
						sourceCode.append(String.format("%n"));
					}
					sourceCode.append("\t");
				}
				sourceCode.append(String.format("}%n"));

				return new ConstructorSource(comment, annotations, visibility, identifier, parameters, exceptions,
						sourceCode, 0, sourceCode.length());
			}
		} // End of class Builder
	} // End of class ConstructorSource

	/**
	 * Class member source containing a field.
	 * 
	 * @author epimethix
	 *
	 */
	public final static class FieldSource implements Member {
		private final CommentSource comment;
		private final List<AnnotationSource> annotations;
		private final String visibility;
		private final List<String> modifiers;
		private final String type;
		private final List<String> typeParameters;
		private final String fieldName;
		private final String array;
		private final String value;
		private final CharSequence field;

		private FieldSource(CommentSource comment, List<AnnotationSource> annotations, String visibility,
				List<String> modifiers, String type, List<String> typeParameters, String fieldName, String array,
				String value, CharSequence sourceCode, int start, int end) {
			this.comment = comment;
			this.annotations = annotations;
			this.visibility = visibility;
			this.modifiers = modifiers;
			this.type = type;
			this.typeParameters = typeParameters;
			this.fieldName = fieldName;
			this.array = array;
			this.value = value;
			this.field = sourceCode.subSequence(start, end);
		}

		@Override
		public Optional<CommentSource> getComment() {
			return Optional.ofNullable(comment);
		}

		@Override
		public List<AnnotationSource> getAnnotations() {
			return annotations;
		}

		@Override
		public String getVisibility() {
			return visibility;
		}

		@Override
		public List<String> getModifiers() {
			return modifiers;
		}

		/**
		 * Gets the fields type.
		 * 
		 * @return the type
		 */
		public String getType() {
			return type;
		}

		/**
		 * Gets the array declaration like '[]' or '[][]', if no array was declared an
		 * empty String is returned.
		 * 
		 * @return the array declaration.
		 */
		public String getArray() {
			return array;
		}

		public Optional<String> getValue() {
			return Optional.ofNullable(value);
		}

		public Optional<String> getStringValue() {
			if (Objects.isNull(value)) {
				return Optional.empty();
			}
			if ("null".equals(value)) {
				return Optional.empty();
			} else if (value.startsWith("\"") && value.endsWith("\"")) {
				return Optional.of(value.substring(1, value.length() - 1));
			}
			return Optional.of(value);
		}

		/**
		 * Gets the fields type parameters.
		 * 
		 * @return the type parameters
		 */
		public List<String> getTypeParameters() {
			return typeParameters;
		}

		@Override
		public String getIdentifier() {
			return fieldName;
		}

		@Override
		public CharSequence getSource() {
			return field;
		}

		@Override
		public void append(StringBuilder out) {
			if (Objects.nonNull(comment)) {
				comment.append(out);
			}
			if (Objects.nonNull(annotations)) {
				for (AnnotationSource as : annotations) {
					as.append(out);
				}
			}
			out.append(field);
		}

		@Override
		public String toString() {
			return String.format("Field '%s'", fieldName);
		}

//		@Override
//		public String toString() {
//			return "FieldSource [annotations=" + annotations + ", visibility=" + visibility + ", modifiers=" + modifiers
//					+ ", type=" + type + ", typeParameters=" + typeParameters + ", fieldName=" + fieldName + "]";
//		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FieldSource other = (FieldSource) obj;
			if (fieldName == null) {
				if (other.fieldName != null)
					return false;
			} else if (!fieldName.equals(other.fieldName))
				return false;
			return true;
		}

		/**
		 * Builder class to create a new {@code FieldSource}.
		 * 
		 * @author epimethix
		 *
		 */
		public static final class Builder {
			private CommentSource comment;
			private List<AnnotationSource> annotations = new ArrayList<>();
			/*
			 * Modifiers
			 */
			private String visibility = "";
			private boolean isStatic;
			private boolean isFinal;
			private boolean isTransient;
			private boolean isVolatile;
			/*
			 * Type
			 */
			private final String type;
			private List<String> typeParameters = new ArrayList<>();
			private String array = "";
			/**
			 * Name
			 */
			private final String identifier;
			/*
			 * Initializer
			 */
			private String value;

			private Builder(String type, String identifier) {
				this.type = type;
				this.identifier = identifier;
			}

			/**
			 * Obtain a {@code Builder} to create a new {@code FieldSource}.
			 * 
			 * @param type       the field type
			 * @param identifier the field name
			 * @return a new {@code Builder}
			 */
			public static final Builder newField(String type, String identifier) {
				return new Builder(type, identifier);
			}

			/**
			 * Obtain a {@code Builder} to create a new {@code FieldSource} with modifiers
			 * 'public final static'.
			 * 
			 * @param type       the field type
			 * @param identifier the field name
			 * @return a new {@code Builder}
			 */
			public static final Builder newConstant(String type, String identifier) {
				return new Builder(type, identifier).setPublic().setStatic().setFinal();
			}

			/**
			 * Set the Java Doc Comment for this field.
			 * 
			 * @param comment the comment
			 * @return this {@code Builder}
			 */
			public final Builder setComment(CommentSource comment) {
				this.comment = comment;
				return this;
			}

			/**
			 * adds an annotation for this field.
			 * 
			 * @param annotation the annotation
			 * @return this {@code Builder}
			 */
			public final Builder addAnnotation(AnnotationSource annotation) {
				annotations.add(annotation);
				return this;
			}

			/**
			 * sets the visibility modifier to 'public'.
			 * 
			 * @return this {@code Builder}
			 */
			public final Builder setPublic() {
				visibility = "public";
				return this;
			}

			/**
			 * sets the visibility modifier to 'protected'.
			 * 
			 * @return this {@code Builder}
			 */
			public final Builder setProtected() {
				visibility = "protected";
				return this;
			}

			/**
			 * sets the visibility modifier to 'private'.
			 * 
			 * @return this {@code Builder}
			 */
			public final Builder setPrivate() {
				visibility = "private";
				return this;
			}

			/**
			 * sets the visibility modifier to ''.
			 * 
			 * @return this {@code Builder}
			 */
			public final Builder setPackagePrivate() {
				visibility = "";
				return this;
			}

			/**
			 * toggles the modifier 'static' which is off by default.
			 * 
			 * @return this {@code Builder}
			 */
			public final Builder setStatic() {
				isStatic = !isStatic;
				return this;
			}

			/**
			 * toggles the modifier 'final' which is off by default.
			 * 
			 * @return this {@code Builder}
			 */
			public final Builder setFinal() {
				isFinal = !isFinal;
				return this;
			}

			/**
			 * toggles the modifier 'transient' which is off by default.
			 * 
			 * @return this {@code Builder}
			 */
			public final Builder setTransient() {
				isTransient = !isTransient;
				return this;
			}

			/**
			 * toggles the modifier 'volatile' which is off by default.
			 * 
			 * @return this {@code Builder}
			 */
			public final Builder setVolatile() {
				isVolatile = !isVolatile;
				return this;
			}

			/**
			 * adds the specified parameter to the field type - type parameter list.
			 * 
			 * @param parameter the type parameter
			 * @return this {@code Builder}
			 */
			public final Builder addTypeParameter(String parameter) {
				typeParameters.add(parameter);
				return this;
			}

			/**
			 * sets the number of array dimensions of the field type.
			 * 
			 * @return this {@code Builder}
			 */
			public final Builder setArrayDimensions(int dimensions) {
				array = "[]".repeat(dimensions);
				return this;
			}

			/**
			 * sets the value or expression to initialize the field with.
			 * 
			 * @param value the initial value
			 * @return this {@code Builder}
			 */
			public final Builder setValue(String value) {
				this.value = value;
				return this;
			}

			/**
			 * sets the {@code String} literal to initialize the field with (wraps the
			 * specified {@code String} in {@code String} quotes).
			 * 
			 * @param value the initial value to wrap in quotes
			 * @return this {@code Builder}
			 */
			public Builder setStringValue(String value) {
				this.value = String.format("\"%s\"", value);
				return this;
			}

			/**
			 * Builds this field.
			 * 
			 * @return the produced {@code FieldSource}
			 */
			public FieldSource build() {
				StringBuilder sourceCode = new StringBuilder("\t");
				List<String> modifiers = new ArrayList<>();
				if (visibility.length() > 0) {
					modifiers.add(visibility);
				}
				if (isStatic) {
					modifiers.add("static");
				}
				if (isFinal) {
					modifiers.add("final");
				}
				if (isTransient) {
					modifiers.add("transient");
				}
				if (isVolatile) {
					modifiers.add("volatile");
				}
				for (String mod : modifiers) {
					sourceCode.append(mod).append(" ");
				}
				sourceCode.append(type);
				if (typeParameters.size() > 0) {
					sourceCode.append("<").append(String.join(", ", typeParameters)).append(">");
				}
				sourceCode.append(array).append(" ").append(identifier);
				if (Objects.nonNull(value)) {
					sourceCode.append(" = ").append(value);
				}
				sourceCode.append(";").append(String.format("%n"));
				FieldSource fieldSource = new FieldSource(comment, annotations, visibility, modifiers, type,
						typeParameters, identifier, array, value, sourceCode, 0, sourceCode.length());
				return fieldSource;
			}

			public static Builder editField(FieldSource fs) {
				Builder b = newField(fs.getType(), fs.getIdentifier()).setArrayDimensions(fs.getArray().length() / 2);
				if (fs.isStatic()) {
					b.setStatic();
				}
				if (fs.isFinal()) {
					b.setFinal();
				}
				if (fs.isPackagePrivate()) {
					b.setPackagePrivate();
				}
				if (fs.isPrivate()) {
					b.setPrivate();
				}
				if (fs.isPublic()) {
					b.setPublic();
				}
				if (fs.isProtected()) {
					b.setProtected();
				}
				if (fs.isTransient()) {
					b.setTransient();
				}
				if (fs.isVolatile()) {
					b.setVolatile();
				}
				Optional<String> opt = fs.getValue();
				if (opt.isPresent()) {
					b.setValue(opt.get());
				}
				Optional<CommentSource> optComment = fs.getComment();
				if (optComment.isPresent()) {
					b.setComment(optComment.get());
				}
				for (AnnotationSource as : fs.getAnnotations()) {
					b.addAnnotation(as);
				}
				return b;
			}
		} // End of class Builder
	} // End of class FieldSource

	/**
	 * Class member source containing a method.
	 * 
	 * @author epimethix
	 *
	 */
	public final static class MethodSource implements Member {
		private final CommentSource comment;
		private final List<AnnotationSource> annotations;
		private final String visibility;
		private final List<String> modifiers;
		private final List<String> typeParameters;
		private final String returnType;
		private final List<String> returnTypeParameters;
		private final String returnTypeArray;
		private final String methodName;
		private final List<String> parameters;
		private final List<String> exceptions;
		private final CharSequence method;

		private MethodSource(CommentSource comment, List<AnnotationSource> annotations, String visibility,
				List<String> modifiers, List<String> typeParameters, String returnType,
				List<String> returnTypeParameters, String returnTypeArray, String methodName, List<String> parameters,
				List<String> exceptions, CharSequence sourceCode, int start, int end) {
			this.comment = comment;
			this.annotations = annotations;
			this.visibility = visibility;
			this.modifiers = modifiers;
			this.typeParameters = typeParameters;
			this.returnType = returnType;
			this.returnTypeParameters = returnTypeParameters;
			this.returnTypeArray = returnTypeArray;
			this.methodName = methodName;
			this.parameters = parameters;
			this.exceptions = exceptions;
			this.method = sourceCode.subSequence(start, end);
		}

		@Override
		public Optional<CommentSource> getComment() {
			return Optional.ofNullable(comment);
		}

		@Override
		public List<AnnotationSource> getAnnotations() {
			return annotations;
		}

		@Override
		public String getVisibility() {
			return visibility;
		}

		@Override
		public List<String> getModifiers() {
			return modifiers;
		}

		/**
		 * Gets the methods type parameters.
		 * 
		 * @return the type parameters
		 */
		public List<String> getTypeParameters() {
			return typeParameters;
		}

		/**
		 * Gets the methods return type.
		 * 
		 * @return the return type
		 */
		public String getReturnType() {
			return returnType;
		}

		/**
		 * Gets the methods return type parameters.
		 * 
		 * @return the return type parameters
		 */
		public List<String> getReturnTypeParameters() {
			return returnTypeParameters;
		}

		/**
		 * gets the array portion of the return type.
		 * 
		 * @return the array or an empty string if the return type is not an array.
		 */
		public String getReturnTypeArray() {
			return returnTypeArray;
		}

		@Override
		public String getIdentifier() {
			return methodName;
		}

		/**
		 * Gets the methods parameters.
		 * 
		 * @return the parameters
		 */
		public List<String> getParameters() {
			return parameters;
		}

		/**
		 * Gets the methods thrown exceptions.
		 * 
		 * @return the exceptions
		 */
		public List<String> getExceptions() {
			return exceptions;
		}

		@Override
		public CharSequence getSource() {
			return method;
		}

		@Override
		public void append(StringBuilder out) {
			if (Objects.nonNull(comment)) {
				comment.append(out);
			}
			if (Objects.nonNull(annotations)) {
				for (AnnotationSource as : annotations) {
					as.append(out);
				}
			}
			out.append(method);
		}

		@Override
		public String toString() {
			return String.format("Method '%s'", methodName);
		}

//		@Override
//		public String toString() {
//			return "MethodSource [annotations=" + annotations + ", visibility=" + visibility + ", modifiers="
//					+ modifiers + ", typeParameters=" + typeParameters + ", returnType=" + returnType
//					+ ", returnTypeParameters=" + returnTypeParameters + ", methodName=" + methodName + ", parameters="
//					+ parameters + ", exceptions=" + exceptions + "]";
//		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
			result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MethodSource other = (MethodSource) obj;
			if (methodName == null) {
				if (other.methodName != null)
					return false;
			} else if (!methodName.equals(other.methodName))
				return false;
			if (parameters == null) {
				if (other.parameters != null)
					return false;
			} else if (!parameters.equals(other.parameters))
				return false;
			return true;
		}

		/**
		 * {@code Builder} class to create a new {@code MethodSource}.
		 * 
		 * @author epimethix
		 *
		 */
		public static final class Builder {
			private CommentSource comment;
			private final List<AnnotationSource> annotations = new ArrayList<>();
			private String visibility = "";
			private boolean isAbstract;
			private boolean isStatic;
			private boolean isFinal;
			private boolean isSynchronized;
			private boolean isNative;
			private boolean isStrictfp;
			private final List<String> typeParameters = new ArrayList<>();
			private final String returnType;
			private List<String> returnTypeParameters = new ArrayList<>();
			private String returnTypeArray = "";
			private final String methodName;
			private final List<String> parameters = new ArrayList<>();
			private final List<String> exceptions = new ArrayList<>();
			private final List<String> statements = new ArrayList<>();
			private boolean isInterfaceMethod;

			private Builder(String returnType, String methodName) {
				this.returnType = returnType;
				this.methodName = methodName;
			}

			/**
			 * Creates a {@code Builder} with the specified method name and 'void' as return
			 * value.
			 * 
			 * @param methodName the method identifier
			 * @return a new {@code Builder}
			 */
			public static final Builder newMethod(String methodName) {
				return new Builder("void", methodName);
			}

			/**
			 * Creates a {@code Builder} with the specified method name and 'void' as return
			 * value.
			 * 
			 * @param returnType the method return type
			 * @param methodName the method identifier
			 * @return a new {@code Builder}
			 */
			public static final Builder newMethod(String returnType, String methodName) {
				return new Builder(returnType, methodName);
			}

			public final Builder setComment(CommentSource comment) {
				this.comment = comment;
				return this;
			}

			public final Builder addAnnotation(AnnotationSource annotation) {
				annotations.add(annotation);
				return this;
			}

			public final Builder setPublic() {
				visibility = "public";
				return this;
			}

			public final Builder setProtected() {
				visibility = "protected";
				return this;
			}

			public final Builder setPrivate() {
				visibility = "private";
				return this;
			}

			public final Builder setPackagePrivate() {
				visibility = "";
				return this;
			}

			public final Builder setAbstract() {
				isAbstract = !isAbstract;
				if (isAbstract && isFinal) {
					isFinal = false;
				}
				return this;
			}

			public final Builder setStatic() {
				isStatic = !isStatic;
				return this;
			}

			public final Builder setFinal() {
				isFinal = !isFinal;
				if (isAbstract && isFinal) {
					isAbstract = false;
				}
				return this;
			}

			public final Builder setSynchronized() {
				isSynchronized = !isSynchronized;
				return this;
			}

			public final Builder setNative() {
				isNative = !isNative;
				return this;
			}

			public final Builder setStrictfp() {
				isStrictfp = !isStrictfp;
				return this;
			}

			public final Builder addTypeParameter(String parameter) {
				typeParameters.add(parameter);
				return this;
			}

			public final Builder addReturnTypeParameter(String parameter) {
				returnTypeParameters.add(parameter);
				return this;
			}

			public final Builder setReturnTypeArrayDimensions(int dimensions) {
				returnTypeArray = "[]".repeat(dimensions);
				return this;
			}

			public final Builder addParameter(String parameter) {
				parameters.add(parameter);
				return this;
			}

			public final Builder addException(String exception) {
				exceptions.add(exception);
				return this;
			}

			public final Builder addStatement(String statement) {
				statements.add(statement);
				return this;
			}

			public final Builder setInterfaceMethod() {
				isInterfaceMethod = !isInterfaceMethod;
				return this;
			}

			public final MethodSource build() {
				StringBuilder sourceCode = new StringBuilder("\t");
				List<String> modifiers = new ArrayList<>();
				if (visibility.length() > 0) {
					modifiers.add(visibility);
				}
				if (isAbstract) {
					modifiers.add("abstract");
				}
				if (isStatic) {
					modifiers.add("static");
				}
				if (isFinal) {
					modifiers.add("final");
				}
				if (isSynchronized) {
					modifiers.add("synchronized");
				}
				if (isNative) {
					modifiers.add("native");
				}
				if (isStrictfp) {
					modifiers.add("strictfp");
				}
				for (String mod : modifiers) {
					sourceCode.append(mod).append(" ");
				}
				if (!typeParameters.isEmpty()) {
					sourceCode.append("<").append(String.join(", ", typeParameters)).append("> ");
				}
				sourceCode.append(returnType);
				if (!returnTypeParameters.isEmpty()) {
					sourceCode.append("<").append(String.join(", ", returnTypeParameters)).append(">");
				}
				sourceCode.append(returnTypeArray).append(" ").append(methodName).append("(");
				if (!parameters.isEmpty()) {
					sourceCode.append(String.join(", ", parameters));
				}
				sourceCode.append(")");
				if (!exceptions.isEmpty()) {
					sourceCode.append(" throws ").append(String.join(", ", exceptions));
				}
				if (isAbstract || isInterfaceMethod) {
					sourceCode.append(String.format(";%n"));
				} else {
					sourceCode.append(String.format(" {%n"));
					if (statements.isEmpty()) {
						CommentSource todo = CommentSource.Builder
								.newLineComment(2, String.format("TODO implement generated method stub %s(%s)",
										methodName, String.join(", ", parameters)))
								.build();
						todo.append(sourceCode);
						if (!"void".equals(returnType)) {
							String returnValue = "null";
							if ("int".equals(returnType) || "byte".equals(returnType) || "short".equals(returnType)) {
								returnValue = "0";
							} else if ("long".equals(returnType)) {
								returnValue = "0L";
							} else if ("char".equals(returnType)) {
								returnValue = "'\\u0000'";
							} else if ("boolean".equals(returnType)) {
								returnValue = "false";
							} else if ("float".equals(returnType)) {
								returnValue = "0.0f";
							} else if ("double".equals(returnType)) {
								returnValue = "0.0d";
							}
							sourceCode.append("\t".repeat(2)).append(String.format("return %s;%n", returnValue));
						}
					} else {
						for (String statement : statements) {
							if (statement.endsWith(";")) {
								sourceCode.append("\t".repeat(2)).append(statement).append(String.format("%n"));
							} else {
								sourceCode.append("\t".repeat(2)).append(statement).append(";")
										.append(String.format("%n"));
							}
						}
					}
					sourceCode.append(String.format("\t}%n"));
				}
				return new MethodSource(comment, annotations, visibility, modifiers, typeParameters, returnType,
						returnTypeParameters, returnTypeArray, methodName, parameters, exceptions, sourceCode, 0,
						sourceCode.length());
			}
		} // End of class Builder
	} // End of class MethodSource

	/**
	 * Class member source containing a member class.
	 * 
	 * @author epimethix
	 *
	 */
	public final static class MemberClassSource implements Member {
		private final CommentSource comment;
		private final List<AnnotationSource> annotations;
		private final String visibility;
		private final List<String> modifiers;
		private final String classKeyword;
		private final String className;
		private final List<String> typeParameters;
		private final String superType;
		private final List<String> interfaces;
		private final CharSequence classSrc;

		private MemberClassSource(CommentSource javaDoc, List<AnnotationSource> annotations, String visibility,
				List<String> modifiers, String classKeyword, String className, List<String> typeParameters,
				String superType, List<String> interfaces, CharSequence sourceCode, int start, int end) {
			this.comment = javaDoc;
			this.annotations = annotations;
			this.visibility = visibility;
			this.modifiers = modifiers;
			this.classKeyword = classKeyword;
			this.className = className;
			this.typeParameters = typeParameters;
			this.superType = superType;
			this.interfaces = interfaces;
			this.classSrc = sourceCode.subSequence(start, end);
		}

		/**
		 * Searches for the class source start (after comment and annotations).
		 * 
		 * @param boundaries the element boundaries
		 * @return the index of the class declaration start
		 */
		public int getSourceStart(Map<Source, Integer[]> boundaries) {
			int start = 0;
			int end = 0;
			if (Objects.nonNull(comment)) {
				end = comment.comment.length();
				boundaries.put(comment, new Integer[] { start, end });
				start = end;
			}
			if (Objects.nonNull(annotations)) {
				for (AnnotationSource as : annotations) {
					end = start + as.annotation.length();
					boundaries.put(as, new Integer[] { start, end });
					start = end;
				}
			}
			return start;
		}

		@Override
		public Optional<CommentSource> getComment() {
			return Optional.ofNullable(comment);
		}

		@Override
		public List<AnnotationSource> getAnnotations() {
			return annotations;
		}

		@Override
		public String getVisibility() {
			return this.visibility;
		}

		@Override
		public List<String> getModifiers() {
			return this.modifiers;
		}

		/**
		 * Gets the used keyword.
		 * 
		 * @return {@code class}, {@code interface} or {@code @interface}
		 */
		public String getClassKeyword() {
			return classKeyword;
		}

		@Override
		public String getIdentifier() {
			return className;
		}

		/**
		 * Gets the class type parameters.
		 * 
		 * @return the type parameters
		 */
		public List<String> getTypeParameters() {
			return typeParameters;
		}

		/**
		 * The super type if one is specified.
		 * 
		 * @return the super type
		 */
		public Optional<String> getSuperType() {
			return Optional.ofNullable(superType);
		}

		/**
		 * The list of interfaces this class implements.
		 * 
		 * @return the interfaces
		 */
		public List<String> getInterfaces() {
			return interfaces;
		}

		@Override
		public CharSequence getSource() {
			return classSrc;
		}

		@Override
		public void append(StringBuilder out) {
			if (Objects.nonNull(comment)) {
				comment.append(out);
			}
			if (Objects.nonNull(annotations)) {
				for (AnnotationSource as : annotations) {
					as.append(out);
				}
			}
			out.append(classSrc);
		}

//		@Override
//		public Integer checkIntegrity(Integer previousEnd, Map<Source, Integer[]> boundaries) {
//			Integer end = previousEnd;
//			if (Objects.nonNull(comment)) {
//				end = comment.checkIntegrity(end, boundaries);
//			}
//			if (Objects.nonNull(annotations)) {
//				for (AnnotationSource as : annotations) {
//					end = as.checkIntegrity(end, boundaries);
//				}
//			}
//			return Member.super.checkIntegrity(end, boundaries);
//		}

		@Override
		public String toString() {
			return String.format("member %s %s", classKeyword, className);
		}

//		@Override
//		public String toString() {
//			return "MemberClassSource [annotations=" + annotations + ", visibility=" + visibility + ", modifiers="
//					+ modifiers + ", classKeyword=" + classKeyword + ", className=" + className + ", typeParameters="
//					+ typeParameters + ", superType=" + superType + ", interfaces=" + interfaces + "]";
//		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((className == null) ? 0 : className.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MemberClassSource other = (MemberClassSource) obj;
			if (className == null) {
				if (other.className != null)
					return false;
			} else if (!className.equals(other.className))
				return false;
			return true;
		}
	} // End of class MemberClassSource

	/**
	 * Source code element containing the end of the source code.
	 * 
	 * @author epimethix
	 *
	 */
	public static class EndOfFileSource implements Source {
		private final String name;
		private final CharSequence endOfFile;

		private EndOfFileSource(CharSequence sourceCode, int start) {
			this.name = UUID.randomUUID().toString();
			this.endOfFile = sourceCode.subSequence(start, sourceCode.length());
		}

		@Override
		public void append(StringBuilder out) {
			out.append(endOfFile);
		}

		@Override
		public CharSequence getSource() {
			return endOfFile;
		}

		@Override
		public String toString() {
			return "end of file";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EndOfFileSource other = (EndOfFileSource) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	} // End of class EndOfFileSource

	/**
	 * Builder class to create a {@code JavaSource} object.
	 * 
	 * @author epimethix
	 *
	 */
	public final static class Builder {
		/**
		 * Obtain a {@code Builder} to create a java class.
		 * 
		 * @param packageName the package to put the new class in
		 * @param className   the simple class name
		 * @return the {@code Builder}
		 */
		public static Builder newClass(String packageName, String className) {
			return new Builder("class", packageName, className);
		}

		/**
		 * Obtain a {@code Builder} to create a java interface.
		 * 
		 * @param packageName the package to put the new interface in
		 * @param className   the simple class name
		 * @return the {@code Builder}
		 */
		public static Builder newInterface(String packageName, String className) {
			return new Builder("interface", packageName, className);
		}

		/**
		 * Obtain a {@code Builder} to create a java annotation.
		 * 
		 * @param packageName the package to put the new annotation in
		 * @param className   the simple class name
		 * @return the {@code Builder}
		 */
		public static Builder newAnnotation(String packageName, String className) {
			return new Builder("@interface", packageName, className);
		}

		/**
		 * Obtain a {@code Builder} to create a java enum.
		 * 
		 * @param packageName the package to put the new enum in
		 * @param className   the simple class name
		 * @return the {@code Builder}
		 */
		public static Builder newEnum(String packageName, String className) {
			return new Builder("enum", packageName, className);
		}

		private CommentSource comment;
		private List<AnnotationSource> annotations = new ArrayList<>();
		private final String newLine = String.format("%n");
		private final String packageName;
		private List<String> importStatements = new ArrayList<>();
		private String visibility = "public ";
		private boolean modFinal;
		private boolean modAbstract;
		private final String classKeyword;
		private List<String> typeParameters = new ArrayList<>();
		private final String className;
		private String superType;
		private List<String> interfaces = new ArrayList<>();
		private List<ConstructorSource> constructors = new ArrayList<>();
		private List<FieldSource> fields = new ArrayList<>();
		private List<MethodSource> methods = new ArrayList<>();

		private Builder(String classKeyword, String packageName, String className) {
			this.classKeyword = classKeyword;
			this.packageName = packageName;
			this.className = className;
		}

//		private Builder() {
//			classKeyword = null;
//			packageName = null;
//			className = null;
//		}

		public final Builder setPackagePrivate() {
			visibility = "";
			return this;
		}

		public final Builder setFinal() {
			modFinal = true;
			return this;
		}

		public final Builder setAbstract() {
			modAbstract = true;
			return this;
		}

		public final Builder addImport(String importString) {
			importStatements.add(importString);
			return this;
		}

		public final Builder addStaticImport(String importString) {
			importStatements.add("static ".concat(importString));
			return this;
		}

		public final Builder setComment(CommentSource comment) {
			this.comment = comment;
			return this;
		}

		public final Builder addAnnotation(AnnotationSource annotation) {
			annotations.add(annotation);
			return this;
		}

		public final Builder addTypeParameter(String typeParameter) {
			typeParameters.add(typeParameter);
			return this;
		}

		public final Builder addInterface(String interface0) {
			interfaces.add(interface0);
			return this;
		}

		public final Builder setSuperType(String superType) {
			this.superType = superType;
			return this;
		}

		public final Builder addField(FieldSource fieldSource) {
			fields.add(fieldSource);
			return this;
		}

		public final Builder addConstructor(ConstructorSource constructor) {
			constructors.add(constructor);
			return this;
		}

		public final Builder addMethod(MethodSource method) {
			methods.add(method);
			return this;
		}

		public JavaSource build() {
			StringBuilder code = new StringBuilder();
			if (Objects.nonNull(packageName) && packageName.trim().length() > 0) {
				code.append("package ").append(packageName).append(";").append(newLine).append(newLine);
			}
			if (!importStatements.isEmpty()) {
				for (String importStatement : importStatements) {
					code.append("import ").append(importStatement).append(";").append(newLine);
				}
				code.append(newLine);
			}
			if (Objects.nonNull(comment)) {
				comment.append(code);
			}
			for (AnnotationSource an : annotations) {
				an.append(code);
			}
			code.append(visibility);
			if (modAbstract) {
				code.append("abstract ");
			} else if (modFinal) {
				code.append("final ");
			}
			code.append(classKeyword).append(" ").append(className);
			if (!typeParameters.isEmpty()) {
				code.append(" <");
				for (int i = 0; i < typeParameters.size(); i++) {
					code.append(typeParameters.get(i));
					if (i + 1 < typeParameters.size()) {
						code.append(", ");
					}
				}
				code.append(">");
			}
			if (Objects.nonNull(superType)) {
				code.append(" extends ").append(superType);
			}
			if (!interfaces.isEmpty()) {
				if ("interface".equals(classKeyword)) {
					code.append(" extends ");
				} else {
					code.append(" implements ");
				}
				for (int i = 0; i < interfaces.size(); i++) {
					code.append(interfaces.get(i));
					if (i + 1 < interfaces.size()) {
						code.append(", ");
					}
				}
			}
			code.append(" {").append(newLine);
			EmptyLineSource emptyLine = new EmptyLineSource();
			if (!fields.isEmpty() || !methods.isEmpty()) {
				if (!fields.isEmpty()) {
					for (int i = 0; i < fields.size(); i++) {
						emptyLine.append(code);
						fields.get(i).append(code);
					}
				}
				if (!constructors.isEmpty()) {
					for (int i = 0; i < constructors.size(); i++) {
						emptyLine.append(code);
						constructors.get(i).append(code);
					}
				}
				if (!methods.isEmpty()) {
					for (int i = 0; i < methods.size(); i++) {
						emptyLine.append(code);
						methods.get(i).append(code);
					}
				}
			} else {
//				emptyLine.append(code);
			}
			code.append("}").append(newLine);
			return new JavaSource(code);
		}

	} // End of class Builder

	private final List<Source> sources;
	private final Map<Source, Integer[]> boundaries;
	private final int length;
	private final boolean isMemberClass;

	private String packageName;
	private List<String> imports;
	private CommentSource comment;
	private List<AnnotationSource> annotations;
	private String visibility;
	private List<String> modifiers;
	private String classKeyword;
	private String className;
	private List<String> typeParameters;
	private String superType;
	private List<String> interfaces;

	private boolean hasChanges;

	private JavaSource(CharSequence sourceCode) {
		this.sources = new ArrayList<>();
		this.boundaries = new HashMap<>();
		this.length = sourceCode.length();
		this.isMemberClass = false;
		readCode(sourceCode, 0, sourceCode.length());
	}

	private JavaSource(MemberClassSource memberClassSource, JavaSource containerClassSource) {
		this.sources = new ArrayList<>();
		this.boundaries = new HashMap<>();
		this.isMemberClass = true;
		this.packageName = containerClassSource.getClassName();
		this.imports = containerClassSource.imports;
		this.annotations = memberClassSource.annotations;
		this.visibility = memberClassSource.visibility;
		this.modifiers = memberClassSource.modifiers;
		this.classKeyword = memberClassSource.classKeyword;
		this.className = memberClassSource.className;
		this.typeParameters = memberClassSource.typeParameters;
		this.superType = memberClassSource.superType;
		this.interfaces = memberClassSource.interfaces;
//		CharSequence sourceCode = memberClassSource.getSource();
		StringBuilder sourceCode = new StringBuilder();
		memberClassSource.append(sourceCode);
		length = sourceCode.length();
		int startClassDef = memberClassSource.getSourceStart(boundaries);
		int endClassDef = seekEndOfOpenBlock(sourceCode, startClassDef);
		endClassDef = nextNewLine(sourceCode, endClassDef);
		ClassDeclarationSource classDeclarationSource = new ClassDeclarationSource(memberClassSource,
				endClassDef - startClassDef);
		boundaries.put(classDeclarationSource, new Integer[] { startClassDef, endClassDef });
		this.sources.add(classDeclarationSource);
		int startClass = endClassDef;
		this.readCode(sourceCode, startClass, startClass);
	}

	private void readCode(CharSequence sourceCode, int start, int startClass) {
//		int start = 0;
//		int startClass = sourceCode.length();
		int length = sourceCode.length();
		Optional<Matcher> test;
		CommentSource javaDocComment = null;
		List<AnnotationSource> annotations = new ArrayList<>();
		boolean packageSet = false;
		while (start < length) {
			test = testIndex(EMPTY_LINE_PATTERN, sourceCode, start);
			if (test.isPresent()) {
				Matcher emptyLine = test.get();
				Source els = new EmptyLineSource(sourceCode, start, emptyLine.end());
				boundaries.put(els, new Integer[] { start, emptyLine.end() });
				sources.add(els);
				start = emptyLine.end();
				continue;
			}
			test = testIndex(JAVA_DOC_COMMENT_START_PATTERN, sourceCode, start);
			if (test.isPresent()) {
				Matcher comment = test.get();
				if (Objects.nonNull(javaDocComment)) {
					addMisplacedJavaDoc(javaDocComment);
					javaDocComment = null;
				}
				int end = nextEndOfComment(sourceCode, comment.end());
				end = nextNewLine(sourceCode, end);
				javaDocComment = new CommentSource(sourceCode, start, end);
				boundaries.put(javaDocComment, new Integer[] { start, end });
				start = end;
				continue;
			}
			test = testIndex(MULTI_LINE_COMMENT_START_PATTERN, sourceCode, start);
			if (test.isPresent()) {
				Matcher comment = test.get();
				int end = nextEndOfComment(sourceCode, comment.end());
				end = nextNewLine(sourceCode, end);
				Source commentSource = new CommentSource(sourceCode, start, end);
				boundaries.put(commentSource, new Integer[] { start, end });
				sources.add(commentSource);
				start = end;
				continue;
			}
			test = testIndex(LINE_COMMENT_PATTERN, sourceCode, start);
			if (test.isPresent()) {
				Matcher comment = test.get();
				Source commentSource = new CommentSource(sourceCode, start, comment.end());
				boundaries.put(commentSource, new Integer[] { start, comment.end() });
				sources.add(commentSource);
				start = comment.end();
				continue;
			}
			test = testIndex(ANNOTATION_SHORT_PATTERN, sourceCode, start);
			if (test.isPresent()) {
				Matcher annotationShort = test.get();
				String annotationType = annotationShort.group(ANNOTATION_SHORT_TYPE_NAME);
				if (!"interface".equals(annotationType)) {
					int end = annotationShort.end();
					if (Objects.nonNull(annotationShort.group(ANNOTATION_SHORT_OPEN_BRACKET))) {
						end = seekEndOfClosingBracket(sourceCode, end);
					}
					// TODO maybe dont seek newline by default when evaluating annotations?
					end = nextNewLine(sourceCode, end);
					AnnotationSource annotationSource = new AnnotationSource(annotationShort, sourceCode, start, end);
					boundaries.put(annotationSource, new Integer[] { start, end });
					annotations.add(annotationSource);
					start = end;
					continue;
				}
			}
			if (start < startClass) {
				if (!packageSet) {
					Optional<Matcher> pkgOpt = testIndex(PACKAGE_PATTERN, sourceCode, start);
					if (pkgOpt.isPresent()) {
						Matcher pkg = pkgOpt.get();
						Source packageSource = new PackageSource(pkg, sourceCode, start, pkg.end());
						packageName = pkg.group(PACKAGE_NAME);
						boundaries.put(packageSource, new Integer[] { start, pkg.end() });
						sources.add(packageSource);
						start = pkg.end();
					}
					packageSet = true;
					continue;
				} else {
					Optional<Matcher> importOpt = testIndex(IMPORT_PATTERN, sourceCode, start);
					if (importOpt.isPresent()) {
						Matcher importMatcher = importOpt.get();
						Source importSource = new ImportSource(importMatcher, sourceCode, start, importMatcher.end());
						boundaries.put(importSource, new Integer[] { start, importMatcher.end() });
						sources.add(importSource);
						if (Objects.isNull(imports)) {
							imports = new ArrayList<>();
						}
						imports.add(importMatcher.group(IMPORT_TYPE));
						start = importMatcher.end();
						continue;
					}
					Optional<Matcher> clsOpt = find(CLASS_SIGNATURE_PATTERN, sourceCode, start);
					if (clsOpt.isPresent()) {
						Matcher classDefinitionMatcher = clsOpt.get();
						ClassDeclarationSource classDeclarationSource = parseClassDeclaration(start, sourceCode,
								classDefinitionMatcher, javaDocComment, annotations);
						int end = start + classDeclarationSource.getSource().length();
						boundaries.put(classDeclarationSource, new Integer[] { start, end });
						sources.add(classDeclarationSource);
						javaDocComment = null;
						annotations = new ArrayList<>();
						start = end;
						startClass = start;
						continue;
//						}
					} else {
						System.err.println("Class declaration not recognized!");
					}
				}
			} else {
				if ("enum".equals(classKeyword)) {
					// TODO read enum constants
				} else if ("@interface".equals(classKeyword)) {
					// TODO Read annotation members
				}
				test = testIndex(INITIALIZER_BLOCK_SIGNATURE_PATTERN, sourceCode, start);
				if (test.isPresent()) {
//					findVerbose(INITIALIZER_BLOCK_SIGNATURE_PATTERN, sourceCode, start);
					if (Objects.nonNull(javaDocComment)) {
						addMisplacedJavaDoc(javaDocComment);
						javaDocComment = null;
					}
					Matcher initializer = test.get();
					int end = seekEndOfBlock(sourceCode, initializer.end());
					end = nextNewLine(sourceCode, end);
					Source initSource = new InitializerBlockSource(initializer, sourceCode, start, end);
					boundaries.put(initSource, new Integer[] { start, end });
					sources.add(initSource);
					start = end;
					continue;
				}
				test = testIndex(MEMBER_CLASS_SIGNATURE_PATTERN, sourceCode, start);
				if (test.isPresent()) {
//					if (System.currentTimeMillis() > 0) {
//						findVerbose(MEMBER_CLASS_SIGNATURE_PATTERN, sourceCode, start);
//						break;
//					}
					Matcher memberClass = test.get();
					MemberClassSource memberClassSource = parseMemberClass(start, sourceCode, memberClass,
							javaDocComment, annotations);
					int end = start + memberClassSource.getSource().length();
					boundaries.put(memberClassSource, new Integer[] { start, end });
					sources.add(memberClassSource);
					javaDocComment = null;
					annotations = new ArrayList<>();
					start = end;
					continue;
				}
				test = testIndex(CONSTRUCTOR_SIGNATURE_PATTERN, sourceCode, start);
				if (test.isPresent()) {
//					if (System.currentTimeMillis() > 0) {
//						findVerbose(CONSTRUCTOR_SIGNATURE_PATTERN, sourceCode, start);
//						break;
//					}
					Matcher constructor = test.get();
					ConstructorSource constructorSource = parseConstructor(start, sourceCode, constructor,
							javaDocComment, annotations);
					int end = start + constructorSource.getSource().length();
					boundaries.put(constructorSource, new Integer[] { start, end });
					sources.add(constructorSource);
					javaDocComment = null;
					annotations = new ArrayList<>();
					start = end;
					continue;
				}
				test = testIndex(METHOD_SIGNATURE_SHORT_PATTERN, sourceCode, start);
				if (test.isPresent()) {
//					if (System.currentTimeMillis() > 0) {
//						findVerbose(METHOD_SIGNATURE_SHORT_PATTERN, sourceCode, start);
//						break;
//					}
					Matcher method = test.get();
					MethodSource methodSource = parseMethod(start, sourceCode, method, javaDocComment, annotations);
					int end = methodSource.getSource().length() + start;
					boundaries.put(methodSource, new Integer[] { start, end });
					sources.add(methodSource);
					javaDocComment = null;
					annotations = new ArrayList<>();
					start = end;
					continue;
				}
				test = testIndex(FIELD_SIGNATURE_PATTERN, sourceCode, start);
				if (test.isPresent()) {
//					if (System.currentTimeMillis() > 0) {
//						findVerbose(FIELD_SIGNATURE_PATTERN, sourceCode, start);
//						break;
//					}
					Matcher field = test.get();
					FieldSource fieldSource = parseField(start, sourceCode, field, javaDocComment, annotations);
					int end = start + fieldSource.getSource().length();
					boundaries.put(fieldSource, new Integer[] { start, end });
					sources.add(fieldSource);
					javaDocComment = null;
					annotations = new ArrayList<>();
					start = end;
					continue;
				}
				test = testIndex(MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_PATTERN, sourceCode, start);
				if (test.isPresent()) {
					Matcher method = test.get();
//					findVerbose(MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_PATTERN, sourceCode, start);
					MethodSource methodSource = parseMethod2(start, sourceCode, method, javaDocComment, annotations);
//					if (System.currentTimeMillis() > 0) {
//						break;
//					}
					int end = start + methodSource.getSource().length();
					boundaries.put(methodSource, new Integer[] { start, end });
					javaDocComment = null;
					annotations = new ArrayList<>();
					sources.add(methodSource);
					start = end;
					continue;
				}
				test = testIndex(MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_PARAMETERS_PATTERN, sourceCode, start);
				if (test.isPresent()) {
//					findVerbose(MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_PARAMETERS_PATTERN, sourceCode, start);
					Matcher mystery = test.get();
					Source source = parseMethodOrField(start, sourceCode, mystery, javaDocComment, annotations);
//					if (System.currentTimeMillis() > 0) {
//						break;
//					}
					int end = start + source.getSource().length();
					boundaries.put(source, new Integer[] { start, end });
					sources.add(source);
					javaDocComment = null;
					annotations = new ArrayList<>();
					start = end;
					continue;
				}
				test = testIndex(END_OF_CLASS_PATTERN, sourceCode, start);
				if (test.isPresent()) {
					Source endOfFileSource = new EndOfFileSource(sourceCode, start);
					boundaries.put(endOfFileSource, new Integer[] { start, length });
					sources.add(endOfFileSource);
					if (Objects.nonNull(javaDocComment)) {
						addMisplacedJavaDoc(javaDocComment);
						javaDocComment = null;
					}
					start = length;
					break;
				}
			}
			System.err.println(
					"failed to find a match! defaulted to end of file. use JavaSource.printDebug() to see what may have gone wrong.");
			Source endOfFileSource = new EndOfFileSource(sourceCode, start);
			boundaries.put(endOfFileSource, new Integer[] { start, length });
			sources.add(endOfFileSource);
			if (Objects.nonNull(javaDocComment)) {
				addMisplacedJavaDoc(javaDocComment);
				javaDocComment = null;
			}
			start = length;
			break;
		} // loop
		checkIntegrity(0, boundaries);
	}

	private ClassDeclarationSource parseClassDeclaration(int start, CharSequence sourceCode, Matcher classDefinition,
			CommentSource javaDocComment, List<AnnotationSource> annotations) {
		this.comment = javaDocComment;
		this.annotations = annotations;
		String modifiersList = classDefinition.group(CLASS_SIGNATURE_MODIFIERS);
		this.modifiers = splitModifiers(modifiersList);
		this.visibility = getVisibilityFromModifiers(this.modifiers);
		this.className = classDefinition.group(CLASS_SIGNATURE_IDENTIFIER);
		this.classKeyword = classDefinition.group(CLASS_SIGNATURE_KEYWORD);
		this.typeParameters = new ArrayList<>();
		int startTypeParams = classDefinition.end(CLASS_SIGNATURE_IDENTIFIER);
		int end = readTypeParameters(sourceCode, startTypeParams, typeParameters);
//		System.err.println(typeParameters);
		this.interfaces = new ArrayList<>();
		if ("interface".equals(classKeyword)) {
			Optional<Matcher> optExtends = testIndex(CLASS_EXTENDS_KEYWORD_PATTERN, sourceCode, end);
			if (optExtends.isPresent()) {
				Matcher extendsMatcher = optExtends.get();
				end = extendsMatcher.end();
				end = readSignatureList(sourceCode, end, this.interfaces);
			} else {
				end = seekEndOfOpenBlock(sourceCode, end);
			}
		} else {
			Optional<Matcher> optExtends = testIndex(CLASS_EXTENDS_PATTERN, sourceCode, end);
			if (optExtends.isPresent()) {
//			findVerbose(CLASS_EXTENDS_PATTERN, sourceCode, end);
				Matcher extendsMatcher = optExtends.get();
				end = extendsMatcher.end();
				Optional<Matcher> typeParametersOpt = testIndex(TYPE_PARAMETERS_START_PATTERN, sourceCode, end);
				if (typeParametersOpt.isPresent()) {
					end = seekEndOfTypeParameters(sourceCode, typeParametersOpt.get().end());
				}
				superType = sourceCode.subSequence(extendsMatcher.start(CLASS_EXTENDS_SUPER_TYPE), end).toString();
			}
			Optional<Matcher> implementsOpt = testIndex(CLASS_IMPLEMENTS_PATTERN, sourceCode, end);
			if (implementsOpt.isPresent()) {
				Matcher implementsMatcher = implementsOpt.get();
				end = readSignatureList(sourceCode, implementsMatcher.end(), this.interfaces);
			} else {
				end = seekEndOfOpenBlock(sourceCode, end);
			}
		}
		int endOfClass = seekEndOfBlock(sourceCode, end);
		int nextNewLine = nextNewLine(sourceCode, end);
		if (endOfClass > nextNewLine) {
			end = nextNewLine;
		}
		ClassDeclarationSource classDeclarationSource = new ClassDeclarationSource(classDefinition, javaDocComment,
				annotations, sourceCode, start, end);
		return classDeclarationSource;
	}

	private MemberClassSource parseMemberClass(int start, CharSequence sourceCode, Matcher memberClass,
			CommentSource javaDocComment, List<AnnotationSource> annotations) {
		String modifiersList = memberClass.group(MEMBER_CLASS_SIGNATURE_MODIFIERS);
		List<String> modifiers = splitModifiers(modifiersList);
		String visibility = getVisibilityFromModifiers(modifiers);
		String classKeyword = memberClass.group(MEMBER_CLASS_SIGNATURE_KEYWORD);
		String className = memberClass.group(MEMBER_CLASS_SIGNATURE_IDENTIFIER);
		List<String> typeParameters = new ArrayList<>();
		int startTypeParams = memberClass.end(MEMBER_CLASS_SIGNATURE_IDENTIFIER);
		int end = readTypeParameters(sourceCode, startTypeParams, typeParameters);
		List<String> interfaces = new ArrayList<>();
		String superType = null;
		if ("interface".equals(classKeyword)) {
			Optional<Matcher> optExtends = testIndex(CLASS_EXTENDS_KEYWORD_PATTERN, sourceCode, end);
			if (optExtends.isPresent()) {
				Matcher extendsMatcher = optExtends.get();
				end = extendsMatcher.end();
				end = readSignatureList(sourceCode, end, interfaces);
			} else {
				end = seekEndOfOpenBlock(sourceCode, end);
			}
		} else {
			Optional<Matcher> optExtends = testIndex(CLASS_EXTENDS_PATTERN, sourceCode, end);
			if (optExtends.isPresent()) {
				Matcher extendsMatcher = optExtends.get();
				end = extendsMatcher.end();
				Optional<Matcher> typeParametersOpt = testIndex(TYPE_PARAMETERS_START_PATTERN, sourceCode, end);
				if (typeParametersOpt.isPresent()) {
					end = seekEndOfTypeParameters(sourceCode, typeParametersOpt.get().end());
				}
				superType = sourceCode.subSequence(extendsMatcher.start(CLASS_EXTENDS_SUPER_TYPE), end).toString();
			}
			Optional<Matcher> implementsOpt = testIndex(CLASS_IMPLEMENTS_PATTERN, sourceCode, end);
			if (implementsOpt.isPresent()) {
				Matcher implementsMatcher = implementsOpt.get();
				end = readSignatureList(sourceCode, implementsMatcher.end(), interfaces);
			} else {
				end = seekEndOfOpenBlock(sourceCode, end);
			}
		}
		end = seekEndOfBlock(sourceCode, end);
		end = nextNewLine(sourceCode, end);
//		System.out.println("member class supertype: " + superType);
//		System.out.println("member class interfaces: " + interfaces);
		MemberClassSource memberClassSource = new MemberClassSource(javaDocComment, annotations, visibility, modifiers,
				classKeyword, className, typeParameters, superType, interfaces, sourceCode, start, end);
		return memberClassSource;
	}

	private ConstructorSource parseConstructor(int start, CharSequence sourceCode, Matcher constructor,
			CommentSource javaDocComment, List<AnnotationSource> annotations) {
		String visibility = constructor.group(CONSTRUCTOR_VISIBILITY);
		if (Objects.isNull(visibility)) {
			visibility = "";
		}
		String identifier = constructor.group(CONSTRUCTOR_IDENTIFIER);
		int end = constructor.end();
		List<String> parameters = new ArrayList<>();
		end = readParameters(sourceCode, end, parameters);
		List<String> exceptions = new ArrayList<>();
		end = readExceptions(sourceCode, end, exceptions);
		end = seekEndOfBlock(sourceCode, end);
		end = nextNewLine(sourceCode, end);
		ConstructorSource constructorSource = new ConstructorSource(javaDocComment, annotations, visibility, identifier,
				parameters, exceptions, sourceCode, start, end);
		return constructorSource;
	}

	private FieldSource parseField(int start, CharSequence sourceCode, Matcher field, CommentSource javaDocComment,
			List<AnnotationSource> annotations) {
		String modifiersList = field.group(FIELD_SIGNATURE_MODIFIERS);
		List<String> modifiers = splitModifiers(modifiersList);
		String visibility = getVisibilityFromModifiers(modifiers);
		String type = field.group(FIELD_SIGNATURE_TYPE_NAME);
		String array = field.group(FIELD_SIGNATURE_TYPE_ARRAY);
		List<String> typeParameters = new ArrayList<>();
		int typeParamsStart = field.end(FIELD_SIGNATURE_TYPE);
		readTypeParameters(sourceCode, typeParamsStart, typeParameters);
		if (Objects.isNull(array) || array.trim().length() == 0) {
			array = field.group(FIELD_SIGNATURE_TYPE_PARAMETERS_ARRAY);
		}
		String fieldName = field.group(FIELD_SIGNATURE_IDENTIFIER);
		if (Objects.isNull(array) || array.trim().length() == 0) {
			array = field.group(FIELD_SIGNATURE_IDENTIFIER_ARRAY);
		}
		if (Objects.isNull(array)) {
			array = "";
		}
		int end = field.end();
		String terminator = field.group(FIELD_SIGNATURE_TERMINATOR);
		String value;
		if ("=".equals(terminator)) {
			int valueStart = end;
			end = seekEndOfStatement(sourceCode, end);
			value = sourceCode.subSequence(valueStart, end - 1).toString().trim();
//			System.err.println(value);
		} else {
			value = null;
		}
		end = nextNewLine(sourceCode, end);
		FieldSource fieldSource = new FieldSource(javaDocComment, annotations, visibility, modifiers, type,
				typeParameters, fieldName, array, value, sourceCode, start, end);
		return fieldSource;
	}

	private MethodSource parseMethod(int start, CharSequence sourceCode, Matcher method, CommentSource javaDocComment,
			List<AnnotationSource> annotations) {
		String modifiersList = method.group(METHOD_SIGNATURE_MODIFIERS);
		List<String> modifiers = splitModifiers(modifiersList);
		String visibility = getVisibilityFromModifiers(modifiers);
		List<String> typeParameters = new ArrayList<>();
		if (Objects.nonNull(method.group(METHOD_SIGNATURE_TYPE_PARAMETERS))) {
//			findVerbose(METHOD_SIGNATURE_SHORT_PATTERN, sourceCode, start);
			int startTypeParams = method.start(METHOD_SIGNATURE_TYPE_PARAMETERS_WITH_BRACKETS);
			readTypeParameters(sourceCode, startTypeParams, typeParameters);
		}
		String returnType = method.group(METHOD_SIGNATURE_RETURN_TYPE_NAME);
		String returnTypeArray = method.group(METHOD_SIGNATURE_RETURN_TYPE_ARRAY);
		List<String> returnTypeParameters = new ArrayList<>();
		int startTypeParams = method.end(METHOD_SIGNATURE_RETURN_TYPE);
		readTypeParameters(sourceCode, startTypeParams, returnTypeParameters);
		if (Objects.isNull(returnTypeArray) || returnTypeArray.trim().length() == 0) {
			returnTypeArray = method.group(METHOD_SIGNATURE_RETURN_TYPE_PARAMETERS_ARRAY);
		}
		if (Objects.isNull(returnTypeArray)) {
			returnTypeArray = "";
		}
		String methodName = method.group(METHOD_SIGNATURE_IDENTIFIER);
		List<String> parameters = new ArrayList<>();
		int end = readParameters(sourceCode, method.end(), parameters);
		List<String> exceptions = new ArrayList<>();
		end = readExceptions(sourceCode, end, exceptions);
		if (sourceCode.charAt(end - 1) == '{') {
			end = seekEndOfBlock(sourceCode, end);
		}
		end = nextNewLine(sourceCode, end);
		MethodSource methodSource = new MethodSource(javaDocComment, annotations, visibility, modifiers, typeParameters,
				returnType, returnTypeParameters, returnTypeArray, methodName, parameters, exceptions, sourceCode,
				start, end);
		return methodSource;
	}

	private MethodSource parseMethod2(int start, CharSequence sourceCode, Matcher method, CommentSource javaDocComment,
			List<AnnotationSource> annotations2) {
		String modifiersList = method.group(MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_MODIFIERS);
		List<String> modifiers = splitModifiers(modifiersList);
		String visibility = getVisibilityFromModifiers(modifiers);
		List<String> typeParameters = new ArrayList<>();
		int end;
		{
			int startTypeParams = method.start(MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_TYPE_PARAMS_OPEN);
			end = readTypeParameters(sourceCode, startTypeParams, typeParameters);
		}
		Matcher typeNameMatcher = testIndex(TYPE_NAME_PATTERN, sourceCode, end).get();
		String returnType = typeNameMatcher.group(TYPE_NAME_NAME);
		String returnTypeArray = typeNameMatcher.group(TYPE_NAME_ARRAY);
		end = typeNameMatcher.end();
		List<String> returnTypeParameters = new ArrayList<>();
		end = readTypeParameters(sourceCode, end, returnTypeParameters);
		Optional<Matcher> test = testIndex(ARRAY_PATTERN, sourceCode, end);
		if (test.isPresent()) {
			Matcher arrayMatcher = test.get();
			returnTypeArray = arrayMatcher.group().trim();
			end = arrayMatcher.end();
		}
		if (Objects.isNull(returnTypeArray)) {
			returnTypeArray = "";
		}
		end = skipWhitespace(sourceCode, end);
		test = testIndex(IDENTIFIER_PATTERN, sourceCode, end);
//		findVerbose(IDENTIFIER_PATTERN, sourceCode, end);
		method = test.get();
		String methodName = test.get().group(IDENTIFIER_NAME);
		end = method.end();
		end = seekEndOfOpenBracket(sourceCode, end);
		List<String> parameters = new ArrayList<>();
		end = readParameters(sourceCode, end, parameters);
		List<String> exceptions = new ArrayList<>();
		end = readExceptions(sourceCode, end, exceptions);
		if (sourceCode.charAt(end - 1) == '{') {
			end = seekEndOfBlock(sourceCode, end);
		}
		end = nextNewLine(sourceCode, end);
		MethodSource methodSource = new MethodSource(javaDocComment, annotations2, visibility, modifiers, typeParameters,
				returnType, returnTypeParameters, returnTypeArray, methodName, parameters, exceptions, sourceCode,
				start, end);
		return methodSource;
	}

	private Source parseMethodOrField(int start, CharSequence sourceCode, Matcher mystery, CommentSource javaDocComment,
			List<AnnotationSource> annotations) {
		String modifiersList = mystery.group(MYSTERY_SIGNATURE_METHOD_OR_FIELD_MODIFIERS);
		List<String> modifiers = splitModifiers(modifiersList);
		String visibility = getVisibilityFromModifiers(modifiers);
		String type = mystery.group(MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_NAME);
		int startTypeParams = mystery.start(MYSTERY_SIGNATURE_METHOD_OR_FIELD_OPEN_TYPE_PARAMETERS);
		List<String> typeParameters = new ArrayList<>();
		int end = readTypeParameters(sourceCode, startTypeParams, typeParameters);
		Optional<Matcher> test = testIndex(ARRAY_PATTERN, sourceCode, end);
		String array = "";
		if (test.isPresent()) {
			Matcher arrayMatcher = test.get();
			array = arrayMatcher.group();
			end = arrayMatcher.end();
		}
//		System.err.println(array);
		end = skipWhitespace(sourceCode, end);
		test = testIndex(MYSTERY_IDENTIFIER_PATTERN, sourceCode, end);
		Matcher matchIdentifier = test.get();
		String identifier = matchIdentifier.group(MYSTERY_IDENTIFIER_NAME);
		char terminator = matchIdentifier.group(MYSTERY_IDENTIFIER_TERMINATOR).charAt(0);
		end = matchIdentifier.end();
		Source source;
		if (terminator == '(') {
			List<String> parameters = new ArrayList<>();
			end = readParameters(sourceCode, end, parameters);
			List<String> exceptions = new ArrayList<>();
			end = readExceptions(sourceCode, end, exceptions);
			if (sourceCode.charAt(end - 1) == '{') {
				end = seekEndOfBlock(sourceCode, end);
			}
			end = nextNewLine(sourceCode, end);
			source = new MethodSource(javaDocComment, annotations, visibility, modifiers, Collections.emptyList(), type,
					typeParameters, array, identifier, parameters, exceptions, sourceCode, start, end);
		} else {
			if (Objects.isNull(array) || array.trim().length() == 0) {
				array = matchIdentifier.group(MYSTERY_IDENTIFIER_ARRAY);
			}
			if (Objects.isNull(array)) {
				array = "";
			}
			String value;
			if (terminator == '=') {
				int startOfValue = end;
				end = seekEndOfStatement(sourceCode, end);
				value = sourceCode.subSequence(startOfValue, end - 1).toString().trim();
			} else {
				value = null;
			}
			end = nextNewLine(sourceCode, end);
			source = new FieldSource(javaDocComment, annotations, visibility, modifiers, type, typeParameters,
					identifier, array, value, sourceCode, start, end);
		}
		return source;
	}

	private void addMisplacedJavaDoc(CommentSource javaDocSource) {
		Integer[] docBoundary = boundaries.get(javaDocSource);
		for (int i = sources.size() - 1, round = 0; i > -1; i--, round++) {
			Integer[] testBoundary = boundaries.get(sources.get(i));
			if (testBoundary[1].equals(docBoundary[0])) {
				if (round == 0) {
					sources.add(javaDocSource);
					System.err.println("Inserted misplaced JavaDoc at the end!");
					return;
				} else {
					sources.add(i + 1, javaDocSource);
					System.err.println("Inserted misplaced JavaDoc!");
					return;
				}
			}
		}
		sources.add(javaDocSource);
		System.err.println("Inserted misplaced JavaDoc at the end BY DEFAULT!");
	}

	/**
	 * Adds an import to this source file if it is not yet present.
	 * <p>
	 * The import will be inserted either after the last import statement, after the
	 * 
	 * @param importSource the import to add
	 */
	public void insertImport(ImportSource importSource) {
		if (!sources.contains(importSource)) {
			int indexOfPackageDefinition = -1;
			int indexOfLastImport = -1;
			{
				int i = 0;
				for (Source s : sources) {
					if (s instanceof PackageSource) {
						indexOfPackageDefinition = i;
					} else if (s instanceof ImportSource) {
						indexOfLastImport = i;
					}
					i++;
				}
			}
			int indexToInsert;
			if (indexOfLastImport > -1) {
				indexToInsert = indexOfLastImport + 1;
			} else if (indexOfPackageDefinition > -1) {
				indexToInsert = indexOfPackageDefinition + 1;
				sources.add(indexToInsert, new EmptyLineSource());
				indexToInsert++;
			} else if (sources.get(0) instanceof CommentSource) {
				indexToInsert = 1;
			} else {
				indexToInsert = 0;
			}
			sources.add(indexToInsert, importSource);
		}
	}

	/**
	 * Adds a field to this {@code JavaSource}.
	 * <p>
	 * The specified field will be inserted after the last occurrence of any other
	 * field. If there are no other Fields the specified {@code FieldSource} will be
	 * inserted after the class declaration.
	 * 
	 * @param field the {@code FieldSource} to insert.
	 */
	public void insertField(FieldSource field) {
		int indexOfClassDefinition = -1;
		int indexOfLastField = -1;
		{
			int i = 0;
			for (Source s : sources) {
				if (s instanceof ClassDeclarationSource) {
					indexOfClassDefinition = i;
				} else if (s instanceof FieldSource) {
					indexOfLastField = i;
				}
				i++;
			}
		}
		int indexToInsert;
		if (indexOfLastField > -1) {
			indexToInsert = indexOfLastField + 1;
		} else {
			indexToInsert = indexOfClassDefinition + 1;
		}
		sources.add(indexToInsert, new EmptyLineSource());
		indexToInsert++;
		sources.add(indexToInsert, field);
	}

	/**
	 * Adds a constructor to this {@code JavaSource}.
	 * <p>
	 * The specified constructor will be either inserted after the last constructor
	 * if present or else the last field if present, otherwise directly after the
	 * class declaration.
	 * 
	 * @param constructor the {@code ConstructorSource} to insert.
	 */
	public void insertConstructor(ConstructorSource constructor) {
		int indexOfClassDefinition = -1;
		int indexOfLastField = -1;
		int indexOfLastConstructor = -1;
		{
			int i = 0;
			for (Source s : sources) {
				if (s instanceof ClassDeclarationSource) {
					indexOfClassDefinition = i;
				} else if (s instanceof FieldSource) {
					indexOfLastField = i;
				} else if (s instanceof ConstructorSource) {
					indexOfLastConstructor = i;
				}
				i++;
			}
		}
		int indexToInsert;
		if (indexOfLastConstructor > -1) {
			indexToInsert = indexOfLastConstructor + 1;
		} else if (indexOfLastField > -1) {
			indexToInsert = indexOfLastField + 1;
		} else {
			indexToInsert = indexOfClassDefinition + 1;
		}
		sources.add(indexToInsert, new EmptyLineSource());
		indexToInsert++;
		sources.add(indexToInsert, constructor);
	}

	/**
	 * Adds a method to this {@code JavaSource}.
	 * <p>
	 * Inserts the specified method either at the end after the last method if
	 * present, else after the last constructor if present, else after the last
	 * field if present otherwise directly after the class declaration.
	 * 
	 * @param method the {@code MethodSource} to insert.
	 */
	public void insertMethod(MethodSource method) {
		int indexOfClassDefinition = -1;
		int indexOfLastField = -1;
		int indexOfLastConstructor = -1;
		int indexOfLastMethod = -1;
		{
			int i = 0;
			for (Source s : sources) {
				if (s instanceof ClassDeclarationSource) {
					indexOfClassDefinition = i;
				} else if (s instanceof FieldSource) {
					indexOfLastField = i;
				} else if (s instanceof ConstructorSource) {
					indexOfLastConstructor = i;
				} else if (s instanceof MethodSource) {
					indexOfLastMethod = i;
				}
				i++;
			}
		}
		int indexToInsert;
		if (indexOfLastMethod > -1) {
			indexToInsert = indexOfLastMethod + 1;
		} else if (indexOfLastConstructor > -1) {
			indexToInsert = indexOfLastConstructor + 1;
		} else if (indexOfLastField > -1) {
			indexToInsert = indexOfLastField + 1;
		} else {
			indexToInsert = indexOfClassDefinition + 1;
		}
		sources.add(indexToInsert, new EmptyLineSource());
		indexToInsert++;
		sources.add(indexToInsert, method);
	}

	/**
	 * Gets the class's package name.
	 * 
	 * @return the package name
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * Gets the imports.
	 * 
	 * @return the imports
	 */
	public List<String> getImports() {
		return imports;
	}

	/**
	 * Gets the comment if there is one.
	 * 
	 * @return the class comment
	 */
	public Optional<CommentSource> getComment() {
		return Optional.ofNullable(comment);
	}

	/**
	 * Gets the class annotations.
	 * 
	 * @return the annotations
	 */
	public List<AnnotationSource> getAnnotations() {
		return annotations;
	}

	/**
	 * Gets the class visibility
	 * 
	 * @return the visibility
	 */
	public String getVisibility() {
		return visibility;
	}

	/**
	 * Gets the class modifiers.
	 * 
	 * @return the modifiers
	 */
	public List<String> getModifiers() {
		return modifiers;
	}

	/**
	 * Gets the used keyword.
	 * 
	 * @return {@code class}, {@code interface} or {@code @interface}
	 */
	public String getClassKeyword() {
		return classKeyword;
	}

	/**
	 * Gets the class identifier without its package name.
	 * 
	 * @return the simple class name
	 */
	public String getSimpleClassName() {
		return className;
	}

	/**
	 * Gets the fully qualified class name.
	 * 
	 * @return the full class name
	 */
	public String getClassName() {
		if (Objects.nonNull(packageName)) {
			return String.format("%s.%s", packageName, className);
		} else {
			return className;
		}
	}

	/**
	 * Gets the path to the java file of this class.
	 * 
	 * @param sourceDirectory the sources directory
	 * @return the path to the java file or nothing if this is a member class
	 */
	public Optional<Path> getPath(Path sourceDirectory) {
		if (!isMemberClass) {
			return Optional.of(sourceDirectory.resolve(Paths.get(getClassName().replaceAll("[.]", "/") + ".java")));
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Gets the class type parameters.
	 * 
	 * @return the type parameters
	 */
	public List<String> getTypeParameters() {
		return typeParameters;
	}

	/**
	 * Gets the class super type if one was declared.
	 * 
	 * @return the super type
	 */
	public Optional<String> getSuperType() {
		return Optional.ofNullable(superType);
	}

	/**
	 * Gets the class interfaces.
	 * 
	 * @return the interfaces
	 */
	public List<String> getInterfaces() {
		return interfaces;
	}

//	public Optional<Integer[]> getBoundaries(Source source) {
//		return Optional.ofNullable(boundaries.get(source));
//	}

	/**
	 * Gets the list of source elements in this class.
	 * 
	 * @return the sources
	 */
	public List<Source> getSources() {
		return sources;
	}

	/**
	 * Get the source code elements of a specific type.
	 * 
	 * @param type the source code type to look for
	 * @return the sources
	 */
	public List<Source> getSources(Class<? extends Source> type) {
		List<Source> result = new ArrayList<>();
		for (Source src : sources) {
			if (src.getClass() == type) {
				result.add(src);
			}
		}
		return sources;
	}

	/**
	 * Gets the field source elements of this class.
	 * 
	 * @return the field sources
	 */
	public List<FieldSource> getFields() {
		List<FieldSource> result = new ArrayList<>();
		for (Source src : sources) {
			if (src instanceof FieldSource) {
				result.add((FieldSource) src);
			}
		}
		return result;
	}

	/**
	 * Gets the constructor source elements of this class.
	 * 
	 * @return the constructors
	 */
	public List<ConstructorSource> getConstructors() {
		List<ConstructorSource> result = new ArrayList<>();
		for (Source src : sources) {
			if (src instanceof ConstructorSource) {
				result.add((ConstructorSource) src);
			}
		}
		return result;
	}

	/**
	 * Gets the method source elements of this class.
	 * 
	 * @return the methods
	 */
	public List<MethodSource> getMethods() {
		List<MethodSource> result = new ArrayList<>();
		for (Source src : sources) {
			if (src instanceof MethodSource) {
				result.add((MethodSource) src);
			}
		}
		return result;
	}

	/**
	 * Gets the member classes of this class.
	 * 
	 * @return the member classes
	 */
	public List<MemberClassSource> getMemberClasses() {
		List<MemberClassSource> result = new ArrayList<>();
		for (Source src : sources) {
			if (src instanceof MemberClassSource) {
				result.add((MemberClassSource) src);
			}
		}
		return result;
	}

	/**
	 * Reads a member class and parses it to a {@code JavaSource}.
	 * 
	 * @param memberClassSource the member class source to parse
	 * @return the {@code JavaSource} containing the supplied member class
	 */
	public JavaSource readMemberClass(MemberClassSource memberClassSource) {
		return new JavaSource(memberClassSource, this);
	}

	public int indexOf(Source fs) {
		return sources.indexOf(fs);
	}

	public Source set(Source fs, int index) {
		hasChanges = true;
		return sources.set(index, fs);
	}

	public Source replace(Source oldSrc, Source newSrc) {
		int i = sources.indexOf(oldSrc);
		if (i > -1) {
			hasChanges = true;
			return sources.set(i, newSrc);
		}
		return null;
	}

	public boolean hasChanges() {
		return hasChanges;
	}

	/**
	 * Print the source code to {@code System.out}.
	 */
	public void print() {
		print(System.out);
	}

	public void print(String path) throws IOException {
		print(new File(path));
	}

	/**
	 * Prints the source code to the specified file using UTF-8.
	 * 
	 * @param javaFile the {@code File} to write to
	 * @throws IOException if a file writing error occurs
	 */
	public void print(File javaFile) throws IOException {
		print(javaFile, StandardCharsets.UTF_8);
	}

	/**
	 * Prints the source code to the specified file using the specified
	 * {@code Charset}.
	 * 
	 * @param javaFile the {@code File} to write to
	 * @param charset  the charset to use
	 * @throws IOException if a file writing error occurs
	 */
	public void print(File javaFile, Charset charset) throws IOException {
		if (!javaFile.getParentFile().exists()) {
			javaFile.getParentFile().mkdirs();
		}
		try (PrintStream ps = new PrintStream(javaFile, charset)) {
			print(ps);
			hasChanges = false;
		}
	}

	/**
	 * Prints the source code elements to {@code System.out} separately to identify
	 * the elements that were created.
	 */
	public void printDebug() {
		printDebug(System.out);
	}

	/**
	 * Prints the source code elements to the specified {@code PrintStream}
	 * separately to identify the elements that were created.
	 * 
	 * @param printStream the stream to print the debug information to
	 */
	public void printDebug(PrintStream printStream) {
		StringBuilder out = new StringBuilder();
		for (Source source : this.sources) {
			if (source instanceof Member) {
				Member member = (Member) source;
				printDebug(out, member.getComment().orElse(null), member.getAnnotations(), member.getSource(), member);
			} else if (source instanceof ClassDeclarationSource) {
				ClassDeclarationSource classDec = (ClassDeclarationSource) source;
				printDebug(out, classDec.getComment().orElse(null), classDec.getAnnotations(), classDec.getSource(),
						classDec);
			} else {
				out.append("# ").append(source.toString()).append("\n'");
				source.append(out);
				out.append("'\n");
			}
		}
		printStream.println(out);
	}

	private void printDebug(StringBuilder out, CommentSource javaDoc, List<AnnotationSource> annotations,
			CharSequence source, Source s) {
		out.append("# ").append(s.toString()).append("\n");
		if (Objects.nonNull(javaDoc)) {
			out.append("-> Java Doc Comment:\n'");
			javaDoc.append(out);
			out.append("'\n");
//			}
		}
		if (Objects.nonNull(annotations) && annotations.size() > 0) {
			out.append("-> Annotations:\n'");
			for (AnnotationSource as : annotations) {
				as.append(out);
			}
			out.append("'\n");
		}
		out.append("-> Source:\n'");
		out.append(source);
		out.append("'\n");
	}

	/**
	 * Prints the source code to the specified {@code PrintStream}.
	 * 
	 * @param ps the {@code PrintStream} to print to
	 */
	public void print(PrintStream ps) {
		StringBuilder sourceCode = new StringBuilder();
		append(sourceCode);
		ps.print(sourceCode);
	}

	/**
	 * Prints the members of this and its subclasses to {@code System.out}.
	 */
	public void printMembers() {
		StringBuilder sb = new StringBuilder();
		printMembers(this, sb, "");
		System.out.print(sb);
	}

	private void printMembers(JavaSource src, StringBuilder sb, String prefix) {
		sb.append(String.format("%s# %s %s%n", prefix, src.getSimpleClassName(), src.getTypeParameters().toString()));
		if (!src.getFields().isEmpty())
			sb.append(String.format("%s- Fields%n", prefix));
		for (FieldSource s : src.getFields()) {
			sb.append(String.format("%s   %s : %s %s%n", prefix, s.getIdentifier(), s.getType(),
					s.getTypeParameters().toString()));
		}
		if (!src.getConstructors().isEmpty())
			sb.append(String.format("%s- Constructors%n", prefix));
		for (ConstructorSource s : src.getConstructors()) {
			sb.append(String.format("%s   %s %s%n", prefix, s.getIdentifier(), s.getParameters().toString()));
		}
		if (!src.getMethods().isEmpty())
			sb.append(String.format("%s- Methods%n", prefix));
		for (MethodSource s : src.getMethods()) {
			sb.append(String.format("%s   %s %s : %s %s%n", prefix, s.getIdentifier(), s.getParameters().toString(),
					s.getReturnType(), s.getReturnTypeParameters().toString()));
		}
		for (MemberClassSource mcs : src.getMemberClasses()) {
			JavaSource srcMember = src.readMemberClass(mcs);
			printMembers(srcMember, sb, prefix + "  ");
		}
	}

	@Override
	public String toString() {
		return String.format("%s %s", classKeyword, getClassName());
	}

	@Override
	public void append(StringBuilder out) {
		for (Source source : this.sources) {
			source.append(out);
		}
	}

	@Override
	public CharSequence getSource() {
		StringBuilder b = new StringBuilder();
		append(b);
		return b.toString();
	}

	@Override
	public Integer checkIntegrity(Integer end, Map<Source, Integer[]> boundaries) {
		String typeName;
		if (Objects.nonNull(packageName)) {
			typeName = packageName + "." + className;
		} else {
			typeName = className;
		}
		if (end == 0) {
			for (Source source : sources) {
				end = source.checkIntegrity(end, boundaries);
				if (end == -1) {
					System.err.println(typeName + ": Something went wrong! The order of elements does not match!");
					break;
				}
			}
			if (end.equals(length)) {
				StringBuilder code = new StringBuilder();
				append(code);
				if (code.length() == length) {
					System.out.println(typeName + ": Integrity check passed.");
				} else {
					System.err.println(typeName + ": Something went wrong! read length: " + code.length()
							+ " != original length: " + length + "!");
				}
			} else {
				System.err.println(typeName + ": Something went wrong! check ended at: " + end + " but length is: "
						+ length + "!");
			}
		} else {
			System.err.println(typeName + ": Integrity check was not executed!");
		}
		return end;
	}
}
