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

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class EasyODF {

	public static final class UserField {
		private Node userFieldDecl;
		private List<Node> userFieldsGet;

		public UserField(Node userFieldDecl, List<Node> fieldOccurences) {
			super();
			this.userFieldDecl = userFieldDecl;
			this.userFieldsGet = fieldOccurences;
		}

		private String getName() {
			return userFieldDecl.getAttributes().getNamedItem("text:name").getNodeValue();
		}

		public void setValue(String value) {
			Node officeStringValue = userFieldDecl.getAttributes().getNamedItem("office:string-value");
			officeStringValue.setNodeValue(value);
			userFieldsGet.forEach(uf -> uf.setTextContent(value));
		}

		public String getValue() {
			return userFieldsGet.stream().findAny().get().getTextContent();
		}
	}

	private static List<Node> searchDOM(OdfTextDocument doc, String tagNameToSearch) throws Exception {
		List<Node> result = new ArrayList<>();
		NodeList list = doc.getContentRoot();
//		System.out.println(list.getLength());
		for (int i = 0; i < list.getLength(); i++) {
			Node n = list.item(i);
			searchDOM(n, tagNameToSearch, result);
		}
		return result;
	}

	private static void searchDOM(Node node, String tagNameToSearch, List<Node> result) {
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
//			System.out.println("Name: " + child.getNodeName() + "/" + child);
			if (child.getNodeName().equals(tagNameToSearch)) {
				result.add(child);
			} else {
				searchDOM(child, tagNameToSearch, result);
			}
		}
	}

	public static Map<String, UserField> getUserFields(OdfTextDocument doc) {
		List<UserField> userFields = new ArrayList<>();
		try {
			List<Node> userFieldsDecl = searchDOM(doc, "text:user-field-decl");
			List<Node> userFieldsGet = searchDOM(doc, "text:user-field-get");
			outer: for (Node nodeDecl : userFieldsDecl) {
				NamedNodeMap declMap = nodeDecl.getAttributes();
				for (int i = 0; i < declMap.getLength(); i++) {
					Node attribute = declMap.item(i);
					if ("text:name".equals(attribute.getNodeName())) {
						String fieldName = attribute.getNodeValue();
						List<Node> fieldOccurences = new ArrayList<>();
						for (Node nodeGet : userFieldsGet) {
							NamedNodeMap getMap = nodeGet.getAttributes();
							for (int j = 0; j < getMap.getLength(); j++) {
								Node getAttribute = getMap.item(j);
								if ("text:name".equals(getAttribute.getNodeName())
										&& fieldName.equals(getAttribute.getNodeValue())) {
									fieldOccurences.add(nodeGet);
								}
							}
						}
						userFields.add(new UserField(nodeDecl, fieldOccurences));
						continue outer;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, UserField> result = new HashMap<>();

		for (UserField f : userFields) {
			result.put(f.getName(), f);
		}
		return result;
	}

	public static void fillFields(OdfTextDocument template, Map<String, String> values) {
		Map<String, UserField> userFields = EasyODF.getUserFields(template);
		for (String fieldName : userFields.keySet()) {
			String value = values.getOrDefault(fieldName, "-");
			UserField uf = userFields.get(fieldName);
			uf.setValue(value);
		}
	}

	/**
	 * open document using "libreoffice --writer file" command.
	 * 
	 * @param doc  the "odt" document to open
	 * @param wait wait for the libreoffice process to finish. this works only if
	 *             libreoffice is being started; if there is already an instance of
	 *             libreoffice running this method will return immediately even if
	 *             {@code wait==true}.
	 */
	public static final void openOdfTextDocument(OdfTextDocument doc, boolean wait) {
		try {
			if (wait) {
				Process p = Runtime.getRuntime()
						.exec(new String[] { "soffice", "--writer", Path.of(doc.getBaseURI()).toString() });
				p.waitFor();
			} else {
				Runtime.getRuntime().exec(new String[] { "soffice", "--writer", Path.of(doc.getBaseURI()).toString() });
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

//	public static final void appendTextDocument(OdfTextDocument doc, OdfTextDocument append) {
//		try {
//			OdfTextDocument d = OdfTextDocument.newTextMasterDocument();
//			d.getContentRoot().appendChild(doc.getContentRoot().cloneNode(true));
//			d.getContentRoot().appendChild(append.getContentRoot().cloneNode(true));
//			
//			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

//	import org.odftoolkit.simple.TextDocument;
//	import org.odftoolkit.simple.table.Table;
//	import org.odftoolkit.simple.text.Paragraph;
//	import org.odftoolkit.simple.text.TextP;
//	import org.odftoolkit.simple.text.TextSpan;
//	import org.w3c.dom.Node;
//	import org.w3c.dom.NodeList;

//	public class AppendODTDocumentsWithImages {
//	    public static void AppendODTDocumentsWithImages() {
//	        try {
//	            // Load the first ODT document
//	            TextDocument doc1 = TextDocument.loadDocument("path/to/first/document.odt");
//
//	            // Load the second ODT document
//	            TextDocument doc2 = TextDocument.loadDocument("path/to/second/document.odt");
//
//	            // Get the content of the second document
//	            NodeList contentToAppend = doc2.getContentRoot().getChildNodes();
//
//	            // Append the content to the first document, including images
//	            for (int i = 0; i < contentToAppend.getLength(); i++) {
//	                Node nodeToAppend = contentToAppend.item(i).cloneNode(true);
//
//	                if (nodeToAppend instanceof TextSpan) {
//	                    Paragraph paragraph = doc1.addParagraph("");
//	                    TextP textP = paragraph.addTextContent(nodeToAppend.getTextContent());
//	                    paragraph.setOdfElement(nodeToAppend);
//	                    textP.setOdfElement(nodeToAppend);
//	                } else if (nodeToAppend instanceof Table) {
//	                    Table tableToAppend = (Table) nodeToAppend.cloneNode(true);
//	                    doc1.getContentRoot().appendChild(tableToAppend);
//	                }
//	            }
//
//	            // Save the modified document to a new file
//	            doc1.save("path/to/output/combined_document.odt");
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	        }
//	    }
//	}

	private EasyODF() {}
}
