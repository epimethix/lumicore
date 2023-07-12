/*
 * Copyright 2022 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.common.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ClipBoardUtils {

	private ClipBoardUtils() {}

	public static void putString(String content) {
		StringSelection data = new StringSelection(content);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(data, data);
	}

	public static String getString() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable clipData = clipboard.getContents(clipboard);
		if (clipData != null) {
			try {
				if (clipData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					return (String) (clipData.getTransferData(DataFlavor.stringFlavor));
				}
			} catch (UnsupportedFlavorException ufe) {
				System.err.println("Flavor unsupported: " + ufe);
			} catch (IOException ioe) {
				System.err.println("Data not available: " + ioe);
			}
		}
		return null;
	}
}
