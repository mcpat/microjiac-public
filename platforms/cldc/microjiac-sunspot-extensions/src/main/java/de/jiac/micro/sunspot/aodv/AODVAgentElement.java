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

import com.github.libxjava.io.BinaryDeserialiserStream;
import com.github.libxjava.io.ByteArrayInputBuffer;
import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.util.IEEEAddress;

import de.jiac.micro.agent.ISensor;
import de.jiac.micro.agent.memory.IShortTermMemory;
import de.jiac.micro.core.ILifecycleAware;
import de.jiac.micro.core.io.IMessage;
import de.jiac.micro.core.scope.AgentScope;
import de.jiac.micro.core.scope.Scope;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class AODVAgentElement implements ISensor, ILifecycleAware, IMessageListener {
    private IShortTermMemory _stm;
    
    private BinaryDeserialiserStream _deserialiser;
    private ByteArrayInputBuffer _byteInput;
    
    public void setShortTermMemory(IShortTermMemory stm) {
        _stm= stm;
    }
    
    public void processMessage(IMessage message) {
        _stm.notice(message);
    }

    public void cleanup() {
        AODVNodeComponent aodv= (AODVNodeComponent) AgentScope.getAgentHandle(AODVNodeComponent.class);
        aodv.unregister(this);
        
        _deserialiser= null;
        _byteInput= null;
    }

    public void initialise() {
        RadioFactory.setProperty("IEEE_ADDRESS", new IEEEAddress(RadioFactory.getRadioPolicyManager().getIEEEAddress()).asDottedHex());
        RadioFactory.getRadioPolicyManager().setRxOn(true);
        _byteInput= new ByteArrayInputBuffer();
        _deserialiser= new BinaryDeserialiserStream(Scope.getContainer().getClassLoader(), _byteInput);
        
        AODVNodeComponent aodv= (AODVNodeComponent) AgentScope.getAgentHandle(AODVNodeComponent.class);
        aodv.register(this);
    }

    public void start() {
        // TODO Auto-generated method stub
        
    }

    public void stop() {
        // TODO Auto-generated method stub
        
    }

}
