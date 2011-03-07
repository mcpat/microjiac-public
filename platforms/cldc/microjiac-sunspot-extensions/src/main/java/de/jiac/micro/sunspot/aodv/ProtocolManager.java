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
import java.util.Hashtable;

import com.github.libxjava.concurrent.AbstractSingleThreadRunner;
import com.sun.spot.peripheral.ChannelBusyException;
import com.sun.spot.peripheral.NoRouteException;
import com.sun.spot.peripheral.radio.ILowPan;
import com.sun.spot.peripheral.radio.IProtocolManager;
import com.sun.spot.peripheral.radio.IncomingData;
import com.sun.spot.peripheral.radio.LowPanHeader;
import com.sun.spot.peripheral.radio.LowPanHeaderInfo;
import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.util.Queue;
import com.sun.squawk.util.IntHashtable;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
final class ProtocolManager implements IProtocolManager {
    final static byte PROTOCOL_NUM= (byte) 222;
    final static byte MAX_HOPS= 3;
    
    static final byte NUM_OFFSET= 0;
    static final byte SEQ_OFFSET= NUM_OFFSET + 1;
    static final byte CTRL_OFFSET= SEQ_OFFSET + 1;
    static final byte DATA_OFFSET= CTRL_OFFSET + 1;
    
    private final class InputHandler extends AbstractSingleThreadRunner {
        public InputHandler() {
            super("InputHandler");
        }

        protected void doRun() {
            while(!isCancelled()) {
                IncomingData incoming= (IncomingData) incomingQueue.get();
                
                if(incoming != null) {
                    processIncomingData(incoming);
                }
            }
        }

        protected void forkExecution(Runnable runnable, String name) {
            Thread thr= new Thread(runnable, name);
            RadioFactory.setAsDaemonThread(thr);
            thr.start();
        }
    }
    
    protected final Queue incomingQueue;
    
    private final InputHandler _inputHandler;
    private final ILowPan _lowpan;
    private final IntHashtable _incomingMessageFragments;
    
    private final Hashtable _outgoingMessageStates;
    
    ProtocolManager(ILowPan lowpan) {
        incomingQueue= new Queue();
        _inputHandler= new InputHandler();
        _lowpan= lowpan;
        _incomingMessageFragments= new IntHashtable();
        _outgoingMessageStates= new Hashtable();
    }

    public void processIncomingData(byte[] payload, LowPanHeaderInfo headerInfo) {
        incomingQueue.put(new IncomingData(payload, headerInfo));
    }
    
    public void send(MessageID mid, byte[] payload, int length, boolean last) throws IOException, ChannelBusyException, NoRouteException {
        MessageState ms= (MessageState) _outgoingMessageStates.get(mid);
        
        if(ms == null) {
            throw new IOException("invalid message id: " + mid);
        }
        
        int newSeq= (ms.lastOutgoingSeq + 1) % 256;
        ms.size+= length;
        ms.lastOutgoingSeq= newSeq;
        payload[NUM_OFFSET]= (byte) ms.mid.messageNum;
        payload[SEQ_OFFSET]= (byte) newSeq;
        payload[CTRL_OFFSET]= last ? (byte) 1 : (byte) 0;
        
        if(ms.broadcast) {
            _lowpan.sendBroadcast(PROTOCOL_NUM, payload, 0, length, MAX_HOPS);
        } else {
            _lowpan.send(LowPanHeader.DISPATCH_SPOT, PROTOCOL_NUM, ms.targetAddress, payload, 0, length);
        }
        
        if(last) {
//            System.out.println("last segment of " + mid.toString() + " (" + ms.size + ") sent");
        }
    }
    
    public void clearOutgoing(MessageID mid) {
        _outgoingMessageStates.remove(mid);
    }
    
    public void clearIncoming(MessageID mid) {
        synchronized (_incomingMessageFragments) {
            _incomingMessageFragments.remove(mid.getId());
        }
    }
    
    public MessageID newOutgoingMessageID(int targetAddress, boolean broadcast) {
        MessageID mid= new MessageID();
        MessageState ms= new MessageState(mid);
        ms.targetAddress= targetAddress | 0x144F0100000000L;
        ms.broadcast= broadcast;
        
        _outgoingMessageStates.put(mid, ms);
        
        return mid;
    }
    
    public int readMessageFragment(MessageID mid, byte[] buffer) throws IOException {
        MessageFragments mf;
        synchronized (_incomingMessageFragments) {
            mf= (MessageFragments) _incomingMessageFragments.get(mid.getId());
        }
        
        if(mf == null) {
            return -1;
        }
        
        return mf.readFragment(buffer);
    }
    
    public MessageID nextAvailableMessage() {
        synchronized (_incomingMessageFragments) {
            while(_incomingMessageFragments.isEmpty()) {
                try {
                    _incomingMessageFragments.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            MessageFragments mf= (MessageFragments) _incomingMessageFragments.elements().nextElement();
            return mf.mid;
        }
    }
    
    public void start() {
        _inputHandler.start();
    }
    
    public void stop() {
        _inputHandler.stop();
        incomingQueue.empty();
    }
    
    protected void processIncomingData(IncomingData incoming) {
        int source= (int) (incoming.headerInfo.sourceAddress & 0xFFFF);
        int num= incoming.payload[NUM_OFFSET] & 0xFF;
        
        final int id= MessageID.getId(num, source);
        MessageFragments mf;
        synchronized (_incomingMessageFragments) {
            mf= (MessageFragments) _incomingMessageFragments.get(id);
            
            if(mf == null) {
                mf= new MessageFragments(num, source);
                _incomingMessageFragments.put(id, mf);
                _incomingMessageFragments.notify();
            }
        }
        
        mf.newFragment(incoming);
    }
}
