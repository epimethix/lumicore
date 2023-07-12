/*
 *  RemoteAI UI - Lumicore example application
 *  Copyright (C) 2023  epimethix@protonmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.epimethix.lumicore.remoteaiui.ui;

import com.github.epimethix.lumicore.common.ui.labels.manager.DefaultLabelsManager;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsController;

@LabelsController("labels")
public final class L {

	public static final String FRAME_APP_TITLE = "FRAME_APP_TITLE";
	public static final String DIALOG_SETUP_TITLE = "DIALOG_SETUP_TITLE";
	public static final String DIALOG_SETUP_API_KEY_TITLE = "DIALOG_SETUP_API_KEY_TITLE";
	public static final String DIALOG_SETUP_ERROR_MSG_API_KEY_IS_EMPTY = "DIALOG_SETUP_ERROR_MSG_API_KEY_IS_EMPTY";
	public static final String DIALOG_SETUP_TEXT_OUTPUT_DIR_TITLE = "DIALOG_SETUP_TEXT_OUTPUT_DIR_TITLE";
	public static final String DIALOG_SETUP_IMAGE_OUTPUT_DIR_TITLE = "DIALOG_SETUP_IMAGE_OUTPUT_DIR_TITLE";
	public static final String DIALOG_SETUP_FINISHED_TITLE = "DIALOG_SETUP_FINISHED_TITLE";
	public static final String MENU_FILE = "MENU_FILE";
	public static final String MENU_EXIT = "MENU_EXIT";
	public static final String MENU_RE_RUN_SETUP = "MENU_RE_RUN_SETUP";

	private final static DefaultLabelsManager LABELS = new DefaultLabelsManager();

	public static String getLabel(String key, Object... args) {
		return LABELS.getLabel(key, args);
	}

	private L() {}
}
