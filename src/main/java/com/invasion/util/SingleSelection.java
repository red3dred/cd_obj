package com.invasion.util;

public record SingleSelection<T>(T object) implements Select<T> {
    @Override
    public T selectNext() {
        return object;
    }

    @Override
    public void reset() {
    }

    @Override
    public String toString() {
        return object.toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public SingleSelection<T> clone() {
        return object instanceof Select<?> i ? new SingleSelection<>((T)i.clone()) : this;
    }
}