/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Config.
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
package de.jiac.micro.config.analysis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import de.dailab.jiac.common.aamm.IItemType;
import de.dailab.jiac.common.aamm.IModelBase;
import de.dailab.jiac.common.aamm.IPropertyType;
import de.dailab.jiac.common.aamm.ListPropertyType;
import de.dailab.jiac.common.aamm.ReferencableAgentElementType;
import de.dailab.jiac.common.aamm.ReferencableAgentType;
import de.dailab.jiac.common.aamm.ReferencableNodeType;
import de.dailab.jiac.common.aamm.ReferencableObjectType;
import de.dailab.jiac.common.aamm.ReferenceType;
import de.dailab.jiac.common.aamm.ext.Reference;
import de.dailab.jiac.common.aamm.resolve.MergedConfiguration;
import de.dailab.jiac.common.aamm.resolve.ResolutionException;
import de.jiac.micro.config.util.MicroJIACToolContext;

/**
 * @author Marcel Patzlaff
 * @version $Revision:$
 */
public class ConventionEnforcer {
    private final MicroJIACToolContext _context;
    
    public ConventionEnforcer(MicroJIACToolContext context) {
        _context= context;
    }
    
    public synchronized void analyseAndEnforce(MergedConfiguration config) throws IOException, ResolutionException {
        ClassInfoPool infoPool= new ClassInfoPool(_context.getLoader());
        
        infoPool.requestClassInfo("de.jiac.micro.core.scope.Scope$ScopeRunner");
        
        for(Reference agentRef : config.agentsToUse) {
            ReferencableAgentType agent= config.getAgent(agentRef);
            infoPool.requestClassInfo(agent.getClazz());
            
            for(IModelBase agentElementRef : agent.getAgentElements()) {
                ReferencableAgentElementType agentElement= config.getAgentElement(agentElementRef);
                infoPool.requestClassInfo(agentElement.getClazz());
            }
        }
        
        for(Reference nodeRef : config.nodesToUse) {
            ReferencableNodeType node= config.getNode(nodeRef);
            for(IPropertyType property : node.getProperties()) {
                if(property instanceof ListPropertyType && property.getName().equals("nodeComponents")) {
                    ListPropertyType nodeComponents= (ListPropertyType) property;
                    for(IItemType item : nodeComponents.getItems()) {
                        ReferencableObjectType nodeComponent= config.getObject(item);
                        infoPool.requestClassInfo(nodeComponent.getClazz());
                    }
                }
            }
        }
        
        infoPool.buildPool();
        dotNode(config, config.nodesToUse[0], infoPool);
    }
    
