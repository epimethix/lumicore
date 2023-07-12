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
package com.github.epimethix.accounting.gui;

import javax.swing.tree.DefaultMutableTreeNode;

import com.github.epimethix.accounting.db.model.Account;
import com.github.epimethix.accounting.db.model.AccountManagerImpl;
import com.github.epimethix.accounting.db.model.Bank;
import com.github.epimethix.accounting.db.model.Category;
import com.github.epimethix.accounting.db.model.Transaction;
import com.github.epimethix.lumicore.ioc.annotation.SwingComponent;
import com.github.epimethix.lumicore.swing.entityaccess.AbstractEntityAccessBrowser;

@SuppressWarnings("serial")
@SwingComponent
public class ExamleEntityAccessBrowser extends AbstractEntityAccessBrowser {
	@Override
	protected void buildMenuTree(DefaultMutableTreeNode rootNode) {
		addEntityAccessView(L.NODE_TRANSACTIONS, Transaction.class, rootNode);
		DefaultMutableTreeNode categoryOne = addNode(L.NODE_CAT_ONE);
		addEntityAccessView(L.NODE_ACCOUNT, Account.class, categoryOne);
		addEntityAccessView(L.NODE_ACCOUNT_MANAGER, AccountManagerImpl.class, categoryOne);
		addEntityAccessView(L.NODE_BANK, Bank.class, categoryOne);
		DefaultMutableTreeNode categoryTwo = addNode(L.NODE_CAT_TWO);
		addEntityAccessView(L.NODE_CATEGORY, Category.class, categoryTwo);
	}
}
