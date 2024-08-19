package com.invasion.util;

public interface Select<T> {
  T selectNext();

  void reset();

  Select<T> clone();
}