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

import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.NEEDED_WHITESPACE_PATTERN;
import static com.github.epimethix.lumicore.sourceutil.SourceAnalyzer.testIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to analyze java source code.
 * 
 * @author epimethix
 * 
 * @see JavaSource
 *
 */
public class SourceAnalyzer {
	private static final String OPTIONAL_WHITESPACE = "(\\s*)";
	private static final String NEEDED_WHITESPACE = "(\\s+)";
	private static final String OPTIONAL_LIST_SEPARATOR = "([,]\\s*)?";
	private static final String ARRAY = "((\\s*\\[\\])*)";
	private static final String JAVA_NAME = "(\\b[A-Za-z_$][A-Za-z0-9_$]*\\b)" + ARRAY;
	private static final String PKG_NAME = "(([A-Za-z0-9_$]+[.]?)*)";
	private static final String TYPE_NAME = "(" + PKG_NAME + JAVA_NAME + ")";

	public static final Pattern NEEDED_WHITESPACE_PATTERN = Pattern.compile(NEEDED_WHITESPACE);

	public static final Pattern IDENTIFIER_PATTERN = Pattern.compile(JAVA_NAME);
	public static final int IDENTIFIER_NAME = 1;
	public static final int IDENTIFIER_ARRAY = 2;

	public static final Pattern ARRAY_PATTERN = Pattern.compile(ARRAY);
	/**
	 * regex {@code Pattern} to match a type name preceded by zero ore more white
	 * spaces.
	 */
	public static final Pattern TYPE_NAME_PATTERN = Pattern.compile("\\s*" + TYPE_NAME);
	/**
	 * regex group index of 'type name' in {@link #TYPE_NAME_PATTERN}.
	 */
	public static final int TYPE_NAME_TYPE = 1;
	public static final int TYPE_NAME_PACKAGE = 2;
	public static final int TYPE_NAME_NAME = 4;
	public static final int TYPE_NAME_ARRAY = 5;

	private static final String TYPE_PARAMETERS = "([<]((([?])|(" + TYPE_NAME + ")" + OPTIONAL_LIST_SEPARATOR
			+ ")*)[>]\\s*)?";

	private static final String TYPE_PARAMETERS_START = "\\s*[<]";
	/**
	 * regex {@code Pattern} to match '{@code <}' preceded by zero ore more white
	 * spaces.
	 */
	public static final Pattern TYPE_PARAMETERS_START_PATTERN = Pattern.compile(TYPE_PARAMETERS_START);
	private static final String LIST_SEPARATOR = "[,]\\s*";
	/**
	 * regex {@code Pattern} to match '{@code ,}' followed by zero ore more white
	 * spaces.
	 */
	public static final Pattern LIST_SEPARATOR_PATTERN = Pattern.compile(LIST_SEPARATOR);
//	private static final String ANNOTATION = "(\\s*([@]" + TYPE_NAME + "(\\s*[(].*[)])?\\s*)*)";
	private static final String ANNOTATION_SHORT = "\\s*([@]" + TYPE_NAME + "(\\s*[(])?)";
	/**
	 * regex {@code Pattern} to match the start of an annotation preceded by zero
	 * ore more white spaces and an optional open bracket.
	 */
	public static final Pattern ANNOTATION_SHORT_PATTERN = Pattern.compile(ANNOTATION_SHORT);
	/**
	 * regex group index of 'type name' in {@link #ANNOTATION_SHORT_PATTERN}.
	 */
	public static final int ANNOTATION_SHORT_TYPE_NAME = 2;
	/**
	 * regex group index of 'open bracket' in {@link #ANNOTATION_SHORT_PATTERN}.
	 */
	public static final int ANNOTATION_SHORT_OPEN_BRACKET = 8;
	/*
	 * Line
	 */
	private static final String EMPTY_LINE = "\\s*(\\r?\\n|$)";
	/**
	 * regex {@code Pattern} to match an empty line containing zero ore more white
	 * spaces followed by the new line character(s) or the end of the input.
	 */
	public static final Pattern EMPTY_LINE_PATTERN = Pattern.compile(EMPTY_LINE);
	private static final String END_OF_LINE = ".*(\\r?\\n|$)";
	/**
	 * regex {@code Pattern} to match any content followed by the new line
	 * character(s) or the end of the input.
	 */
	public static final Pattern END_OF_LINE_PATTERN = Pattern.compile(END_OF_LINE);
	/*
	 * Comments
	 */
	private static final String LINE_COMMENT = "\\s*[/]{2}" + END_OF_LINE;
	/**
	 * regex {@code Pattern} to match double slash ({@code //}) preceded by zero or
	 * more white spaces, followed by any content including the next new line.
	 */
	public static final Pattern LINE_COMMENT_PATTERN = Pattern.compile(LINE_COMMENT);
//	public static final String JAVA_DOC_COMMENT = "(?s)\\s*[/][*]{2}.*[*][/]";
//	public static final Pattern JAVA_DOC_COMMENT_PATTERN = Pattern.compile(JAVA_DOC_COMMENT);
	private static final String JAVA_DOC_COMMENT_START = "\\s*[/][*]{2}";
	/**
	 * regex {@code Pattern} to match opening a java doc comment ({@code /**})
	 * preceded by zero or more white spaces.
	 */
	public static final Pattern JAVA_DOC_COMMENT_START_PATTERN = Pattern.compile(JAVA_DOC_COMMENT_START);
//	public static final String MULTI_LINE_COMMENT = "(?s)\\s*[/][*].*[*][/]";
//	public static final Pattern MULTI_LINE_COMMENT_PATTERN = Pattern.compile(MULTI_LINE_COMMENT);
	private static final String MULTI_LINE_COMMENT_START = "\\s*[/][*]";
	/**
	 * regex {@code Pattern} to match opening a multi line comment ({@code /*})
	 * preceded by zero or more white spaces.
	 */
	public static final Pattern MULTI_LINE_COMMENT_START_PATTERN = Pattern.compile(MULTI_LINE_COMMENT_START);
	private static final String MULTI_LINE_COMMENT_END = "[*][/]";
	/**
	 * regex {@code Pattern} to match ending a multi line comment
	 * ({@code *}{@code /}).
	 */
	public static final Pattern MULTI_LINE_COMMENT_END_PATTERN = Pattern.compile(MULTI_LINE_COMMENT_END);
	/*
	 * Package
	 */
	private static final String PACKAGE = "package\\s+" + PKG_NAME + "\\s*[;]" + END_OF_LINE;
	/**
	 * regex {@code Pattern} to match the java package declaration including new
	 * line.
	 */
	public static final Pattern PACKAGE_PATTERN = Pattern.compile(PACKAGE);
	/**
	 * regex group index of 'package name' in {@link #PACKAGE_PATTERN}.
	 */
	public static final int PACKAGE_NAME = 1;
	/*
	 * Import
	 */
	private static final String IMPORT = "import\\s+(static\\s+)?(" + PKG_NAME + "[*]|" + TYPE_NAME + ")\\s*[;]"
			+ END_OF_LINE;
	/**
	 * regex {@code Pattern} to match a java import declaration including new line.
	 */
	public static final Pattern IMPORT_PATTERN = Pattern.compile(IMPORT);
	/**
	 * regex group index of 'static' in {@link #IMPORT_PATTERN}.
	 */
	public static final int IMPORT_STATIC = 1;
	/**
	 * regex group index of 'type' in {@link #IMPORT_PATTERN}.
	 */
	public static final int IMPORT_TYPE = 2;

