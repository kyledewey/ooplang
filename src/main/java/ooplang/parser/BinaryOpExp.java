package ooplang.parser;

public class BinaryOpExp implements Exp {
    public final Op op;
    public final Exp left;
    public final Exp right;

    public BinaryOpExp(final Op op,
                       final Exp left,
                       final Exp right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof BinaryOpExp) {
            final BinaryOpExp asBin = (BinaryOpExp)other;
            return (op.equals(asBin.op) &&
                    left.equals(asBin.left) &&
                    right.equals(asBin.right));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (op.hashCode() +
                left.hashCode() +
                right.hashCode());
    }

    @Override
    public String toString() {
        return ("BinaryOpExp(" +
                op.toString() + ", " +
                left.toString() + ", " +
                right.toString() + ")");
    }
}
