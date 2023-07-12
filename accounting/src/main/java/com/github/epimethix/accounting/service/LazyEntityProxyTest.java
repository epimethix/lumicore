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
package com.github.epimethix.accounting.service;

import com.github.epimethix.accounting.db.repository.AccountManagerRepositoryImpl;
import com.github.epimethix.lumicore.ioc.annotation.Autowired;
import com.github.epimethix.lumicore.ioc.annotation.PostConstruct;
import com.github.epimethix.lumicore.ioc.annotation.Service;

@Service
public class LazyEntityProxyTest {

	@Autowired
	private AccountManagerRepositoryImpl accountManagerRepository;

	public LazyEntityProxyTest() {}

	@PostConstruct
	public void init() {
//		LazyAccountManager am = accountManagerRepository.lazyEntityTest(1L);
//		System.err.println(am.getName());
//		AccountManager am2 = (AccountManager) am;
//		System.err.println(am2.getName());
	}
}
