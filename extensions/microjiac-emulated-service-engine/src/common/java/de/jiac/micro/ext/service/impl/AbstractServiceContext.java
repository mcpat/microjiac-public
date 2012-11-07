/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Emulated-Service-Engine.
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
package de.jiac.micro.ext.service.impl;

import java.util.Vector;

import de.jiac.micro.core.io.IAddress;
import de.jiac.micro.ext.service.IServiceContext;

/**
 * @author Marcel Patzlaff
 */
/*package*/ abstract class AbstractServiceContext implements IServiceContext {
    private boolean _blocking= true;
    private int _timeoutInMilliSeconds= 10000;
    /*package*/ String asyncMethodDescriptor;
    /*package*/ String asyncMethodName;
    /*package*/ Object asyncResult;
    /*package*/ final Vector providers;
    protected final Class serviceClass;
    private boolean _locked;
    
    protected ServiceEngine engine;
    
    protected AbstractServiceContext(Class serviceClass) {
        providers= new Vector();
        this.serviceClass= serviceClass;
        _locked= false;
    }
    
    public final synchronized Vector getProviders() {
        ensureReleased();
        return providers;
    }
    
    public final synchronized int getTimeout() {
        return _timeoutInMilliSeconds;
    }
    
    public final synchronized void setTimeout(int timeout) {
        ensureReleased();
        _timeoutInMilliSeconds= timeout;
    }
    
    public final synchronized boolean isBlocking() {
        return _blocking;
    }

    public final synchronized void setBlocking(boolean blocking) {
        ensureReleased();
        _blocking= blocking;
    }

    public final synchronized boolean isLocked() {
        return _locked;
    }

    public final synchronized void lookupProviders(int max) {
        ensureReleased();
        engine.search(this, max);
    }

    public final synchronized void setPreferredProvider(IAddress providerAddress) {
        ensureReleased();
        int oldIndex= providers.indexOf(providerAddress);
        if(oldIndex > 0) {
            providers.removeElementAt(oldIndex);
        } else if(oldIndex == 0) {
            return;
        }
        
        providers.insertElementAt(providerAddress, 0);
    }
    
    /*package*/ final void setServiceEngine(ServiceEngine serviceEngine) {
        engine= serviceEngine;
    }
    
    /*package*/ final synchronized void lock(boolean val) {
        if(val && _locked) {
            throw new RuntimeException("context is locked");
        }
        
        _locked= val;
    }
    
    protected final void ensureReleased() {
        if(isLocked()) {
            throw new RuntimeException("context is locked");
        }
    }
    
    protected final boolean hasAsyncResult() {
        return asyncMethodName != null;
    }
    
    protected final Object fetchAsyncResult(String mName, String mDescr) throws Exception {
        if(!asyncMethodName.equals(mName) || !asyncMethodDescriptor.equals(mDescr)) {
            throw new RuntimeException("result for " + mName + asyncMethodDescriptor + " available");
        }
        
        try {
            final Object result= asyncResult;
            
            if(result instanceof Exception) {
                throw (Exception) result;
            }
            
            return result;
        } finally {
            asyncMethodDescriptor= null;
            asyncMethodName= null;
            asyncResult= null;
        }
    }
}