    private void dotNode(MergedConfiguration config, Reference nodeRef, ClassInfoPool infoPool) throws IOException, ResolutionException {
        ReferencableNodeType node= config.getNode(nodeRef);
        HashSet<ClassInfo> nodeComps= new HashSet<ClassInfo>();
        for(IPropertyType property : node.getProperties()) {
            if(property instanceof ListPropertyType && property.getName().equals("nodeComponents")) {
                ListPropertyType nodeComponents= (ListPropertyType) property;
                for(IItemType item : nodeComponents.getItems()) {
                    ReferencableObjectType nodeComponent= config.getObject(item);
                    nodeComps.add(infoPool.getClassInfo(nodeComponent.getClazz()));
                }
            }
        }
        
        HashMap<String, HashSet<ClassInfo>> allAgentElements= new HashMap<String, HashSet<ClassInfo>>();
        for(ReferenceType agentRef : node.getAgentRefs()) {
            ReferencableAgentType agent= config.getAgent(agentRef);
            
            HashSet<ClassInfo> elements= new HashSet<ClassInfo>();
            
            for(IModelBase agentElementRef : agent.getAgentElements()) {
                ReferencableAgentElementType agentElement= config.getAgentElement(agentElementRef);
                ClassInfo info= infoPool.getClassInfo(agentElement.getClazz());
                elements.add(info);
            }
            
            allAgentElements.put(agent.getId(), elements);
        }

        File file= File.createTempFile("testgraph_" + nodeRef.id, ".dot");
        PrintStream printer= new PrintStream(new FileOutputStream(file));

        printer.println("digraph references {");
        printer.println(" graph [rankdir = \"LR\"];");
        
        printer.println(" subgraph cluster_0 {");
        for(ClassInfo ci : nodeComps) {
            printer.println("  \"" + nodeRef.id + ":" + ci.className + "\" [label = \"" + getSmallName(ci.className) + "\"];");
        }
        printer.println("  label = \"" + nodeRef.id + "\";");
        printer.println(" }");
        printer.println();
        
        Iterator<String> iter= allAgentElements.keySet().iterator();
        for(int i= 1; iter.hasNext(); ++i) {
            printer.println(" subgraph cluster_" + i + "{");
            String agentId= iter.next();
            for(ClassInfo ci : allAgentElements.get(agentId)) {
                printer.println("  \"" + agentId + ":" + ci.className + "\" [label = \"" + getSmallName(ci.className) + "\"];");
            }
            printer.println("  label = \"" + agentId + "\";");
            printer.println(" }");
            printer.println();
        }
        
        // now draw the relations
        
        // system handles
        ClassInfo scopeInfo= infoPool.getClassInfo("de.jiac.micro.core.scope.Scope$ScopeRunner");
        for(String indirectHandle : scopeInfo.indirectHandles) {
            printer.println("  \"SYSTEM\" -> \"" + getSmallName(indirectHandle) + "\" [label = \"INDIRECT\"];");
        }
        
        // handle relations
        ClassInfo handleRoot= infoPool.getClassInfo("de.jiac.micro.core.IHandle");
        HashSet<ClassInfo> handleInfos= infoPool.getDerivativeClassInfos(handleRoot);
        
        for(ClassInfo ci : handleInfos) {
            if(!ci.isInterface) {
                ClassInfo superClass= infoPool.getClassInfo(ci.superClassName);
                if(handleInfos.contains(superClass)) {
                    printer.println("  \"" + getSmallName(ci.className) + "\" -> \"" + getSmallName(superClass.className) + "\";");
                }
            }
            
            for(String intfName : ci.superInterfaceNames) {
                ClassInfo superInterface= infoPool.getClassInfo(intfName);
                if(handleInfos.contains(superInterface)) {
                    printer.println("  \"" + getSmallName(ci.className) + "\" -> \"" + getSmallName(superInterface.className) + "\";");
                }
            }
        }
        
        for(ClassInfo ci : nodeComps) {
            if(ci.getProvidedHandle() != null) {
                printer.println("  \"" + nodeRef.id + ":" + ci.className + "\" -> \"" + getSmallName(ci.getProvidedHandle()) + "\" [label = \"DIRECT\"];");
            }
            
            for(String indirectHandle : ci.indirectHandles) {
                printer.println("  \"" + nodeRef.id + ":" + ci.className + "\" -> \"" + getSmallName(indirectHandle) + "\" [label = \"INDIRECT\"];");
            }
        }
        
        for(String agentId : allAgentElements.keySet()) {
            for(ClassInfo ci : allAgentElements.get(agentId)) {
                if(ci.isActuator()) {
                    printer.append("  \"" + agentId + ":" + ci.className + "\" -> \"");
                    
                    if(ci.getProvidedHandle() != null) {
                        printer.append(getSmallName(ci.getProvidedHandle()));
                    } else {
                        printer.append("null");
                    }
                    
                    printer.println("\" [label = \"DIRECT\"];");
                }
                
                for(String indirectHandle : ci.indirectHandles) {
                    printer.append("  \"" + agentId + ":" + ci.className + "\" -> \"");
                    printer.append(getSmallName(indirectHandle)).println("\" [label = \"INDIRECT\"];");
                }
                
                for(MethodKey key : ci.referencedHandlesInMethods.keySet()) {
                    for(String handleClassName : ci.referencedHandlesInMethods.get(key)) {
                        printer.append("  \"" + agentId + ":" + ci.className + "\" -> \"");
                        printer.println(getSmallName(handleClassName) + "\" [label = \"" + key.methodName + "\"];");
                    }
                }
            }
        }
        
        printer.println("}");
        printer.close();
    }
    
    private static String getSmallName(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }
}
