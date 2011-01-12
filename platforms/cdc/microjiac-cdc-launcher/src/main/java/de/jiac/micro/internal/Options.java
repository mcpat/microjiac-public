/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC CDC-Launcher.
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
package de.jiac.micro.internal;

import java.util.HashMap;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class Options {
    public static class Option {
        protected String value= null;
        
        protected Option() {}
        
        public String getValue() {
            return value;
        }
    }
    
    private final HashMap _options= new HashMap();
    private final StringBuffer _optionHelp= new StringBuffer();
    
    
    private final int[] _longestOp= {0, 0};
    
    public Option createOption(String shortForm, String longForm, String description) {
        Option opt= new Option();
        
        if(shortForm != null) {
            String optStr= "-" + shortForm;
            _options.put(optStr, opt);
            _optionHelp.append(optStr);
            
            if(longForm != null) {
                _optionHelp.append(",");
            }
            
            if(_longestOp[0] < optStr.length()) {
                _longestOp[0]= optStr.length();
            }
        }
        
        _optionHelp.append('\t');
        
        if(longForm != null) {
            String optStr= "--" + longForm;
            _options.put(optStr, opt);
            _optionHelp.append(optStr);
            if(_longestOp[1] < optStr.length()) {
                _longestOp[1]= optStr.length();
            }
        }
        
        _optionHelp.append('\t');
        
        if(description != null) {
            _optionHelp.append(description);
        }
        
        _optionHelp.append('\n');
        
        return opt;
    }
    
    public boolean load(String[] args) {
        final int last= args.length - 1;
        
        for(int i= 0; i <= last; i++) {
            String optStr= args[0];
            String optVal= null;
            boolean isLongForm= optStr.startsWith("--");
            
            if(isLongForm) {
                int equal= optStr.indexOf('=');
                if(equal > 0) {
                    optVal= optStr.substring(equal + 1);
                    optStr= optStr.substring(0, equal);
                }
            }
            
            Option opt= (Option) _options.get(optStr);
            
            if(opt == null) {
                System.err.println("unrecognised option: " + optStr);
                return false;
            }
            
            if(!isLongForm && i < last) {
                optVal= args[++i];
            }
            
            if(optVal == null) {
                System.err.println("missing option value: " + optStr);
                return false;
            }
            
            opt.value= optVal;
        }
        
        return true;
    }
    
    public void printUsage(String program) {
        System.err.println(" usage: " + program + " [options]");
        
        final int len= _optionHelp.length();
        
        for(int i= 0; i < len;) {
            System.out.print("  ");
            for(int f= 0; f <= 1; f++) {
                int sep= _optionHelp.indexOf("\t", i);
                System.err.print(_optionHelp.substring(i, sep));
                
                for(int space= sep - i - 1; space <= _longestOp[f]; space++) {
                    System.out.print(' ');
                }
                
                i= sep + 1;
            }
            
            int newline= _optionHelp.indexOf("\n", i);
            System.err.println(_optionHelp.substring(i, newline));
            i= newline + 1;
        }
    }
}
