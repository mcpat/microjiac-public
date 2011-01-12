/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC MIDlet-Launcher.
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
package de.jiac.micro.internal;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.github.libxjava.lang.IClassLoader;
import com.github.libxjava.lang.SimpleClassLoader;

import de.jiac.micro.core.IHandle;
import de.jiac.micro.core.IScope;

/**
 * Launcher for MIDlet-based JAVA editions.
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public final class NodeLauncher extends MIDlet implements IHandle, IScope.IScopeStateChangeListener {
    public static final String NODE_CONFIGURATION_KEY= "MicroJIAC-Node-Configuration";
    
    private IScope _nodeScope= null;
    private final IClassLoader _classLoader;
    
    public NodeLauncher() {
        _classLoader= new SimpleClassLoader();
    }
    
    protected synchronized void destroyApp(boolean unconditional) {
        _nodeScope.signal(IScope.SIG_TERMINATE);
    }

    protected synchronized void pauseApp() {
        _nodeScope.signal(IScope.SIG_PAUSE);
    }

    protected synchronized void startApp() throws MIDletStateChangeException {
        if(_nodeScope == null) {
            String className= getAppProperty(NODE_CONFIGURATION_KEY);
            
            if(className == null) {
                throw new MIDletStateChangeException("node configuration is not specified");
            }
            
            Class nodeScopeClass;
            try {
                nodeScopeClass = _classLoader.loadClass(IScope.NODE_SCOPE_CLASS);
                _nodeScope= (IScope) nodeScopeClass.newInstance();
                _nodeScope.setup(_classLoader,className, this);
            } catch (Exception e) {
                throw new MIDletStateChangeException(e.getMessage());
            }
        }
        
        _nodeScope.signal(IScope.SIG_START);
    }

    public void onSetup(IScope scope) {
        scope.getContainerReference().addHandle(this);
    }
    
    public void onStart(IScope scope) {
        // TODO Auto-generated method stub
    }

    public synchronized void onTerminate(IScope scope) {
        _nodeScope= null;
        notifyDestroyed();
    }

    public void onPause(IScope scope) {
        notifyPaused();
    }
}
