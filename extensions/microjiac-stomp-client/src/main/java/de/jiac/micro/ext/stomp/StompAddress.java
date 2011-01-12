/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC STOMP-Client.
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
package de.jiac.micro.ext.stomp;

import de.jiac.micro.core.io.IAddress;
import de.jiac.micro.core.io.IMulticastAddress;
import de.jiac.micro.core.io.IUnicastAddress;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
class StompAddress implements IAddress, IMulticastAddress, IUnicastAddress {
    private byte type;
    private String address;
    private String addressName;
    
    static StompAddress getAddressForString(String addrString) {
        addrString= addrString.toLowerCase();
        
        if(addrString.startsWith("uc://")) {
            return new StompAddress(addrString, UNICAST);
        } else if(addrString.startsWith("mc://")) {
            return new StompAddress(addrString, MULTICAST);
        } else {
            throw new IllegalArgumentException("invalid address '" + addrString + "'");
        }
    }
    
    public StompAddress(byte type, String addressName) {
        this.type= type;
        this.addressName= addressName.toLowerCase();
        initAddress();
    }
    
    private StompAddress(String address, byte type) {
        this.type= type;
        this.address= address;
        this.addressName= address.substring(address.lastIndexOf('/') + 1);
    }
    
    public byte getType() {
        return type;
    }

    public String getGroupName() {
        return getAddressName();
    }

    public String getSelector() {
        return getAddressName();
    }

    public String getTargetId() {
        return getAddressName();
    }

    public String getAddressName() {
       return addressName;
    }
    
    public String toString() {
        return address;
    }
    
    private void initAddress() {
        this.address= (type == UNICAST ? "uc" : "mc") + "://" + addressName;
    }
}
