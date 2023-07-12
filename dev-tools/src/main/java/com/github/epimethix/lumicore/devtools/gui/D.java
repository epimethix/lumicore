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

import java.util.Locale;

import com.github.epimethix.lumicore.common.ui.labels.manager.DefaultLabelsManager;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsController;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManager;

@LabelsController("dev-tools-labels")
public final class D {
	private final static LabelsManager DEV_TOOLS_LABELS = new DefaultLabelsManager("dev-tools-labels");
	public static final String DEV_TOOLS_PROPERTIES_MISSING_0 = "dev-tools-properties-missing-0";
	public static final String DEV_TOOLS_PROPERTIES_MISSING_1 = "dev-tools-properties-missing-1";
	public static final String DEV_TOOLS_PROPERTIES_MISSING_2 = "dev-tools-properties-missing-2";
//		public static final String DEV_TOOLS_PROPERTIES_MISSING_3 = "dev-tools-properties-missing-3";
	public static final String DEV_TOOLS_GENERATE_PROPERTIES_TEMPLATE = "dev-tools-generate-properties-template";
	public static final String DEV_TOOLS_GENERATE_PROPERTIES_TEMPLATE_TITLE = "dev-tools-generate-properties-template-title";
	public static final String DEV_TOOLS_PROPERTIES_REFRESH = "dev-tools-properties-refresh";
	public static final String DEV_TOOLS_PROPERTIES_REFRESH_TITLE = "dev-tools-properties-refresh-title";
	public static final String APP_NAME = "app-name";
	public static final String BUTTON_SAVE = "button-save";
	public static final String BUTTON_CANCEL = "button-cancel";
	public static final String MENU_FILE = "menu-file";
	public static final String MENU_DIAGRAM_NEW = "menu-diagram-new";
	public static final String MENU_DIAGRAM_OPEN = "menu-diagram-open";
	public static final String MENU_SAVE = "menu-save";
	public static final String MENU_SAVE_AS = "menu-save-as";
	public static final String MENU_EXIT = "menu-exit";
	public static final String DIAGRAM_FILE_DIR = "diagram-file-dir";
	public static final String DIAGRAM_FILE_NAME = "diagram-file-name";
	public static final String BUTTON_SELECT = "button-select";
	public static final String BUTTON_SELECT_ALL = "button-select-all";
	public static final String BUTTON_UN_SELECT = "button-un-select";
	public static final String BUTTON_UN_SELECT_ALL = "button-un-select-all";
	public static final String LABEL_CLASS_SELECTOR_TITLE = "label-class-selector-title";
	public static final String LABEL_CLASS_SELECTOR_CLASSES = "label-class-selector-classes";
	public static final String LABEL_CLASS_SELECTOR_SELECTED = "label-class-selector-selected";
	public static final String DIALOG_TITLE_NEW_ERD = "dialog-title-new-erd";
	public static final String CLASS_NAME = "class-name";
	public static final String PACKAGE_NAME = "package-name";
	public static final String DIALOG_TITLE_ADD_ENTITY = "dialog-title-add-entity";
	public static final String MENU_ADD_FIELD = "menu-add-field";
	public static final String MENU_ADD_CONSTRUCTOR = "menu-add-constructor";
	public static final String MENU_ADD_METHOD = "menu-add-method";
	public static final String BUTTON_MANAGE = "button-manage";
	public static final String BUTTON_NEW_ENTITY = "button-new-entity";
	public static final String BUTTON_ADD_ENTITY = "button-add-entity";
	public static final String STATUS_SAVED = "status-saved";
	public static final String STATUS_SAVE_ERROR = "status-save-error";
	public static final String DOCUMENT_HAS_UNSAVED_CHANGES = "document-has-unsaved-changes";
	public static final String DOCUMENTS_HAVE_UNSAVED_CHANGES = "documents-have-unsaved-changes";
	public static final String FIELD_TYPE_NAME = "field-type-name";
	public static final String FIELD_IDENTIFIER = "field-identifier";
	public static final String DIALOG_TITLE_ADD_FIELD = "dialog-title-add-field";
	public static final String MENU_ENTITY_INFO = "menu-entity-info";
	public static final String FIELD_VALUE = "field-value";
	public static final String CREATE_DIAGRAM_UML = "create-diagram-uml";
	public static final String CREATE_DIAGRAM_ERD = "create-diagram-erd";
	public static final String DIALOG_TITLE_NEW_DIAGRAM = "dialog-title-new-diagram";
	public static final String FONT_SIZE = "font-size";
	public static final String MENU_EXPORT_AS = "menu-export-as";
	public static final String MENU_VIEW_SOURCE = "menu-view-source";
	public static final String DIALOG_PROPERTIES_TITLE = "dialog-properties-title";
	public static final String DIALOG_PROPERTIES_MESSAGE_ALL = "dialog-properties-message-all";
	public static final String DIALOG_PROPERTIES_MESSAGE = "dialog-properties-message";
	public static final String DIALOG_SHOW_INSTANCE_METHODS = "dialog-show-instance-methods";
	public static final String DIALOG_SHOW_STATIC_METHODS = "dialog-show-static-methods";
	public static final String DIALOG_SHOW_CONSTRUCTORS = "dialog-show-constructors";
	public static final String DIALOG_SHOW_INSTANCE_FIELDS = "dialog-show-instance-fields";
	public static final String DIALOG_SHOW_STATIC_FIELDS = "dialog-show-static-fields";
	public static final String BUTTON_PROPERTIES = "button-properties";
	public static final String BUTTON_RELATIONS = "button-relations";
	public static final String ERROR_MSG_COULD_NOT_LOAD_CLASS = "error-msg-could-not-load-class";
	public static final String ERROR_MSG_SELECT_CLASS = "error-msg-select-class";
	public static final String WIZARD_TITLE_NEW_ENTITY = "WIZARD_TITLE_NEW_ENTITY";
	public static final String WIZARD_TITLE_NEW_ENTITY_PACKAGE_AND_NAME = "WIZARD_TITLE_NEW_ENTITY_PACKAGE_AND_NAME";
	public static final String WIZARD_TITLE_NEW_ENTITY_PATTERN_AND_OPTIONS = "WIZARD_TITLE_NEW_ENTITY_PATTERN_AND_OPTIONS";
	public static final String WIZARD_TITLE_NEW_ENTITY_SUPER_CLASS = "WIZARD_TITLE_NEW_ENTITY_SUPER_CLASS";
	public static final String WIZARD_TITLE_NEW_ENTITY_FIELDS = "WIZARD_TITLE_NEW_ENTITY_FIELDS";
	public static final String ERROR_MSG_PACKAGE_NAME_EMPTY = "ERROR_MSG_PACKAGE_NAME_EMPTY";
	public static final String ERROR_MSG_CLASS_NAME_EMPTY = "ERROR_MSG_CLASS_NAME_EMPTY";
	public static final String ERROR_MSG_CLASS_ALREADY_EXISTS = "ERROR_MSG_CLASS_ALREADY_EXISTS";
	public static final String ENTITY_PATTERN = "ENTITY_PATTERN";
	public static final String ENTITY_PATTERN_MUTABLE = "ENTITY_PATTERN_MUTABLE";
	public static final String ENTITY_PATTERN_IMMUTABLE = "ENTITY_PATTERN_IMMUTABLE";
	public static final String ENTITY_ID = "ENTITY_ID";
	public static final String SETUP_TITLE = "SETUP_TITLE";
	public static final String INVALID_BUNDLE_FILE_NAME = "INVALID_BUNDLE_FILE_NAME";
	public static final String MENU_BUNDLE_OPEN = "MENU_BUNDLE_OPEN";
	public static final String BUTTON_TRANSLATE = "BUTTON_TRANSLATE";
	public static final String TITLE_KEY = "TITLE_KEY";
	public static final String TRANSLATE_DIALOG_TITLE = "TRANSLATE_DIALOG_TITLE";
	public static final String TRANSLATE_DIALOG_FROM = "TRANSLATE_DIALOG_FROM";
	public static final String TRANSLATE_DIALOG_TO = "TRANSLATE_DIALOG_TO";
	public static final String CHECK_BOX_SHOW_ALL = "CHECK_BOX_SHOW_ALL";
	public static final String TRANSLATION_MESSAGE_NOTHING_TO_DO = "TRANSLATION_MESSAGE_NOTHING_TO_DO";
	public static final String MESSAGE_SELECT_CONSTANTS_FILE = "MESSAGE_SELECT_CONSTANTS_FILE";
	public static final String OPEN_I18N = "OPEN_I18N";
	public static final String MESSAGE_I18N_ALREADY_EXISTS = "MESSAGE_I18N_ALREADY_EXISTS";
	public static final String MESSAGE_NEW_LOCALE = "MESSAGE_NEW_LOCALE";
	public static final String MESSAGE_NEW_LOCALE_TITLE = "MESSAGE_NEW_LOCALE_TITLE";
	public static final String MESSAGE_ENTER_GENERATOR_API_KEY = "MESSAGE_ENTER_GENERATOR_API_KEY";
	public static final String MESSAGE_ENTER_GENERATOR_API_KEY_TITLE = "MESSAGE_ENTER_GENERATOR_API_KEY_TITLE";
	public static final String MESSAGE_ADD_LOCALES = "MESSAGE_ADD_LOCALES";
	public static final String MESSAGE_ADD_LOCALES_TITLE = "MESSAGE_ADD_LOCALES_TITLE";
//	public static final String BUTTON_RELOAD = null;

//	public static final LabelsManager getLabelsManager() {
//		return DEV_TOOLS_LABELS;
//	}

	public static String getLabel(String key, Object... args) {
		return DEV_TOOLS_LABELS.getLabel(key, args);
	}

	public static String getLabel(Locale locale, String key, Object... args) {
		return DEV_TOOLS_LABELS.getLabel(locale, key, args);
	}

	private D() {}
}
