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
package com.github.epimethix.lumicore.odftools;

import java.io.File;
import java.util.Map;

import org.odftoolkit.odfdom.doc.OdfTextDocument;

import com.github.epimethix.lumicore.odftools.EasyODF.UserField;

public class ODFExample {

	public static void main(String[] args) throws Exception {
		/*
		 * User Fields example
		 */
		File in = new File("src/main/resources/testText.odt");
		File out = new File("src/main/resources/outText.odt");
		OdfTextDocument t = OdfTextDocument.loadDocument(in.getAbsolutePath());
		Map<String, UserField> userFields = EasyODF.getUserFields(t);
		int i = 1;
		for (String key : userFields.keySet()) {
			UserField f = userFields.get(key);
			f.setValue("Field #" + i++);
		}
		t.save(out);
		EasyODF.openOdfTextDocument(t, true);
		System.out.println("Closed");
		// open the document template
//        TextDocument document = TextDocument.loadDocument(new File("template.ott"));
		// get the user field
//        TextUserFieldDeclElement userFieldDecl = (TextUserFieldDeclElement) document.getContentRoot()
//                .getFirstChildByTagName("text:user-field-decl");
//        // get the name of the user field
//        String userFieldName = userFieldDecl.getTextNamgetChildNodes()eAttribute();
//        // get the user field input element
//        TextUserFieldInputElement userFieldInput = (TextUserFieldInputElement) document.getContentRoot()
//                .getFirstChildByTagName("text:user-field-input");
//        // set the new value to the user field
//        userFieldInput.setTextContent("Hello Text");
//        // get the user field get element
//        TextUserFieldGetElement userFieldGet = (TextUserFieldGetElement) document.getContentRoot()
//                .getFirstChildByTagName("text:user-field-get");
		// set the name of the user field
//        userFieldGet.setTextNameAttribute(userFieldName);
		// save the document as odt
//        document.save("testOutput.odt");
//        // save the document as pdf
//        OdfPackage odfPackage = document.getPackage();
//        odfPackage.save("testOutput.pdf");
	}
}