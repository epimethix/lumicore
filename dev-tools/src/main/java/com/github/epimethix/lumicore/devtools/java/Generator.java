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
package com.github.epimethix.lumicore.devtools.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;

import com.github.epimethix.lumicore.classscan.ClassScanner;
import com.github.epimethix.lumicore.classscan.ClasspathScanner;
import com.github.epimethix.lumicore.common.Reflect;
import com.github.epimethix.lumicore.common.orm.model.Entity;
import com.github.epimethix.lumicore.orm.ORM;

public class Generator {


	public static final synchronized void generateEntityHashCodeAndEquals(File sourcesDir, String modelPackage, boolean keepOld)
			throws IOException, NoSuchMethodException {
		ClassScanner scanner = new ClasspathScanner(modelPackage);

		Collection<Class<?>> entityClasses = scanner.searchClassesAssignableFrom(Entity.class);

		String importArrays = "import java.util.Arrays;";

		for (Class<?> entityClass : entityClasses) {
			String relativePath = entityClass.getName().replaceAll("[.]", "/").concat(".java");
			File srcFile = new File(sourcesDir, relativePath);
			if (srcFile.exists()) {
				boolean shouldImportArrays = false;
				Field[] fields = ORM.selectEntityFields(entityClass);
				for (Field field : fields) {
					if (field.getType().isArray()) {
						shouldImportArrays = true;
						break;
					}
				}
				boolean arraysIsImported = false;
				boolean hashCodeInserted = false;
				boolean equalsInserted = false;
				StringBuffer dataOut = new StringBuffer();
				try (FileReader fr = new FileReader(srcFile); BufferedReader br = new BufferedReader(fr)) {
					String line;
					boolean lastLineWasOverride = false;
					int blockLevel = 0;
					int seekEndOfBlock = -1;
//					boolean firstRound = true;
					boolean lastRound = false;
					while (Objects.nonNull(line = br.readLine())) {
						for (char chr : line.toCharArray()) {
							if (chr == '{') {
								blockLevel++;
							} else if (chr == '}') {
								blockLevel--;
								if (blockLevel == 0) {
									lastRound = true;
								}
							}
						}
						if (seekEndOfBlock != -1 && seekEndOfBlock > blockLevel) {
							seekEndOfBlock = -1;
							continue;
						} else if (seekEndOfBlock != -1) {
							continue;
						}
						String selection = line.trim();
						if (selection.startsWith("public int hashCode")) {
							if (!lastLineWasOverride) {
								dataOut.append("\t@Override\n");
							}
							generateHashCode(entityClass, fields, dataOut);
							seekEndOfBlock = blockLevel;
							hashCodeInserted = true;
						} else if (selection.startsWith("public boolean equals")) {
							if (!lastLineWasOverride) {
								dataOut.append("\t@Override\n");
							}
							generateEquals(entityClass, fields, dataOut);
							seekEndOfBlock = blockLevel;
							equalsInserted = true;
						} else {
							if (lastRound) {
								if (!hashCodeInserted) {
									dataOut.append("\n");
									dataOut.append("\t@Override\n");
									generateHashCode(entityClass, fields, dataOut);
								}

								if (!equalsInserted) {
									dataOut.append("\n");
									dataOut.append("\t@Override\n");
									generateEquals(entityClass, fields, dataOut);
								}
							}
							if (selection.equals(importArrays)) {
								arraysIsImported = true;
							}
							dataOut.append(line).append("\n");
						}
						if (selection.equals("@Override")) {
							lastLineWasOverride = true;
						} else if (lastLineWasOverride) {
							lastLineWasOverride = false;
						}
//						if (firstRound) {
//							firstRound = false;
//						}
					}
				} // end of try

				String importStr;
				if (shouldImportArrays && !arraysIsImported) {
					importStr = String.format("%n%n%s", importArrays);
					dataOut.insert(dataOut.indexOf("\n"), importStr);
				} else {
					importStr = "";
				}

				File outPutFile = new File(srcFile.getPath().concat(".tmp"));
				if (outPutFile.exists()) {
					outPutFile.delete();
				}

				try (FileWriter fw = new FileWriter(outPutFile)) {
					fw.write(dataOut.toString());
				}
				if (keepOld) {
					File oldFile = new File(srcFile.getPath().concat(".old"));
					if (oldFile.exists()) {
						oldFile.delete();
					}
					srcFile.renameTo(oldFile);
				} else {
					srcFile.delete();
				}
				outPutFile.renameTo(srcFile);
			}
		}
	}

