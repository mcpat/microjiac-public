/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Base-Implementation.
 *
 * Copyright (c) 2007-2011 DAI-Labor, Technische Universität Berlin
 *
 * This library includes software developed at DAI-Labor, Technische
 * Universität Berlin (http://www.dai-labor.de)
 *
 * This library is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

/* 
* $Id: IUnicastAddress.java 28614 2010-08-11 09:37:13Z marcel $
*/ 
package de.jiac.micro.core.io;

/**
 * The {@code IUnicastAddress} interface represents a unicast address. 
 * @author Vladimir Sch&ouml;ner
 */
public interface IUnicastAddress extends IAddress {

	/**
	 * Returns the {@code String} representation for the target or source selector,
	 * depending on in which context the {@code IUnicastAddress} used for.
	 * @return the target or source selector.
	 */
	String getSelector();
	
	/**
	 * Returns the {@code String} representation for the target or source id.
	 * @return the target or source id.
	 */
	String getTargetId();
}