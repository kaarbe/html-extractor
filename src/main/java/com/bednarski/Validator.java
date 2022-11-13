package com.bednarski;

import java.util.stream.Collectors;

public class Validator {

  public static boolean isValid(final String input) {
    int uniqueCharsCount = input
        .chars()
        .mapToObj(c -> (char) c)
        .collect(Collectors.toSet())
        .size();
    return uniqueCharsCount > 4;
  }
}