	/*
	 * Class
	 */
//	private static final String CLASS_VISIBILITY = "((\bpublic\b)\\s+)?";
//	private static final String CLASS_VISIBILITY = "((\\bpublic\\b)\\s+)?";
	private static final String CLASS_MODIFIERS = "((((\\bpublic\\b)|(\\babstract\\b)|(\\bfinal\\b)|(\\bstrictfp\\b))\\s+)*)";
	private static final String CLASS_KEYWORD = "((([@]?interface)|(enum)|(class))\\s+)";
//	private static final String CLASS_SUPERS = "(" + CLASS_EXTENDS + "|" + CLASS_IMPLEMENTS + "){0,2}\\s*";

	private static final String CLASS_SIGNATURE = CLASS_MODIFIERS + CLASS_KEYWORD + JAVA_NAME;
//			+ TYPE_PARAMETERS + OPTIONAL_WHITESPACE + CLASS_EXTENDS + CLASS_IMPLEMENTS 
//			+ ".*[{]" + END_OF_LINE;
	/**
	 * regex {@code Pattern} to match a java class declaration until and including
	 * class name.
	 */
	public static final Pattern CLASS_SIGNATURE_PATTERN = Pattern.compile(CLASS_SIGNATURE);
	/**
	 * regex group index of 'visibility' in {@link #CLASS_SIGNATURE_PATTERN}.
	 */
//	public static final int CLASS_SIGNATURE_PUBLIC = 2;
	/**
	 * regex group index of 'modifiers' in {@link #CLASS_SIGNATURE_PATTERN}.
	 */
	public static final int CLASS_SIGNATURE_MODIFIERS = 1;
	/**
	 * regex group index of 'keyword' in {@link #CLASS_SIGNATURE_PATTERN}.
	 */
	public static final int CLASS_SIGNATURE_KEYWORD = 9;
	/**
	 * regex group index of 'identifier' in {@link #CLASS_SIGNATURE_PATTERN}.
	 */
	public static final int CLASS_SIGNATURE_IDENTIFIER = 13;
//	public static final int CLASS_SIGNATURE_TYPE_PARAMETERS = 15;
//	public static final int CLASS_SIGNATURE_SUPERTYPE = 26;
//	public static final int CLASS_SIGNATURE_SUPERTYPE_TYPE_PARAMETERS = 32;
//	public static final int CLASS_SIGNATURE_INTERFACES = 42;
	private static final String CLASS_EXTENDS = "\\s*(extends\\s+(" + TYPE_NAME + ")\\s*)";
	/**
	 * regex {@code Pattern} to match the 'extends' keyword preceded by zero or more
	 * white spaces until and including extended type name but without any type
	 * parameters.
	 */
	public static final Pattern CLASS_EXTENDS_PATTERN = Pattern.compile(CLASS_EXTENDS);
	/**
	 * regex group index of 'type name' in {@link #CLASS_EXTENDS_PATTERN}.
	 */
	public static final int CLASS_EXTENDS_SUPER_TYPE = 2;
	private static final String CLASS_EXTENDS_KEYWORD = "\\s*(extends)\\s+";
	/**
	 * regex {@code Pattern} to match the 'extends' keyword preceded by zero or more
	 * white spaces, followed by one or more white spaces.
	 */
	public static final Pattern CLASS_EXTENDS_KEYWORD_PATTERN = Pattern.compile(CLASS_EXTENDS_KEYWORD);
	private static final String CLASS_IMPLEMENTS = "\\s*(implements)\\s+";
	/**
	 * regex {@code Pattern} to match the 'implements' keyword preceded by zero or
	 * more white spaces, followed by one or more white spaces.
	 */
	public static final Pattern CLASS_IMPLEMENTS_PATTERN = Pattern.compile(CLASS_IMPLEMENTS);
	/*
	 * Initializer Block
	 */
	private static final String INITIALIZER_BLOCK_SIGNATURE = "\\s*((\\bstatic\\b\\s*)?[{])";
	/**
	 * regex {@code Pattern} to match an initializer block opening; ending with
	 * curly open bracket ('{').
	 */
	public static final Pattern INITIALIZER_BLOCK_SIGNATURE_PATTERN = Pattern.compile(INITIALIZER_BLOCK_SIGNATURE);
	/**
	 * regex group index of 'static' in
	 * {@link #INITIALIZER_BLOCK_SIGNATURE_PATTERN}.
	 */
	public static final int INITIALIZER_BLOCK_STATIC = 2;

