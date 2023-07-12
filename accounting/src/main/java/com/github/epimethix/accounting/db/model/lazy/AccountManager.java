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
package com.github.epimethix.accounting.db.model.lazy;

import com.github.epimethix.accounting.db.model.AccountManagerImpl;
import com.github.epimethix.lumicore.common.orm.model.LazyEntity;
import com.github.epimethix.lumicore.orm.annotation.entity.ImplementationClass;


@ImplementationClass(AccountManagerImpl.class)
public interface AccountManager extends LazyEntity<Long> {

	String getName();

	String getEmail();

	String getPhone();
}
