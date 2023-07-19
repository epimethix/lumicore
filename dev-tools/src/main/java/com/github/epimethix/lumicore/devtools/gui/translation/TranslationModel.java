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
package com.github.epimethix.lumicore.devtools.gui.translation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.epimethix.lumicore.devtools.fs.TranslationModelFile;
import com.github.epimethix.lumicore.devtools.gui.translation.dialog.TranslateDialog.TranslationJob;
import com.github.epimethix.lumicore.sourceutil.JavaSource;
import com.github.epimethix.lumicore.sourceutil.JavaSource.FieldSource;
import com.github.epimethix.lumicore.swing.control.LTextArea;

public class TranslationModel {

	@SuppressWarnings("serial")
	final static class LabelTextField extends LTextArea {
		private String initialValue;

		LabelTextField() {
			this("");
		}

		LabelTextField(String initialValue) {
			super(2, 25);
			this.initialValue = initialValue;
			setText(initialValue);
		}

		boolean hasChanges() {
			return !Objects.equals(getText(), initialValue);
		}

		public void clearChanges() {
			initialValue = getText();
		}
	}

	private File file;

	private final TranslationModelFile modelFile;

	private JavaSource labelsConstants;

//	private final java.util.function.Consumer<T>

//	private Set<Properties> propertiesFiles;

	private Map<File, Properties> bundleFiles = new HashMap<>();
	private Map<String, Properties> localeProperties = new HashMap<>();
	private Map<String, Map<String, LabelTextField>> localeKeyTextFields = new HashMap<>();

	private final File relativeParent;
	private final int relativeIndex;

//	public TranslationModel(File file) throws IOException {
//		this(new File("").getAbsoluteFile(), file);
//	}

	public TranslationModel(File parent, File file) throws IOException {
		this.file = Objects.requireNonNull(file);
		this.relativeParent = parent;
		this.relativeIndex = relativeParent.getPath().length() + 1;
//		System.err.println("Parent: " + parent.getPath());
//		System.err.println("I18N: " + file.getPath());
		ObjectMapper om = new ObjectMapper();
		modelFile = om.readValue(file, TranslationModelFile.class);
//		System.err.println("Java Constants: " + javaConstantsFile.getPath());
		Set<String> ks = new HashSet<>(modelFile.getLocaleFiles().keySet());
		for (String locale : ks) {
			File f = new File(relativeParent, modelFile.getLocaleFiles().get(locale));
			Properties p = new Properties();
			if (f.exists()) {
				try (InputStream is = Files.newInputStream(f.toPath())) {
					p.load(is);
				}
				bundleFiles.put(f, p);
				localeProperties.put(locale, p);
			} else {
				modelFile.getLocaleFiles().remove(locale);
				modelFile.getLocaleLoaded().remove(locale);
			}
		}
		refresh();
	}

//	public TranslationModel(String name, File labelsConstantsFile, File bundleFile) throws IOException {
//		this(new File("").getAbsoluteFile(), name, labelsConstantsFile, bundleFile);
//	}

	public TranslationModel(File parent, String name, File labelsConstantsFile, File bundleFile) throws IOException {
		this.relativeParent = parent;
		this.relativeIndex = relativeParent.getPath().length() + 1;
//		labelsConstants = JavaSource.readFile(labelsConstantsFile);
		modelFile = new TranslationModelFile();
		modelFile.setName(name);
		modelFile.setConstantsFile(labelsConstantsFile.getPath().substring(relativeIndex));
		refresh(bundleFile);
	}

	public void refresh() throws IOException {
		refresh(null);
	}