	/*
	 * Member Class
	 */
	private static final String MEMBER_CLASS_MODIFIERS = "((((\\bprivate\\b)|(\\bpublic\\b)|(\\bprotected\\b)|(\\babstract\\b)|(\\bstatic\\b)|(\\bfinal\\b)|(\\bstrictfp\\b))\\s+)*)";
	private static final String MEMBER_CLASS_SIGNATURE = OPTIONAL_WHITESPACE + MEMBER_CLASS_MODIFIERS + CLASS_KEYWORD
			+ JAVA_NAME;// + OPTIONAL_WHITESPACE + TYPE_PARAMETERS + CLASS_EXTENDS + CLASS_IMPLEMENTS
//			+ ".*[{]" + END_OF_LINE;
	/**
	 * regex {@code Pattern} to match a member class declaration including and
	 * ending with the class name.
	 */
	public static final Pattern MEMBER_CLASS_SIGNATURE_PATTERN = Pattern.compile(MEMBER_CLASS_SIGNATURE);
	/**
	 * regex group index of 'visibility' in {@link #MEMBER_CLASS_SIGNATURE_PATTERN}.
	 */
//	public static final int MEMBER_CLASS_SIGNATURE_VISIBILITY = 3;
	/**
	 * regex group index of 'modifiers' in {@link #MEMBER_CLASS_SIGNATURE_PATTERN}.
	 */
	public static final int MEMBER_CLASS_SIGNATURE_MODIFIERS = 2;
	/**
	 * regex group index of 'keyword' in {@link #MEMBER_CLASS_SIGNATURE_PATTERN}.
	 */
	public static final int MEMBER_CLASS_SIGNATURE_KEYWORD = 13;
	/**
	 * regex group index of 'identifier' in {@link #MEMBER_CLASS_SIGNATURE_PATTERN}.
	 */
	public static final int MEMBER_CLASS_SIGNATURE_IDENTIFIER = 17;
//	public static final int MEMBER_CLASS_SIGNATURE_TYPE_PARAMETERS = 21;
//	public static final int MEMBER_CLASS_SIGNATURE_SUPERTYPE = 32;
//	public static final int MEMBER_CLASS_SIGNATURE_SUPERTYPE_TYPE_PARAMETERS = 37;
//	public static final int MEMBER_CLASS_SIGNATURE_INTERFACES = 47;
	/*
	 * Field
	 */
	private static final String FIELD_MODIFIERS = "((((\\bprivate\\b)|(\\bpublic\\b)|(\\bprotected\\b)|(\\bstatic\\b)|(\\bfinal\\b)|(\\btransient\\b)|(\\bvolatile\\b))\\s+)*)";
	private static final String INITIALIZER_OR_END_STATEMENT = "\\s*(([=])|([;]))";
	private static final String FIELD_SIGNATURE = OPTIONAL_WHITESPACE + FIELD_MODIFIERS + TYPE_NAME + TYPE_PARAMETERS
			+ ARRAY + OPTIONAL_WHITESPACE + JAVA_NAME + INITIALIZER_OR_END_STATEMENT;
	/**
	 * regex {@code Pattern} to match a field signature with simple type parameters
	 * (not containing sub-type-parameters) ending with either an equals sign (=) or
	 * a semicolon (;).
	 */
	public static final Pattern FIELD_SIGNATURE_PATTERN = Pattern.compile(FIELD_SIGNATURE);
	/**
	 * regex group index of 'visibility' in {@link #FIELD_SIGNATURE_PATTERN}.
	 */
//	public static final int FIELD_SIGNATURE_VISIBILITY = 3;
	/**
	 * regex group index of 'modifiers' in {@link #FIELD_SIGNATURE_PATTERN}.
	 */
	public static final int FIELD_SIGNATURE_MODIFIERS = 2;
	/**
	 * regex group index of 'type' in {@link #FIELD_SIGNATURE_PATTERN}.
	 */
	public static final int FIELD_SIGNATURE_TYPE = 12;
	/**
	 * regex group index of 'type package' in {@link #FIELD_SIGNATURE_PATTERN}.
	 */
	public static final int FIELD_SIGNATURE_TYPE_PACKAGE = 13;
	/**
	 * regex group index of 'type name' in {@link #FIELD_SIGNATURE_PATTERN}.
	 */
	public static final int FIELD_SIGNATURE_TYPE_NAME = 15;
	/**
	 * regex group index of 'type array' in {@link #FIELD_SIGNATURE_PATTERN}.
	 */
	public static final int FIELD_SIGNATURE_TYPE_ARRAY = 16;
	/**
	 * regex group index of 'type parameters' in {@link #FIELD_SIGNATURE_PATTERN}.
	 */
	public static final int FIELD_SIGNATURE_TYPE_PARAMETERS = 19;
	/**
	 * regex group index of 'type parameters array' in
	 * {@link #FIELD_SIGNATURE_PATTERN}.
	 */
	public static final int FIELD_SIGNATURE_TYPE_PARAMETERS_ARRAY = 30;
	/**
	 * regex group index of 'identifier' in {@link #FIELD_SIGNATURE_PATTERN}.
	 */
	public static final int FIELD_SIGNATURE_IDENTIFIER = 33;
	/**
	 * regex group index of 'identifier array' in {@link #FIELD_SIGNATURE_PATTERN}.
	 */
	public static final int FIELD_SIGNATURE_IDENTIFIER_ARRAY = 34;
	/**
	 * regex group index of 'terminator' (= or ;) in
	 * {@link #FIELD_SIGNATURE_PATTERN}.
	 */
	public static final int FIELD_SIGNATURE_TERMINATOR = 36;
	/*
	 * Constructor
	 */
	private static final String CONSTRUCTOR_MODIFIERS = "(((\\bprivate\\b)|(\\bpublic\\b)|(\\bprotected\\b))\\s+)?";
	private static final String CONSTRUCTOR_SIGNATURE = OPTIONAL_WHITESPACE + CONSTRUCTOR_MODIFIERS + JAVA_NAME
			+ "\\s*[(]";
	/**
	 * regex {@code Pattern} to match a constructor signature until and including
	 * the open bracket of the parameter list.
	 */
	public static final Pattern CONSTRUCTOR_SIGNATURE_PATTERN = Pattern.compile(CONSTRUCTOR_SIGNATURE);
	/**
	 * regex group index of 'visibility' in {@link #CONSTRUCTOR_SIGNATURE_PATTERN}.
	 */
	public static final int CONSTRUCTOR_VISIBILITY = 2;
	/**
	 * regex group index of 'identifier' in {@link #CONSTRUCTOR_SIGNATURE_PATTERN}.
	 */
	public static final int CONSTRUCTOR_IDENTIFIER = 7;
	/*
	 * Method
	 */
	private static final String METHOD_MODIFIERS = "((((\\bprivate\\b)|(\\bpublic\\b)|(\\bprotected\\b)|(\\babstract\\b)|(\\bstatic\\b)|(\\bfinal\\b)|(\\bsynchronized\\b)|(\\bnative\\b)|(\\bstrictfp\\b)|(\\bdefault\\b))\\s+)*)";
//	private static final String PARAMETERS = "\\(((" + ANNOTATION + TYPE_NAME + TYPE_PARAMETERS + OPTIONAL_WHITESPACE
//			+ JAVA_NAME + OPTIONAL_LIST_SEPARATOR + ")*)\\)";
	private static final String THROWS = "(\\s*throws\\s+)";
	/**
	 * regex {@code Pattern} to match the throws keyword, preceded by zero or more
	 * white spaces, followed by one or more white spaces.
	 */
	public static final Pattern THROWS_PATTERN = Pattern.compile(THROWS);
//	private static final String OPEN_BLOCK_OR_END_LINE = "\\s*(([{])|([;]))";
//	private static final String METHOD_SIGNATURE = OPTIONAL_WHITESPACE + VISIBILITY + METHOD_MODIFIERS + TYPE_PARAMETERS
//			+ TYPE_NAME + TYPE_PARAMETERS + OPTIONAL_WHITESPACE + JAVA_NAME + OPTIONAL_WHITESPACE + PARAMETERS + THROWS
//			+ OPEN_BLOCK_OR_END_LINE;
	private static final String METHOD_SIGNATURE_SHORT = OPTIONAL_WHITESPACE + METHOD_MODIFIERS + TYPE_PARAMETERS
			+ TYPE_NAME + TYPE_PARAMETERS + ARRAY + OPTIONAL_WHITESPACE + JAVA_NAME + OPTIONAL_WHITESPACE + "[(]";
//	public static final Pattern METHOD_SIGNATURE_PATTERN = Pattern.compile(METHOD_SIGNATURE);
	/**
	 * regex {@code Pattern} to match a method with simple type parameters (without
	 * sub-type-parameters) including and ending with the open bracket of the
	 * parameter list.
	 */
	public static final Pattern METHOD_SIGNATURE_SHORT_PATTERN = Pattern.compile(METHOD_SIGNATURE_SHORT);
	/**
	 * regex group index of 'visibility' in {@link #METHOD_SIGNATURE_SHORT_PATTERN}.
	 */
//	public static final int METHOD_SIGNATURE_VISIBILITY = 3;
	/**
	 * regex group index of 'modifiers' in {@link #METHOD_SIGNATURE_SHORT_PATTERN}.
	 */
	public static final int METHOD_SIGNATURE_MODIFIERS = 2;
//	public static final int METHOD_SIGNATURE_MODIFIERS = 2;
	/**
	 * regex group index of 'type parameters' (with brackets) in
	 * {@link #METHOD_SIGNATURE_SHORT_PATTERN}.
	 */
	public static final int METHOD_SIGNATURE_TYPE_PARAMETERS_WITH_BRACKETS = 15;
	/**
	 * regex group index of 'type parameters' in
	 * {@link #METHOD_SIGNATURE_SHORT_PATTERN}.
	 */
	public static final int METHOD_SIGNATURE_TYPE_PARAMETERS = 16;
	/**
	 * regex group index of 'return type' in
	 * {@link #METHOD_SIGNATURE_SHORT_PATTERN}.
	 */
	public static final int METHOD_SIGNATURE_RETURN_TYPE = 27;
	public static final int METHOD_SIGNATURE_RETURN_TYPE_PACKAGE = 28;
	public static final int METHOD_SIGNATURE_RETURN_TYPE_NAME = 30;
	public static final int METHOD_SIGNATURE_RETURN_TYPE_ARRAY = 31;
	/**
	 * regex group index of 'return type parameters' in
	 * {@link #METHOD_SIGNATURE_SHORT_PATTERN}.
	 */
	public static final int METHOD_SIGNATURE_RETURN_TYPE_PARAMETERS_WITH_BRACKETS = 33;
	public static final int METHOD_SIGNATURE_RETURN_TYPE_PARAMETERS = 34;
	public static final int METHOD_SIGNATURE_RETURN_TYPE_PARAMETERS_ARRAY = 45;
	/**
	 * regex group index of 'identifier' in {@link #METHOD_SIGNATURE_SHORT_PATTERN}.
	 */
	public static final int METHOD_SIGNATURE_IDENTIFIER = 48;
//	public static final int METHOD_SIGNATURE_THROWS = 33;
//	public static final int METHOD_SIGNATURE_TERMINATOR = 40;
	/*
	 * Mystery Member ... Method
	 */
//	TODO (\bprivate\b)|(\bpublic\b)|(\bprotected\b)|
//	private static final String VISIBILITY = "(((\\bprivate\\b)|(\\bpublic\\b)|(\\bprotected\\b))\\s+)?";
//	if (System.currentTimeMillis() > 0) {
//		findVerbose(CLASS_SIGNATURE_PATTERN, sourceCode, start);
//		break;
//	}

