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

import com.github.libxjava.io.ByteArrayInputBuffer;
import com.github.libxjava.io.ByteArrayOutputBuffer;
import com.sun.spot.peripheral.radio.ILowPan;
import com.sun.spot.peripheral.radio.IProtocolManager;
import com.sun.spot.peripheral.radio.IncomingData;
import com.sun.spot.peripheral.radio.LowPan;
import com.sun.spot.peripheral.radio.LowPanHeader;
import com.sun.spot.peripheral.radio.LowPanHeaderInfo;
import com.sun.spot.peripheral.radio.RadioPacket;
import com.sun.spot.util.Queue;

import de.jiac.micro.core.scope.AbstractScopeAwareRunner;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class DiscoveryProtocolManager implements IProtocolManager {
    public interface IDiscoveryListener {
        void notifyDiscovered(int address, String identification, int signalStrength);
    }
    
    public final static byte PROTOCOL_NUM= (byte) 223;
    final static byte MAX_HOPS= 2;
    
    private final static int MAX_SIZE= RadioPacket.MIN_PAYLOAD_LENGTH - ILowPan.MAC_PAYLOAD_OFFSET - LowPanHeader.MAX_UNFRAG_HEADER_LENGTH;
    
    private final class BeaconProcessor extends AbstractScopeAwareRunner {
        protected final Queue packets;
        
        protected BeaconProcessor() {
            super("BeaconProcessor");
            packets= new Queue();
        }
        
        protected void doRun() {
            while(!isCancelled()) {
                try {
                    IncomingData incoming= (IncomingData) packets.get();
                    processIncomingData(incoming);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private final ByteArrayOutputBuffer _writeBuffer;
    private final ByteArrayInputBuffer _readBuffer;
    private final StringBuffer _idBuffer;
    private final BeaconProcessor _processor;
    private final IDiscoveryListener _listener;
    
    public DiscoveryProtocolManager(IDiscoveryListener listener) {
        _listener= listener;
        _processor= new BeaconProcessor();
        _writeBuffer= new ByteArrayOutputBuffer(MAX_SIZE);
        _readBuffer= new ByteArrayInputBuffer();
        _idBuffer= new StringBuffer();
    }

    public void processIncomingData(byte[] payload, LowPanHeaderInfo headerInfo) {
        _processor.packets.put(new IncomingData(payload, headerInfo));
    }
    
    public void sendIdentification(String identification) throws IOException {
        synchronized (_writeBuffer) {
            try {
                for(int i= 0; i < identification.length(); ++i) {
                    _writeBuffer.write(identification.charAt(i));
                }
                
                if(_writeBuffer.size() > MAX_SIZE) {
                    throw new IOException("identification is too large");
                }
                LowPan.getInstance().sendBroadcast(PROTOCOL_NUM, _writeBuffer.getByteArrayReference(), 0, _writeBuffer.size(), MAX_HOPS);
            } finally {
                _writeBuffer.resetNew();
            }
        }
    }
    
    public void start() {
        _processor.packets.empty();
        LowPan.getInstance().registerProtocol(PROTOCOL_NUM, this);
        _processor.start();
    }

    public void stop() {
        LowPan.getInstance().deregisterProtocol(PROTOCOL_NUM);
        _processor.stop();
    }
    
    protected void processIncomingData(IncomingData incoming) {
        synchronized (_readBuffer) {
            final byte[] oldBuf= _readBuffer.getByteArrayReference();
            try {
                _readBuffer.setInput(incoming.payload);
                for(int i= 0; i < incoming.payload.length; ++i) {
                    _idBuffer.append((char) (incoming.payload[i] & 0xFF));
                }
                
                _listener.notifyDiscovered((int) (incoming.headerInfo.originator & 0xFFFF), _idBuffer.toString(), incoming.headerInfo.rssi);
            } finally {
                _readBuffer.setInput(oldBuf);
                _idBuffer.setLength(0);
            }
        }
    }
}
