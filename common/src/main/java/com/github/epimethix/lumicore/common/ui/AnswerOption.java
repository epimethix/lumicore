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

public enum AnswerOption {
	OK(Answer.OK), 
	
	OK_CANCEL(Answer.OK, Answer.CANCEL), 
	
	NEW_OK_CANCEL(Answer.NEW, Answer.OK, Answer.CANCEL), 
	
	YES_NO(Answer.YES, Answer.NO),
	
	YES_NO_CANCEL(Answer.YES, Answer.NO, Answer.CANCEL), 
	
	SAVE_DISCARD(Answer.SAVE, Answer.DISCARD),
	
	SAVE_CANCEL(Answer.SAVE, Answer.CANCEL), 
	
	SAVE_DISCARD_CANCEL(Answer.SAVE, Answer.DISCARD, Answer.CANCEL),

	OPEN_CANCEL(Answer.OPEN, Answer.CANCEL),

	CONNECT_CANCEL(Answer.CONNECT, Answer.CANCEL),
	
	CONTINUE_CANCEL(Answer.CONTINUE, Answer.CANCEL), 
	
	CONTINUE_SKIP_CANCEL(Answer.CONTINUE, Answer.SKIP, Answer.CANCEL),
	
	REPLACE_RENAME_SKIP_CANCEL(Answer.REPLACE, Answer.RENAME, Answer.SKIP, Answer.CANCEL), 
	
	NEXT_PREVIOUS_CANCEL(Answer.NEXT, Answer.PREVIOUS, Answer.CANCEL), 
	
	NEXT_SKIP_PREVIOUS_CANCEL(Answer.NEXT, Answer.SKIP, Answer.PREVIOUS, Answer.CANCEL), 
	
	EXIT_CANCEL(Answer.EXIT, Answer.CANCEL);

	private final Answer[] answers;

	private AnswerOption(Answer... answers) {
		this.answers = answers;
	}
	
	public Answer[] getAnswers() {
		return answers;
	}
}