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

import com.sun.spot.peripheral.radio.RadioFactory;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public final class MessageID {
    public static int getId(int messageNum, int source) {
        return (source << 8) | messageNum;
    }
    
    private static int NUM= 0;
    
    private synchronized static int nextNum() {
        final int nn= NUM;
        NUM= (NUM + 1) % 256;
        return nn;
    }
    
    final int messageNum;
    final int source;
    
    MessageID() {
        this(nextNum(), (int) (RadioFactory.getRadioPolicyManager().getIEEEAddress() & 0xFFFF));
    }
    
    MessageID(int messageNum, int source) {
        this.messageNum= messageNum;
        this.source= source;
    }

    public boolean equals(Object obj) {
        if(obj instanceof MessageID) {
            return ((MessageID) obj).getId() == getId();
        }
        
        return false;
    }

    public int hashCode() {
        return getId();
    }
    
    public int getId() {
        return getId(messageNum, source);
    }

    public String toString() {
        return Integer.toHexString(getId());
    }
}
