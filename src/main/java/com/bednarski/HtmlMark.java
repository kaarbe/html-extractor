package com.bednarski;

public class HtmlMark {

  private int startIndex;
  private int endIndex;
  private StringBuilder content;

  public HtmlMark() {
    this.content = new StringBuilder();
  }

  private HtmlMark(int startIndex) {
    this.startIndex = startIndex;
    this.content = new StringBuilder();
  }

  public static HtmlMark withStartIndex(int startIndex) {
    return new HtmlMark(startIndex);
  }

  public void append(char c) {
    this.content.append(c);
  }

  public boolean isOpening() {
    String content = this.content.toString();
    return !content.startsWith("</") && content.startsWith("<") && content.endsWith(">");
  }

  public boolean isClosing() {
    String content = this.content.toString();
    return content.startsWith("</") && content.endsWith(">");
  }

  public boolean isPairWith(HtmlMark other) {
    if (!(this.isOpening() && other.isClosing())) {
      return false;
    }
    String content = this.content.toString().replace("<", "").replace(">", "");
    String otherContent = other.content.toString().replace("</", "").replace(">", "");
    return content.equals(otherContent);
  }

  public int getStartIndex() {
    return startIndex;
  }

  public void setStartIndex(int startIndex) {
    this.startIndex = startIndex;
  }

  public int getEndIndex() {
    return endIndex;
  }

  public void setEndIndex(int endIndex) {
    this.endIndex = endIndex;
  }

  public StringBuilder getContent() {
    return content;
  }

  public void setContent(StringBuilder content) {
    this.content = content;
  }
}