	private static final String MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS = OPTIONAL_WHITESPACE + METHOD_MODIFIERS
			+ "([<])";
	/**
	 * regex {@code Pattern} to match a method with complex type parameters
	 * (including sub-type-parameters) .
	 */
	public static final Pattern MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_PATTERN = Pattern
			.compile(MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS);
	/**
	 * regex group index of 'visibility' in
	 * {@link #MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_PATTERN}.
	 */
//	public static final int MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_VISIBILITY = 3;
	/**
	 * regex group index of 'modifiers' in
	 * {@link #MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_PATTERN}.
	 */
	public static final int MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_MODIFIERS = 2;
	/**
	 * regex group index of '{@code <}' of method type parameters in
	 * {@link #MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_PATTERN}.
	 */
	public static final int MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_TYPE_PARAMS_OPEN = 15;
	/**
	 * regex group index of 'identifier' in
	 * {@link #MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_PATTERN}.
	 */
//	public static final int MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_IDENTIFIER = 18;
	/**
	 * regex group index of '(' of method parameters in
	 * {@link #MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_PATTERN}.
	 */
	public static final int MYSTERY_SIGNATURE_METHOD_TYPE_PARAMETERS_PARAMS_OPEN = 15;

	private static final String METHOD_SIGNATURE_TERMINATOR = "[^{;]*([{]|[;])";
	/**
	 * regex {@code Pattern} to match anything until and including the end of the
	 * method signature indicated by either '{' or ';'
	 */
	public static final Pattern METHOD_SIGNATURE_TERMINATOR_PATTERN = Pattern.compile(METHOD_SIGNATURE_TERMINATOR);
	/**
	 * regex group index of 'method signature terminator' (either ';' or '{') in
	 * {@link #METHOD_SIGNATURE_TERMINATOR_PATTERN}.
	 */
	public static final int METHOD_SIGNATURE_TERMINATOR_TERMINATOR = 3;

//	private static final String MYSTERY_MODIFIERS = "(" + FIELD_MODIFIERS + "|" + METHOD_MODIFIERS + ")";

