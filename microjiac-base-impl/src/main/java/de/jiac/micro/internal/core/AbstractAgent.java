/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Base-Implementation.
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
package de.jiac.micro.internal.core;

import java.util.Hashtable;

import org.slf4j.Logger;

import de.jiac.micro.agent.IActuator;
import de.jiac.micro.agent.IAgentElement;
import de.jiac.micro.agent.IConnectionFactory;
import de.jiac.micro.agent.ISensor;
import de.jiac.micro.agent.memory.IShortTermMemory;
import de.jiac.micro.core.IAgent;
import de.jiac.micro.core.IHandle;
import de.jiac.micro.core.LifecycleHandler;
import de.jiac.micro.core.LifecycleHandler.LifecycleState;
import de.jiac.micro.core.LifecycleHandler.ListableLifecycleContext;
import de.jiac.micro.util.List;
import de.jiac.micro.util.List.Node;

/**
 * @author Erdene-Ochir Tuguldur
 * @author Marcel Patzlaff
 */
public abstract class AbstractAgent extends AbstractContainer implements IAgent {
    protected static class ElementContext extends ListableLifecycleContext {
        public String id = null;
        public String scheme = null;
        public IHandle handle = null;

        public IAgentElement getElement() {
            return (IAgentElement) value();
        }
    }

    protected List installedElements;
    
    protected Hashtable properties;

    public AbstractAgent() {
        installedElements = new List() {
            protected Node newNode() {
                return new ElementContext();
            }
        };
        
        properties= new Hashtable();
    }
    
    public final Object getProperty(String key) {
        //ensureNotDestroyed();
        return properties.get(key);
    }
    
    public final void setProperty(String key, Object value) {
        //ensureNotDestroyed();
        if (value == null) {
            properties.remove(key);
        } else {
            Class expected= null;
            Object oldValue= properties.get(key);
            if (oldValue != null) {
                expected= oldValue.getClass();
            }

            if (expected != null && value.getClass() != expected) {
            	Logger logger= getLogger();
                if (logger.isWarnEnabled()) {
                    logger.warn("ignore property change of '" + key + "': " + expected + " required but got "
                            + value.getClass() + "");
                }
                return;
            }

            properties.put(key, value);
        }
    }

    public void initialise() {
        for (Node n = installedElements.head(), end = installedElements.tail(); (n = n.next()) != end;) {
            ElementContext context = (ElementContext) n;
            initialiseElement(context);
        }
    }

    public void start() {
        for (Node n = installedElements.head(), end = installedElements.tail(); (n = n.next()) != end;) {
            ElementContext context = (ElementContext) n;
            startElement(context);
        }
    }

    public void stop() {
        for (Node n = installedElements.head(), end = installedElements.tail(); (n = n.next()) != end;) {
            ElementContext context = (ElementContext) n;
            stopElement(context);
        }
    }

    public void cleanup() {
        for (Node head = installedElements.head(), end = installedElements.tail(), n; (n = head.next()) != end;) {
            ElementContext context = (ElementContext) n;
            cleanupElement(context);
            installedElements.delete(context);
        }
    }

    public void addAgentElement(IAgentElement agentElement) {
        if (findElementContext(agentElement) != null) {
            return;
        }
        installedElements.addLast(agentElement);
    }

    private final ElementContext findElementContext(IAgentElement element) {
        for (Node n = installedElements.head(), end = installedElements.tail(); (n = n.next()) != end;) {
            ElementContext context = (ElementContext) n;
            if (context.getElement() == element) {
                return context;
            }
        }
        return null;
    }

    protected void initialiseElement(ElementContext context) {
        IAgentElement element = context.getElement();
        if(element instanceof ISensor) {
            ((ISensor) element).setShortTermMemory(getShortTermMemory());
        }
        
        LifecycleHandler.makeTransitionsTo(context, LifecycleState.INITIALISED, getLogger());

        if (element instanceof IActuator) {
            context.handle = ((IActuator) element).getHandle();
            if (context.handle != null) {
                handles.addFirst(context.handle);
            }
        }

        if (element instanceof IConnectionFactory) {
            context.scheme = ((IConnectionFactory) element).getScheme();
        }
    }

    protected void startElement(ElementContext context) {
        LifecycleHandler.makeTransitionsTo(context, LifecycleState.ACTIVE, getLogger());
    }

    protected void stopElement(ElementContext context) {
        LifecycleHandler.makeTransitionsTo(context, LifecycleState.INITIALISED, getLogger());
    }

    protected void cleanupElement(ElementContext context) {
        context.scheme = null;


        if (context.handle != null) {
            handles.remove(context.handle);
            context.handle = null;
        }
        
        LifecycleHandler.makeTransitionsTo(context, LifecycleState.DESTROYED, getLogger());
    }
    
    protected abstract IShortTermMemory getShortTermMemory();
}
