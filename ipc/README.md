# lumicore :: IPC

IPC stands for Inter-Process-Communication. This project can be used in case an application is supposed to run in single instance mode but will be run subsequently to for example open files in case an os file association is coupled with the application.

There always is a receiver (the first instance which runs the application) and a sender (subsequently started instances). The sender only passes the arguments from the main method to the receiver and immediately afterwards exits. the receiver then acts upon the input.

At the time the only implementation of the `IPCController` interface is the `FileSystemIPCController`. The application should define a dedicated sub-directory of its nesting directory for IPC messages.


### Usage

```java

	private static IPCController ipcController;

	public static void main(String[] args) {
		/*
		 * Locking on AppFiles.LOCK_FILE to enforce single instance
		 */
		if (ApplicationUtils.lockSingleInstance(AppFiles.LOCK_FILE)) {
			ipcController = FileSystemIPCController.getIPCController(AppFiles.MESSAGES_DIR, Mode.RECEIVER);
			try {
				Lumicore.startApplication(MyApplication.class, args);
			} catch (ConfigurationException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		} else {
			/*
			 * If locking on file for single application instance failed then the main args
			 * are passed to the currently running instance.
			 */
			ipcController = FileSystemIPCController.getIPCController(AppFiles.MESSAGES_DIR, Mode.SENDER);
			ipcController.putMessage(String.join(";", args));
			System.exit(0);
		}
	}
	
	...
	
	public MyApplication(String[] args) {
	
		// TODO act upon args
		
		ipcController.addMessageListener(this::openDocument);
	}

	/**
	 * This method is used as {@code MessageListener} for applications instance that
	 * are started additionally. as a default behavior default the main args are passed.
	 * 
	 * @param args the args from the main method from another application instance
	 */
	public void openDocument(String args) {
		System.err.println("Open Document: " + args);
		// TODO load Document, show UI...
	}
```