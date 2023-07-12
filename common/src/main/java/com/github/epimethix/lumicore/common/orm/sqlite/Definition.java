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
package com.github.epimethix.lumicore.common.orm.sqlite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.epimethix.lumicore.orm.annotation.field.Column;

/**
 * A Definition represents a field/column definition for a database table.
 * <p>
 * The class Definition is instantiated through the define() methods.
 * <p>
 * Also Definition contains string parse methods
 */
public final class Definition {
	public static final int TYPE_NOT_SUPPORTED = -1;
	/**
	 * Datatype: INTEGER
	 */
	public final static int TYPE_INTEGER = 1;
	/**
	 * Datatype: INTEGER PRIMARY KEY
	 */
	public final static int TYPE_INTEGER_PK = 2;
	/**
	 * Datatype: INTEGER PRIMARY KEY AUTOINCREMENT
	 */
	public final static int TYPE_INTEGER_PK_AI = 3;
	/**
	 * Datatype: REAL
	 */
	public final static int TYPE_REAL = 4;
	/**
	 * Datatype: REAL PRIMARY KEY
	 */
	public static final int TYPE_REAL_PK = 5;
	/**
	 * Datatype: TEXT
	 */
	public final static int TYPE_TEXT = 6;
	/**
	 * Datatype: TEXT PRIMARY KEY
	 */
	public final static int TYPE_TEXT_PK = 7;
	/**
	 * Datatype: TEXT PRIMARY KEY
	 */
	public final static int TYPE_TEXT_PK_UUID = 8;
	/**
	 * Datatype: BLOB
	 */
	public final static int TYPE_BLOB = 9;
	/**
	 * Datatype: GENERIC
	 * <p>
	 * Autodetect type based on java field type
	 */
	public final static int TYPE_AUTO = 10;
	/**
	 * NULLABLE
	 */
	public final static int NULLABLE = 1;
	/**
	 * NOT NULLABLE
	 */
	public final static int NOT_NULL = 2;
	/**
	 * Default Value: None
	 */
	public final static int NO_DEFAULT = 1;
	/**
	 * Default Value: As Specified
	 */
	public final static int DEFAULT_AS_SPECIFIED = 2;
	/**
	 * Default Value: NULL
	 */
	public final static int DEFAULT_NULL = 3;

	/**
	 * Define a field that is nullable and has no default value.
	 * 
	 * @param fieldName The field name.
	 * @param type      The field type from Definition.TYPE_...
	 * 
	 * @return The field Definition for a create statement.
	 * 
	 */
	public final static Definition define(String fieldName, int type) {
		return define(fieldName, type, Column.NULL_HANDLING_DEFAULT);
	}

	/**
	 * Define a field with specified null handling and no default value.
	 * 
	 * @param fieldName    The field name.
	 * @param type         The field type from Definition.TYPE_...
	 * @param nullHandling Either Definition.NULLABLE or Definition.NOT_NULL.
	 * 
	 * @return The field Definition for a create statement.
	 * 
	 */
	public final static Definition define(String fieldName, int type, int nullHandling) {
		return define(fieldName, type, nullHandling, Column.DEFAULT_HANDLING_DEFAULT);
	}

	/**
	 * Define a field with specified null handling and specified default handling.
	 * 
	 * @param fieldName       The field name.
	 * @param type            The field type from Definition.TYPE_...
	 * @param nullHandling    Either Definition.NULLABLE or Definition.NOT_NULL.
	 * @param defaultHandling Either Definition.NO_DEFAULT or
	 *                        Definition.DEFAULT_NULL.
	 * 
	 * @return The field Definition for a create statement.
	 * 
	 */
	public final static Definition define(String fieldName, int type, int nullHandling, int defaultHandling) {
		return define(fieldName, type, nullHandling, defaultHandling, Column.DEFAULT_VALUE_DEFAULT);
	}

	/**
	 * Define a field with specified null handling and a specified default value.
	 * 
	 * @param fieldName       The field name.
	 * @param type            The field type from Definition.TYPE_...
	 * @param nullHandling    Either Definition.NULLABLE or Definition.NOT_NULL.
	 * @param defaultHandling Definition.DEFAULT_AS_SPECIFIED
	 * @param defaultValue    can be a String, Character, Number, Boolean or null
	 *                        Value.
	 * @return The field Definition for a create statement.
	 * 
	 */
	public final static Definition define(String fieldName, int type, int nullHandling, int defaultHandling,
			Object defaultValue) {
		return define(fieldName, type, nullHandling, defaultHandling, defaultValue,
				Column.CHECK_CONSTRAINT_DEFAULT);
	}

