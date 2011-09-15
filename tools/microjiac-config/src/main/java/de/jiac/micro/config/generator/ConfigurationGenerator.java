/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Config.
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
package de.jiac.micro.config.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;

import de.dailab.jiac.common.aamm.ComplexType;
import de.dailab.jiac.common.aamm.IEntryType;
import de.dailab.jiac.common.aamm.IItemType;
import de.dailab.jiac.common.aamm.IModelBase;
import de.dailab.jiac.common.aamm.IPropertyType;
import de.dailab.jiac.common.aamm.IReferencableComplexType;
import de.dailab.jiac.common.aamm.ISimpleType;
import de.dailab.jiac.common.aamm.ListPropertyType;
import de.dailab.jiac.common.aamm.MapPropertyType;
import de.dailab.jiac.common.aamm.ReferencableAgentElementType;
import de.dailab.jiac.common.aamm.ReferencableAgentType;
import de.dailab.jiac.common.aamm.ReferencableNodeType;
import de.dailab.jiac.common.aamm.ReferencableObjectType;
import de.dailab.jiac.common.aamm.ReferencePropertyType;
import de.dailab.jiac.common.aamm.ReferenceType;
import de.dailab.jiac.common.aamm.SimplePropertyType;
import de.dailab.jiac.common.aamm.beans.Introspector;
import de.dailab.jiac.common.aamm.check.Expression;
import de.dailab.jiac.common.aamm.check.DefinitionChecker.CheckerResult;
import de.dailab.jiac.common.aamm.ext.Reference;
import de.dailab.jiac.common.aamm.resolve.MergedConfiguration;
import de.dailab.jiac.common.aamm.resolve.ResolutionException;
import de.dailab.jiac.common.aamm.util.IMetaDataConstants;
import de.jiac.micro.config.analysis.ConventionEnforcer;
import de.jiac.micro.config.util.MicroJIACToolContext;

