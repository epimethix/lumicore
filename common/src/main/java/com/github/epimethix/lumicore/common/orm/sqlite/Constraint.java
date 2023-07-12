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
import java.util.Arrays;
import java.util.List;

import com.github.epimethix.lumicore.common.ConfigurationException;
import com.github.epimethix.lumicore.common.orm.model.Entity;

/**
 * Interface for defining entity constraints.
 * 
 * 
 * @author epimethix
 * 
 * @see PrimaryKeyConstraint
 * @see ForeignKeyConstraint
 * @see UniqueConstraint
 * @see CheckConstraint
 */
public interface Constraint {

	/**
	 * Tests if the given field name occurs in this Constraint
	 * 
	 * @param field the field name to look for
	 * 
	 * @return true if the field name is contained in this Constraint
	 */
	boolean contains(String field);

	/**
	 * get the SQL String representing this Constraint
	 */
	String getSQL();

	/**
	 * Create a UNIQUE constraint for the specified field(s).
	 * 
	 * @param fields The field(s) to be unique.
	 * 
	 * @return A UniqueConstraint for a create statement.
	 * 
	 */
	public static UniqueConstraint uniqueConstraint(String... fields) {
		return new UniqueConstraint(fields);
	}

	/**
	 * Create a FOEIGN KEY constraint for the specified fields.
	 * 
	 * @param key           The fields holding the foreign index.
	 * @param foreignEntity The foreign entity.
	 * @param foreignField  The foreign fields.
	 * 
	 * @return A ForeignKeyConstraint for a create statement.
	 * 
	 */
	public static ForeignKeyConstraint foreignKeyConstraint(String[] key, String foreignEntity, String[] foreignField) {
		try {
			return new ForeignKeyConstraint(key, foreignEntity, foreignField);
		} catch (ConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Create a FOEIGN KEY constraint for the specified field.
	 * 
	 * @param key           The field holding the foreign index.
	 * @param foreignEntity The foreign entity.
	 * @param foreignField  The foreign field.
	 * 
	 * @return A ForeignKeyConstraint for a create statement.
	 * 
	 */
	public static ForeignKeyConstraint foreignKeyConstraint(String key, Class<? extends Entity<?>> foreignEntity,
			String foreignField) {
		return foreignKeyConstraint(key, Entity.getEntityName(foreignEntity), foreignField);
	}

	/**
	 * Create a FOEIGN KEY constraint for the specified field.
	 * 
	 * @param key           The field holding the foreign index.
	 * @param foreignEntity The foreign entity.
	 * @param foreignField  The foreign field.
	 * 
	 * @return A ForeignKeyConstraint for a create statement.
	 * 
	 */
	public static ForeignKeyConstraint foreignKeyConstraint(String key, String foreignEntity, String foreignField) {
		return foreignKeyConstraint(new String[] { key }, foreignEntity, new String[] { foreignField });
	}

	/**
	 * Create a FOEIGN KEY constraint for the specified fields.
	 * 
	 * @param key           The fields holding the foreign index.
	 * @param foreignEntity The foreign entity.
	 * @param foreignField  The foreign fields.
	 * 
	 * @return A ForeignKeyConstraint for a create statement.
	 * 
	 */
	public static ForeignKeyConstraint foreignKeyConstraint(String[] key, Class<? extends Entity<?>> foreignEntity,
			String[] foreignField) {
		return foreignKeyConstraint(key, Entity.getEntityName(foreignEntity), foreignField);
	}

	/**
	 * Create a PRIMARY KEY constraint for the specified fields.
	 * 
	 * @param fields The fields to create the PRIMARY KEY of.
	 * @return A PrimaryKeyConstraint for a create statement.
	 */
	public static PrimaryKeyConstraint primaryKeyConstraint(String... fields) {
		return new PrimaryKeyConstraint(fields);
	}

	/**
	 * Create a CHECK constraint for the entity
	 * 
	 * @param check the check constraint condition string
	 * 
	 * @return The specified check constraint
	 */
	public static CheckConstraint checkConstraint(String check) {
		return new CheckConstraint(check);
	}

	/**
	 * Parses a String and produces a Constraint Object.
	 * 
	 * @param def the constraint definition
	 * 
	 * @return the Constraint
	 * 
	 * @throws IllegalArgumentException if the constraint could not be recognized
	 */
	public static Constraint parseConstraint(String def) {
		String defUC = def.toUpperCase();
		if (defUC.startsWith(Voc.PRIMARY_KEY)) {
			int open = def.indexOf("(");

			String fieldsPart = def.substring(open + 1, def.length() - 1);
			String[] fields = splitFieldList(fieldsPart);

			return primaryKeyConstraint(fields);
		} else if (defUC.startsWith(Voc.FOREIGN_KEY)) {
			int start = def.indexOf("(") + 1;
			int end = def.indexOf(")");
			String[] key = splitFieldList(def.substring(start, end));
			start = defUC.indexOf(Voc.FK_REFERENCES);
			start = defUC.indexOf(" ", start) + 1;
			if (def.charAt(start) == '`') {
				end = def.indexOf('`', start + 1);
				start++;
			} else {
				end = def.indexOf(' ', start);
			}
			if (end == -1) {
				System.out.println(def);
			}
			String foreignEntity = def.substring(start, end);
			start = def.indexOf('(', start) + 1;
			end = def.indexOf(')', start);
			String[] foreignKey = splitFieldList(def.substring(start, end));
			return foreignKeyConstraint(key, foreignEntity, foreignKey);
		} else if (defUC.startsWith(Voc.UNIQUE)) {
			int start = def.indexOf('(') + 1;
			String[] fields = splitFieldList(def.substring(start, def.length() - 1));
			return uniqueConstraint(fields);
		} else if (defUC.startsWith(Voc.CHECK)) {
			int start = def.indexOf('(') + 1;
			return checkConstraint(def.substring(start, def.length() - 1));
		} else {
			throw new IllegalArgumentException(
					String.format("Constraint.parseConstraint: constraint not recognized [%s]", def));
		}
	}

	/**
	 * Splits a list of fields into a String[]
	 * 
	 * @param fieldsPart the list of fields (not containing any brackets)
	 * 
	 * @return a String[] of field names
	 */
	static String[] splitFieldList(String fieldsPart) {
		String[] fields = fieldsPart.split(",");
		if (fieldsPart.contains(",")) {
			fields = fieldsPart.split(",");
		} else {
			fields = new String[] { fieldsPart };
		}
		for (int i = 0; i < fields.length; i++) {
			fields[i] = fields[i].trim();
			if (fields[i].startsWith("`") && fields[i].endsWith("`")) {
				fields[i] = fields[i].substring(1, fields[i].length() - 1);
			}
		}
		return fields;
	}

	/**
	 * Parses all constraints from the given CREATE statement
	 * 
	 * @param create the create statement
	 * 
	 * @return a {@code List<Constraint>} containing all found (non field
	 *         definition) constraints
	 */
	public static List<Constraint> parseConstraints(String create) {
		List<String> rawDef = Definition.parseCreateToList(create);
		List<Constraint> constraints = new ArrayList<>();
		for (String def : rawDef) {
			if (isConstraint(def)) {
				constraints.add(parseConstraint(def));
			}
		}
		return constraints;
	}

	/**
	 * tests if the given definition string is a constraint
	 * 
	 * @param def the definition string
	 * 
	 * @return true if the definition String starts with a constraint name
	 */
	static boolean isConstraint(String def) {
		def = def.toUpperCase();
		if (def.startsWith(Voc.UNIQUE)) {
			return true;
		} else if (def.startsWith(Voc.PRIMARY_KEY)) {
			return true;
		} else if (def.startsWith(Voc.FOREIGN_KEY)) {
			return true;
		} else if (def.startsWith(Voc.CHECK)) {
			return true;
		}
		return false;
	}

	/**
	 * Primary Key Constraint
	 * 
	 * @see Constraint#primaryKeyConstraint(String...)
	 */
	public final static class PrimaryKeyConstraint implements Constraint {
		/**
		 * the list of fields contained in this PrimaryKeyConstraint
		 */
		private final List<String> fields;

		private PrimaryKeyConstraint(String... fields) {
			this.fields = new ArrayList<String>();
			for (String s : fields) {
				this.fields.add(s);
			}
		}

		@Override
		public final boolean contains(String field) {
			for (String f : fields) {
				if (f.equals(field)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String getSQL() {
			StringBuilder sql = new StringBuilder("PRIMARY KEY (");
			for (int i = 0; i < fields.size(); i++) {
				sql.append("`").append(fields.get(i)).append("`");
				if (i + 1 < fields.size()) {
					sql.append(", ");
				} else {
					sql.append(")");
				}
			}
			return sql.toString();
		}

		@Override
		public String toString() {
			return getSQL();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fields == null) ? 0 : fields.hashCode());
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
			PrimaryKeyConstraint other = (PrimaryKeyConstraint) obj;
			if (fields == null) {
				if (other.fields != null)
					return false;
			} else if (!fields.equals(other.fields))
				return false;
			return true;
		}
	} // End of class PrimaryKeyConstraint

	/**
	 * Foreign Key Constraint for entity definition
	 * 
	 * @see Constraint#foreignKeyConstraint(String, String, String)
	 * @see Constraint#foreignKeyConstraint(String[], String, String[])
	 */
	public final static class ForeignKeyConstraint implements Constraint {
		/**
		 * The key(s) (fields) of the foreign key.
		 */
		private final String[] key;
		/**
		 * The foreign entity containing the referenced key(s)
		 */
		private final String foreignEntity;
		/**
		 * The foreign field(s) (usually the primary key)
		 */
		private final String[] foreignField;

		private ForeignKeyConstraint(String[] key, String foreignEntity, String[] foreignField)
				throws ConfigurationException {
			if (key.length != foreignField.length) {
				throw new ConfigurationException(ConfigurationException.FOREIGN_KEY_CONSTRAINT_MALFORMED, key.length,
						foreignField.length);
			}
			this.key = key;
			this.foreignEntity = foreignEntity;
			this.foreignField = foreignField;
		}

		public String[] getKey() {
			return key;
		}

		public String getForeignEntity() {
			return foreignEntity;
		}

		public String[] getForeignField() {
			return foreignField;
		}

		@Override
		public boolean contains(String field) {
			for (String s : key) {
				if (s.equals(field)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String getSQL() {
			int n = key.length;
			if (n == 1) {
				return String.format("FOREIGN KEY (`%s`) REFERENCES `%s`(`%s`)", key[0], foreignEntity,
						foreignField[0]);
			} else if (n == 2) {
				return String.format("FOREIGN KEY (`%s`, `%s`) REFERENCES `%s`(`%s`, `%s`)", key[0], key[1],
						foreignEntity, foreignField[0], foreignField[1]);
			} else if (n == 3) {
				return String.format("FOREIGN KEY (`%s`, `%s`, `%s`) REFERENCES `%s`(`%s`, `%s`, `%s`)", key[0], key[1],
						key[2], foreignEntity, foreignField[0], foreignField[1], foreignField[2]);
			}
			return null;
		}

		@Override
		public String toString() {
			return getSQL();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((foreignEntity == null) ? 0 : foreignEntity.hashCode());
			result = prime * result + Arrays.hashCode(foreignField);
			result = prime * result + Arrays.hashCode(key);
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
			ForeignKeyConstraint other = (ForeignKeyConstraint) obj;
			if (foreignEntity == null) {
				if (other.foreignEntity != null)
					return false;
			} else if (!foreignEntity.equals(other.foreignEntity))
				return false;
			if (!Arrays.equals(foreignField, other.foreignField))
				return false;
			if (!Arrays.equals(key, other.key))
				return false;
			return true;
		}
	} // End of class ForeignKeyConstraint

	/**
	 * Unique Constraint for entity definition
	 * 
	 * @see Constraint#uniqueConstraint(String...)
	 */
	public final static class UniqueConstraint implements Constraint {
		/**
		 * the {@code List<String>} of fields contained in this UniqueConstraint
		 */
		private final List<String> fields;

		private UniqueConstraint(String... fields) {
			this.fields = new ArrayList<String>();
			for (String s : fields) {
				this.fields.add(s);
			}
		}

		@Override
		public boolean contains(String field) {
			return fields.contains(field);
		}

		@Override
		public String getSQL() {
			StringBuilder sql = new StringBuilder(Voc.UNIQUE);
			sql.append(" (");
			for (int i = 0; i < fields.size(); i++) {
				sql.append("`").append(fields.get(i)).append("`");
				if (i + 1 < fields.size()) {
					sql.append(", ");
				} else {
					sql.append(")");
				}
			}
			return sql.toString();
		}

		@Override
		public String toString() {
			return getSQL();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fields == null) ? 0 : fields.hashCode());
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
			UniqueConstraint other = (UniqueConstraint) obj;
			if (fields == null) {
				if (other.fields != null)
					return false;
			} else if (!fields.equals(other.fields))
				return false;
			return true;
		}

	} // End of class UniqueConstraint

	/**
	 * Check constraint for entity definition
	 * <p>
	 * <b>Note:</b> this is pretty much only a prototype by now
	 * 
	 * @see Constraint#checkConstraint(String)
	 */
	public final static class CheckConstraint implements Constraint {
		/**
		 * The check constraint String
		 */
		private final String constraint;

		private CheckConstraint(String constraint) {
			this.constraint = constraint;
		}

		public String getConstraint() {
			return constraint;
		}

		@Override
		public boolean contains(String field) {
			return constraint.contains(field);
		}

		@Override
		public String getSQL() {
			return String.format("CHECK (%s)", constraint);
		}

		@Override
		public String toString() {
			return getSQL();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((constraint == null) ? 0 : constraint.hashCode());
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
			CheckConstraint other = (CheckConstraint) obj;
			if (constraint == null) {
				if (other.constraint != null)
					return false;
			} else if (!constraint.equals(other.constraint))
				return false;
			return true;
		}
	} // end of class CheckConstraint

} // end of interface Constraint