	/**
	 * 
	 * @param fieldName       The field name.
	 * @param type            The field type from Definition.TYPE_...
	 * @param nullHandling    Either Definition.NULLABLE or Definition.NOT_NULL.
	 * @param defaultHandling Definition.DEFAULT_AS_SPECIFIED
	 * @param defaultValue    Either a String, Integer or Double Object as Default
	 *                        Value.
	 * @param check           a check constraint
	 * 
	 * @return The field Definition for a create statement.
	 * 
	 */
	public final static Definition define(String fieldName, int type, int nullHandling, int defaultHandling,
			Object defaultValue, String check) {
		return new Definition(fieldName, type, nullHandling, defaultHandling, defaultValue, check);
	}

	/**
	 * Parses the individual create expressions.
	 * 
	 * @param create a create statement.
	 * 
	 * @return a List<String> containing the field definitions and constraints from
	 *         the create statement.
	 */
	static final List<String> parseCreateToList(String create) {
		List<String> rawDefinitions = new ArrayList<>();
		int iOpenBracket = create.indexOf("(");
		int iCloseBracket = create.lastIndexOf(")");
		String defs = create.substring(iOpenBracket + 1, iCloseBracket);
		int start = 0;
		int bracketLevel = 0;
		for (int i = 0; i < defs.length(); i++) {
			char charAtI = defs.charAt(i);
			if (charAtI == '(') {
				bracketLevel++;
			} else if (charAtI == ')') {
				bracketLevel--;
			} else if (charAtI == ',' && bracketLevel == 0) {
				rawDefinitions.add(defs.substring(start, i).trim());
				start = i + 1;
			}
			if (i + 1 == defs.length()) {
				rawDefinitions.add(defs.substring(start).trim());
			}
		}
		return rawDefinitions;
	}

	/**
	 * Gets the NOT NULL constraint if there is one.
	 * 
	 * @param constraints the full constraints string.
	 * 
	 * @return the upper case constraint string or null if there is none.
	 */
	private static final String getNullConstraint(String constraints) {
		if (constraints.toUpperCase().contains(Voc.NOT_NULL)) {
			return Voc.NOT_NULL;
		}
		return null;
	}

	/**
	 * Get the default constraint if there is one.
	 * 
	 * @param constraints the string containing all constraints
	 * 
	 * @return the default constraint if one was found, null otherwise
	 */
	private static final String getDefaultConstraint(String constraints) {
		String constraintsUC = constraints.toUpperCase();
		if (constraintsUC.contains(Voc.DEFAULT)) {
			if (constraintsUC.contains(Voc.DEFAULT_NULL)) {
				return Voc.DEFAULT_NULL;
			}
			String beginTextDefaultUC = Voc.DEFAULT.concat(" '");
			if (constraintsUC.contains(beginTextDefaultUC)) {
				int startIndex = constraintsUC.indexOf(Voc.DEFAULT);
				int endIndex = constraints.indexOf("'", startIndex + beginTextDefaultUC.length()) + 1;
				return constraints.substring(startIndex, endIndex);
			} else {
				int startIndex = constraintsUC.indexOf(Voc.DEFAULT);
				int endIndex = constraints.indexOf(" ", startIndex + Voc.DEFAULT.length() + 1);
				if (endIndex < 0) {
					endIndex = constraints.length();
				}
				return constraints.substring(startIndex, endIndex);
			}
		}
		return null;
	}

	/**
	 * extracts the value from a defaultConstraint String.
	 * 
	 * @param defaultConstraint the constraint string
	 * @param type              the value type.
	 * 
	 * @return the default value.
	 */
	private static final Object getDefaultValue(String defaultConstraint, int type) {
		Object defaultValue;
		if (defaultConstraint.contains("'")) {
			int beginIndex = defaultConstraint.indexOf("'") + 1;
			int endIndex = defaultConstraint.indexOf("'", beginIndex);
			defaultValue = defaultConstraint.substring(beginIndex, endIndex);
		} else {
			int beginIndex = defaultConstraint.indexOf(" ") + 1;
			String strValue = defaultConstraint.substring(beginIndex);
			if (type == TYPE_INTEGER) {
				defaultValue = Long.parseLong(strValue);
			} else if (type == TYPE_REAL) {
				defaultValue = Double.parseDouble(strValue);
			} else {
				defaultValue = strValue;
			}
		}
		return defaultValue;
	}