	private static final String MYSTERY_MODIFIERS = "((((\\bprivate\\b)|(\\bpublic\\b)|(\\bprotected\\b)|(\\babstract\\b)|(\\bstatic\\b)|(\\bfinal\\b)|(\\bsynchronized\\b)|(\\bnative\\b)|(\\bstrictfp\\b)|(\\bdefault\\b)|(\\btransient\\b)|(\\bvolatile\\b))\\s+)*)";
//	private static final String FIELD_MODIFIERS = "((((\\bprivate\\b)|(\\bpublic\\b)|(\\bprotected\\b)|(\\bstatic\\b)|(\\bfinal\\b)|(\\btransient\\b)|(\\bvolatile\\b))\\s+)*)";
	/*
	 * Mystery Member ... Field or Method
	 */
	private static final String MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_PARAMETERS = OPTIONAL_WHITESPACE
			+ MYSTERY_MODIFIERS + TYPE_NAME + "([<])";
	/**
	 * regex {@code Pattern} to match either a field with complex type parameters
	 * (including sub-type-parameters) or a method that returns a type with complex
	 * type parameters ending with and including either '(', '=' or ';'.
	 */
	public static final Pattern MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_PARAMETERS_PATTERN = Pattern
			.compile(MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_PARAMETERS);
	/**
	 * regex group index of 'visibility' in
	 * {@link #MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_PARAMETERS_PATTERN}.
	 */
//	public static final int MYSTERY_SIGNATURE_METHOD_OR_FIELD_VISIBILITY = 3;
	/**
	 * regex group index of 'modifiers' in
	 * {@link #MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_PARAMETERS_PATTERN}.
	 */
	public static final int MYSTERY_SIGNATURE_METHOD_OR_FIELD_MODIFIERS = 2;
	/**
	 * regex group index of 'method or field type' in
	 * {@link #MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_PARAMETERS_PATTERN}.
	 */
	public static final int MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE = 17;
	public static final int MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_PACKAGE = 18;
	public static final int MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_NAME = 20;
	/**
	 * regex group index of 'open type parameters' in
	 * {@link #MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_PARAMETERS_PATTERN}.
	 */
	public static final int MYSTERY_SIGNATURE_METHOD_OR_FIELD_OPEN_TYPE_PARAMETERS = 23;
	/**
	 * regex group index of 'identifier' in
	 * {@link #MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_PARAMETERS_PATTERN}.
	 */
//	public static final int MYSTERY_SIGNATURE_METHOD_OR_FIELD_IDENTIFIER = 32;
	/**
	 * regex group index of 'terminator' (either ';', '=' or '(') in
	 * {@link #MYSTERY_SIGNATURE_METHOD_OR_FIELD_TYPE_PARAMETERS_PATTERN}.
	 */
//	public static final int MYSTERY_SIGNATURE_METHOD_OR_FIELD_TERMINATOR = 35;

	private static final String MYSTERY_IDENTIFIER = JAVA_NAME + "\\s*([(]|[;]|[=])";
	/**
	 * regex {@code Pattern} to match either a field or a method name ending with
	 * and including either '(', '=' or ';'.
	 */
	public static final Pattern MYSTERY_IDENTIFIER_PATTERN = Pattern.compile(MYSTERY_IDENTIFIER);
	public static final int MYSTERY_IDENTIFIER_NAME = 1;
	public static final int MYSTERY_IDENTIFIER_ARRAY = 2;
	public static final int MYSTERY_IDENTIFIER_TERMINATOR = 4;

	private static final String END_OF_CLASS = "\\s*}";
	/**
	 * regex {@code Pattern} to match the end of the class block preceded by zero or
	 * more white spaces.
	 */
	public static final Pattern END_OF_CLASS_PATTERN = Pattern.compile(END_OF_CLASS);

	/* @formatter:off
	 * #############################################################################
	 * #############################################################################
	 * ##########                                                         ##########
	 * ##########                GRADLE SOURCE PATTERNS                   ##########
	 * ##########                                                         ##########
	 * #############################################################################
	 * #############################################################################
	 * @formatter:on
	 */
	
	/*
	 * settings.gradle
	 */
	
	private static final String ROOT_PROJECT_NAME = "rootProject.name\\s*=\\s*";
	public static final Pattern ROOT_PROJECT_NAME_PATTERN = Pattern.compile(ROOT_PROJECT_NAME);
	
	private static final String CHILD_PROJECT = "\\s*include\\s*[(]";
	public static final Pattern CHILD_PROJECT_PATTERN = Pattern.compile(CHILD_PROJECT);

