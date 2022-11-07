package com.bednarski;

public class HtmlTag {

  private final StringBuilder content;
  private final int startIndex;
  private int endIndex;

  private HtmlTag(final int startIndex) {
    this.startIndex = startIndex;
    this.content = new StringBuilder();
  }

  public static HtmlTag withStartIndex(final int startIndex) {
    return new HtmlTag(startIndex);
  }

  public void append(char c) {
    this.content.append(c);
  }

  public boolean isOpening() {
    final String content = this.content.toString();
    return !content.startsWith("</")
        && content.startsWith("<")
        && content.endsWith(">");
  }

  public boolean isClosing() {
    final String content = this.content.toString();
    return content.startsWith("</")
        && content.endsWith(">");
  }

  public boolean isPairWith(HtmlTag other) {
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

  public int getStartIndex() {
    return startIndex;
  }

  public int getEndIndex() {
    return endIndex;
  }

  public void setEndIndex(int endIndex) {
    this.endIndex = endIndex;
  }
}
