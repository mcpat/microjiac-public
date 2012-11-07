/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC MIDlet-Platform.
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
package de.jiac.micro.midlet;

import java.io.IOException;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordFilter;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import com.github.libxjava.io.BinaryDeserialiserStream;
import com.github.libxjava.io.BinarySerialiserStream;
import com.github.libxjava.io.ByteArrayInputBuffer;
import com.github.libxjava.io.ByteArrayOutputBuffer;
import com.github.libxjava.lang.IClassLoader;

import de.jiac.micro.core.IContainer;
import de.jiac.micro.core.scope.Scope;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class RMSStore {
    class StoreFilter implements RecordFilter {
        String key;
        
        public boolean matches(byte[] candidate) {
            deserialiser.flush();
            inputBuffer.setInput(candidate);
            
            try {
                return deserialiser.readUTF().equals(key);
            } catch (IOException ioe) {
                // should not happen
                throw new RuntimeException("could not deserialise record");
            }
        }
    }
    
    private final String _storeName;
    private RecordStore _store= null;
    
    BinaryDeserialiserStream deserialiser;
    ByteArrayInputBuffer inputBuffer;
    
    private BinarySerialiserStream _serialiser;
    private ByteArrayOutputBuffer _outputBuffer;
    
    private StoreFilter _filter;
    
    public RMSStore() {
        // TODO generate store name
        IContainer container= Scope.getContainer();
        _storeName= container.toString();
    }
    
    public synchronized Object retrieve(String id) throws IOException {
        checkStore();
        int index= search(id);
        
        if(index >= 0) {
            try {
                return deserialiser.readObject();
            } catch (Exception e) {
                throw new IOException("could not deserialise object: " + e.getMessage());
            }
        }
        
        return null;
    }

    public synchronized Object store(String id, Object obj) throws IOException {
        if(id == null) {
            throw new IOException("id must not be null");
        }
        
        checkStore();
        
        if(_serialiser == null) {
            _outputBuffer= new ByteArrayOutputBuffer();
            _serialiser= new BinarySerialiserStream(_outputBuffer);
        }
        
        Object old= null;
        
        try {
            // serialise object
            _serialiser.writeUTF(id);
            _serialiser.writeObject(obj);
            _serialiser.flush();
            
            int index= search(id);
            
            try {
                if(index >= 0) {
                    try {
                        old= deserialiser.readObject();
                    } catch (Exception e) {
                        // ignore it
                        old= null;
                    }
                    _store.setRecord(index, _outputBuffer.getByteArrayReference(), 0, _outputBuffer.size());
                } else {
                    _store.addRecord(_outputBuffer.getByteArrayReference(), 0, _outputBuffer.size());
                }
            } catch (Exception e) {
                throw new IOException("could not write object to database: " + e.getMessage());
            }
        } finally {
            try {
                _serialiser.flush();
            } catch (IOException ioe) {
                // should not happen
            }
            _outputBuffer.reset();
        }
        
        return old;
    }
    
    private void checkStore() throws IOException {
        if(_store == null) {
            try {
                _store= RecordStore.openRecordStore(_storeName, true);
                
                if(_store == null) {
                    throw new IOException("could not create new store");
                }
            } catch (RecordStoreException e) {
                throw new IOException("could not initialise store: " + e.getMessage());
            }
        }
    }
    
    private int search(final String key) throws IOException {
        if(deserialiser == null) {
            inputBuffer= new ByteArrayInputBuffer();
            IClassLoader classLoader= Scope.getContainer().getClassLoader();
            deserialiser= new BinaryDeserialiserStream(classLoader, inputBuffer);
        }
        
        if(_filter == null) {
            _filter= new StoreFilter();
        }
        
        // set current key
        _filter.key= key;
        try {
            RecordEnumeration records= _store.enumerateRecords(_filter, null, false);
            int count= records.numRecords();
            
            if(count > 1) {
                throw new IOException("store is corrupted and contains key more then once");
            }
            
            return count == 1 ? records.nextRecordId() : -1;
        } catch (RecordStoreException e) {
            throw new IOException("could not search for key: " + e.getMessage());
        }
    }
}
