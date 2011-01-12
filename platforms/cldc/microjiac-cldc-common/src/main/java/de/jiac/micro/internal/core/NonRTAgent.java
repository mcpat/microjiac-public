/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC CLDC-Common.
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

package de.jiac.micro.internal.core;

import java.io.IOException;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;

import org.slf4j.Logger;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.agent.IConnectionFactory;
import de.jiac.micro.agent.memory.IShortTermMemory;
import de.jiac.micro.core.feature.PrimitiveScheduler;
import de.jiac.micro.core.handle.IResourceHandle;
import de.jiac.micro.core.io.IStreamConnection;
import de.jiac.micro.internal.agent.SimpleLongTermMemory;
import de.jiac.micro.internal.io.ConnectionMapperFactory;
import de.jiac.micro.util.List.Node;


/**
 * @author Erdene-Ochir Tuguldur
 * @author Marcel Patzlaff
 *
 */
public class NonRTAgent extends AbstractAgent {
	protected class AgentAccess implements IResourceHandle {
        public AgentAccess() {}
        
        public synchronized void close(Connection con) {

            try {
                if(con != null) {
                    con.close();
                }
            } catch (IOException ioe) {
                Logger logger= getLogger();

                if (logger.isWarnEnabled()) {
                    logger.warn("error while closing connection", ioe);
                }
            }
//            context.connections.remove(con);
        }

        public synchronized Connection open(String url) throws IOException {
            return open(url, false);
        }

        public synchronized IStreamConnection openStreamConnection(String url) throws IOException {
            return (IStreamConnection) open(url, true);
        }
        
        private Connection open(String url, boolean stream) throws IOException {
            // first try to open a connection locally
            Connection c= establishConnection(url);

            if (c == null) {
                // now try global connection
                c= Connector.open(url);
            }
            
            if (stream) {
                Connection unmapped= c;
                IClassLoader classLoader= getClassLoader();
                try {
                    c= ConnectionMapperFactory.getStreamConnection(classLoader, url, c);
                } catch (IOException ioe) {
                    close(unmapped);
                    throw ioe;
                }
            }

//            if (!context.connections.contains(c)) {
//                context.connections.addLast(c);
//            }

            return c;
        }
    }
	
    private PrimitiveScheduler _scheduler;
    
    public NonRTAgent() {
        super();
    }

	public void cleanup() {
        super.cleanup();
        removeHandle(_scheduler);
        _scheduler= null;
    }

    public void initialise() {
        _scheduler= new PrimitiveScheduler(getLogger());
        addHandle(_scheduler);
        addHandle(new SimpleLongTermMemory());
        addHandle(new AgentAccess());
        super.initialise();
    }

    public void start() {
        _scheduler.start();
        super.start();
    }

    public void stop() {
        super.stop();
        _scheduler.stop();
    }

    /* package */final synchronized Connection establishConnection(String uri) throws IOException {
    	int colon= uri.indexOf(':');
    	if (colon <= 0) {
    		return null;
    	}
    	
    	String scheme= uri.substring(0, colon);
    	
    	for(Node n= installedElements.head(), end= installedElements.tail(); (n= n.next()) != end;) {
    		ElementContext context= (ElementContext) n;
    		if (context.scheme != null && context.scheme.equalsIgnoreCase(scheme)) {
    			IConnectionFactory factory= (IConnectionFactory) context.getElement();
    			Connection c= factory.openConnection(uri);
    			if (c != null) {
    				return c;
    			}
    		}
    	}
    	return null;
    }

    protected final IShortTermMemory getShortTermMemory() {
        return _scheduler.getShortTermMemory();
    }
}
