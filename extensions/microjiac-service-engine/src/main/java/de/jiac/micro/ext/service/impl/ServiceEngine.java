/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Service-Engine.
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

package de.jiac.micro.ext.service.impl;

import java.io.IOException;
import java.util.Vector;

import org.slf4j.Logger;

import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.agent.handle.ICommunicationHandle;
import de.jiac.micro.core.handle.IReflector;
import de.jiac.micro.core.io.IAddress;
import de.jiac.micro.core.scope.Scope;
import de.jiac.micro.ext.service.IService;
import de.jiac.micro.interaction.rr.IRequestHandler;
import de.jiac.micro.interaction.rr.RequestResponseProtocol;

/**
 * @author Marcel Patzlaff
 */
public final class ServiceEngine implements IRequestHandler {
    static final String SEARCH= "mjiac-search";
    static final String INVOKE= "mjiac-invoke";
    static final String SEARCH_GROUP= "mjiacservicegroup";
    
    private static class Synchroniser {
        protected Synchroniser() {}
        public String source= null;
        public Object content= null; 
    }
    
    private static Object getDefaultValue(String mDescriptor) {
        char type= mDescriptor.charAt(mDescriptor.indexOf(')') + 1);
        Object defaultValue= null;
        switch(type) {
            case 'B': {
                defaultValue= new Byte(Byte.MIN_VALUE);
                break;
            }
            
            case 'C': {
                defaultValue= new Character(Character.MIN_VALUE);
                break;
            }
            
            case 'D': {
                defaultValue= new Double(Double.NaN);
                break;
            }
            
            case 'F': {
                defaultValue= new Float(Float.NaN);
                break;
            }
            
            case 'I': {
                defaultValue= new Integer(Integer.MIN_VALUE);
                break;
            }
            
            case 'J': {
                defaultValue= new Long(Long.MIN_VALUE);
                break;
            }
            
            case 'S': {
                defaultValue= new Short(Short.MIN_VALUE);
                break;
            }
            
            case 'Z': {
                defaultValue= Boolean.FALSE;
                break;
            }
        }
        
        return defaultValue;
    }
    
    private static IAddress getAddress(String addressStr) {
        ICommunicationHandle ch= (ICommunicationHandle) Scope.getContainer().getHandle(ICommunicationHandle.class);
        
        if(ch != null) {
            return ch.getAddressForString(addressStr);
        }
        
        return null;
    }
    
    private class AsyncRunner implements Runnable {
        protected boolean searchOnly;
        protected AbstractServiceContext context;
        protected String mName;
        protected String mDescriptor;
        protected Object[] arguments;
        
        protected AsyncRunner() {}
        
        public void run() {
            try {
                if(searchOnly) {
                    // FIXME: multiple providers
                    try {
                        internalSearchWithBlocking(context, 1);
                    } catch (Exception e) {
                        logger.warn("could not lookup providers", e);
                    }
                } else {
                    try {
                        synchronized (context) {
                            context.asyncMethodName= mName;
                            context.asyncMethodDescriptor= mDescriptor;
                        }
                        
                        Object result= internalSearchAndInvokeWithBlocking(context, mName, mDescriptor, arguments);
                        
                        synchronized (context) {
                            context.asyncResult= result;
                        }
                    } catch (Exception e) {
                        synchronized(context) {
                            context.asyncResult= e;
                        }
                    }
                }
            } finally {
                context.lock(false);
            }
        }
    }
    
    protected final Logger logger;
    private final RequestResponseProtocol _requestProtocol;
    private Vector _services;
    
    public ServiceEngine(RequestResponseProtocol requestProtocol) {
        _requestProtocol= requestProtocol;
        _requestProtocol.setRequestHandler(this);
        _services= new Vector();
        logger= Scope.getScope().getContainerReference().getLogger("ServiceEngine");
    }
    
    public void onRequest(String requestId, String key, Object content, String source) {
        if(SEARCH.equals(key)) {
            String serviceName= (String) content;
            if(getServiceInstance(serviceName) != null) {
                try {
                    _requestProtocol.respond(requestId, key, serviceName, source);
                } catch (IOException e) {
                    logger.error("could not respond to search request", e);
                }
            }
        } else if(INVOKE.equals(key)) {
            try {
                ServiceInvocation request= (ServiceInvocation) content;
                IService service= getServiceInstance(request.serviceName);
                if(service != null) {
                    IReflector reflector= (IReflector) Scope.getContainer().getHandle(IReflector.class);
                    
                    // reflector does not want an array if the method only has one argument!
                    Object args= request.arguments.length == 1 ? request.arguments[0] : request.arguments;
                    request.setResult(reflector.invokeMethodWithDescriptor(service, request.serviceName, request.mName, request.mDescriptor, args));
                    _requestProtocol.respond(requestId, key, request, source);
                }
            } catch (Exception e) {
                logger.error("could not execute service", e);
            }
        }
    }

