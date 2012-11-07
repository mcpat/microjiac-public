/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC ActiveMQ-Broker.
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
package de.jiac.micro.ext.jms;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class JMSTransportConnector {
    public String transportURI= null;
    public String discoveryURI= null;
    
    public void setTransportURI(String transportURI) {
        this.transportURI= transportURI;
    }
    
    public void setDiscoveryURI(String discoveryURI) {
        this.discoveryURI= discoveryURI;
    }
}
