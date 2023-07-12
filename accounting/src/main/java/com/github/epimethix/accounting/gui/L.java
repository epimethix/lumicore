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

import com.github.epimethix.lumicore.common.ui.labels.manager.DefaultLabelsManager;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsController;
import com.github.epimethix.lumicore.common.ui.labels.manager.LabelsManager;

@LabelsController("labels")
public class L {
	private final static LabelsManager LABELS = new DefaultLabelsManager();
	public static final String NODE_CAT_ONE = "node-cat-one";
	public static final String NODE_CAT_TWO = "node-cat-two";
	public static final String APPLICATION_TITLE = "application-title";
	public static final String ACCOUNT_NAME = "ACCOUNT_NAME";
	public static final String ACCOUNT_BIC = "ACCOUNT_BIC";
	public static final String ACCOUNT_IBAN = "ACCOUNT_IBAN";
	public static final String ACCOUNT_ACCOUNT_MANAGER = "ACCOUNT_ACCOUNT_MANAGER";
	public static final String ACCOUNT_BANK = "ACCOUNT_BANK";
	public static final String ACCOUNT_MANAGER_NAME = "ACCOUNT_MANAGER_NAME";
	public static final String ACCOUNT_MANAGER_EMAIL = "ACCOUNT_MANAGER_EMAIL";
	public static final String ACCOUNT_MANAGER_PHONE = "ACCOUNT_MANAGER_PHONE";
	public static final String BANK_NAME = "BANK_NAME";
	public static final String BANK_BANK_CODE = "BANK_BANK_CODE";
	public static final String CATEGORY_PARENT = "CATEGORY_PARENT";
	public static final String CATEGORY_NAME = "CATEGORY_NAME";
	public static final String CATEGORY_DESCRIPTION = "CATEGORY_DESCRIPTION";
	public static final String TRANSACTION_DIRECTION = "TRANSACTION_DIRECTION";
	public static final String TRANSACTION_EXPENSE = "TRANSACTION_EXPENSE";
	public static final String TRANSACTION_REVENUE = "TRANSACTION_REVENUE";
	public static final String TRANSACTION_BOOKING_TEXT = "TRANSACTION_BOOKING_TEXT";
	public static final String TRANSACTION_CATEGORY = "TRANSACTION_CATEGORY";
	public static final String TRANSACTION_ACCOUNT = "TRANSACTION_ACCOUNT";
	public static final String TRANSACTION_TIME = "TRANSACTION_TIME";
	public static final String TRANSACTION_AMOUNT = "TRANSACTION_AMOUNT";
	public static final String NODE_TRANSACTIONS = "NODE_TRANSACTIONS";
	public static final String NODE_ACCOUNT = "NODE_ACCOUNT";
	public static final String NODE_ACCOUNT_MANAGER = "NODE_ACCOUNT_MANAGER";
	public static final String NODE_BANK = "NODE_BANK";
	public static final String NODE_CATEGORY = "NODE_CATEGORY";
	public static final String TEST_1 = "TEST_1";
	public static final String TEST_2 = "TEST_2";
	public static final String TEST_3 = "TEST_3";

	public static String getLabel(String key, Object... args) {
		return LABELS.getLabel(key, args);
	}

	private L() {}
}
