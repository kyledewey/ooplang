package ooplang.codegen;

import ooplang.parser.ClassName;

import ooplang.typechecker.ClassInformation;
import ooplang.typechecker.SingleClassInformation;
import ooplang.typechecker.MethodInformation;
import ooplang.typechecker.MethodSignature;
import ooplang.typechecker.TypeErrorException;

import java.util.Map;
import java.util.HashMap;

public class VTable {
    final ClassInformation classInformation;
    final Map<ClassName, MethodInformation[]> vtables;
    
    public VTable(final ClassInformation classInformation) {
        this.classInformation = classInformation;
        vtables = new HashMap<ClassName, MethodInformation[]>();
    }

    public int methodIndex(final ClassName forClass,
                           final MethodSignature signature) throws TypeErrorException, CodegenException {
        final MethodInformation[] table = getVTable(forClass);
        for (int index = 0; index < table.length; index++) {
            final MethodSignature thisSignature =
                MethodSignature.getSignature(table[index].methodDef);
            if (signature.equals(thisSignature)) {
                return index;
            }
        }
        throw new CodegenException("No such signature " +
                                   signature.toString() +
                                   " for class " +
                                   forClass.toString());
    } // methodIndex
    
    public MethodInformation[] getVTable(final ClassName forClass) throws TypeErrorException {
        MethodInformation[] retval = vtables.get(forClass);
        if (retval != null) {
            return retval;
        }

        // compute it if we don't have it
        final SingleClassInformation info = classInformation.getClass(forClass);
        retval = new MethodInformation[info.methods.size()];
        int newMethodsStartAt = 0;
        if (info.classDef.extendsName.isPresent()) {
            MethodInformation[] parentVTable = getVTable(info.classDef.extendsName.get());
            for (int index = 0; index < parentVTable.length; index++) {
                // basic idea: we want each entry to be the same method as the parent,
                // or be an override of the parent.  Get the corresponding MethodInformation
                // object for _this_ class for each entry.
                retval[index] = classInformation.getMethodInformation(forClass,
                                                                      MethodSignature.getSignature(parentVTable[index].methodDef));
            }
            newMethodsStartAt = parentVTable.length;
        }
        // remaining methods can go in anywhere at the end
        // look for methods that are from this class
        for (final MethodInformation method : info.methods.values()) {
            if (method.originalDefiner.equals(forClass)) {
                retval[newMethodsStartAt++] = method;
            }
        }
        assert(newMethodsStartAt == retval.length);
        vtables.put(forClass, retval);
        return retval;
    } // getVTable
} // VTable


