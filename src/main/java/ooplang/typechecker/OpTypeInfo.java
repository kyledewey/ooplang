package ooplang.typechecker;

import ooplang.parser.Type;

public class OpTypeInfo {
    public final Type returnType;
    public final Type leftType;
    public final Type rightType;

    public OpTypeInfo(final Type returnType,
                      final Type leftType,
                      final Type rightType) {
        this.returnType = returnType;
        this.leftType = leftType;
        this.rightType = rightType;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof OpTypeInfo) {
            final OpTypeInfo otherOp = (OpTypeInfo)other;
            return (returnType.equals(otherOp.returnType) &&
                    leftType.equals(otherOp.leftType) &&
                    rightType.equals(otherOp.rightType));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (returnType.hashCode() +
                leftType.hashCode() +
                rightType.hashCode());
    }

    @Override
    public String toString() {
        return ("OpTypeInfo(" +
                returnType.toString() + ", " +
                leftType.toString() + ", " +
                rightType.toString() + ")");
    }
}