	/**
	 * Returns the next match if any with diagnostic console output.
	 * 
	 * @param pattern the {@code Pattern} to search
	 * @param text    the text to search in
	 * @param start   the starting index for the search
	 * @return the match of the specified search if there is any
	 */
	public static Optional<Matcher> findVerbose(Pattern pattern, CharSequence text, int start) {
		Matcher m = pattern.matcher(text);
		if (m.find(start)) {
			System.out.printf("found '%s' starting at %d, ending at %d%n...", m.group(), m.start(), m.end());
			for (int i = 1; i <= m.groupCount(); i++) {
				System.out.printf("Group %d: '%s'", i, m.group(i));
				if (i + 1 <= m.groupCount()) {
					System.out.print(" / ");
				}
				if (i > 0 && i % 5 == 0) {
					System.out.println();
					System.out.print("...");
				}
			}
			System.out.println();
			return Optional.of(m);
		} else {
			System.err.printf("No match found for pattern '%s'%nin text '%s'%nstarting from index %d%n",
					pattern.pattern(), text, start);
		}
		return Optional.empty();
	}

	/**
	 * Returns the next match if any.
	 * 
	 * @param pattern the {@code Pattern} to search
	 * @param text    the text to search in
	 * @param start   the starting index for the search
	 * @return the match of the specified search if there is any
	 */
	public static Optional<Matcher> find(Pattern pattern, CharSequence text, int start) {
//		Check check = Benchmark.start(SourceAnalyzer.class, "find", "SA.find");
//		try {
		Matcher m = pattern.matcher(text);
		if (m.find(start)) {
			return Optional.of(m);
		}
		return Optional.empty();
//		} finally {
//			check.stop();
//		}
	}

	/**
	 * Tests if the specified {@code Pattern} occurs at the specified index in the
	 * given text.
	 * 
	 * @param pattern The {@code Pattern} to test
	 * @param text    the text to search
	 * @param index   the index to test
	 * @return the match if it occurs at the specified index
	 */
	public static Optional<Matcher> testIndex(Pattern pattern, CharSequence text, int index) {
//		Check check = Benchmark.start(SourceAnalyzer.class, "testIndex", "SA.testIndex");
//		try {
		Matcher m = pattern.matcher(text);
		if (m.find(index)) {
			if (m.start() == index) {
				return Optional.of(m);
			}
		}
		return Optional.empty();
//		} finally {
//			check.stop();
//			if(check.time() > 1_000_000_000L) {
//				System.err.println("Lag in SourceAnalyzer::testIndex! " + pattern.toString() + " @ " + index); 
//			}
//		}
	}

	/**
	 * searches for the end of a multi line comment.
	 * 
	 * @param sourceCode the text to search in
	 * @param start      the starting index
	 * @return the index after the end of the multi line comment / '-1' is returned
	 *         if there was no occurence of closing a multi line comment.
	 */
	public static int nextEndOfComment(CharSequence sourceCode, int start) {
		Optional<Matcher> endOfCommentOpt = find(MULTI_LINE_COMMENT_END_PATTERN, sourceCode, start);
		if (endOfCommentOpt.isPresent()) {
			Matcher endOfComment = endOfCommentOpt.get();
			return endOfComment.end();
		}
		return -1;
	}

	/**
	 * searches for the next list separator (,).
	 * 
	 * @param sourceCode the list to search in
	 * @param start      the starting index to search from
	 * @return the end of the next list separator (equal to the starting index of
	 *         the next item) or '-1' if there was no match.
	 */
	public static int nextListSeparator(CharSequence sourceCode, int start) {
		Optional<Matcher> nextSeparator = find(LIST_SEPARATOR_PATTERN, sourceCode, start);
		if (nextSeparator.isPresent()) {
			Matcher endOfSeparator = nextSeparator.get();
			return endOfSeparator.end();
		}
		return -1;
	}

	/**
	 * searches for the next new line.
	 * 
	 * @param sourceCode the text to search in
	 * @param start      the starting index to search from
	 * @return the index after the next new line or the text length if the end of
	 *         the input has been reached.
	 */
	public static int nextNewLine(CharSequence sourceCode, int start) {
		Optional<Matcher> optEol = find(END_OF_LINE_PATTERN, sourceCode, start);
		if (optEol.isPresent()) {
			return optEol.get().end();
		}
		return sourceCode.length();
	}

	public static int nextWhitespace(CharSequence sourceCode, int start) {
		Optional<Matcher> test = testIndex(NEEDED_WHITESPACE_PATTERN, sourceCode, start);
		if (test.isPresent()) {
			start = test.get().end();
		}
		return start;
	}

	/**
	 * searches for the next method signature terminator (either ';' or '{')
	 * 
	 * @param sourceCode the text to search in
	 * @param start      the starting index for the search
	 * @return the index after the method signature terminator
	 */
	public static int nextMethodSignatureTerminator(CharSequence sourceCode, int start) {
		Optional<Matcher> nextTerminator = find(METHOD_SIGNATURE_TERMINATOR_PATTERN, sourceCode, start);
		if (nextTerminator.isPresent()) {
			start = nextTerminator.get().end();
		}
		return start;
	}

	/**
	 * searches for a character that occurs outside of any brackets.
	 * 
	 * @param sourceCode   the text to search in
	 * @param start        the starting index for the search
	 * @param openBracket  the opening bracket to consider
	 * @param closeBracket the closing bracket to consider
	 * @param charToSeek   the character to search
	 * @return the index of the next occurrence of the specified character to seek
	 *         or '-1' if the character could not be found
	 */
	public static int seekBalanced(CharSequence sourceCode, int start, char openBracket, char closeBracket,
			char charToSeek) {
		return seekBalanced(sourceCode, start, openBracket, closeBracket, charToSeek, (char) -1);
	}

	/**
	 * searches for a character that occurs outside of any brackets limited by a
	 * terminating character.
	 * 
	 * @param sourceCode   the text to search in
	 * @param start        the starting index for the search
	 * @param openBracket  the opening bracket to consider
	 * @param closeBracket the closing bracket to consider
	 * @param charToSeek   the character to search
	 * @param terminator   the character to terminate the search at
	 * @return the index of the next occurrence of the specified character to seek
	 *         or '-1' if the character could not be found or the terminator
	 *         character has been reached
	 */
	public static int seekBalanced(CharSequence sourceCode, int start, char openBracket, char closeBracket,
			char charToSeek, char terminator) {
		return seekBalanced(sourceCode, start, openBracket, closeBracket, (char) -1, (char) -1, charToSeek, terminator);
	}

