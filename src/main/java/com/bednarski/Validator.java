package com.bednarski;

import java.util.Set;
import java.util.stream.Collectors;

public class Validator {

  /**
   * Returns a boolean value indicating if given text input is considered valid. An input is valid if it contains all
   * characters required to construct an HTML tag ('<', '>', '/') and has at least 8 characters in general and number
   * of its unique characters is higher than 3, since the shortest possible character combination to create an input
   * that may contain some text to extract is '&lt;a&gt;a&lt;/a&gt;'
   *
   * @param input a text to validate.
   * @return true if given input is considered valid or false if otherwise.
   */
  public static boolean isValid(final String input) {
    if (input.length() < 8) {
      return false;
    }
    Set<Character> uniqueChars = input
        .chars()
        .mapToObj(c -> (char) c)
        .collect(Collectors.toSet());
    return uniqueChars.size() > 3
        && uniqueChars.contains('<')
        && uniqueChars.contains('>')
        && uniqueChars.contains('/');
  }
}
