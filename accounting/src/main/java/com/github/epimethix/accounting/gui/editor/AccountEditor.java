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

import com.github.epimethix.accounting.db.model.Account;
import com.github.epimethix.accounting.db.model.AccountManagerImpl;
import com.github.epimethix.accounting.db.model.Bank;
import com.github.epimethix.accounting.db.repository.AccountRepository;
import com.github.epimethix.accounting.gui.L;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.swing.editor.EntityEditorPanel;

@SuppressWarnings("serial")
public class AccountEditor extends EntityEditorPanel<Account, Long>{

	public AccountEditor(SwingUI ui, AccountRepository repository) {
		super(ui, repository);

//		@PrimaryKey
//		private final Long id;
//		private final String name;
		addTextField(L.ACCOUNT_NAME, "name", true);
//		private final String BIC;
		addTextField(L.ACCOUNT_BIC, "BIC", true);
//		private final String IBAN;
		addTextField(L.ACCOUNT_IBAN, "IBAN", true);
//		@ManyToOne
//		private final AccountManager accountManager;
		addToOneField(L.ACCOUNT_ACCOUNT_MANAGER, "accountManager", true, AccountManagerImpl.class);
//		@ManyToOne
//		private final Bank bank;
		addToOneField(L.ACCOUNT_BANK, "bank", true, Bank.class);
//		private BigDecimal balance;
		// TODO DBDecimalField
	}
	
	

}
