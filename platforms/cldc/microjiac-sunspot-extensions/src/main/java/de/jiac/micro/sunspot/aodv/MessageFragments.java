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

import java.io.IOException;

import com.sun.spot.peripheral.radio.IncomingData;
import com.sun.squawk.util.IntHashtable;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
class MessageFragments {
    private final static long MAX_WAIT= 2000;
    
    final MessageID mid;
    private final IntHashtable _sortedFragments;
    
    private int _seqToRead;
    private boolean _lastRead= false;
    
    MessageFragments(int messageNum, int source) {
        mid= new MessageID(messageNum, source);
        _seqToRead= 1;
        _sortedFragments= new IntHashtable();
    }
    
    public void newFragment(IncomingData incoming) {
        int seq= incoming.payload[ProtocolManager.SEQ_OFFSET] & 0xFF;
//System.out.println(" ::MF:: " + seq + " received (" + incoming.payload.length + ") from " + mid.toString());
        
        synchronized (_sortedFragments) {
            if(_sortedFragments.put(seq, incoming) != null) {
                System.err.println("multiple data segment " + seq);
            }
            
            if(_seqToRead == seq) {
                _sortedFragments.notify();
            }
        }
    }
    
    public int readFragment(byte[] buffer) throws IOException {
        IncomingData incoming;
        
        synchronized (_sortedFragments) {
            if(_lastRead) {
                return -1;
            }
            
            long deadline= System.currentTimeMillis() + MAX_WAIT;
            while(!_sortedFragments.containsKey(_seqToRead)) {
                long toWait= deadline - System.currentTimeMillis();
                if(toWait <= 0) {
                    throw new IOException("waiting for segment " + _seqToRead + " timed out");
                }
                
                try {
                    _sortedFragments.wait(MAX_WAIT);
                } catch (InterruptedException e) {
                    throw new IOException("operation was interrupted");
                }
            }
            
            incoming= (IncomingData) _sortedFragments.remove(_seqToRead);
            _lastRead= incoming.payload[ProtocolManager.CTRL_OFFSET] == 1;
//System.out.println(" ::MF:: segment " + _seqToRead + " read (last = " + _lastRead + ")");
            _seqToRead= (_seqToRead + 1) % 256;
        }
        
        int numBytes= incoming.payload.length - ProtocolManager.DATA_OFFSET;
        
        System.arraycopy(incoming.payload, ProtocolManager.DATA_OFFSET, buffer, 0, numBytes);

        return numBytes;
    }
}
