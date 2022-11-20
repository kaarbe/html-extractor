package com.github.kaarbe;

 class Identifier {

  private Identifier() { }

  /**
   * Checks if a character is equal to '<' which can indicate that it's the beginning of an HTML tag.
   *
   * @param c a character to check.
   * @return true if the character is the HTML opening character, false otherwise.
   */
  static boolean isTagOpeningChar(final char c) {
    return Character.valueOf('<').equals(c);
  }

  /**
   * Checks if a character is equal to '>' which can indicate that it's the ending of an HTML tag.
   *
   * @param c a character to check.
   * @return true if the character is the HTML tag closing character, false otherwise.
   */
  static boolean isTagClosingChar(final char c) {
    return Character.valueOf('>').equals(c);
  }
}
