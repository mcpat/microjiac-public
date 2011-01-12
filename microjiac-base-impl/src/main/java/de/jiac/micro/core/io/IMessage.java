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
 * $Id: IMessage.java 28614 2010-08-11 09:37:13Z marcel $
 */
package de.jiac.micro.core.io;

import java.util.Enumeration;

import de.jiac.micro.agent.memory.IFact;

/**
 * The {@code IMessage} interface introduces the features for a application message. 
 * With the aid of this interface the application is able to set the message content or the message header.
 * It's also possible to read the content from the message and evaluate it.  
 * 
 * @author Vladimir Sch&ouml;ner
 */
public interface IMessage extends IFact {
	interface DefaultHeader {
		/**
		 * Key for the {@code source-address} header. Is set by the transport
		 * layer
		 */
		String SOURCE_ADDRESS = "source-address";

		/**
		 * Key for the {@code target-address} header.
		 */
		String TARGET_ADDRESS = "target-address";

		/**
		 * Key for the optional {@code content-type} header.
		 */
		String CONTENT_TYPE = "content-type";
		/**
		 * Key for the {@code message-number} header. The application-developer has
		 * no permission to modify this key.
		 */
		String MESSAGE_NUMBER = "message-number"; 

	}
	
	/**
	 * The setter method for the header.
	 * @param key the key for that header.
	 * @param value the value for the header.
	 */
	void setHeader(String key, String value);

	/**
	 * The setter method for the header. 
	 * @param content the content for the header.
	 */
	void setContent(Object content);

	/**
	 * The getter method for all keys which exists in the header.
	 * @return the value for all header keys.
	 */
	Enumeration getHeaderKeys();

	/**
	 * The getter method for message content.
	 * @return the message content.
	 */
	Object getContent();

	/**
	 * The getter method for the given key.
	 * @param key the key for that header.
	 * @return the value for the given key.
	 */
	String getHeader(String key);
}
