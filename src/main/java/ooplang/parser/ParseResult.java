package ooplang.parser;

public class ParseResult<A> {
    public final A result;
    public final int nextPosition;

    public ParseResult(final A result,
                       final int nextPosition) {
        this.result = result;
        this.nextPosition = nextPosition;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof ParseResult) {
            final ParseResult<A> otherRes = (ParseResult<A>)other;
            return (result.equals(otherRes.result) &&
                    nextPosition == otherRes.nextPosition);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return result.hashCode() + nextPosition;
    }

    @Override
    public String toString() {
        return ("ParseResult(" +
                result.toString() + ", " +
                nextPosition + ")");
    }
}
