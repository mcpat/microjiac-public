/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC CLDC-Common.
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
package de.jiac.micro.core.scope;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
final class ScopeMemory implements IScopeMemory {
//    private final Scope _owner;
    private Object _reference;
    
    public ScopeMemory(Scope owner) {
//        _owner= owner;
    }

    public void enter(Runnable logic) {
//        checkAccess();
        logic.run();
    }

    public Object getReference() {
        return _reference;
    }

    public Object allocate(Class clazz) throws IllegalAccessException, InstantiationException {
//        checkAccess();
        return clazz.newInstance();
    }
    
    public void free(Object obj) {
        // just do nothing (in this version)
    }

    public void setReference(Object reference) {
        _reference= reference;
    }

//    private void checkAccess() {
//        if(Scope.getScope(_owner.getClass()) != _owner) {
//            throw new RuntimeException("illegal access");
//        }
//    }
}
