package ooplang.typechecker;

import ooplang.parser.ClassName;
import ooplang.parser.ClassDef;
import ooplang.parser.MethodSignature;
import ooplang.parser.MethodDef;
import ooplang.parser.Variable;
import ooplang.parser.Type;

import java.util.Map;
import java.util.HashMap;

// Need to record:
// - Methods on a class (mapping signature to definition)
//     - The class that originally introduced the method
//     - The class we are inheriting this methoed from
// - Instance variables on a class (mapping variable to type)
//     - The class we are inheriting this instance variable from
public class ClassInformation {
    private final Map<ClassName, Map<MethodSignature, MethodInformation>> methods;
    private final Map<ClassName, Map<Variable, InstanceVariableInformation>> instanceVariables;

    public class ClassInformation() {
        methods = new HashMap<ClassName, Map<MethodSignature, MethodInformation>>();
        instanceVariables = new HashMap<ClassName, Map<Variable, InstanceVariableInformation>>();
    }

    public boolean classExists(final ClassName className) {
        final boolean retval = methods.containsKey(className);
        assert(retval == instanceVariables.containsKey(className));
        return retval;
    }
    
    // all classes that it extends must be added first
    private void addClass(final ClassDef classDef) throws TypeErrorException {
        final ClassName myName = classDef.className;
        if (classExists(myName)) {
            throw new TypeErrorException("Duplicate class name added: " + myName.toString());
        }

        final Map<MethodSignature, MethodInformation> myMethods =
            new HashMap<MethodSignature, MethodInformation>();
        final Map<Variable, InstanceVariableInformation> myInstanceVariables =
            new HashMap<Variable, InstanceVariableInformation>();
        
        // add on inherited things
        if (classDef.extendsName.isPresent()) {
            final ClassName superName = classDef.extendsName.get();
            final Map<MethodSignature, MethodInformation> parentMethods =
                methods.get(superName);
            final Map<Variable, InstanceVariableInformation> parentInstanceVariables =
                instanceVariables.get(superName);
            assert(parentMethods != null);
            assert(parentInstanceVariables != null);
            myMethods.putAll(parentMethods);
            myInstanceVariables.putAll(parentInstanceVariables);
        }

        // add methods we define
        final Set<MethodSignature> signaturesOnSelf = new HashSet<MethodSignature>();
        for (final MethodDef methodDef : classDef.methodDefs) {
            final MethodSignature signature = MethodSignature.getSignature(methodDef);
            // make sure it's not a duplicate within this class
            if (signaturesOnSelf.contains(signature)) {
                throw new TypeErrorException("Duplicate method signature on same class: " +
                                             signature.toString());
            } else {
                signaturesOnSelf.add(signature);
            }

            // add it, possibly overriding
            final MethodInformation existingMethod = myMethods.get(signature);
            if (existingMethod == null) {
                // whole new method
                myMethods.put(signature,
                              new MethodInformation(methodDef.methodName,
                                                    myName,
                                                    myName));
            } else {
                // overriding existing method
                // make sure the return type is the same
                if (methodDef.returnType.equals(existingMethod.methodDef.returnType)) {
                    myMethods.put(signature,
                                  new MethodInformation(methodDef.methodName,
                                                        existingMethod.originalDefiner,
                                                        myName));
                } else {
                    throw new TypeErrorException("Attempt to change return type: " +
                                                 methodDef.methodName.toString());
                }
            }
        } // for each method

        // add instance variables we define
        for (final Param instanceVariable : classDef.instanceVariables) {
            if (myInstanceVariables.containsKey(instanceVariable.variable)) {
                throw new TypeErrorException("Redeclaration of (possibly inherited) instance variable: " +
                                             instanceVariable.variable.toString());
            } else {
                myInstanceVariables.put(instanceVariable.variable,
                                        new InstanceVariableInformation(instanceVariable.type,
                                                                        myName));
            }
        } // for each instance variable
        
        // save to the ClassInformation
        methods.put(myName, myMethods);
        instanceVariables.put(myName, myInstanceVariables);
    } // addClass

    // adds parent classes too
    // no-op if the class is already defined
    public void transitivelyAddClass(final Map<ClassName, ClassDef> allClasses,
                                     final ClassDef classDef) throws TypeErrorException {
        if (!classExists(classDef.className)) {
            if (classDef.extendsName.isPresent()) {
                final ClassName superName = classDef.extendsName.get();
                final ClassDef superDef = allClasses.get(superName);
                if (superDef != null) {
                    transitivelyAddClass(allClasses, superDef);
                } else {
                    throw new TypeErrorException("No such class defined: " + superName.toString());
                }
            }
            addClass(classDef);
        }
    } // transitivelyAddClass
                
    public void addClasses(final Map<ClassName, ClassDef> classes) {
        for (final ClassDef classDef : classes.values()) {
            transitivelyAddClass(classes, classDef);
        }
    } // addClasses
} // ClassInformation