	private static void generateEquals(Class<?> entityClass, Field[] fields, StringBuffer dataOut)
			throws NoSuchMethodException {
		dataOut.append("\t").append("public boolean equals(Object obj) {\n");

		dataOut.append("\t\t").append("if (this == obj) {").append("\n");
		dataOut.append("\t\t\t").append("return true;").append("\n");
		dataOut.append("\t\t").append("}").append("\n");
		dataOut.append("\t\t").append("if (obj == null) {").append("\n");
		dataOut.append("\t\t\t").append("return false;").append("\n");
		dataOut.append("\t\t").append("}").append("\n");

		Class<?> superClass = entityClass.getSuperclass();
		if (!Reflect.typeEquals(Object.class, superClass)) {
			try {
				Method equals = superClass.getDeclaredMethod("equals", Object.class);
			} catch (NoSuchMethodException e) {
				System.err.printf("<%s> Superclass <%s> does not implement <boolean equals(Object)>!%n",
						entityClass.getSimpleName(), superClass.getSimpleName());
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			dataOut.append("\t\t").append("if (!super.equals(obj)) {").append("\n");
			dataOut.append("\t\t\t").append("return false;").append("\n");
			dataOut.append("\t\t").append("}").append("\n");
		}

		dataOut.append("\t\t").append("if (getClass() != obj.getClass()) {").append("\n");
		dataOut.append("\t\t\t").append("return false;").append("\n");
		dataOut.append("\t\t").append("}").append("\n");

		String entityClassName = entityClass.getSimpleName();
		String otherDeclaration = String.format("%s other = (%s) obj;", entityClassName, entityClassName);

		dataOut.append("\t\t").append(otherDeclaration).append("\n");

		for (Field field : fields) {
			Class<?> fType = field.getType();
			String fName = field.getName();
			if (fType.isArray()) {
				String comparison = String.format("if (!Arrays.equals(%s, other.%s)) {", fName, fName);
				dataOut.append("\t\t").append(comparison).append("\n");
				dataOut.append("\t\t\t").append("return false;").append("\n");
				dataOut.append("\t\t").append("}").append("\n");
			} else if (fType.isPrimitive()) {
				String comparison = String.format("if (%s != other.%s) {", fName, fName);
				dataOut.append("\t\t").append(comparison).append("\n");
				dataOut.append("\t\t\t").append("return false;").append("\n");
				dataOut.append("\t\t").append("}").append("\n");
			} else if (Entity.class.isAssignableFrom(fType)) {
				String referencedFieldName = ORM.getReferencedFieldName(field);
				Field referencedField = ORM.getReferencedField((Class<? extends Entity<?>>) fType,
						referencedFieldName);
				try {
					Method getter = Reflect.getGetter((Class<? extends Entity<?>>) fType, referencedField.getName());
					String getterName = getter.getName();
					dataOut.append("\t\t").append(String.format("if (%s == null) {", fName)).append("\n");
					dataOut.append("\t\t\t").append(
							String.format("if (other.%s != null && other.%s.%s() != null) {", fName, fName, getterName))
							.append("\n");
					dataOut.append("\t\t\t\t").append("return false;").append("\n");
					dataOut.append("\t\t\t").append("}").append("\n");
					dataOut.append("\t\t").append(String.format("} else if (%s.%s() == null) {", fName, getterName))
							.append("\n");
					dataOut.append("\t\t\t").append(
							String.format("if (other.%s != null && other.%s.%s() != null) {", fName, fName, getterName))
							.append("\n");
					dataOut.append("\t\t\t\t").append("return false;").append("\n");
					dataOut.append("\t\t\t").append("}").append("\n");
					dataOut.append("\t\t").append(String.format("} else if (other.%s == null) {", fName)).append("\n");
					dataOut.append("\t\t\t").append("return false;").append("\n");
					dataOut.append("\t\t").append(String.format("} else if (!%s.%s().equals(other.%s.%s())) {", fName,
							getterName, fName, getterName)).append("\n");
					dataOut.append("\t\t\t").append("return false;").append("\n");
					dataOut.append("\t\t").append("}").append("\n");

				} catch (NoSuchMethodException e) {
					System.err.printf("[%s].[%s] getter missing!%n", fType.getSimpleName(), referencedField.getName());
					throw e;
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			} else {
				dataOut.append("\t\t").append(String.format("if (%s == null) {", fName)).append("\n");
				dataOut.append("\t\t\t").append(String.format("if (other.%s != null) {", fName)).append("\n");
				dataOut.append("\t\t\t\t").append("return false;").append("\n");
				dataOut.append("\t\t\t").append("}").append("\n");
				dataOut.append("\t\t").append(String.format("} else if (!%s.equals(other.%s)) {", fName, fName))
						.append("\n");
				dataOut.append("\t\t\t").append("return false;").append("\n");
				dataOut.append("\t\t").append("}").append("\n");
			}
		}

		dataOut.append("\t\t").append("return true;").append("\n");
		dataOut.append("\t").append("}\n");

	}

	private static void generateHashCode(Class<?> entityClass, Field[] fields, StringBuffer dataOut)
			throws NoSuchMethodException {
		dataOut.append("\t").append("public int hashCode() {\n");
		dataOut.append("\t\t").append("final int prime = 31;").append("\n");
		Class<?> superClass = entityClass.getSuperclass();
		if (Reflect.typeEquals(Object.class, superClass)) {
			dataOut.append("\t\t").append("int result = 1;").append("\n");
		} else {
			try {
				Method superHashCode = superClass.getDeclaredMethod("hashCode");
			} catch (NoSuchMethodException e) {
				System.err.printf("<%s> Superclass <%s> does not implement <int hashCode()>!%n",
						entityClass.getSimpleName(), superClass.getSimpleName());
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			dataOut.append("\t\t").append("int result = super.hashCode();").append("\n");
		}
		boolean longTempDeclared = false;
		for (Field field : fields) {
			String fName = field.getName();
			Class<?> fType = field.getType();
			if (fType.isArray()) {
				dataOut.append("\t\t").append(String.format("result = prime * result + Arrays.hashCode(%s);", fName))
						.append("\n");
			} else if (fType.isPrimitive()) {
				if (fType.getName().equals("long")) {
					dataOut.append("\t\t")
							.append(String.format("result = prime * result + (int) (%s ^ (%s >>> 32));", fName, fName))
							.append("\n");
				} else if (fType.getName().equals("float")) {
					dataOut.append("\t\t")
							.append(String.format("result = prime * result + Float.floatToIntBits(%s);", fName))
							.append("\n");
				} else if (fType.getName().equals("double")) {
					if (!longTempDeclared) {
						dataOut.append("\t\t").append("long temp;").append("\n");
						longTempDeclared = true;
					}
					dataOut.append("\t\t").append(String.format("temp = Double.doubleToLongBits(%s);", fName))
							.append("\n");
					dataOut.append("\t\t").append("result = prime * result + (int) (temp ^ (temp >>> 32));")
							.append("\n");
				} else if (fType.getName().equals("boolean")) {
					dataOut.append("\t\t").append(String.format("result = prime * result + (%s ? 1231 : 1237);", fName))
							.append("\n");
				} else {
					dataOut.append("\t\t").append(String.format("result = prime * result + %s;", fName)).append("\n");
				}
			} else if (Entity.class.isAssignableFrom(fType)) {
				String referencedFieldName = ORM.getReferencedFieldName(field);
				Field referencedField = ORM.getReferencedField((Class<? extends Entity<?>>) fType,
						referencedFieldName);
				Method getter;
				try {
					getter = Reflect.getGetter((Class<? extends Entity<?>>) fType, referencedField.getName());
					dataOut.append("\t\t").append(String.format(
							"result = prime * result + ((%s == null || %s.%s() == null) ? 0 : %s.%s().hashCode());",
							fName, fName, getter.getName(), fName, getter.getName())).append("\n");
				} catch (NoSuchMethodException e) {
					System.err.printf("[%s].[%s] getter missing!%n", fType.getSimpleName(), referencedField.getName());
					throw e;
				} catch (SecurityException e) {
					e.printStackTrace();
				}
			} else {
				dataOut.append("\t\t").append(
						String.format("result = prime * result + ((%s == null) ? 0 : %s.hashCode());", fName, fName))
						.append("\n");
			}
		}
		dataOut.append("\t\t").append("return result;").append("\n");
		dataOut.append("\t").append("}\n");

	}
}
