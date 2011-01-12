/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC MIDP-Extensions.
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
package de.jiac.micro.internal.midp.ui;

import java.util.Hashtable;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;

import de.jiac.micro.core.IScope;
import de.jiac.micro.core.scope.NodeScope;
import de.jiac.micro.internal.NodeLauncher;

/**
 * @author Marcel Patzlaff
 * @version $Revision$
 */
final class MIDPPlatformMonitor implements CommandListener {
    static class UIEntry {
        CommandListener listener;
        Displayable ui;
        
        public UIEntry(Displayable d, CommandListener l) {
            ui= d;
            listener= l;
        }
    }
    
    private final static Command BACK= new Command("Back", Command.BACK, 0);
    private final static Command EXIT= new Command("Exit", Command.STOP, 2);
    
    private final static String MAIN= "main";
    
    
    private Ticker _headline;
    private List _monitor;
    
    private String _currentScreen= "";

    private final Display __display;
    /**
     * Hashtable&lt;String,UIEntry&gt;
     */
    private Hashtable _agentUIs= new Hashtable();
    private final NodeScope _scope;
    
    public MIDPPlatformMonitor() {
        _headline= new Ticker("Platform Monitor");
        _monitor= new List("Agents", Choice.IMPLICIT);
        _monitor.addCommand(EXIT);
        _monitor.setCommandListener(this);
        
        __display= Display.getDisplay((NodeLauncher) NodeScope.getNodeHandle(NodeLauncher.class));
        __display.setCurrent(_monitor);
        _currentScreen= MAIN;
        _scope= NodeScope.getNodeScope();
    }
    
    public synchronized void addAgent(String name) {
        int size= _monitor.size();
        int pos= -1;
        
        String element;
        for(int i= 0; i < size; ++i) {
            element= _monitor.getString(i);
            
            if(element.compareTo(name) > 0) {
                pos= i;
                break;
            }
        }
        
        if(pos >= 0) {
            _monitor.insert(pos, name, null);
        } else {
            _monitor.append(name, null);
        }
    }
    
    public synchronized void destroy() {
        _agentUIs.clear();
        _agentUIs= null;
        _currentScreen= "";
        _monitor= null;
        _headline= null;
    }
    
    public synchronized void removeAgent(String name) {
        int size= _monitor.size();
        String element;
        for(int i= 0; i < size; ++i) {
            element= _monitor.getString(i);
            
            if(element.equals(name)) {
                _monitor.delete(i);
                break;
            }
        }
        
        _agentUIs.remove(name);
        
        if(_currentScreen.equals(name)) {
            __display.setCurrent(_monitor);
            _currentScreen= MAIN;
        }
    }
    
    public synchronized void setAgentUI(String name, Displayable ui, CommandListener listener) {
        _agentUIs.put(name, new UIEntry(ui, listener));
        setCurrent(name, ui);
    }
    
    public synchronized void setCurrent(String name, Displayable ui) {
        UIEntry entry= (UIEntry) _agentUIs.get(name);
        
        if(entry != null) {
            entry.ui= ui;
            
            if(_currentScreen.equals(name)) {
                displayCustom(entry.ui);
            }
        }
    }

    public synchronized void commandAction(Command c, Displayable d) {
        if(c == EXIT) {
            _scope.signal(IScope.SIG_TERMINATE);
            return;
        } else if (c == BACK) {
            __display.setCurrent(_monitor);
            _currentScreen= MAIN;
        } else if (d == _monitor){
            int index= _monitor.getSelectedIndex();
            
            if(index >= 0 && index < _monitor.size()) {
                String element= _monitor.getString(index);
                UIEntry entry= (UIEntry) _agentUIs.get(element);
                
                if(entry != null && entry.ui != null) {
                    displayCustom(entry.ui);
                    _currentScreen= element;
                }
            }
        } else if (!_currentScreen.equals(MAIN)) {
            // delegate the event
            UIEntry entry= (UIEntry) _agentUIs.get(_currentScreen);
            if(entry != null && entry.listener != null) {
                entry.listener.commandAction(c, d);
                
                // ensure that we are still listening on this displayable
                d.setCommandListener(this);
            }
        }
    }
    
    private void displayCustom(Displayable d) {
        d.addCommand(BACK);
        __display.setCurrent(d);
        d.setCommandListener(this);
    }
}
