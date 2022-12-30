package ooplang.parser;

import java.util.List;

public class NewExp implements Exp {
    public final ClassName className;
    public final List<Exp> exps;

    public NewExp(final ClassName className,
                  final List<Exp> exps) {
        this.className = className;
        this.exps = exps;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof NewExp) {
            final NewExp asNew = (NewExp)other;
            return (className.equals(asNew.className) &&
                    exps.equals(asNew.exps));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return className.hashCode() + exps.hashCode();
    }

    @Override
    public String toString() {
        return ("NewExp(" +
                className.toString() + ", " +
                exps.toString() + ")");
    }
}
