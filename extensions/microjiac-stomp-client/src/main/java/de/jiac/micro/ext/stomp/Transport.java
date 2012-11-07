/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC STOMP-Client.
 *
 * Copyright (c) 2007-2012 DAI-Labor, Technische Universität Berlin
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
 * $Id: Transport.java 23214 2009-05-06 12:18:42Z marcel $ 
 */
package de.jiac.micro.ext.stomp;

import java.io.IOException;

import org.slf4j.Logger;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.core.io.IMessage;


/**
 * @author Marcel Patzlaff
 * @version $Revision: 23214 $
 */
public abstract class Transport {
    public static interface ITransportDelegate {
        void onMessage(Transport source, IMessage message, String destName);
        void onError(Transport source, Object error);
        IClassLoader getClassLoader();
        Logger getLogger();
    }
    
    protected ITransportDelegate delegate;
    
    public final void setTransportDelegate(ITransportDelegate delegate) {
        this.delegate= delegate;
    }
    
    public abstract void doStart() throws IOException;
    public abstract void doStop() throws IOException;
    
    public abstract void doRegister(String prefix, String name) throws IOException;
    public abstract void doUnregister(String prefix, String name) throws IOException;
    public abstract void doSend(IMessage message) throws IOException;
}
