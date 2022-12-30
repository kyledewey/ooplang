package ooplang.parser;

import java.util.List;

public class MethodDef {
    public final Type returnType;
    public final MethodName methodName;
    public final List<Param> params;
    public final Stmt body;

    public MethodDef(final Type returnType,
                     final MethodName methodName,
                     final List<Param> params,
                     final Stmt body) {
        this.returnType = returnType;
        this.methodName = methodName;
        this.params = params;
        this.body = body;
    }

    @Override
    public int hashCode() {
        return (returnType.hashCode() +
                methodName.hashCode() +
                params.hashCode() +
                body.hashCode());
    }

    @Override
    public String toString() {
        return ("MethodDef(" +
                returnType.toString() + ", " +
                methodName.toString() + ", " +
                params.toString() + ", " +
                body.toString() + ")");
    }
}
