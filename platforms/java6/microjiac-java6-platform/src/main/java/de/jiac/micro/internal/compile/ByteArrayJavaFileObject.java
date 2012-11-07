/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Java6-Platform.
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
package de.jiac.micro.internal.compile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

import com.github.libxjava.io.ByteArrayOutputBuffer;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ByteArrayJavaFileObject extends SimpleJavaFileObject {
    private final ByteArrayOutputBuffer _buffer= new ByteArrayOutputBuffer();
    
    public ByteArrayJavaFileObject(String className, Kind kind) {
        super(URI.create("bytearray:///" + className.replace('.','/') + kind.extension), kind);
    }

    @Override
    public InputStream openInputStream() {
        return new ByteArrayInputStream(_buffer.toByteArray());
    }

    @Override
    public OutputStream openOutputStream() {
        _buffer.resetNew();
        return _buffer;
    }
    
    public byte[] getByteArray() {
        return _buffer.toByteArray();
    }
}
