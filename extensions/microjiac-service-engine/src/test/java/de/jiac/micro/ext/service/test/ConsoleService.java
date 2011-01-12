/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Service-Engine.
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
package de.jiac.micro.ext.service.test;


/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ConsoleService implements IConsoleService {
    public String doGetDescription() {
        return "It's just a f***ing console service!!!111";
    }

    public void doPrint(String message) {
        System.out.print(message);
    }

    public void doPrintln(String message) {
        System.out.println(message);
    }

//    public int doRead() {
//        try {
//            InputStreamReader reader= new InputStreamReader(System.in);
//            return reader.read();
//        } catch (IOException ioe) {
//            _logger.info("could not read from stdin", ioe);
//            return -1;
//        }
//    }
//
//    public String doReadln() {
//        try {
//            InputStreamReader reader= new InputStreamReader(System.in);
//            StringBuffer buffer= new StringBuffer();
//            char ch;
//            while((ch= (char) reader.read()) != '\n') {
//                buffer.append(ch);
//            }
//            
//            return buffer.toString();
//        } catch (IOException ioe) {
//            _logger.info("could not read from stdin", ioe);
//            return null;
//        }
//    }
}
