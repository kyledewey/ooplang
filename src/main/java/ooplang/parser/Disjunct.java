package ooplang.parser;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

// used for doing OR in a grammar
public abstract class Disjunct<A> {
    public abstract ParseResult<A> parse(final int position) throws ParseException;

    public Disjunct<Optional<A>> optional() {
        final Disjunct<A> self = this;
        return new Disjunct<Optional<A>>() {
            public ParseResult<Optional<A>> parse(final int position) {
                try {
                    final ParseResult<A> a = self.parse(position);
                    return new ParseResult<Optional<A>>(Optional.of(a.result),
                                                        a.nextPosition);
                } catch (final ParseException e) {
                    return new ParseResult<Optional<A>>(Optional.empty(),
                                                        position);
                }
            }
        };
    }
    
    public Disjunct<List<A>> star() {
        final Disjunct<A> self = this;
        return new Disjunct<List<A>>() {
            public ParseResult<List<A>> parse(int position) throws ParseException {
                final List<A> as = new ArrayList<A>();
                boolean shouldRun = true;
                while (shouldRun) {
                    try {
                        final ParseResult<A> a = self.parse(position);
                        position = a.nextPosition;
                        as.add(a.result);
                    } catch (final ParseException e) {
                        shouldRun = false;
                    }
                }
                return new ParseResult<List<A>>(as, position);
            }
        };
    } // star
    
    public Disjunct<A> or(final Disjunct<A> other) {
        final Disjunct<A> self = this;
        return new Disjunct<A>() {
            public ParseResult<A> parse(final int position) throws ParseException {
                try {
                    return self.parse(position);
                } catch (final ParseException e) {
                    return other.parse(position);
                }
            }
        };
    } // or

    public static <A> Disjunct<A> or(final Disjunct<A>... disjuncts) {
        assert(disjuncts.length > 0);
        Disjunct<A> retval = disjuncts[disjuncts.length - 1];
        for (int index = disjuncts.length - 2; index >= 0; index--) {
            retval = disjuncts[index].or(retval);
        }
        return retval;
    }
} // Disjunct
