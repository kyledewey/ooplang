package ooplang.typechecker;

import ooplang.parser.ClassName;
import ooplang.parser.ClassDef;
import ooplang.parser.MethodDef;
import ooplang.parser.Variable;
import ooplang.parser.Type;
import ooplang.parser.Param;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

// Need to record:
// - Methods on a class (mapping signature to definition)
//     - The class that originally introduced the method
//     - The class we are inheriting this methoed from
// - Instance variables on a class (mapping variable to type)
//     - The class we are inheriting this instance variable from
public class ClassInformation {
    private final Map<ClassName, SingleClassInformation> classes;

    public ClassInformation(final List<ClassDef> classList) throws TypeErrorException {
        classes = new HashMap<ClassName, SingleClassInformation>();
        addClasses(classList);
    }

    public SingleClassInformation baseClass(final ClassName className) throws TypeErrorException {
        final SingleClassInformation self = getClass(className);
        if (self.classDef.extendsName.isPresent()) {
            return baseClass(self.classDef.extendsName.get());
        } else {
            return self;
        }
    } // baseClass
    
    public SingleClassInformation getClass(final ClassName className) throws TypeErrorException {
        final SingleClassInformation retval = classes.get(className);
        if (retval == null) {
            throw new TypeErrorException("No such class: " + className.toString());
        } else {
            return retval;
        }
    } // getClass

    public MethodDef getMethod(final ClassName target,
                               final MethodSignature signature) throws TypeErrorException {
        return getMethodInformation(target, signature).methodDef;
    } // getMethod
    
    public MethodInformation getMethodInformation(final ClassName target,
                                                  final MethodSignature signature) throws TypeErrorException {
        final MethodInformation info = getClass(target).methods.get(signature);
        if (info == null) {
            throw new TypeErrorException("No such method on class " +
                                         target.toString() +
                                         " with signature " +
                                         signature.toString());
        } else {
            return info;
        }
    } // getMethodInformation

    public Type typeofField(final ClassName className,
                            final Variable fieldName) throws TypeErrorException {
        final InstanceVariableInformation info =
            getClass(className).instanceVariables.get(fieldName);
        if (info == null) {
            throw new TypeErrorException("No such field " +
                                         fieldName.toString() +
                                         " on class " +
                                         className.toString());
        } else {
            return info.type;
        }
    } // typeofField
    
    public boolean classExists(final ClassName className) {
        return classes.containsKey(className);
    } // classExists

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
            final SingleClassInformation parent = classes.get(superName);
            assert(parent != null);
            myMethods.putAll(parent.methods);
            myInstanceVariables.putAll(parent.instanceVariables);
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
                              new MethodInformation(methodDef,
                                                    myName,
                                                    myName));
            } else {
                // overriding existing method
                // make sure the return type is the same
                if (methodDef.returnType.equals(existingMethod.methodDef.returnType)) {
                    myMethods.put(signature,
                                  new MethodInformation(methodDef,
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
        
        // save it
        classes.put(myName,
                    new SingleClassInformation(classDef,
                                               myMethods,
                                               myInstanceVariables));
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
                
    private void addClasses(final Map<ClassName, ClassDef> classes) throws TypeErrorException {
        for (final ClassDef classDef : classes.values()) {
            transitivelyAddClass(classes, classDef);
        }
    } // addClasses

    // adds all the superclasses of the given class to supers
    public static void superClasses(final Map<ClassName, ClassDef> classes,
                                    final Set<ClassName> supers,
                                    final ClassName forClass) throws TypeErrorException {
        supers.add(forClass);
        final ClassDef classDef = classes.get(forClass);
        if (classDef == null) {
            throw new TypeErrorException("No such class: " + forClass.toString());
        } else if (classDef.extendsName.isPresent()) {
            final ClassName superClass = classDef.extendsName.get();
            if (supers.contains(superClass)) {
                throw new TypeErrorException("Cyclic inheritance involving: " + forClass.toString());
            } else {
                superClasses(classes, supers, superClass);
            }
        }
    } // superClasses

    // gets all superclasses of the given class
    // throws an exception on cyclic inheritance
    public static Set<ClassName> superClasses(final Map<ClassName, ClassDef> classes,
                                              final ClassName forClass) throws TypeErrorException {
        final Set<ClassName> retval = new HashSet<ClassName>();
        superClasses(classes, retval, forClass);
        return retval;
    } // superClasses

    public static void assertNoCyclicInheritance(final Map<ClassName, ClassDef> classes) throws TypeErrorException {
        for (final ClassName className : classes.keySet()) {
            superClasses(classes, className);
        }
    } // assertNoCyclicInheritance

    private void addClasses(final List<ClassDef> classes) throws TypeErrorException {
        final Map<ClassName, ClassDef> classMap = new HashMap<ClassName, ClassDef>();
        for (final ClassDef classDef : classes) {
            if (!classMap.containsKey(classDef.className)) {
                classMap.put(classDef.className, classDef);
            } else {
                throw new TypeErrorException("Class with duplicate name: " +
                                             classDef.className.toString());
            }
        }
        assertNoCyclicInheritance(classMap);
        addClasses(classMap);
    } // addClasses
} // ClassInformation
