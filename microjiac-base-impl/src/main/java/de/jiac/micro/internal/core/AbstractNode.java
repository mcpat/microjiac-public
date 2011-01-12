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
 * $Id$ 
 */
package de.jiac.micro.internal.core;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.core.INode;
import de.jiac.micro.core.IScope;
import de.jiac.micro.core.LifecycleHandler;
import de.jiac.micro.core.LifecycleHandler.LifecycleState;
import de.jiac.micro.core.LifecycleHandler.ListableLifecycleContext;
import de.jiac.micro.core.scope.AgentScope;
import de.jiac.micro.util.List;
import de.jiac.micro.util.List.Node;

/**
 * @author Erdene-Ochir Tuguldur
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public abstract class AbstractNode extends AbstractContainer implements INode, IScope.IScopeStateChangeListener {

	protected final static class ComponentContext extends ListableLifecycleContext {
        public Object handle;

        public AbstractNodeComponent getComponent() {
            return (AbstractNodeComponent) value();
        }
    }


    /**
     * List&lt;ComponentContext&gt;
     */
    private final List _nodeComponents;

    /**
     * List&lt;AgentScope&gt;
     */
    private final List _agentScopes;

    protected AbstractNode() {
        _nodeComponents = new List() {
            protected Node newNode() {
                return new ComponentContext();
            }
        };
        _agentScopes = new List();
    }

    public void setNodeComponents(AbstractNodeComponent[] nodeComponents) {
        if (nodeComponents != null && _nodeComponents.size() <= 0) {
            for (int i = 0; i < nodeComponents.length; ++i) {
                addNodeComponent(nodeComponents[i]);
            }
        }
    }

    public void initialise() {
        getLogger().debug("initialising node");
        for (Node n = _nodeComponents.head(), end = _nodeComponents.tail(); (n = n.next()) != end;) {
            ComponentContext context = (ComponentContext) n;
            initialiseComponent(context);
        }

        AbstractNodeConfiguration config= (AbstractNodeConfiguration) getHandle(AbstractNodeConfiguration.class);
        final String[] initialAgentConfigurations = config.agentClassNames;
        for (int i = 0; i < initialAgentConfigurations.length; i++) {
            String agentConfigurationName = initialAgentConfigurations[i];
            final AgentScope agentScope = new AgentScope();
            _agentScopes.addLast(agentScope);
            agentScope.setup(getClassLoaderForAgent(null), agentConfigurationName, this);
        }

        traverseAgentScopes(IScope.SIG_INITIALISE);
        getLogger().debug("node initialised");
    }

    public void start() {
        getLogger().debug("starting node");
        for (Node n = _nodeComponents.head(), end = _nodeComponents.tail(); (n = n.next()) != end;) {
            ComponentContext context = (ComponentContext) n;
            startComponent(context);
        }

        traverseAgentScopes(IScope.SIG_START);
        getLogger().debug("node started");
    }

    public void stop() {
        getLogger().debug("stopping node");
        traverseAgentScopes(IScope.SIG_PAUSE);
        
        for (Node n = _nodeComponents.head(), end = _nodeComponents.tail(); (n = n.next()) != end;) {
            ComponentContext context = (ComponentContext) n;
            startComponent(context);
        }
        getLogger().debug("node stopped");
    }

    public void cleanup() {
        getLogger().debug("cleaning up node");
        traverseAgentScopes(IScope.SIG_TERMINATE);
        
        synchronized(_agentScopes) {
            long maxblock= System.currentTimeMillis() + 3000;
            while(_agentScopes.size() > 0) {
                long currentTime= System.currentTimeMillis();
                
                if(currentTime >= maxblock) {
                    break;
                }
                
                try {
                    _agentScopes.wait(maxblock - currentTime);
                } catch (InterruptedException e) {
                    getLogger().debug("waiting for agents to clean up", e);
                }
            }
        }
        
        for (Node head = _nodeComponents.head(), end = _nodeComponents.tail(), n; (n = head.next()) != end;) {
            ComponentContext context = (ComponentContext) n;
            cleanupComponent(context);
            _nodeComponents.delete(context);
        }
        
        getLogger().debug("node cleaned up");
    }
    
    protected abstract IClassLoader getClassLoaderForAgent(String[] classPath);

    protected void addNodeComponent(AbstractNodeComponent nodeComponent) {
        _nodeComponents.addLast(nodeComponent);
    }

    protected void removeNodeComponent(AbstractNodeComponent nodeComponent) {
        _nodeComponents.remove(nodeComponent);
    }

    protected void initialiseComponent(ComponentContext context) {
        LifecycleHandler.makeTransitionsTo(context, LifecycleState.INITIALISED, getLogger());
        
        context.handle= context.getComponent().getNodeHandle();
        if(context.handle != null) {
            handles.addFirst(context.handle);
        }
    }

    protected void startComponent(ComponentContext context) {
        LifecycleHandler.makeTransitionsTo(context, LifecycleState.ACTIVE, getLogger());
    }

    protected void stopComponent(ComponentContext context) {
        LifecycleHandler.makeTransitionsTo(context, LifecycleState.INITIALISED, getLogger());
    }

    protected void cleanupComponent(ComponentContext context) {
        if(context.handle != null) {
            handles.remove(context.handle);
            context.handle= null;
        }
        LifecycleHandler.makeTransitionsTo(context, LifecycleState.DESTROYED, getLogger());
    }

    private void traverseAgentScopes(byte signal) {
        for (Node n = _agentScopes.head(), end = _agentScopes.tail(); (n = n.next()) != end;) {
            AgentScope agentScope = (AgentScope) n.value();
            agentScope.signal(signal);
        }
    }

	public void onSetup(IScope scope) {
		for (Node n = _nodeComponents.head(), end = _nodeComponents.tail(); (n = n.next()) != end;) {
			ComponentContext context = (ComponentContext) n;
            context.getComponent().addHandlesOn((AgentScope)scope);
        }
	}
	
	public void onStart(IScope scope) {
        // TODO Auto-generated method stub
        
    }

    public void onTerminate(IScope scope) {
        synchronized(_agentScopes) {
            if(!_agentScopes.remove(scope)) {
                throw new Error("Invalid state of node");
            }
            
            for (Node n = _nodeComponents.head(), end = _nodeComponents.tail(); (n = n.next()) != end;) {
                ComponentContext context = (ComponentContext) n;
                context.getComponent().removeHandlesFrom((AgentScope)scope);
            }
            
            _agentScopes.notify();
        }
	}
	
	public void onPause(IScope scope) {
	}
}
