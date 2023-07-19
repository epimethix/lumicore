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
package com.github.epimethix.lumicore.common.ui;

import java.util.Locale;

import com.github.epimethix.lumicore.common.ui.labels.manager.DefaultLabelsManager;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsController;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManager;

@LabelsController("lumicore-labels")
public class C {
	private final static LabelsManager LABELS = new DefaultLabelsManager("lumicore-labels");
	public static final String CUT = "cut";
	public static final String COPY = "copy";
	public static final String PASTE = "paste";
	public static final String DLG_BTN_OK = "dlg-btn-ok";
	public static final String DLG_BTN_CANCEL = "dlg-btn-cancel";
	public static final String DLG_MSG_PARENT_MUST_BE_CHOSEN_FORMAT = "dlg-msg-parent-must-be-chosen-format";
	public static final String DLG_TITLE_CHOOSE_PARENT = "dlg-title-choose-parent";
	public static final String SEL_TOP = "sel-top";
	public static final String SEL_NO_SELECTION = "sel-no-selection";
	public static final String DLG_BTN_CHOOSE_SUPER = "dlg-btn-choose-super";
	public static final String ED_BTN_NEW = "ed-btn-new";
	public static final String ED_BTN_SAVE = "ed-btn-save";
	public static final String ED_BTN_DELETE = "ed-btn-delete";
	public static final String DLG_MSG_EDITOR_HAS_UNSAVED_CHANGES = "dlg-msg-editor-has-unsaved-changes";
	public static final String DLG_MSG_ENTRY_NOT_SAVED_YET = "dlg-msg-entry-not-saved-yet";
	public static final String DLG_MSG_PROCEED_DELETING_ENTRY = "dlg-msg-proceed-deleting-entry";
	public static final String DLG_MSG_COULD_NOT_DELETE_ITEM_FORMAT = "dlg-msg-could-not-delete-item-format";
	public static final String DLG_MSG_ENTRY_CAN_NOT_BE_DELETED = "dlg-msg-entry-can-not-be-deleted";
	public static final String CTRL_IF_FORMAT = "ctrl-if-format";
	public static final String FIELD_IS_REQUIRED = "field-is-required";
	public static final String DLG_TITLE_CHOOSE_CROP = "dlg-title-choose-crop";
	public static final String FILE_SELECT = "file-select";
	public static final String CHOOSE_FILE_BUTTON = "choose-file-button";
	public static final String STARTUP_MESSAGE = "startup-message";
	public static final String ALREADY_RUNNING_MESSAGE = "already-running-message";
	public static final String MENU_LANGUAGES = "menu-languages";
	public static final String FIELD_SELECTION_IS_NULL = "field-selection-is-null";
	public static final String DLG_BTN_CLEAR = "dlg-btn-clear";
	public static final String DLG_BTN_SELECT = "dlg-btn-select";
	public static final String DLG_SELECTION_TITLE = "dlg-selection-title";
	public static final String ED_BTN_CANCEL = "ed-btn-cancel";
	public static final String TREE_MENU_NEW = "tree-menu-new";
	public static final String TREE_MENU_DELETE = "tree-menu-delete";
	public static final String TREE_MENU_EDIT = "tree-menu-edit";
	public static final String DATA_TABLE_TITLE = "data-table-title";
	public static final String TEXT_VALIDATION_ERROR_EXACT_LENGTH = "text-validation-error-exact-length";
	public static final String TEXT_VALIDATION_ERROR_MIN_LENGTH = "text-validation-error-min-length";
	public static final String TEXT_VALIDATION_ERROR_MAX_LENGTH = "text-validation-error-max-length";
	public static final String DIALOG_TITLE_FILE_EXISTS = "dialog-title-file-exists";
	public static final String DIALOG_MESSAGE_FILE_EXISTS = "dialog-message-file-exists";
	public static final String BUTTON_CHOOSE = "button-choose";
	public static final String INTEGER_ERROR_MESSAGE_VALUE_IS_EMPTY = "INTEGER_ERROR_MESSAGE_VALUE_IS_EMPTY";
	public static final String INTEGER_ERROR_MESSAGE_VALUE_IS_NEGATIVE = "INTEGER_ERROR_MESSAGE_VALUE_IS_NEGATIVE";
	public static final String INTEGER_ERROR_MESSAGE_VALUE_IS_POSITIVE = "INTEGER_ERROR_MESSAGE_VALUE_IS_POSITIVE";
	public static final String INTEGER_ERROR_MESSAGE_VALUE_IS_GREATER_THAN_MAX = "INTEGER_ERROR_MESSAGE_VALUE_IS_GREATER_THAN_MAX";
	public static final String INTEGER_ERROR_MESSAGE_VALUE_IS_LESS_THAN_MIN = "INTEGER_ERROR_MESSAGE_VALUE_IS_LESS_THAN_MIN";
	public static final String FIELD_MAY_NOT_BE_EMPTY_ERROR = "FIELD_MAY_NOT_BE_EMPTY_ERROR";
	public static final String DATE_VALIDATION_ERROR = "DATE_VALIDATION_ERROR";
	public static final String PATH_FIELD_ERROR_WRONG_PARENT = "PATH_FIELD_ERROR_WRONG_PARENT";
	public static final String BUTTON_NEXT = "BUTTON_NEXT";
	public static final String BUTTON_PREVIOUS = "BUTTON_PREVIOUS";
	public static final String BUTTON_SKIP = "BUTTON_SKIP";
	public static final String BUTTON_FINISH = "BUTTON_FINISH";
	public static final String BUTTON_CANCEL = "BUTTON_CANCEL";
	public static final String BUTTON_OK = "BUTTON_OK";
	public static final String BUTTON_YES = "BUTTON_YES";
	public static final String BUTTON_NO = "BUTTON_NO";
	public static final String BUTTON_SAVE = "BUTTON_SAVE";
	public static final String BUTTON_DISCARD = "BUTTON_DISCARD";
	public static final String BUTTON_CONTINUE = "BUTTON_CONTINUE";
	public static final String BUTTON_ABORT = "BUTTON_ABORT";
	public static final String BUTTON_REPLACE = "BUTTON_REPLACE";
	public static final String BUTTON_RENAME = "BUTTON_RENAME";
	public static final String BUTTON_OPEN = "BUTTON_OPEN";
	public static final String BUTTON_EXIT = "BUTTON_EXIT";
	public static final String BUTTON_NEW = "BUTTON_NEW";
	public static final String BUTTON_CONNECT = "BUTTON_CONNECT";
	public static final String BUTTON_GENERATE = "BUTTON_GENERATE";
	public static final String CREATE_DIR_MESSAGE = "CREATE_DIR_MESSAGE";
	public static final String CREATE_DIR_MESSAGE_TITLE = "CREATE_DIR_MESSAGE_TITLE";
	public static final String CREATE_DIR_ERROR_EMPTY_NAME_TITLE = "CREATE_DIR_ERROR_EMPTY_NAME_TITLE";
	public static final String CREATE_DIR_ERROR_EMPTY_NAME = "CREATE_DIR_ERROR_EMPTY_NAME";
	public static final String CREATE_DIR_ALREADY_EXISTS_TITLE = "CREATE_DIR_ALREADY_EXISTS_TITLE";
	public static final String CREATE_DIR_ALREADY_EXISTS = "CREATE_DIR_ALREADY_EXISTS";
	public static final String DIR_CHOOSER_DIALOG_TITLE_FOR = "DIR_CHOOSER_DIALOG_TITLE_FOR";
	public static final String DIR_CHOOSER_DIALOG_TITLE = "DIR_CHOOSER_DIALOG_TITLE";
	public static final String BUTTON_SAVE_DOCUMENT = "BUTTON_SAVE_DOCUMENT";
	public static final String CHECKBOX_CODE_EXAMPLES = "CHECKBOX_CODE_EXAMPLES";
	public static final String CHECKBOX_REFERENCES = "CHECKBOX_REFERENCES";
	public static final String BUTTON_ADD = "BUTTON_ADD";
	public static final String BUTTON_REMOVE = "BUTTON_REMOVE";
	public static final String BUTTON_RELOAD = "BUTTON_RELOAD";
	public static final String CRYPTO_TITLE_SET_UP_SECRET = "CRYPTO_TITLE_SET_UP_SECRET";
	public static final String CRYPTO_ENTER_NEW_SECRET = "CRYPTO_ENTER_NEW_SECRET";
	public static final String CRYPTO_ENTER_NEW_SECRET_AGAIN = "CRYPTO_ENTER_NEW_SECRET_AGAIN";
	public static final String CRYPTO_ENTER_OLD_SECRET = "CRYPTO_ENTER_OLD_SECRET";
	public static final String CRYPTO_ENTER_SECRET = "CRYPTO_ENTER_SECRET";
	public static final String CRYPTO_TITLE_RESET_SECRET = "CRYPTO_TITLE_RESET_SECRET";
	public static final String CRYPTO_TITLE_GET_SECRET = "CRYPTO_TITLE_GET_SECRET";
	public static final String ERROR_MESSAGE_SECRETS_DONT_MATCH_TITLE = "ERROR_MESSAGE_SECRETS_DONT_MATCH_TITLE";
	public static final String ERROR_MESSAGE_SECRETS_DONT_MATCH = "ERROR_MESSAGE_SECRETS_DONT_MATCH";
	public static final String ERROR_MESSAGE_SECRET_INVALID = "ERROR_MESSAGE_SECRET_INVALID";
	public static final String ERROR_MESSAGE_SECRET_INVALID_TITLE = "ERROR_MESSAGE_SECRET_INVALID_TITLE";
	public static final String BOOLEAN_FIELD_MUST_BE_CHECKED = "BOOLEAN_FIELD_MUST_BE_CHECKED";
	public static final String CRYPTO_USER_NAME = "CRYPTO_USER_NAME";
	public static final String MENU_THEMES = "MENU_THEMES";
	public static final String MENU_THEME_LIGHT = "MENU_THEME_LIGHT";
	public static final String MENU_THEME_DARK = "MENU_THEME_DARK";

//	public static final LabelsManager getLabelsManager() {
//		return LABELS;
//	}

	public static String getLabel(String key, Object... args) {
		return LABELS.getLabel(key, args);
	}

	public static String getLabel(Locale locale, String key, Object... args) {
		return LABELS.getLabel(locale, key, args);
	}

	private C() {}
}
