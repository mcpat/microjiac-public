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

import de.jiac.micro.core.io.IMulticastAddress;
import de.jiac.micro.core.io.IUnicastAddress;

/** 
 * Vladimir Sch&ouml;ner
 * $Revision$
 * 
 * TODO implement methods
 */
public class Address implements IMulticastAddress, IUnicastAddress {
    public static IMulticastAddress createMulticastAddress(String groupName) {
        return new Address(MULTICAST, "mc://" + groupName);
    }
    
    public static IUnicastAddress createUnicastAddress(long nodeID, String agentID) {
        return new Address(UNICAST, "uc://" + nodeID + "#" + agentID);
    }
    
    public static Address parseAddress(String addressStr) {
        String prefix= addressStr.substring(0, 5);
        byte type;
        
        if(prefix.equals("uc://")) {
            type= UNICAST;
        } else if(prefix.equals("mc://")) {
            type= MULTICAST;
        } else {
            throw new IllegalArgumentException("invalid address string '" + addressStr + "'");
        }
        
        return new Address(type, addressStr);
    }
    
	private byte _type;
	private String _address;
	
	private String _agentId;
	private String _nodeAddressAsString;
	private long _nodeAddress;
	
	private String _groupName;
	
	public Address(byte type, String address) {
        _type= type;
        _address= address;
        initFields();
    }
    
    public byte getType() {
        return _type;
    }

    public String toString() {
        return _address;
    }
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    public int hashCode() {
        return toString().hashCode();
    }

    public String getTargetId() {
        if(_type != UNICAST) {
            throw new RuntimeException("operation not available for '" + _address + "'");
        }
        
        return _nodeAddressAsString;
    }

    public long getNodeAddress() {
        if(_type != UNICAST) {
            throw new RuntimeException("operation not available for '" + _address + "'");
        }
        
        return _nodeAddress;
    }
    
    public String getSelector() {
        if(_type != UNICAST) {
            throw new RuntimeException("operation not available for '" + _address + "'");
        }
        
        return _agentId;
    }
    
    public String getGroupName() {
        if(_type != MULTICAST) {
            throw new RuntimeException("operation not available for '" + _address + "'");
        }
        
        return _groupName;
    }

    private void initFields() {
        switch(_type) {
            case UNICAST: {
                int hashmark= _address.indexOf('#');
                _nodeAddressAsString= _address.substring(5, hashmark);
                _agentId= _address.substring(hashmark + 1);
                _nodeAddress= Long.parseLong(_nodeAddressAsString);
                break;
            }
            
            case MULTICAST: {
                _groupName= _address.substring(5);
                break;
            }
        }
    }
}