	/**
	 * searches for a character that occurs outside of any of two types of brackets
	 * limited by a terminating character.
	 * 
	 * @param sourceCode    the text to search in
	 * @param start         the starting index for the search
	 * @param openBracket   the opening bracket to consider
	 * @param closeBracket  the closing bracket to consider
	 * @param openBracket1  the additional opening bracket to consider
	 * @param closeBracket1 the additional closing bracket to consider
	 * @param charToSeek    the character to search
	 * @param terminator    the character to terminate the search at
	 * @return the index of the next occurrence of the specified character to seek
	 *         or '-1' if the character could not be found or the terminator
	 *         character has been reached
	 */
	public static int seekBalanced(CharSequence sourceCode, int start, char openBracket, char closeBracket,
			char openBracket1, char closeBracket1, char charToSeek, char terminator) {
//		Check check = Benchmark.start(SourceAnalyzer.class, "seekBalanced", "SA.seekBalanced");
		int length = sourceCode.length();
		boolean inChar = false;
		boolean inString = false;
		char previousChar = 0;
		int escapeChars = 0;
		int bracketLevel = 0;
//		try {
		for (int i = start; i < length; i++) {
			char currentChar = sourceCode.charAt(i);
			try {
				if (!inChar && !inString) {
					if (currentChar == '"') {
						inString = true;
						continue;
					} else if (currentChar == '\'') {
						inChar = true;
						continue;
					} else if (currentChar == '/' && previousChar == currentChar) {
						i = nextNewLine(sourceCode, i + 1) - 1;
						continue;
					} else if (previousChar == '/' && currentChar == '*') {
						if (i + 1 < length) {
							i = nextEndOfComment(sourceCode, i + 1) - 1;
						}
						continue;
					}
				} else if (inChar) {
					if (currentChar == '\'' && previousChar != '\\') {
						inChar = false;
					}
					continue;
				} else if (inString) {
					if (currentChar == '"' && escapeChars % 2 == 0) {
						inString = false;
					}
					continue;
				}
			} finally {
				previousChar = currentChar;
				if (inString && currentChar == '\\') {
					escapeChars++;
				} else if (escapeChars > 0) {
					escapeChars = 0;
				}
			}
			if (currentChar == charToSeek && bracketLevel == 0) {
				return i;
			} else if (currentChar == terminator && bracketLevel == 0) {
				return -1;
			} else if (currentChar == openBracket || currentChar == openBracket1) {
				bracketLevel++;
			} else if (currentChar == closeBracket || currentChar == closeBracket1) {
				bracketLevel--;
			}
		}
		return -1;
//		} finally {
//			check.stop();
//		}
	}

	public static int seekClosingBracket(CharSequence sourceCode, int indexOfOpenBracket) {
		char openBracket = sourceCode.charAt(indexOfOpenBracket);
		char closeBracket;
		switch (openBracket) {
		case '(':
			closeBracket = ')';
			break;
		case '[':
			closeBracket = ']';
			break;
		case '{':
			closeBracket = '}';
			break;
		default:
			throw new IllegalArgumentException(
					"The supplied index must point to an opening bracket (either one of '(', '[' or '{'");
		}
		return seekBalanced(sourceCode, indexOfOpenBracket + 1, openBracket, closeBracket, closeBracket);
	}

	/**
	 * Searches for the next closing bracket ')' that was not opened.
	 * 
	 * @param sourceCode the text to search in
	 * @param start      the starting index for the search
	 * 
	 * @return the index after the closing bracket or '-1' if no occurrence was
	 *         found
	 */
	public static int seekEndOfClosingBracket(CharSequence sourceCode, int start) {
		int i = seekBalanced(sourceCode, start, '(', ')', ')');
		return i == -1 ? -1 : i + 1;
	}

	/**
	 * Searches for the next occurrence of ',' considering '(', ')', '{@code <}' and
	 * '{@code >}' terminating the search with the next ')' that was not opened.
	 * 
	 * @param sourceCode the text to search in
	 * @param start      the starting index to search from
	 * @return the index of the next list separator or '-1' if the end of the list
	 *         has been reached
	 */
	public static int seekIndexOfNextParameterListSeparator(CharSequence sourceCode, int start) {
		return seekBalanced(sourceCode, start, '(', ')', '<', '>', ',', ')');
	}

	/**
	 * Searches for the end of a statement (;) considering '{' and '}'.
	 * 
	 * @param sourceCode the text to search in
	 * @param start      the starting index to search from
	 * @return the index after the end of a statement or '-1' if no occurrence was
	 *         found
	 */
	public static int seekEndOfStatement(CharSequence sourceCode, int start) {
		int i = seekBalanced(sourceCode, start, '{', '}', ';');
		return i == -1 ? -1 : i + 1;
	}

	/**
	 * Searches for the next occurrence of '(' not considering any brackets.
	 * 
	 * @param sourceCode the text to search in
	 * @param start      the starting index to search from
	 * @return the index after the opening of a bracket or '-1' if no occurrence was
	 *         found
	 */
	public static int seekEndOfOpenBracket(CharSequence sourceCode, int start) {
		int i = seekBalanced(sourceCode, start, (char) -1, (char) -1, '(');
		return i == -1 ? -1 : i + 1;
	}

	/**
	 * Searches for the next occurrence of '{' not considering any brackets.
	 * 
	 * @param sourceCode the text to search in
	 * @param start      the starting index to search from
	 * @return the index after the opening of a block or '-1' if no occurrence was
	 *         found
	 */
	public static int seekEndOfOpenBlock(CharSequence sourceCode, int start) {
		int i = seekBalanced(sourceCode, start, (char) -1, (char) -1, '{');
		return i == -1 ? -1 : i + 1;
	}

	/**
	 * Searches for the next '}' that was not opened.
	 * 
	 * @param sourceCode the text to search in
	 * @param start      the starting index to search from
	 * @return the index after the closing of a block or '-1' if no occurrence was
	 *         found
	 */
	public static int seekEndOfBlock(CharSequence sourceCode, int start) {
		int i = seekBalanced(sourceCode, start, '{', '}', '}');
		return i == -1 ? -1 : i + 1;
	}

