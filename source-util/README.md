# lumicore :: Source Util

source-util is a tool to read/create, manipulate and persist java source code.

## Limitations

code (identifiers) must use only Java-Characters. Enums are NIY. Annotations are NIY.

## Usage

```java
		/*
		 * Loading an existing File from source
		 */
		JavaSource src_utf8 = JavaSource.readFile("/path/to/java/File.java");
		JavaSource src_utf16 = JavaSource.readFile(new File("/path/to/java/File.java"), StandardCharsets.UTF_16);

		JavaSource src = JavaSource.readSource("public class SomeClass { /* some source code */ }");
		/*
		 * Insert elements
		 */
		src.insertImport(ImportSource.newImport("some.imported.Class"));
		src.insertField(FieldSource.Builder.newField("String", "testString").build());
		src.insertConstructor(ConstructorSource.Builder.newConstructor("SomeClass").build());
		src.insertMethod(MethodSource.Builder.newMethod("voidMethod").addParameter("String p").build());
		/*
		 * Update Field value
		 */
		Optional<FieldSource> opt = src.getFields().stream().filter(fSrc -> fSrc.getIdentifier().equals("testString"))
				.findFirst();
		if (opt.isPresent()) {
			FieldSource fSrc = opt.get();
			FieldSource fSrc2 = FieldSource.Builder.editField(fSrc).setStringValue("val").build();
			src.replace(fSrc, fSrc2);
		}
		/*
		 * Obtain method signature info from compiled class (may contain actual type parameters?)
		 */
		MethodSource methodSrc = JavaSource
				.readGenericMethodString(Test.class.getDeclaredMethod("main", String[].class));
		/*
		 * Generate a new File
		 */
		JavaSource.Builder srcBuilder = JavaSource.Builder.newClass("com.example", "TestClass")
				/*
				 * Add class type parameter
				 */
				.addTypeParameter("E")
				/*
				 * Add static import
				 */
				.addStaticImport("java.nio.file.Paths.get")
				/*
				 * Set super
				 */
				.setSuperType("JPanel")
				/*
				 * Add import
				 */
				.addImport("javax.swing.JPanel")
				/*
				 * Add class annotation (java.lang)
				 */
				.addAnnotation(AnnotationSource.Builder.newAnnotation(0, "SuppressWarnings")
						
						.setStringValue("serial").build())
				/*
				 * Add interface
				 */
				.addInterface("ActionListener")
				/*
				 * Add imports
				 */
				.addImport("java.awt.event.ActionListener").addImport("java.awt.event.ActionEvent")
				/*
				 * Add Interface method
				 */
				.addMethod(MethodSource.Builder.newMethod("actionPerformed")

						.addAnnotation(AnnotationSource.Builder.newAnnotation(1, "Override").build())
						
						.setPublic()

						.addParameter("ActionEvent e")

						.addStatement("// TODO Auto-generated method stub").build())
				/*
				 * Add constructor
				 */
				.addConstructor(ConstructorSource.Builder.newConstructor("TestClass")

						.addStatement("super(new BorderLayout())").build())
				/*
				 * Add import
				 */
				.addImport("java.awt.BorderLayout")
				/*
				 * Add Field
				 */
				.addField(FieldSource.Builder.newConstant("String", "TEST_CONSTANT")

						.setStringValue("Test").build());

		JavaSource generatedSrc = srcBuilder.build();
		/*
		 * persist using UTF-8
		 */
		generatedSrc.print("/path/to/new/JavaFile.java");
		/*
		 * Persist using UTF-16
		 */
		generatedSrc.print(new File("/path/to/new/JavaFile.java"), StandardCharsets.UTF_16);
		/*
		 * Print source to console
		 */
		generatedSrc.print();
```

## Runnable example

```java
public class Test {
	public static void main(String[] args) {
		JavaSource.Builder srcBuilder = JavaSource.Builder.newClass("com.example", "TestClass")
				/*
				 * Add class type parameter
				 */
				.addTypeParameter("E")
				/*
				 * Add static import
				 */
				.addStaticImport("java.nio.file.Paths.get")
				/*
				 * Set super
				 */
				.setSuperType("JPanel")
				/*
				 * Add import
				 */
				.addImport("javax.swing.JPanel")
				/*
				 * Add class annotation (java.lang)
				 */
				.addAnnotation(AnnotationSource.Builder.newAnnotation(0, "SuppressWarnings")
						
						.setStringValue("serial").build())
				/*
				 * Add interface
				 */
				.addInterface("ActionListener")
				/*
				 * Add imports
				 */
				.addImport("java.awt.event.ActionListener").addImport("java.awt.event.ActionEvent")
				/*
				 * Add Interface method
				 */
				.addMethod(MethodSource.Builder.newMethod("actionPerformed")

						.addAnnotation(AnnotationSource.Builder.newAnnotation(1, "Override").build())
						
						.setPublic()

						.addParameter("ActionEvent e")

						.addStatement("// TODO Auto-generated method stub").build())
				/*
				 * Add constructor
				 */
				.addConstructor(ConstructorSource.Builder.newConstructor("TestClass")

						.addStatement("super(new BorderLayout())").build())
				/*
				 * Add import
				 */
				.addImport("java.awt.BorderLayout")
				/*
				 * Add Field
				 */
				.addField(FieldSource.Builder.newConstant("String", "TEST_CONSTANT")

						.setStringValue("Test").build());

		JavaSource generatedSrc = srcBuilder.build();
		/*
		 * Print source to console
		 */
		generatedSrc.print();
	}
}
```