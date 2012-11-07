/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC CDC-Common.
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
package de.jiac.micro.test;

import junit.framework.TestCase;
import de.jiac.micro.core.IHandle;
import de.jiac.micro.core.handle.IReflector;
import de.jiac.micro.core.scope.Scope;
import de.jiac.micro.internal.latebind.Reflector;
import de.jiac.micro.test.environment.TestScope;

/**
 *
 * @author Marcel Patzlaff
 */
public class ReflectorTest extends TestCase {
    private final static class Context {
        final DummyClass testObj= new DummyClass();
        Object returnValue= null;
        Exception exception= null;
        
        protected Context() {}
    }
    
    private static Context propertyTestDriver(final String propertyName, final Object value) {
        final Context context= new Context();
        final Runnable testRunner= new Runnable() {
            public void run() {
                IReflector reflector= (IReflector) Scope.getContainer().getHandle(IReflector.class);
                synchronized (context) {
                    try {
                        reflector.writeProperty(context.testObj, propertyName, value);
                    } catch (Exception e) {
                        context.exception= e;
                    } finally {
                        context.notifyAll();
                    }
                }
            }
        };
        
        synchronized (context) {
            TestScope.runInScope(testRunner, new IHandle[]{new Reflector()});
            try {
                context.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail();
            }
        }
        
        return context;
    }
    
    private static Context methodTestDriver(final String methodName, final Object arguments) {
        final Context context= new Context();
        final Runnable testRunner= new Runnable() {
            public void run() {
                IReflector reflector= (IReflector) Scope.getContainer().getHandle(IReflector.class);
                synchronized (context) {
                    try {
                        context.returnValue= reflector.invokeMethod(context.testObj, methodName, arguments);
                    } catch (Exception e) {
                        context.exception= e;
                    } finally {
                        context.notifyAll();
                    }
                }
            }
        };
        
        synchronized (context) {
            TestScope.runInScope(testRunner, new IHandle[]{new Reflector()});
            try {
                context.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail();
            }
        }
        
        return context;
    }
    
    public void testInvisibleMethod() {
        Context context= methodTestDriver("invisibleMethod", null);
        assertNull(context.returnValue);
        assertNotNull(context.exception);
        assertEquals(context.exception.getClass(), NoSuchMethodException.class);
    }
    
    public void testWriteIntProperty() {
        // positive test
        {
            Context context= propertyTestDriver("number", new Short((short) 42));
            assertNull(context.returnValue);
            assertNull(context.exception);
            assertEquals(context.testObj.getNumber(), 42);
        }
        
        // negative test
        {
            Context context= propertyTestDriver("number", new Long(42L));
            assertNull(context.returnValue);
            assertNotNull(context.exception);
            assertEquals(context.exception.getClass(), IllegalArgumentException.class);
        }
    }
    
    public void testWriteStringProperty() {
        // positive test
        {
            Context context= propertyTestDriver("string", "Hello World!");
            assertNull(context.returnValue);
            assertNull(context.exception);
            assertEquals(context.testObj.getString(), "Hello World!");
        }
        
        // negative test
        {
            Context context= propertyTestDriver("string", new Character('c'));
            assertNull(context.returnValue);
            assertNotNull(context.exception);
            assertEquals(context.exception.getClass(), IllegalArgumentException.class);
        }
    }
    
    public void testOverloadedMethod() {
        // positive test
        {
            Context context= methodTestDriver("overloadedMethod", "Hello World!");
            assertNull(context.exception);
            assertNotNull(context.returnValue);
            assertEquals(context.returnValue, new Byte((byte) 1));
        }
        
        // positive test
        {
            Context context= methodTestDriver("overloadedMethod", new Object[]{new short[0][0]});
            assertNotNull(context.returnValue);
            assertNull(context.exception);
            assertEquals(context.returnValue, new Byte((byte) 1));
        }
        
        // positive test
        {
            Context context= methodTestDriver("overloadedMethod", new Long(666L));
            assertNull(context.exception);
            assertNotNull(context.returnValue);
            assertEquals(context.returnValue, new Byte((byte) 2));
        }
        
        // positive test
        {
            Context context= methodTestDriver("overloadedMethod", new Object[]{new short[0]});
            assertNull(context.exception);
            assertNotNull(context.returnValue);
            assertEquals(context.returnValue, new Byte((byte) 3));
        }
        
        // positive test
        {
            Context context= methodTestDriver("overloadedMethod", new Float(3f));
            assertNull(context.exception);
            assertNotNull(context.returnValue);
            assertEquals(context.returnValue, new Byte((byte) 4));
        }
        
        // negative test
        {
            Context context= methodTestDriver("overloadedMethod", new Object[0]);
            assertNotNull(context.exception);
            assertNull(context.returnValue);
            assertEquals(context.exception.getClass(), NoSuchMethodException.class);
        }
    }
}