	/**
	 * extracts a check constraint if there is one
	 * 
	 * @param constraints the full constraints string.
	 * 
	 * @return the substring containing the check constraint or an empty string if
	 *         there is none.
	 */
	private static final String getCheckConstraint(String constraints) {
		String constraintsUC = constraints.toUpperCase();
		if (constraintsUC.contains(Voc.CHECK)) {
			int startIndex = constraintsUC.indexOf(Voc.CHECK);
			int openBracket = constraints.indexOf("(", startIndex);
			int endBracket = -1;
			int bracketLevel = 0;
			for (int i = openBracket + 1; i < constraints.length(); i++) {
				char x = constraints.charAt(i);
				if (x == '(') {
					bracketLevel++;
				} else if (x == ')' && bracketLevel > 0) {
					bracketLevel--;
				} else if (x == ')') {
					endBracket = i;
					break;
				}
			}
			if (endBracket > 0) {
				return constraints.substring(openBracket + 1, endBracket);
			}
		}
		return "";
	}

	/**
	 * Parse a field Definition from an input string.
	 * 
	 * @param definition the field definition input string.
	 * 
	 * @return a Definition object.
	 */
	public final static Definition parseDefinition(String definition) {
		definition = definition.trim();
		/*
		 * Parse name
		 */
		String name;
		int iRest;
		if (definition.charAt(0) == '`') {
			name = definition.substring(1, iRest = definition.indexOf("`", 1));
			iRest += 2;
		} else {
			name = definition.substring(0, iRest = definition.indexOf(" "));
			iRest++;
		}

		/*
		 * Parse type
		 */
		String rest = definition.substring(iRest);
		int type = 0;
		{
			String restUC = rest.toUpperCase();
			int nextSpace = rest.indexOf(" ");
			String typeNameUC;
			if (nextSpace < 0) {
				typeNameUC = restUC;
			} else {
				typeNameUC = restUC.substring(0, nextSpace);
			}
			switch (typeNameUC) {
			case Voc.INTEGER:
				if (restUC.startsWith(Voc.INTEGER_PK_AI)) {
					type = TYPE_INTEGER_PK_AI;
					iRest = Voc.INTEGER_PK_AI.length();
				} else if (restUC.startsWith(Voc.INTEGER_PK)) {
					type = TYPE_INTEGER_PK;
					iRest = Voc.INTEGER_PK.length();
				} else {
					type = TYPE_INTEGER;
					iRest = Voc.INTEGER.length();
				}
				break;
			case Voc.REAL:
				if (restUC.startsWith(Voc.REAL_PK)) {
					type = TYPE_REAL_PK;
					iRest = Voc.REAL_PK.length();
				} else {
					type = TYPE_REAL;
					iRest = Voc.REAL.length();
				}
				break;
			case Voc.TEXT:
				if (restUC.startsWith(Voc.TEXT_PK)) {
					type = TYPE_TEXT_PK;
					iRest = Voc.TEXT_PK.length();
				} else {
					type = TYPE_TEXT;
					iRest = Voc.TEXT.length();
				}
				break;
			case Voc.BLOB:
				type = TYPE_BLOB;
				iRest = Voc.BLOB.length();
				break;
			default:
				throw new IllegalArgumentException(
						String.format("parseDefinition: Data type not recognized: Name: %s (%s)", name, rest));
			}
			if (iRest >= restUC.length()) {
				return define(name, type);
			}
		}
		/*
		 * Parse Field Constraints
		 */
		String constraints = rest.substring(iRest + 1);
		iRest = -1;
		String nullConstraint = null;
		int nullHandling = NULLABLE;
		if (Objects.nonNull(nullConstraint = getNullConstraint(constraints))) {
			if (nullConstraint.equals(Voc.NOT_NULL)) {
				nullHandling = NOT_NULL;
			}
		}
		int defaultHandling = NO_DEFAULT;
		Object defaultValue = Column.DEFAULT_VALUE_DEFAULT;

		String defaultConstraint;
		if (Objects.nonNull(defaultConstraint = getDefaultConstraint(constraints))) {
			if (defaultConstraint.equals(Voc.DEFAULT_NULL)) {
				defaultHandling = DEFAULT_NULL;
			} else {
				defaultHandling = DEFAULT_AS_SPECIFIED;
				defaultValue = getDefaultValue(defaultConstraint, type);
			}
		}

		String check = getCheckConstraint(constraints);

		return define(name, type, nullHandling, defaultHandling, defaultValue, check);
	}

