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
/*
 * $Id$ 
 */
package de.jiac.micro.ext.service.impl;

import java.io.IOException;

import com.github.libxjava.io.IDeserialiser;
import com.github.libxjava.io.ISerialisable;
import com.github.libxjava.io.ISerialiser;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class ServiceInvocation implements ISerialisable {
    public String serviceName;
    public String mName;
    public String mDescriptor;
    public Object[] arguments;
    public Object result;
    
    public ServiceInvocation() {}
    
    public ServiceInvocation(String serviceName, String mName, String mDescriptor, Object[] arguments) {
        this.serviceName= serviceName;
        this.mName= mName;
        this.mDescriptor= mDescriptor;
        this.arguments= arguments;
    }
    
    public void setResult(Object result) {
        arguments= null;
        this.result= result;
    }

    public void deserialise(IDeserialiser in) throws IOException, ClassNotFoundException {
        serviceName= (String) in.readObject();
        mName= (String) in.readObject();
        mDescriptor= (String) in.readObject();
        
        int len= in.readInt();
        arguments= new Object[len];
        for(int i= 0; i < len; ++i) {
            arguments[i]= in.readObject();
        }
        
        result= in.readObject();
    }

    public void serialise(ISerialiser out) throws IOException {
        out.writeObject(serviceName);
        out.writeObject(mName);
        out.writeObject(mDescriptor);
        
        int len= arguments == null ? 0 : arguments.length;
        out.writeInt(len);
        for(int i= 0; i < len; ++i) {
            out.writeObject(arguments[i]);
        }
        
        out.writeObject(result);
    }
}
