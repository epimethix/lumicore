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

public enum Answer {
	OK(C.BUTTON_OK), CANCEL(C.BUTTON_CANCEL), NEW(C.BUTTON_NEW), YES(C.BUTTON_YES), NO(C.BUTTON_NO),
	SAVE(C.BUTTON_SAVE), DISCARD(C.BUTTON_DISCARD), OPEN(C.BUTTON_OPEN), CONTINUE(C.BUTTON_CONTINUE),
	REPLACE(C.BUTTON_REPLACE), RENAME(C.BUTTON_RENAME), SKIP(C.BUTTON_SKIP), NEXT(C.BUTTON_NEXT),
	PREVIOUS(C.BUTTON_PREVIOUS), EXIT(C.BUTTON_EXIT), CONNECT(C.BUTTON_CONNECT), FINISH(C.BUTTON_FINISH),
	CLEAR(C.BUTTON_CLEAR), SELECT(C.BUTTON_SELECT);

	private final String labelKey;

	private Answer(String labelKey) {
		this.labelKey = labelKey;
	}

	public String getLabelKey() {
		return labelKey;
	}
}