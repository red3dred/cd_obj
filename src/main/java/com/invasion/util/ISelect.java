package com.invasion.util;

public interface ISelect<T> {
  T selectNext();

  void reset();

  ISelect<T> clone();
}