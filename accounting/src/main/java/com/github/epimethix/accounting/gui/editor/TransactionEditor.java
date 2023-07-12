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

import java.math.RoundingMode;

import com.github.epimethix.accounting.db.model.Account;
import com.github.epimethix.accounting.db.model.Category;
import com.github.epimethix.accounting.db.model.Transaction;
import com.github.epimethix.accounting.db.model.Transaction.Direction;
import com.github.epimethix.accounting.db.repository.TransactionRepository;
import com.github.epimethix.accounting.gui.L;
import com.github.epimethix.lumicore.common.DateTime;
import com.github.epimethix.lumicore.common.swing.SwingUI;
import com.github.epimethix.lumicore.swing.editor.AbstractEditorPanel;

@SuppressWarnings("serial")
public class TransactionEditor extends AbstractEditorPanel<Transaction, Long> {

	public TransactionEditor(SwingUI ui, TransactionRepository repository) {
		super(ui, repository);

//		private final Long id;
//		private final Direction direction;
		addEnumRadioPicker(L.TRANSACTION_DIRECTION, "direction", true, Direction.class, (val) -> {
			switch (val) {
			case EXPENSE:
				return L.getLabel(L.TRANSACTION_EXPENSE);
			default:
				return L.getLabel(L.TRANSACTION_REVENUE);
			}
		}, 2);
//		private final LocalDateTime time;
		addDateTimeField(L.TRANSACTION_TIME, "time", true, DateTime.DEFAULT_DATE_TIME_FORMAT);
//		private final BigDecimal amount;
		// TODO Decimal Field
		addBigDecimalField(L.TRANSACTION_AMOUNT, "amount", 2, RoundingMode.HALF_DOWN, true);
//		private final String bookingText;
		addTextField(L.TRANSACTION_BOOKING_TEXT, "bookingText", true);
//		@ManyToOne
//		private final Category category;
		addToOneField(L.TRANSACTION_CATEGORY, "category", true, Category.class);
//		@ManyToOne
//		private final Account account;
		addToOneField(L.TRANSACTION_ACCOUNT, "account", true, Account.class);

	}

}
