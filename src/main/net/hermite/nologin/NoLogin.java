/*
 * Copyright 2015 Lifok
 *
 * This file is part of NoLogin.

 * NoLogin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NoLogin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NoLogin.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.hermite.nologin;

import lombok.Getter;
import net.hermite.nologin.account.AccountManager;
import net.hermite.nologin.auth.Validator;

public class NoLogin 
{
	@Getter
	private AccountManager accountManager = new AccountManager();
	@Getter
	private Validator validator = new Validator();
}
