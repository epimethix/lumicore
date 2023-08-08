/*
 *  Accounting - Lumicore example application
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
package com.github.epimethix.accounting.gui.editor;

import com.github.epimethix.accounting.db.model.Bank;
import com.github.epimethix.accounting.db.repository.BankRepository;
import com.github.epimethix.accounting.gui.L;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.swing.editor.EntityEditorPanel;

@SuppressWarnings("serial")
public class BankEditor extends EntityEditorPanel<Bank, Long>{

	public BankEditor(SwingUI ui, BankRepository repository) {
		super(ui, repository);

//		@PrimaryKey
//		private final Long id;
//		private final String name;
		addTextField(L.BANK_NAME, "name", true);
//		private final String bankCode;
		addTextField(L.BANK_BANK_CODE, "bankCode", false);
	}
	
	

}
