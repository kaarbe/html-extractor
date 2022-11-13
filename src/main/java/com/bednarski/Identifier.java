package com.bednarski;

public class Identifier {

  public static boolean isTagClosingChar(final char c) {
    return Character.valueOf('>').equals(c);
  }

  public static boolean isTagOpeningChar(final char c) {
    return Character.valueOf('<').equals(c);
  }
}
