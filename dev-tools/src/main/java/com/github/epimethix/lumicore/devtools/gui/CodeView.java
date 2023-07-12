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
package com.github.epimethix.lumicore.devtools.gui;

import java.awt.GridLayout;
import java.io.File;

import javax.swing.JPanel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.github.epimethix.lumicore.common.swing.FileDocument;

@SuppressWarnings("serial")
public class CodeView extends JPanel implements FileDocument {
	private final File file;
	private final RSyntaxTextArea syntaxTextArea;
	private final String code;
	
	public CodeView(File file, String code) {
		super(new GridLayout(1,1));
		this.file = file;
		this.code = code;
		syntaxTextArea = new RSyntaxTextArea();
		syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
		syntaxTextArea.setCodeFoldingEnabled(true);
		syntaxTextArea.setText(code);
		syntaxTextArea.setEditable(false);
		RTextScrollPane sp = new RTextScrollPane(syntaxTextArea);
		syntaxTextArea.select(0, 0);
		add(sp);
	}

	@Override
	public File getDocumentFile() {
		return file;
	}

	@Override
	public String getDocumentName() {
		return file.getName();
	}

	public void refresh(CodeView newCodeView) {
		syntaxTextArea.setText(newCodeView.code);
//		syntaxTextArea.select(0, 0);
	}
}