/**
 * Like all other generator classes, this one is a mess. So far, I hadn't any idea
 * how to make it more readable/maintainable :-/
 * 
 * 
 * @author Erdene-Ochir Tuguldur
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class ConfigurationGenerator {
    private final ClassLoader _loader;
    private final Logger _logger;
    private final DateFormat _dateFormat;
    
    private ConfigurationGenerator(ClassLoader loader, Logger log) {
        _loader= loader;
        _logger= log;
        
        _dateFormat= DateFormat.getDateTimeInstance();
    }
    
    public AbstractConfiguration[] generate(String packageName, MergedConfiguration configuration) throws ResolutionException {
        ArrayList<AbstractConfiguration> result= new ArrayList<AbstractConfiguration>();
        
        for(Reference nodeReference : configuration.nodesToUse) {
            HashSet<String> fqacn= new HashSet<String>();
            String className= nodeReference.toJavaIdentifier();
            StringWriter buffer= new StringWriter();
            PrintWriter writer= new PrintWriter(buffer);
            ReferencableNodeType node= configuration.getNode(nodeReference);
            writeHeader(writer);
            visitNode(configuration, writer, node, packageName, className, fqacn);
            writer.flush();
            writer.close();
            result.add(new NodeConfiguration(packageName + "." + className, buffer.getBuffer(), fqacn.toArray(new String[fqacn.size()])));
        }
        
        for(Reference agentReference : configuration.agentsToUse) {
            String className= agentReference.toJavaIdentifier();
            StringWriter buffer= new StringWriter();
            PrintWriter writer= new PrintWriter(buffer);
            ReferencableAgentType agent= configuration.getAgent(agentReference);
            writeHeader(writer);
            visitAgent(configuration, writer, agent, packageName, className);
            writer.flush();
            writer.close();
            result.add(new AgentConfiguration(packageName + "." + className, buffer.getBuffer()));
        }
        
        return result.toArray(new AbstractConfiguration[result.size()]);
    }
    
    public AbstractConfiguration[] generate(File targetDir, String packageName, MergedConfiguration configuration) throws IOException, ResolutionException {
        HashSet<AbstractConfiguration> descriptors = new HashSet<AbstractConfiguration>();
    	for (Reference nodeReference : configuration.nodesToUse) {
    	    HashSet<String> fqacn= new HashSet<String>();
    		String className= nodeReference.toJavaIdentifier();
    		File sourceFile= new File(targetDir, className + ".java");
        	PrintWriter writer = new PrintWriter(new FileOutputStream(sourceFile));
        	ReferencableNodeType node= configuration.getNode(nodeReference);
        	writeHeader(writer);
    		visitNode(configuration, writer, node, packageName, className, fqacn);
    		writer.flush();
    		writer.close();
    		descriptors.add(new NodeConfiguration(packageName + "." + className, null, fqacn.toArray(new String[fqacn.size()]))); 
    	}
    	
        for (Reference agentReference : configuration.agentsToUse) {
            String className= agentReference.toJavaIdentifier();
            File sourceFile= new File(targetDir, className + ".java");
            PrintWriter writer = new PrintWriter(new FileOutputStream(sourceFile));
            ReferencableAgentType agent= configuration.getAgent(agentReference);
            writeHeader(writer);
            visitAgent(configuration, writer, agent, packageName, className);
            writer.flush();
            writer.close();
            descriptors.add(new AgentConfiguration(packageName + "." + className, null)); 
        }
    	
    	return descriptors.toArray(new AbstractConfiguration[descriptors.size()]);
    }
    
    protected void visitNode(MergedConfiguration configuration, PrintWriter writer, ReferencableNodeType nodeType, String packageName, String className, HashSet<String> fullQualifiedAgentConfigurationNames) {
    	String id = getJavaExpression(Reference.createFor(nodeType).toString());
    	String displayName = getJavaExpression(nodeType.getDisplayName() == null ? nodeType.getId() : nodeType.getDisplayName());

    	_logger.info("generate configuration class: " + className);
    	
    	writer.println("package " + packageName + ";");
    	writer.println("import de.jiac.micro.core.*;");
    	writer.println("import de.jiac.micro.internal.core.*;");
    	writer.println();
    	writer.println("public class " + className + " extends AbstractNodeConfiguration {");    	
    	writer.println("\t");
    	
    	// constructor of GeneratedNodeConfiguration
    	_logger.debug("generate constructor of node configuration");
    	writer.println("\tpublic " + className + "() {");
    	writer.println("\t\tsuper(" + id + ", " + displayName + ", " + nodeType.getClazz() + ".class, new String[]{");
    	
    	for(Iterator<ReferenceType> iter= nodeType.getAgentRefs().iterator(); iter.hasNext();) {
    	    ReferenceType agentRef= iter.next();
    	    String fullQualifiedAgentConfigurationName= packageName + "." + Reference.createFrom(agentRef).toJavaIdentifier();
    	    fullQualifiedAgentConfigurationNames.add(fullQualifiedAgentConfigurationName);
    	    writer.append("\t\t\t\"").append(fullQualifiedAgentConfigurationName).print('\"');
    	    if(iter.hasNext()) {
    	        writer.print(',');
    	    }
    	    writer.println();
    	}
    	
        writer.println("\t\t});");
    	writer.println("\t}");
    	writer.println();

    	HashSet<Reference> toProcess= new HashSet<Reference>();
    	
    	_logger.debug("generate 'configureNode(AbstractNode node)'");
    	writer.println("\tprotected void configureNode(AbstractNode node) {");
    	insertPropertySetters("\t\t", writer, nodeType, "node", toProcess);
    	writer.println("\t}");
        writer.println();
    	
    	// now the delayed objects
    	_logger.debug("generate get<full-qualified-id> object methods");
    	HashSet<Reference> processed= new HashSet<Reference>();
    	do {
    	    HashSet<Reference> temporarySet= new HashSet<Reference>(toProcess);
    	    toProcess.clear();
    	    for(Reference ref : temporarySet) {
    	        visitObject("\t", writer, configuration, ref, toProcess);
    	        processed.add(ref);
    	    }
    	    
    	    toProcess.removeAll(processed);
    	} while(!toProcess.isEmpty());
    	
    	writer.println("}");
    }
    
    protected void visitAgent(MergedConfiguration configuration, PrintWriter writer, ReferencableAgentType agentType, String packageName, String className) throws ResolutionException {
        String id = getJavaExpression(Reference.createFor(agentType).toString());
        String displayName = getJavaExpression(agentType.getDisplayName() == null ? agentType.getId() : agentType.getDisplayName());
        
        Hashtable<String, ComplexType> properties = new Hashtable<String, ComplexType>();
        properties.put(id, agentType);
        
        _logger.info("generate configuration class: " + className);
        
        writer.println("package " + packageName + ";");
        writer.println("import de.jiac.micro.core.*;");
        writer.println("import de.jiac.micro.internal.core.*;");
        writer.println();
        writer.println("public class " + className + " extends AbstractAgentConfiguration {");       
        writer.println("\t");
        
        // constructor of GeneratedNodeConfiguration
        _logger.debug("generate constructor of agent configuration");
        writer.println("\tpublic " + className + "() {");
        writer.append("\t\tsuper(").append(id).append(", ").append(displayName).append(", ").append(agentType.getClazz()).println(".class);");
        writer.println("\t}");
        writer.println();
        
        HashSet<Reference> toProcess= new HashSet<Reference>();
        
        _logger.debug("generate 'configureAgent(AbstractAgent agent)'");
        writer.println("\tprotected void configureAgent(AbstractAgent agent){");
        for(IModelBase agentElementReference : agentType.getAgentElements()) {
            ReferencableAgentElementType ae= configuration.getAgentElement(agentElementReference);
            Reference ref= Reference.createFor(ae);
            toProcess.add(ref);
            writer.append("\t\t").append("agent.addAgentElement(get").append(ref.toJavaIdentifier()).println("());");
        }
        
        insertPropertySetters("\t\t", writer, agentType, "agent", toProcess);
        writer.println("\t}");
        writer.println();
        
        // now the delayed objects
        _logger.debug("generate get<full-qualified-id> object methods");
        HashSet<Reference> processed= new HashSet<Reference>();
        do {
            HashSet<Reference> temporarySet= new HashSet<Reference>(toProcess);
            toProcess.clear();
            for(Reference ref : temporarySet) {
                visitObject("\t", writer, configuration, ref, toProcess);
                processed.add(ref);
            }
            
            toProcess.removeAll(processed);
        } while(!toProcess.isEmpty());
        
        writer.println("}");
    }
    
    protected void visitObject(String indent, PrintWriter writer, MergedConfiguration config, Reference ref, HashSet<Reference> toProcess) {
        IReferencableComplexType object= config.cache.get(ref);
        boolean singleton= (object instanceof ReferencableObjectType) && ((ReferencableObjectType)object).isSingleton();
        
        String identifier= ref.toJavaIdentifier();
        
        if(singleton) {
            writer.println(indent + "private " + object.getClazz() + " " + identifier  + "= null;");
        }
        
        // method body
        writer.println(indent + "protected final " + object.getClazz() + " get" + identifier + "() {");
        String workIndent;
        if(singleton) {
            writer.println(indent + "\tif(" + identifier + " == null) {");
            workIndent= indent + "\t\t";
            writer.println(workIndent + identifier + "= new " + object.getClazz() + "();");
        } else {
            workIndent= indent + "\t";
            writer.println(workIndent + object.getClazz() + " " + identifier + "= new " + object.getClazz() + "();");
        }
        
        insertPropertySetters(workIndent, writer, object, identifier, toProcess);

        if(singleton) {
            writer.println(indent + "\t}");
        }
        
        writer.println(indent + "\treturn " + identifier + ";");
        
        writer.println(indent + "}");
    }
    
    protected void visitSimpleProperty(String indent, PrintWriter writer, String variable, SimplePropertyType simplePropertyType) {
        Method setter= (Method) simplePropertyType.getMetaData(IMetaDataConstants.SETTER_METHOD_KEY);
        writer.append(indent);
        insertCastExpression(writer, variable, setter.getDeclaringClass());
        writer.println("." + setter.getName() + "(" + getJavaExpression(simplePropertyType) + ");");
    }
    
    private void insertListPropertyArrayInitialisation(String indent, PrintWriter writer, String typeName, List<IItemType> list, HashSet<Reference> toProcess) {
        writer.println("new " + typeName + "[]{");
        
        for(Iterator<IItemType> iter= list.iterator(); iter.hasNext(); ) {
            IItemType item= iter.next();
            writer.print(indent + "\t");
            if(item instanceof ReferenceType) {
                Reference ref= Reference.createFrom((ReferenceType) item);
                toProcess.add(ref);
                writer.print("get" + ref.toJavaIdentifier() + "()");
            } else {
                writer.print(getJavaExpression((ISimpleType) item));
            }
            
            writer.println(iter.hasNext() ? "," : "");
        }
        
        writer.print(indent + "}");
    }
    
    private void insertListPropertyListInitialisation(String indent, PrintWriter writer, String listVariable, Class<?> listType, List<IItemType> list, HashSet<Reference> toProcess) {
        String addMethod;
        try {
            Class<?> vectorClass= Introspector.loadClass(listType, "java.util.Vector");
            if(vectorClass.isAssignableFrom(listType)) {
                writer.println(indent + listType.getName() + " " + listVariable + "= new " + listType.getName() + "();");
                addMethod= "addElement";
            } else {
                if(listType.isInterface()) {
                    writer.println(indent + "java.util.ArrayList " + listVariable + "= new java.util.ArrayList();");
                } else {
                    writer.println(indent + listType.getName() + " " + listVariable + "= " + listType.getName() + "();");
                }
                addMethod= "add";
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        
        for(IItemType item : list) {
            writer.print(indent + listVariable + "." + addMethod + "(");
            
            if(item instanceof ReferenceType) {
                Reference ref= Reference.createFrom((ReferenceType) item);
                toProcess.add(ref);
                writer.print("get" + ref.toJavaIdentifier() + "()");
            } else {
                writer.print(getJavaExpression((ISimpleType) item));
            }
            writer.println(");");
        }
    }
    
    protected void visitListProperty(String indent, PrintWriter writer, String variable, ListPropertyType listPropertyType, HashSet<Reference> toProcess) {
        List<IItemType> list= listPropertyType.getItems();
        
        if(list.size() <= 0) {
            return;
        }
        
        Method setter= (Method) listPropertyType.getMetaData(IMetaDataConstants.SETTER_METHOD_KEY);
        if(setter != null) {
            Class<?> paramType= setter.getParameterTypes()[0];
            
            if(paramType.isArray()) {
                writer.append(indent);
                insertCastExpression(writer, variable, setter.getDeclaringClass());
                writer.print("." + setter.getName() + "(");
                insertListPropertyArrayInitialisation(indent, writer, paramType.getComponentType().getName(), list, toProcess);
                writer.println(");");
            } else {
                String listVariable= listPropertyType.getName();
                insertListPropertyListInitialisation(indent, writer, listVariable, paramType, list, toProcess);
                writer.append(indent);
                insertCastExpression(writer, listVariable, setter.getDeclaringClass());
                writer.println("." + setter.getName() + "(" + listVariable + ");");
            }
        } else {
            // we initialise the list item per item
            Method indexedSetter= null;
            
            for(int i= 0, last= list.size() - 1; i <= last; ++i) {
                IItemType item= list.get(i);
                
                if(indexedSetter == null) {
                    indexedSetter= (Method) item.getMetaData(IMetaDataConstants.SETTER_METHOD_KEY);
                }
                writer.append(indent);
                insertCastExpression(writer, variable, indexedSetter.getDeclaringClass());
                writer.print("." + indexedSetter.getName() + "(" + i + ", ");
                
                if(item instanceof ReferenceType) {
                    Reference ref= Reference.createFrom((ReferenceType) item);
                    toProcess.add(ref);
                    writer.print("get" + ref.toJavaIdentifier() + "()");
                } else {
                    writer.print(getJavaExpression((ISimpleType) item));
                }
                
                writer.println(");");
            }
        }
    }
    
    protected void visitMapProperty(String indent, PrintWriter writer, String variable, MapPropertyType mapPropertyType, HashSet<Reference> toProcess) {
        Method setter= (Method) mapPropertyType.getMetaData(IMetaDataConstants.SETTER_METHOD_KEY);
        
        if(setter != null) {
            // we build the map
            Class<?> paramType= setter.getParameterTypes()[0];
            String typeName= paramType.isInterface() ? "java.util.Hashtable" : paramType.getName();
            
            writer.println(indent + typeName + " " + mapPropertyType.getName() + "= new " + typeName + "();");
            
            for(IEntryType entry : mapPropertyType.getEntries()) {
                writer.print(indent + mapPropertyType.getName() + ".put(" + getJavaExpression(entry.getKey()) + ", ");
                
                if(entry instanceof ReferenceType) {
                    Reference ref= Reference.createFrom((ReferenceType) entry);
                    toProcess.add(ref);
                    writer.print("get" + ref.toJavaIdentifier() + "()");
                } else {
                    writer.print(getJavaExpression((ISimpleType) entry));
                }
                
                writer.println(");");
                writer.append(indent);
                insertCastExpression(writer, variable, setter.getDeclaringClass());
                writer.println("." + setter.getName() + "(" + mapPropertyType.getName() + ");");
            }
        } else {
            // we initialise the map entry per entry
            Method mappedSetter= null;
            for(IEntryType entry : mapPropertyType.getEntries()) {
                if(mappedSetter == null) {
                    mappedSetter= (Method) entry.getMetaData(IMetaDataConstants.SETTER_METHOD_KEY);
                }
                
                writer.append(indent);
                insertCastExpression(writer, variable, mappedSetter.getDeclaringClass());
                writer.print("." + mappedSetter.getName() + "(" + getJavaExpression(entry.getKey()) + ", ");
                
                if(entry instanceof ReferenceType) {
                    Reference ref= Reference.createFrom((ReferenceType) entry);
                    toProcess.add(ref);
                    writer.print("get" + ref.toJavaIdentifier() + "()");
                } else {
                    writer.print(getJavaExpression((ISimpleType) entry));
                }
                
                writer.println(");");
            }
        }
    }
    
    protected void visitObjectProperty(String indent, PrintWriter writer, String variable, ReferencePropertyType objectReferencePropertyType, HashSet<Reference> toProcess) {
        Method setter= (Method) objectReferencePropertyType.getMetaData(IMetaDataConstants.SETTER_METHOD_KEY);
        Reference ref= Reference.createFrom(objectReferencePropertyType);
        toProcess.add(ref);
        writer.append(indent);
        insertCastExpression(writer, variable, setter.getDeclaringClass());
        writer.println("." + setter.getName() + "(get" + ref.toJavaIdentifier() + "());");
    }
    
    private String getJavaExpression(ISimpleType value) {
        Expression expr= Expression.parseAndConvertToJavaExpression(value.getValue(), _loader);
        return expr.convertToJavaExpression((Class<?>) value.getMetaData(ISimpleType.KEY_CLASSTYPE));
    }
    
    private String getJavaExpression(String string) {
        Expression expr= Expression.parseAndConvertToJavaExpression(string, _loader);
        return expr.convertToJavaExpression(String.class);
    }
    
    private void insertCastExpression(PrintWriter writer, String variable, Class<?> type) {
        writer.append("((").append(type.getName()).append(')').append(variable).append(')');
    }

    private void insertPropertySetters(String workIndent, PrintWriter writer, IReferencableComplexType object, String identifier, HashSet<Reference> toProcess) {
        for(IPropertyType property : object.getProperties()) {
            if(property instanceof SimplePropertyType) {
                visitSimpleProperty(workIndent, writer, identifier, (SimplePropertyType) property);
            } else if(property instanceof ListPropertyType) {
                visitListProperty(workIndent, writer, identifier, (ListPropertyType) property, toProcess);
            } else if(property instanceof MapPropertyType) {
                visitMapProperty(workIndent, writer, identifier, (MapPropertyType) property, toProcess);
            } else {
                visitObjectProperty(workIndent, writer, identifier, (ReferencePropertyType) property, toProcess);
            }
        }
    }
    
    private void writeHeader(PrintWriter writer) {
        writer.println("//");
        writer.println("// This file was generated by the Configuration Generator for MicroJIAC.");
        writer.println("// Any modifications to this file will be lost upon re-running the configurator!");
        writer.append("// Generated on: ").println(_dateFormat.format(new Date(System.currentTimeMillis())));
        writer.println("//");
        writer.println();
        writer.println();
    }
    
    /**
     * Return the array of generated node configurations.
     * 
     * @param applicationNamespace  the namespace where the application definition is located
     * @param loader                the classloader which has access to all required resources and classes
     * @param log                   the log that outputs the plugin informations
     * @return                      full-qualified class names of all generated node configurations
     * 
     * @throws Exception            if an error occures during execution
     */
    public static AbstractConfiguration[] execute(String applicationNamespace, ClassLoader loader, Logger log) throws Exception {
        String packageName= "de.jiac.micro.internal.latebind";

        MicroJIACToolContext context= new MicroJIACToolContext(loader);
        ConfigurationGenerator generator= new ConfigurationGenerator(context.getLoader(), log);
        MergedConfiguration conf= context.createResolver().resolveAndMerge(applicationNamespace);
        CheckerResult checkerResult= context.createChecker().flattenAndCheck(conf);
        
        // first print the warnings
        for(String warning : checkerResult.warnings) {
            log.warn(warning);
        }
        
        // print errors
        for(String error : checkerResult.errors) {
            log.error(error);
        }
        
        if(checkerResult.hasErrors()) {
            throw new GeneratorException("checker has found errors");
        }
        
        return generator.generate(packageName, conf);
    }
    
    /**
     * Return the array of descriptors for each generated configuration.
     * 
     * @param rootDirectory         the directory to place the package and classes into
     * @param applicationNamespace  the namespace where the application definition is located
     * @param loader                the classloader which has access to all required resources and classes
     * @param log                   the log that outputs the plugin informations
     * @return                      descriptors of the generated configurations
     * 
     * @throws Exception            if an error occures during execution
     */
    public static AbstractConfiguration[] execute(File rootDirectory, String applicationNamespace, ClassLoader loader, Logger log) throws Exception {
        String packageName= "de.jiac.micro.internal.latebind";
        File lateBindDirectory= new File(rootDirectory, packageName.replace('.', File.separatorChar));
        
        if(!lateBindDirectory.exists()) {
            lateBindDirectory.mkdirs();
        }
        
        MicroJIACToolContext context= new MicroJIACToolContext(loader);
        ConfigurationGenerator generator= new ConfigurationGenerator(context.getLoader(), log);
        MergedConfiguration conf= context.createResolver().resolveAndMerge(applicationNamespace);
        CheckerResult checkerResult= context.createChecker().flattenAndCheck(conf);
        
        // first print the warnings
        for(String warning : checkerResult.warnings) {
            log.warn(warning);
        }
        
        // print errors
        for(String error : checkerResult.errors) {
            log.error(error);
        }
        
        if(checkerResult.hasErrors()) {
            throw new GeneratorException("checker has found errors");
        }
        
//ConventionEnforcer analyser= context.createEnforcer();
//analyser.analyseAndEnforce(conf);
        
        return generator.generate(lateBindDirectory, packageName, conf);
    }
    
//    public static void main(String args[]) throws Exception {
//        execute(
//            new File(System.getProperty("java.io.tmpdir")),
//            args[0],
//            ConfigurationGenerator.class.getClassLoader(),
//            new SimpleLog(ConfigurationGenerator.class.getName())
//        );
//    }
}
