package xyz.chromabeam.util.tuples.mutable;

import xyz.chromabeam.util.tuples.immutable.TripletI;

public class Triplet<A, B, C> implements TripletI<A, B, C> {
    public A a;
    public B b;
    public C c;
    public Triplet(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public A a() {
        return a;
    }

    @Override
    public B b() {
        return b;
    }

    @Override
    public C c() {
        return c;
    }

    public Triplet<A, B, C> with(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
        return this;
    }
}