    public void onResponse(String requestId, String key, Object content, String source, Object ctx) {
        Synchroniser sync= (Synchroniser) ctx;
        synchronized (sync) {
            sync.source= source;
            sync.content= content;
            sync.notify();
        }
    }

    public void onTimeout(String requestId, Object ctx) {
        Synchroniser sync= (Synchroniser) ctx;
        synchronized (sync) {
            sync.notify();
        }
    }

    void deployService(IService service) {
        synchronized (_services) {
            if(!_services.contains(service)) {
                _services.addElement(service);
            }
        }
    }
    
    void undeployService(IService service) {
        _services.removeElement(service);
    }
    
    void search(final AbstractServiceContext context, int max) {
        context.lock(true);
        
        if(context.isBlocking()) {
            try {
                internalSearchWithBlocking(context, max);
            } catch (Exception e) {
                logger.warn("could not lookup providers", e);
            } finally {
                context.lock(false);
            }
        } else {
            // TODO we should cache the AsyncRunners!
            
            AsyncRunner runner= new AsyncRunner();
            runner.context= context;
            runner.searchOnly= true;
            
            // TODO thread pooling?
            Scope.createScopeAwareThread(runner, "worker").start();
        }
    }

    Object searchAndInvoke(final AbstractServiceContext context, final String mName, final String mDescriptor, final Object[] arguments) throws Exception {
        context.lock(true);
        
        if(context.isBlocking()) {
            try {
                return internalSearchAndInvokeWithBlocking(context, mName, mDescriptor, arguments);
            } finally {
                context.lock(false);
            }
        } else {
            // TODO if services are used excessively than try to cache the AsyncRunners
            AsyncRunner runner= new AsyncRunner();
            runner.context= context;
            runner.mName= mName;
            runner.mDescriptor= mDescriptor;
            runner.arguments= arguments;
            
            // TODO thread pooling?
            Scope.createScopeAwareThread(runner, mName).start();
            return getDefaultValue(mDescriptor);
        }
    }
    
    protected void internalSearchWithBlocking(final AbstractServiceContext context, final int max) throws IOException, InterruptedException {
        long timeout= context.getTimeout();
        Synchroniser sync= new Synchroniser();
        final String serviceName= context.serviceClass.getName();
        
        synchronized (sync) {
            // FIXME: wait for max providers
            _requestProtocol.broadcastRequest(SEARCH, serviceName, SEARCH_GROUP, timeout, sync);
            sync.wait(timeout);
        }
        
        if(sync.source == null || sync.content == null || !serviceName.equals(sync.content)) {
            throw new RuntimeException("service unavailable: '" + serviceName + "'");
        }
        
        IAddress providerAddress= getAddress(sync.source);
        
        if(providerAddress != null && !(context.providers.contains(providerAddress))) {
            context.providers.addElement(providerAddress);
        }
    }
    
    /**
     * Note: The caller must lock the specified ServiceContext!
     */
    protected Object internalSearchAndInvokeWithBlocking(AbstractServiceContext context, String mName, String mDescriptor, Object[] arguments) throws Exception {
        // FIXME: reuse providers already looked-up!
        
        long timeout= context.getTimeout();
        long searchStart= System.currentTimeMillis();
        Synchroniser sync= new Synchroniser();
        final String serviceName= context.serviceClass.getName();
        
        synchronized (sync) {
            // wait at most one third of the timeout for search responses
            _requestProtocol.broadcastRequest(SEARCH, serviceName, SEARCH_GROUP, timeout / 3, sync);
            sync.wait(timeout);
        }
        
        if(sync.source == null || sync.content == null || !serviceName.equals(sync.content)) {
            throw new RuntimeException("service unavailable: '" + serviceName + "'");
        }
        
        String providerAddress= sync.source;
        sync.source= null;
        sync.content= null;
        timeout-= (System.currentTimeMillis() - searchStart);
        
        if(timeout <= 0) {
            throw new RuntimeException("no time to invoke service: '" + serviceName + "'");
        }
        
        synchronized (sync) {
            ServiceInvocation invocation= new ServiceInvocation(serviceName, mName, mDescriptor, arguments);
            _requestProtocol.request(INVOKE, invocation, providerAddress, timeout, sync);
            sync.wait(timeout);
        }
        
        if(sync.content == null) {
            throw new RuntimeException("service invocation timed out: " + serviceName + "'");
        }
        
        return ((ServiceInvocation) sync.content).result;
    }
    
    private IService getServiceInstance(String serviceName) {
        IClassLoader classLoader= Scope.getContainer().getClassLoader();
        try {
            Class serviceInterface= classLoader.loadClass(serviceName);
            
            for(int i= 0; i < _services.size(); ++i) {
                IService current= (IService) _services.elementAt(i);
                if(serviceInterface.isInstance(current)) {
                    return current;
                }
            }
        } catch (ClassNotFoundException e) {
            logger.warn("service not found: '" + serviceName + "'", e);
        }
        
        return null;
    }
}
