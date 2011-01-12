/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Base-Implementation.
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
package de.jiac.micro.util;

import java.util.NoSuchElementException;

import de.jiac.micro.core.scope.Scope;

/**
 * @author Erdene-Ochir Tuguldur
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class List {
    public static class Node {
        Object _value;
        Node _next;
        Node _previous;
        
        public final Node next() {
            return _next;
        }
        
        public final Node previous() {
            return _previous;
        }
        
        public final Object value() {
            return _value;
        }
    }
    
    private final class CapacityIncreaser implements Runnable {
        CapacityIncreaser() {}
        
        public synchronized void run() {
            if(_emptyNodes.next() != null || _emptyNodes.previous() != null) {
                return;
            }
            
            Node node= _emptyNodes;
            
            for(int i= 0; i < 4; ++i) {
                node._next= newNode();
                node._next._previous= node;
                node= node._next;
            }
        }
    }
    
    private static volatile int ONE= 1;
    
    private final transient Node _head;
    private final transient Node _tail;
    
    private final transient Node _emptyNodes;
    private volatile transient int _size;
    
    private final transient CapacityIncreaser _capacityIncreaser;
    
    public List() {
        _head= newNode();
        _tail= newNode();
        _emptyNodes= newNode();
        _head._next= _tail;
        _tail._previous= _head;
        _size= 0;
        _capacityIncreaser= new CapacityIncreaser();
    }
    
    public final synchronized Node addFirst(Object obj) {
        if(_emptyNodes._next == null) {
            Scope.executeInScope(_capacityIncreaser);
        }
        
        // get new node
        final Node newNode= _emptyNodes._next;
        _emptyNodes._next= newNode._next;
        
        // add node behind head
        final Node next= _head._next;
        newNode._next= next;
        next._previous= newNode;
        _head._next= newNode;
        newNode._previous= _head;
        
        newNode._value= obj;
        _size+= ONE;
        return newNode;
    }
    
    public final synchronized Node addLast(Object obj) {
        if(_emptyNodes._next == null) {
            Scope.executeInScope(_capacityIncreaser);
        }
        
        // get new node
        final Node newNode= _emptyNodes._next;
        _emptyNodes._next= newNode._next;
        
        // add node before tail
        final Node previous= _tail._previous;
        newNode._previous= previous;
        previous._next= newNode;
        _tail._previous= newNode;
        newNode._next= _tail;
        
        newNode._value= obj;
        _size+= ONE;
        return newNode;
    }
    
    public final synchronized boolean contains(Object obj) {
        return search(obj) != null;
    }
    
    public final synchronized Node get(int index) {
        if(_size <= index || index < 0) {
            throw new NoSuchElementException("index out of bounds: " + index);
        }
        
        Node node;
        
        if(_size >> 1 > index) {
            // from head
            node= _head;
            for(int i= index; i >= 0; --i) {
                node= node._next;
            }
        } else {
            // from tail
            node= _tail;
            for(int i= index; i >= 0; --i) {
                node= node._previous;
            }
        }
        
        return node;
    }
    
    public final synchronized Object removeFirst() {
        if(_head._next == _tail) {
            return null;
        }
        
        final Node first= _head._next;
        final Object val= first._value;
        delete(first);
        return val;
    }
    
    public final synchronized Object removeLast() {
        if(_tail._previous == _head) {
            return null;
        }
        
        final Node last= _tail._previous;
        final Object val= last._value;
        delete(last);
        return val;
    }
    
    public final synchronized boolean remove(Object obj) {
        Node node= search(obj);
        
        if(node != null) {
            delete(node);
            return true;
        }
        
        return false;
    }
    
    public final synchronized void clear() {
        for(Node node; (node= _head._next) != _tail; ) {
//        for(Node node= _head; (node= node._next) != _tail; ) {
            delete(node);
        }
    }
    
    public final Node head() {
        return _head;
    }
    
    public final Node tail() {
        return _tail;
    }
    
    public final int size() {
        return _size;
    }
    
    protected Node newNode() {
        return new Node();
    }
    
    public synchronized final Node search(Object value) {
        for(Node node= _head; (node= node._next) != _tail; ) {
            if(value == null ? node._value == null : (value == node._value || value.equals(node._value))) {
                return node;
            }
        }
        
        return null;
    }
    
    public synchronized final void delete(Node node) {
        if(_emptyNodes._previous != null) {
            _emptyNodes._previous._previous= null;
            _emptyNodes._previous._next= _emptyNodes._next;
            _emptyNodes._next= _emptyNodes._previous;
            _emptyNodes._previous= null;
        }
        
        _size-= ONE;
        node._value= null;
        
        // remove from list
        node._next._previous= node._previous;
        node._previous._next= node._next;

        // save the old neighbour pointers to support iteration
        _emptyNodes._previous= node;
        node._next= _tail._next;
        node._previous= _tail;
        _tail._next= node;
    }
}