	/**
	 * Searches for the next '{@code >}' that was not opened.
	 * 
	 * @param sourceCode the text to search in
	 * @param start      the starting index to search from
	 * @return the index after the closing the type parameters or '-1' if no
	 *         occurrence was found
	 */
	public static int seekEndOfTypeParameters(CharSequence sourceCode, int start) {
		int i = seekBalanced(sourceCode, start, '<', '>', '>');
		return i == -1 ? -1 : i + 1;
	}

	/**
	 * searches for the next ',' considering '{@code <}', '{@code >}' and
	 * '{@code (}', '{@code )}'.
	 * 
	 * @param sourceCode the text to search in
	 * @param start      the starting index to search from
	 * @return the index of the list separator or '-1' if no occurrence was found or
	 *         the closing bracket of the type parameters was reached
	 */
	public static int seekIndexOfNextTypeParameterListSeparator(CharSequence sourceCode, int start) {
		return seekBalanced(sourceCode, start, '<', '>', '(', ')', ',', '>');
	}

	/**
	 * Searches for the next ',' considering '{@code <}' and '{@code >}'
	 * 
	 * @param sourceCode the text to search in
	 * @param start      the starting index to search from
	 * @return the index the list separator or '-1' if no occurrence was found or
	 *         the opening of a block was reached
	 * 
	 */
	public static int seekIndexOfNextSignatureListSeparator(CharSequence sourceCode, int start) {
		return seekBalanced(sourceCode, start, '<', '>', ',', '{');
	}

	/**
	 * Reads a signature list (class interfaces or method exceptions) to the
	 * specified {@code List<String>}.
	 * 
	 * @param sourceCode the text to search in
	 * @param start      the starting index of the list
	 * @param list       the list to add the results to
	 * @return the index after the opening of the block or end of statement
	 */
	public static int readSignatureList(CharSequence sourceCode, int start, List<String> list) {
		int end = nextMethodSignatureTerminator(sourceCode, start);
		int endOfList = end - 1;
		while (start < end) {
			int nextSeparator = seekIndexOfNextSignatureListSeparator(sourceCode, start);
			if (nextSeparator != -1) {
				list.add(sourceCode.subSequence(start, nextSeparator).toString());
				start = nextListSeparator(sourceCode, nextSeparator);
			} else {
				list.add(sourceCode.subSequence(start, endOfList).toString().trim());
				start = end;
			}
		}
		return end;
	}

	/**
	 * Reads a list of type parameters (if there are any) to the specified
	 * {@code List<String>}.
	 * 
	 * @param sourceCode     the text to search in
	 * @param start          the index where type parameters could start
	 * @param typeParameters the list to add the results to
	 * @return the index after the type parameters or the specified start if there
	 *         were no type parameters
	 */
	public static int readTypeParameters(CharSequence sourceCode, int start, List<String> typeParameters) {
		Optional<Matcher> tpMatcher = testIndex(TYPE_PARAMETERS_START_PATTERN, sourceCode, start);
		if (tpMatcher.isPresent()) {
			start = tpMatcher.get().end();
			int end = seekEndOfTypeParameters(sourceCode, start);
			int endOfList = end - 1;
			while (start < end) {
				int indexOfNextSeparator = seekIndexOfNextTypeParameterListSeparator(sourceCode, start);
				if (indexOfNextSeparator != -1) {
					typeParameters.add(sourceCode.subSequence(start, indexOfNextSeparator).toString());
					start = nextListSeparator(sourceCode, indexOfNextSeparator);
				} else {
					typeParameters.add(sourceCode.subSequence(start, endOfList).toString());
					start = end;
				}
			}
		}
		return start;
	}

	/**
	 * Reads the exceptions of a method declaration (if there are any).
	 * 
	 * @param sourceCode the text to search in
	 * @param start      the index after the method parameters close
	 * @param exceptions the list to add results to
	 * @return the index after the opening of the method body or statement
	 *         terminator
	 */
	public static int readExceptions(CharSequence sourceCode, int start, List<String> exceptions) {
		Optional<Matcher> optThrows = testIndex(THROWS_PATTERN, sourceCode, start);
		if (optThrows.isPresent()) {
			start = readSignatureList(sourceCode, optThrows.get().end(), exceptions);
		} else {
			start = nextMethodSignatureTerminator(sourceCode, start);
		}
		return start;
	}

	/**
	 * Reads the parameter list of a constructor or method to the specified
	 * {@code List<String>}.
	 * 
	 * @param sourceCode the text to search in
	 * @param start      the starting index of the list
	 * @param parameters the list to add results to
	 * @return the index after the parameters close
	 */
	public static int readParameters(CharSequence sourceCode, int start, List<String> parameters) {
		int end = seekEndOfClosingBracket(sourceCode, start);
		int endOfParameters = end - 1;
		if (endOfParameters - start > 0) {
			while (start < endOfParameters) {
				int nextSeparator = seekIndexOfNextParameterListSeparator(sourceCode, start);
				if (nextSeparator != -1) {
					parameters.add(sourceCode.subSequence(start, nextSeparator).toString());
					start = find(LIST_SEPARATOR_PATTERN, sourceCode, nextSeparator).get().end();
				} else {
					parameters.add(sourceCode.subSequence(start, endOfParameters).toString());
					start = endOfParameters;
				}
			}
		}
		return end;
	}

	/**
	 * Splits a list of words separated by one or more whitespaces.
	 * 
	 * @param modifiersList the list of words separated by whitespaces
	 * @return the words without whitespaces
	 */
	public static List<String> splitModifiers(String modifiersList) {
		List<String> modifiers = new ArrayList<>();
		if (Objects.nonNull(modifiersList)) {
			String[] modifiersArray = modifiersList.trim().split("\\s+");
			for (String modifier : modifiersArray) {
				if (!modifier.isEmpty()) {
					modifiers.add(modifier);
				}
			}
		}
		return modifiers;
	}

	public static String getVisibilityFromModifiers(List<String> modifiersList) {
		String[] visibilityModifiers = { "private", "protected", "public" };
		for (String visibility : visibilityModifiers) {
			if (modifiersList.contains(visibility)) {
				return visibility;
			}
		}
		return "";
	}

	private SourceAnalyzer() {}
}