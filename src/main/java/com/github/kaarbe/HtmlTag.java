package com.github.kaarbe;

import java.util.List;

class HtmlTag {

  private final StringBuilder content;
  private final int startIndex;
  private int endIndex;

  private HtmlTag(final int startIndex) {
    this.startIndex = startIndex;
    this.content = new StringBuilder();
  }

  static HtmlTag findOne(final int startIndex, final List<Character> chars) {
    int currentIndex = findTagOpeningChar(startIndex, chars);
    HtmlTag tag = HtmlTag.withStartIndex(currentIndex);
    while (!Identifier.isTagClosingChar(chars.get(currentIndex)) && currentIndex < chars.size()) {
      tag.append(chars.get(currentIndex++));
    }
    return tag
        .append(chars.get(currentIndex))
        .setEndIndex(currentIndex);
  }

  private static int findTagOpeningChar(final int startIndex, final List<Character> chars) {
    int index = startIndex;
    while (!Identifier.isTagOpeningChar(chars.get(index))) {
      index++;
    }
    return index;
  }

  private static HtmlTag withStartIndex(final int startIndex) {
    return new HtmlTag(startIndex);
  }

  HtmlTag append(final char c) {
    this.content.append(c);
    return this;
  }

  boolean isOpening() {
    final String content = this.content.toString();
    return !content.startsWith("</")
        && content.startsWith("<")
        && content.endsWith(">");
  }

  boolean isClosing() {
    final String content = this.content.toString();
    return content.startsWith("</")
        && content.endsWith(">");
  }

  boolean isPairWith(final HtmlTag other) {
    if (!(this.isOpening() && other.isClosing())) {
      return false;
    }
    final String content = this.content
        .toString()
        .replace("<", "")
        .replace(">", "");
    final String otherContent = other.content
        .toString()
        .replace("</", "")
        .replace(">", "");
    if (content.isEmpty() || otherContent.isEmpty()) {
      return false;
    }
    return content.equals(otherContent);
  }

  int getStartIndex() {
    return startIndex;
  }

  int getEndIndex() {
    return endIndex;
  }

  HtmlTag setEndIndex(final int endIndex) {
    this.endIndex = endIndex;
    return this;
  }
}
