package xyz.chromabeam.util.tuples.mutable;

import xyz.chromabeam.util.tuples.immutable.PairI;

public class Pair<A, B> implements PairI<A, B> {
    public A a;
    public B b;
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public A a() {
        return a;
    }

    @Override
    public B b() {
        return b;
    }

    public Pair<A, B> with(A a, B b) {
        this.a = a;
        this.b = b;
        return this;
    }
}
