/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC SunSPOT-Extensions.
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
 * $Id$ 
 */
package de.jiac.micro.sunspot.aodv;

import java.io.IOException;

import com.github.libxjava.io.BufferedInputStream;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public final class MessageInputStream extends BufferedInputStream {
    private final ProtocolManager _protocol;
    
    private MessageID _mid;
    
    public MessageInputStream(ProtocolManager protocol) {
        super(256);
        _protocol= protocol;
    }

    public void setMessageID(MessageID mid) {
        _mid= mid;
    }
    
    protected int internalRead(byte[] buffer) throws IOException {
        return _protocol.readMessageFragment(_mid, buffer);
    }
}