	private void refresh(File bundleFile) throws IOException {
		File javaConstantsFile = new File(relativeParent, modelFile.getConstantsFile());
		labelsConstants = JavaSource.readFile(javaConstantsFile);
		File parentDir;
		if (!bundleFiles.keySet().isEmpty()) {
			Set<File> keySet = bundleFiles.keySet();
			parentDir = keySet.iterator().next().getParentFile();
		} else if (Objects.nonNull(bundleFile)) {
			parentDir = bundleFile.getParentFile();
//			String bundleName = bundleFile.getName().substring(0, bundleFile.getName().indexOf("_"));
		} else {
//			System.err.println("Refresh aborted");
			return;
		}
//		System.err.println(parentDir.getPath());
		File[] siblings = parentDir.listFiles(
				f -> f.getName().startsWith(modelFile.getName().concat("_")) && f.getName().endsWith(".properties"));
//		System.err.println(Arrays.asList(siblings));
		for (File f : siblings) {
			String fName = f.getName();
			String locale = fName.substring(fName.indexOf("_") + 1, fName.indexOf("."));
			if (!localeProperties.containsKey(locale)) {
				Properties p = new Properties();
				try (InputStream is = Files.newInputStream(f.toPath())) {
					p.load(is);
					bundleFiles.put(f, p);
					localeProperties.put(locale, p);
					modelFile.getLocaleFiles().put(locale, f.getPath().substring(relativeIndex));
					modelFile.getLocaleLoaded().put(locale, true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		for (FieldSource fs : labelsConstants.getFields()) {
			if (fs.isStatic() && fs.isFinal() && "String".equals(fs.getType())) {
				String key = fs.getIdentifier();
				Optional<String> optValue = fs.getStringValue();
//				String value;
				boolean shouldSet = false;
				if (optValue.isEmpty()) {
					shouldSet = true;
				} else if ((optValue.get()).equals("")) {
					shouldSet = true;
//				} else if (!value.equals(key)) {
//					shouldSet = true;
				}

//				System.err.println("check field " + fs.getIdentifier());
				if (shouldSet) {
//					System.err.println("set field " + fs.getIdentifier());
					FieldSource fsSet = FieldSource.Builder.editField(fs).setStringValue(key).build();
					labelsConstants.replace(fs, fsSet);
				}
			} else {
//				System.err.println("IGNORE field " + fs.getIdentifier());
//				D.getLabel(D.GETLABELTEST);
			}
		}
	}

	public boolean isNew() {
		return Objects.isNull(file);
	}

	public JavaSource getLabelsConstants() {
		return labelsConstants;
	}

	public String getName() {
		return modelFile.getName();
	}

	public String[] getAvailableLocales() {
		return localeProperties.keySet().toArray(new String[] {});
	}

	public boolean isLocaleLoaded(String locale) {
		return modelFile.getLocaleLoaded().getOrDefault(locale, false);
	}

	public Properties getProperties(String locale) {
		return localeProperties.get(locale);
	}

	public boolean isEmpty(String key, String locale) {
		return localeProperties.get(locale).getOrDefault(key, "").toString().trim().isEmpty();
	}

	public Set<String> keySet() {
		Set<String> keys = new HashSet<>();
		for (FieldSource fs : labelsConstants.getFields()) {
			if (fs.isStatic() && fs.isFinal() && "String".equals(fs.getType())) {
				Optional<String> key = fs.getStringValue();
				if (key.isPresent()) {
					keys.add(key.get());
				}
			}
		}
		return keys;
	}

	public void persist() throws StreamWriteException, DatabindException, IOException {
		persist(file);
	}

	public void persist(File fStore) throws StreamWriteException, DatabindException, IOException {
		this.file = Objects.requireNonNull(fStore);
		ObjectMapper om = new ObjectMapper();
//		System.err.println(modelFile.getConstantsFile());
		om.writerWithDefaultPrettyPrinter().writeValue(file, modelFile);
		Map<File, String> fileLocales = new HashMap<>();
		for (String locale : modelFile.getLocaleFiles().keySet()) {
			File localeFile = new File(modelFile.getLocaleFiles().get(locale));
			fileLocales.put(localeFile, locale);
		}
		for (String locale : localeProperties.keySet()) {
			Map<String, LabelTextField> m = localeKeyTextFields.get(locale);
			if (Objects.nonNull(m)) {
				Properties p = localeProperties.get(locale);
				for (String key : m.keySet()) {
					LabelTextField tf = m.get(key);
					if (tf.hasChanges()) {
						p.put(key, tf.getText());
						tf.clearChanges();
					}
				}
			}

		}
		for (File f : bundleFiles.keySet()) {
			Properties p = bundleFiles.get(f);
			try (OutputStream out = Files.newOutputStream(f.toPath())) {
				String loc = fileLocales.get(f);
				String message = getName();
				if (Objects.nonNull(loc)) {
					message = String.format("%s::%s", getName(), new Locale(loc).getDisplayLanguage(Locale.ENGLISH));
//					String displayLanguage = ;
				}
				p.store(out, message);
			}
		}
		labelsConstants.print(new File(relativeParent, modelFile.getConstantsFile()));
		refresh();
	}

	public boolean hasChanges() {
		for (String locale : localeProperties.keySet()) {
			Map<String, LabelTextField> m = localeKeyTextFields.get(locale);
			if (Objects.nonNull(m)) {
				for (String key : m.keySet()) {
					LabelTextField tf = m.get(key);
					if (tf.hasChanges()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public File getFile() {
		return file;
	}

	public String[] getLoadedLocales() {
		List<String> loaded = new ArrayList<>();
		for (String key : modelFile.getLocaleLoaded().keySet()) {
			if (modelFile.getLocaleLoaded().getOrDefault(key, false)) {
				loaded.add(key);
			}
		}
		Collections.sort(loaded, (a, b) -> {
			if ("en".equals(a)) {
				return "00".compareTo(b);
			}
			if ("en".equals(b) && Objects.nonNull(a)) {
				return a.compareTo("00");
			}
			return a.compareTo(b);

		});
		return loaded.toArray(new String[] {});
	}

	public boolean isComplete(String key) {
		boolean complete = true;
		List<Properties> loadedProperties = new ArrayList<>();
		for (String l : localeProperties.keySet()) {
			if (modelFile.getLocaleLoaded().get(l)) {
				loadedProperties.add(localeProperties.get(l));
			}
		}
		for (Properties p : loadedProperties) {
			if (!p.containsKey(key) || Objects.isNull(p.get(key)) || p.get(key).toString().trim().isEmpty()) {
				complete = false;
				break;
			}
		}
		return complete;
	}

	public LabelTextField getEditor(String locale, String key) {
		Properties p = localeProperties.get(locale);
		if (Objects.nonNull(p)) {
			Map<String, LabelTextField> m = localeKeyTextFields.get(locale);
			if (Objects.isNull(m)) {
				m = new HashMap<>();
				localeKeyTextFields.put(locale, m);
			}
			LabelTextField ltf = m.get(key);
			if (Objects.isNull(ltf)) {
				ltf = new LabelTextField(p.getOrDefault(key, "").toString());
				m.put(key, ltf);
			}
			return ltf;
		}
		return null;
	}

	public void clear() {
		localeKeyTextFields.clear();
	}

	public void setLoaded(String locale, boolean b) {
		if (modelFile.getLocaleLoaded().containsKey(locale)) {
			modelFile.getLocaleLoaded().put(locale, b);
		}
	}

	public File getParentFile() {
		return relativeParent;
	}

	public void createNewLocale(String text) {
		Set<File> keys = bundleFiles.keySet();
		if (!keys.isEmpty()) {
			File parent = keys.iterator().next().getParentFile();
			File newBundle = new File(parent, String.format("%s_%s.properties", modelFile.getName(), text));
			if (!keys.contains(newBundle)) {
				try {
					newBundle.createNewFile();
					refresh();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public List<String> getMissingLocales(String[] localesToCheck, String key) {
		List<String> missing = new ArrayList<>();
		for (String locale : localesToCheck) {
			Properties p = localeProperties.get(locale);
			if (Objects.nonNull(p)) {
				if (!p.containsKey(key) || Objects.isNull(p.get(key)) || p.get(key).toString().trim().isEmpty()) {
					missing.add(locale);
				}
			}
		}
		return missing;
	}

	public String getLabel(String locale, String key) {
		String val = null;
		Properties p = localeProperties.get(locale);
		if (Objects.nonNull(p)) {
			if (p.containsKey(key)) {
				return p.getProperty(key);
			}
		}
		return val;
	}

	public void update(TranslationJob job) {
		Properties p = localeProperties.get(job.getLocaleTo());
		if (Objects.nonNull(p)) {
			Map<String, LabelTextField> tfs = localeKeyTextFields.get(job.getLocaleTo());
			if (Objects.nonNull(tfs)) {
				Set<String> keySet = job.keySet();
				for (String key : keySet) {
					LabelTextField tf = tfs.get(key);
					tf.setText(job.getLabel(key));
				}
			}
		}
	}

	public String[] getHiddenLocales() {
		List<String> hidden = new ArrayList<>();
		for (String locale : modelFile.getLocaleLoaded().keySet()) {
			if (!modelFile.getLocaleLoaded().get(locale)) {
				hidden.add(locale);
			}
		}
		return hidden.toArray(new String[] {});
	}

}