	/**
	 * Parse the field definitions from an create statement.
	 * 
	 * @param createStatement a create statement.
	 * 
	 * @return a {@code Map<String, Definition>} containing the field Definitions.
	 *         The SQL name is the key.
	 */
	public final static Map<String, Definition> parseDefinitions(String createStatement) {
		List<String> rawDefinitions = parseCreateToList(createStatement);
		Map<String, Definition> definitions = new HashMap<>();
		for (String def : rawDefinitions) {
			if (Constraint.isConstraint(def)) {
				continue;
			}
			Definition d = parseDefinition(def);
			definitions.put(d.getName(), d);
		}
		return definitions;
	}

	/**
	 * Dump the definition data to a string for debugging.
	 * 
	 * @param d the definition to examine.
	 * 
	 * @return the Definition content string.
	 */
	public static final String getContentString(Definition d) {
		return String.format("name: %s // type: %d // null: %d // default: %d // def val: %s// check: %s",
				d.getName(), d.getType(), d.getNullHandling(), d.getDefaultHandling(),
				String.valueOf(d.getDefaultValue()), d.getCheck());
	}

	/**
	 * Field name definition
	 */
	private final String name;
	/**
	 * Field type definition
	 */
	private final int type;
	/**
	 * Field definition null handling
	 */
	private final int nullHandling;
	/**
	 * Field definition default value
	 */
	private final int defaultHandling;
	/**
	 * Field value default value
	 */
	private final Object defaultValue;
	/**
	 * CHECK constraint
	 */
	private final String check;

	/**
	 * This constructor is used by the define methods
	 * 
	 * @param fieldName       the field name
	 * @param type            the field type
	 * @param nullHandling    the null handling
	 * @param defaultHandling the default handling
	 * @param defaultValue    the default value
	 * @param check           the check constraint
	 */
	private Definition(String fieldName, int type, int nullHandling, int defaultHandling, Object defaultValue,
			String check) {
		this.name = fieldName;
		this.type = type;
		this.nullHandling = nullHandling;
		this.defaultHandling = defaultHandling;
		this.defaultValue = defaultValue;
		this.check = check;
	}

	/**
	 * get the defined entity name.
	 * 
	 * @return the defined entity name or an empty string.
	 */
	public String getName() {
		return name;
	}

	/**
	 * get the defined type.
	 * 
	 * @return the field type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * Gets the fields null handling
	 * 
	 * @return the nullability of the field.
	 * 
	 * @see Definition#NULLABLE
	 * @see Definition#NOT_NULL
	 */
	public int getNullHandling() {
		return nullHandling;
	}

	/**
	 * Gets the fields default handling
	 * 
	 * @return the default handling
	 * 
	 * @see Definition#NO_DEFAULT
	 * @see Definition#DEFAULT_AS_SPECIFIED
	 * @see Definition#DEFAULT_NULL
	 */
	public int getDefaultHandling() {
		return defaultHandling;
	}

