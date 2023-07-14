# lumicore :: Stack Util

Stack util is a utility project for different stack analysis.

## `@CallerSensitive`

this annotation signals that a method will produce a result specific to the caller.

## `StackUtils`

Get caller information, print the stack.

## `AccessCheck`

Acts as an allow-list; Restricts access to allowed callers.

### Usage

Creating access checks:

```java
	private final AccessCheck check = AccessCheck.Builder.newBuilder().allowSelf()
			.allowIntermediateCaller(SomeIntermetiateCaller.class).allowCaller(AllowedCaller.class).build();

	/**
	 * Gets the Password.
	 * 
	 * @return the password.
	 * @throws IllegalAccessException if the caller class does not have permission
	 *                                to call the method.
	 */
	@CallerSensitive
	public String getPassword() throws IllegalAccessException {
		String caller = check.checkPermission();
		System.err.println("Allowed caller is: " + caller);
		return "Password";
	}

	/**
	 * Gets the Password.
	 * 
	 * @return "Password" or null if the permission check failed.
	 */
	@CallerSensitive
	public String getPasswordWithoutException() {
		String caller = null;
		try {
			caller = check.checkPermission();
		} catch (IllegalAccessException e) {
			// AccessCheck.checkPermission failed.
			return null;
		}
		System.err.println("Allowed caller is: " + caller);
		return "Password";
	}
```

Using the utility methods (drawback here is that no intermediates can be specified):

```java

	/*
	 * Allow Classes
	 */
	 @CallerSensitive
	public String getPassword() throws IllegalAccessException {
		boolean allowSelf = true;
		AccessCheck.allowCaller(allowSelf, SomeAllowedClass.class, SomeOtherAllowedClass.class);
		return "Password";
	}
...
	/*
	 * Using string there are more possibilities
	 */
	 @CallerSensitive
	public String getPassword() throws IllegalAccessException {
		boolean allowSelf = false;
		AccessCheck.allowCaller(allowSelf, "some.allowed.package", "some.allowed.CallerClass"/* and subclasses */,
				"some.other.AllowedClass::"/* excluding subclasses */, "some.Allowed::method");
		return "Password";
	}
```