	/**
	 * Gets the specified default value
	 * 
	 * @return the default value if specified, an empty string otherwise.
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * checks if the field type is either {@link Definition#TYPE_INTEGER_PK} or
	 * {@link Definition#TYPE_INTEGER_PK_AI}
	 * 
	 * @return true if auto generated
	 */
	public boolean isAutoGenerated() {
		switch (type) {
		case TYPE_INTEGER_PK:
		case TYPE_INTEGER_PK_AI:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Checks if the field type is {@link Definition#TYPE_TEXT_PK_UUID}
	 * 
	 * @return true if an UUID must be generated before adding a new record.
	 */
	public boolean mustGenerateUUID() {
		return type == TYPE_TEXT_PK_UUID;
	}

	/**
	 * Get the check constraint if there is one.
	 * 
	 * @return the check constraint or an empty string
	 */
	public String getCheck() {
		return check;
	}

	/**
	 * Compares the produced SQL Strings of this Definition to the supplied
	 * Definition
	 * 
	 * @param d The Definition to compare this Definition to.
	 * 
	 * @return true if the SQL Strings are equal
	 */
	public final boolean sqlEquals(Definition d) {
		return toString().equals(d.toString());
	}

	/**
	 * Appends the specified type and a space to the supplied StringBuffer
	 * 
	 * @param sb   the StringBuffer to append to
	 * @param type the type string to append
	 */
	private void appendType(StringBuilder sb, String type) {
		sb.append(String.format("%s ", type));
	}

	/**
	 * get field definition SQL string
	 */
	@Override
	public final String toString() {
		StringBuilder def = new StringBuilder();
		def.append("`").append(name).append("` ");
		boolean isPrimaryKey = false;
		switch (type) {
		case TYPE_INTEGER:
			appendType(def, Voc.INTEGER);
			break;
		case TYPE_INTEGER_PK:
			appendType(def, Voc.INTEGER_PK);
			isPrimaryKey = true;
			break;
		case TYPE_INTEGER_PK_AI:
			appendType(def, Voc.INTEGER_PK_AI);
			isPrimaryKey = true;
			break;
		case TYPE_REAL_PK:
			appendType(def, Voc.REAL_PK);
			break;
		case TYPE_REAL:
			appendType(def, Voc.REAL);
			break;
		case TYPE_TEXT:
			appendType(def, Voc.TEXT);
			break;
		case TYPE_TEXT_PK:
		case TYPE_TEXT_PK_UUID:
			appendType(def, Voc.TEXT_PK);
			isPrimaryKey = true;
			break;
		case TYPE_BLOB:
			appendType(def, Voc.BLOB);
			break;
		}

		if (!isPrimaryKey && nullHandling == NOT_NULL) {
			def.append(Voc.NOT_NULL).append(" ");
		}

		if (!isPrimaryKey && defaultHandling != NO_DEFAULT) {
			// && (defaultHandling == DEFAULT_NULL || defaultHandling ==
			// DEFAULT_AS_SPECIFIED)) {
			if (Objects.nonNull(defaultValue) && defaultHandling == DEFAULT_AS_SPECIFIED) {
				String defValueString = "";
				if (defaultValue instanceof Number) {
					if (defaultValue instanceof Double || defaultValue instanceof Float) {
						defValueString = Double.toString(((Number) defaultValue).doubleValue());
					} else {
						defValueString = Long.toString(((Number) defaultValue).longValue());
					}
				} else if (defaultValue instanceof String) {
					defValueString = "'".concat((String) defaultValue).concat("'");

				} else if (defaultValue instanceof Character) {
					defValueString = Integer.toString((int) (Character) defaultValue);
				} else if (defaultValue instanceof Boolean) {
					defValueString = (Boolean) defaultValue ? "1" : "0";
				}
				if (!defValueString.isEmpty()) {
					def.append(Voc.DEFAULT).append(" ").append(defValueString).append(" ");
				}
			} else if (nullHandling != NOT_NULL && (defaultHandling == DEFAULT_NULL
					|| (defaultHandling == DEFAULT_AS_SPECIFIED && Objects.isNull(defaultValue)))) {
				def.append(Voc.DEFAULT_NULL).append(" ");
			}
		}

		if (Objects.nonNull(check) && !check.isEmpty()) {
			def.append("CHECK (").append(check).append(") ");
		}
		return def.toString().trim();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((check == null) ? 0 : check.hashCode());
		result = prime * result + defaultHandling;
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + nullHandling;
		result = prime * result + type;
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
		Definition other = (Definition) obj;
		if (check == null) {
			if (other.check != null)
				return false;
		} else if (!check.equals(other.check))
			return false;
		if (defaultHandling != other.defaultHandling)
			return false;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nullHandling != other.nullHandling)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public int getReferencingType() {
		return getReferencingType(type);
	}

	public static int getReferencingType(int t) {
		switch (t) {
		case TYPE_AUTO:
			return TYPE_AUTO;
		case TYPE_INTEGER:
		case TYPE_INTEGER_PK:
		case TYPE_INTEGER_PK_AI:
			return TYPE_INTEGER;
		case TYPE_TEXT:
		case TYPE_TEXT_PK:
		case TYPE_TEXT_PK_UUID:
			return TYPE_TEXT;
		case TYPE_REAL:
		case TYPE_REAL_PK:
			return TYPE_REAL;
		case TYPE_BLOB:
			return TYPE_BLOB;
		default:
			return TYPE_AUTO;
		}
	}
} // End of class Definition
