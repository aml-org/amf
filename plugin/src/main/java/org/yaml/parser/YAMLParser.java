package org.yaml.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.containers.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.YAMLTokenTypes;
import org.yaml.YamlElementTypes;


/**
 * @author oleg
 */
public class YAMLParser implements PsiParser {
  public static final TokenSet HASH_STOP_TOKENS = TokenSet.create(YAMLTokenTypes.RBRACE, YAMLTokenTypes.COMMA);
  public static final TokenSet ARRAY_STOP_TOKENS = TokenSet.create(YAMLTokenTypes.RBRACKET, YAMLTokenTypes.COMMA);
  private PsiBuilder myBuilder;
  private boolean eolSeen = false;
  private int myIndent;
  private PsiBuilder.Marker myAfterLastEolMarker;

  private final Stack<TokenSet> myStopTokensStack = new Stack<>();

  @NotNull
  public ASTNode parse(@NotNull final IElementType root, @NotNull final PsiBuilder builder) {
    myBuilder = builder;
    myStopTokensStack.clear();
    final PsiBuilder.Marker fileMarker = mark();
    parseFile();
    assert myBuilder.eof() : "Not all tokens were passed.";
    fileMarker.done(root);
    return builder.getTreeBuilt();
  }

  private void parseFile() {
    passJunk();
    parseDocument();
    passJunk();
    while (!myBuilder.eof()) {
      parseDocument();
      passJunk();
    }
    dropEolMarker();
  }

  private void parseDocument() {
    final PsiBuilder.Marker marker = mark();
    if (myBuilder.getTokenType() == YAMLTokenTypes.DOCUMENT_MARKER) {
      advanceLexer();
    }
    parseBlockNode(0, false);
    dropEolMarker();
    marker.done(YamlElementTypes.DOCUMENT());
  }

  private void parseBlockNode(int indent, boolean insideSequence) {
    passJunk();

    final PsiBuilder.Marker marker = mark();
    PsiBuilder.Marker endOfNodeMarker = null;
    IElementType nodeType = null;


    // It looks like tag for a block node should be located on a separate line
    if (getTokenType() == YAMLTokenTypes.TAG && myBuilder.lookAhead(1) == YAMLTokenTypes.EOL) {
      advanceLexer();
    }

    int numberOfItems = 0;
    while (!eof() && (isJunk() || !eolSeen || myIndent + getIndentBonus(insideSequence) >= indent)) {
      if (isJunk()) {
        advanceLexer();
        continue;
      }

      if (!myStopTokensStack.isEmpty() && myStopTokensStack.peek().contains(getTokenType())) {
        rollBackToEol();
        break;
      }

      numberOfItems++;
      final IElementType parsedTokenType = parseSingleStatement(eolSeen ? myIndent : indent);
      if (nodeType == null) {
        if (parsedTokenType == YamlElementTypes.SEQUENCE_ITEM()) {
          nodeType = YamlElementTypes.SEQUENCE();
        }
        else if (parsedTokenType == YamlElementTypes.KEY_VALUE_PAIR()) {
          nodeType = YamlElementTypes.MAPPING();
        }
        else if (numberOfItems > 1) {
          nodeType = YamlElementTypes.COMPOUND_VALUE();
        }
      }
      if (endOfNodeMarker != null) {
        endOfNodeMarker.drop();
      }
      endOfNodeMarker = mark();

    }

    if (endOfNodeMarker != null) {
      dropEolMarker();
      endOfNodeMarker.rollbackTo();
    }
    else {
      rollBackToEol();
    }
    if (nodeType != null) {
      marker.done(nodeType);
    }
    else {
      marker.drop();
    }
  }

  /**
   * @link {http://www.yaml.org/spec/1.2/spec.html#id2777534}
   */
  private int getIndentBonus(final boolean insideSequence) {
    if (!insideSequence && getTokenType() == YAMLTokenTypes.SEQUENCE_MARKER) {
      return 1;
    }
    else {
      return 0;
    }
  }

  private int getShorthandIndentAddition() {
    final int offset = myBuilder.getCurrentOffset();
    final IElementType nextToken = myBuilder.lookAhead(1);
    if (nextToken != YAMLTokenTypes.SEQUENCE_MARKER && nextToken != YAMLTokenTypes.SCALAR_KEY) {
      return 1;
    }
    if (myBuilder.rawLookup(1) == YAMLTokenTypes.WHITESPACE) {
      return myBuilder.rawTokenTypeStart(2) - offset;
    }
    else {
      return 1;
    }
  }

  @Nullable
  private IElementType parseSingleStatement(int indent) {
    if (eof()) {
      return null;
    }

    final PsiBuilder.Marker marker = mark();
    if (getTokenType() == YAMLTokenTypes.TAG) {
      advanceLexer();
    }

    final IElementType tokenType = getTokenType();
    final IElementType nodeType;
    if (tokenType == YAMLTokenTypes.LBRACE) {
      nodeType = parseHash();
    }
    else if (tokenType == YAMLTokenTypes.LBRACKET) {
      nodeType = parseArray();
    }
    else if (tokenType == YAMLTokenTypes.SEQUENCE_MARKER) {
      nodeType = parseSequenceItem(indent);
    }
    else if (tokenType == YAMLTokenTypes.QUESTION) {
      nodeType = parseExplicitKeyValue(indent);
    }
    else if (tokenType == YAMLTokenTypes.SCALAR_KEY) {
      nodeType = parseScalarKeyValue(indent);
    }
    else if (YamlElementTypes.SCALAR_VALUES().contains(getTokenType())) {
      nodeType = parseScalarValue(indent);
    }
    else {
      advanceLexer();
      nodeType = null;
    }

    if (nodeType != null) {
      marker.done(nodeType);
    }
    else {
      marker.drop();
    }
    return nodeType;
  }

  @Nullable
  private IElementType parseScalarValue(int indent) {
    final IElementType tokenType = getTokenType();
    assert YamlElementTypes.SCALAR_VALUES().contains(tokenType) : "Scalar value expected!";
    if (tokenType == YAMLTokenTypes.SCALAR_LIST || tokenType == YAMLTokenTypes.SCALAR_TEXT) {
      return parseMultiLineScalar(tokenType);
    }
    else if (tokenType == YAMLTokenTypes.TEXT) {
      return parseMultiLinePlainScalar(indent);
    }
    else if (tokenType == YAMLTokenTypes.SCALAR_DSTRING || tokenType == YAMLTokenTypes.SCALAR_STRING) {
      return parseQuotedString();
    }
    else {
      advanceLexer();
      return null;
    }
  }

  @NotNull
  private IElementType parseQuotedString() {
    advanceLexer();
    return YamlElementTypes.SCALAR_QUOTED_STRING();
  }

  @NotNull
  private IElementType parseMultiLineScalar(final IElementType tokenType) {
    int indent = -1;
    IElementType type = getTokenType();
    PsiBuilder.Marker endOfValue = null;
    while (type == tokenType || type == YAMLTokenTypes.INDENT || type == YAMLTokenTypes.EOL) {
      if (indent == -1 && type == YAMLTokenTypes.INDENT) {
        indent = getCurrentTokenLength();
      }

      if (type == tokenType || type == YAMLTokenTypes.INDENT && getCurrentTokenLength() > indent) {
        advanceLexer();
        if (endOfValue != null) {
          endOfValue.drop();
        }
        endOfValue = myBuilder.mark();
      }
      else {
        advanceLexer();
      }

      type = getTokenType();
    }
    if (endOfValue == null) {
      rollBackToEol();
    }
    else {
      dropEolMarker();
      endOfValue.rollbackTo();
    }
    return tokenType == YAMLTokenTypes.SCALAR_LIST ? YamlElementTypes.SCALAR_LIST_VALUE() : YamlElementTypes.SCALAR_TEXT_VALUE();
  }

  @NotNull
  private IElementType parseMultiLinePlainScalar(final int indent) {
    PsiBuilder.Marker lastTextEnd = null;

    IElementType type = getTokenType();
    while (type == YAMLTokenTypes.TEXT || type == YAMLTokenTypes.INDENT || type == YAMLTokenTypes.EOL) {
      advanceLexer();

      if (type == YAMLTokenTypes.TEXT) {
        if (lastTextEnd != null && myIndent < indent) {
          break;
        }
        if (lastTextEnd != null) {
          lastTextEnd.drop();
        }
        lastTextEnd = mark();
      }
      type = getTokenType();
    }

    rollBackToEol();
    assert lastTextEnd != null;
    lastTextEnd.rollbackTo();
    return YamlElementTypes.SCALAR_PLAIN_VALUE();
  }

  @NotNull
  private IElementType parseExplicitKeyValue(int indent) {
    assert getTokenType() == YAMLTokenTypes.QUESTION;

    int indentAddition = getShorthandIndentAddition();
    advanceLexer();

    if (!myStopTokensStack.isEmpty() && myStopTokensStack.peek() == HASH_STOP_TOKENS // This means we're inside some hash
        && getTokenType() == YAMLTokenTypes.SCALAR_KEY) {
      parseScalarKeyValue(indent);
    }
    else {
      myStopTokensStack.add(TokenSet.create(YAMLTokenTypes.COLON));
      eolSeen = false;

      passJunk();

      parseBlockNode(indent + indentAddition, false);

      myStopTokensStack.pop();

      passJunk();
      if (getTokenType() == YAMLTokenTypes.COLON) {
        indentAddition = getShorthandIndentAddition();
        advanceLexer();

        eolSeen = false;
        parseBlockNode(indent + indentAddition, false);
      }
    }

    return YamlElementTypes.KEY_VALUE_PAIR();
  }


  @NotNull
  private IElementType parseScalarKeyValue(int indent) {
    assert getTokenType() == YAMLTokenTypes.SCALAR_KEY : "Expected scalar key";
    eolSeen = false;

    int indentAddition = getShorthandIndentAddition();
    advanceLexer();

    final PsiBuilder.Marker rollbackMarker = mark();

    passJunk();
    if (eolSeen && (eof() || myIndent + getIndentBonus(false) < indent + indentAddition)) {
      dropEolMarker();
      rollbackMarker.rollbackTo();
    }
    else {
      rollbackMarker.drop();
      parseBlockNode(indent + indentAddition, false);
    }

    return YamlElementTypes.KEY_VALUE_PAIR();
  }

  @NotNull
  private IElementType parseSequenceItem(int indent) {
    assert getTokenType() == YAMLTokenTypes.SEQUENCE_MARKER;

    int indentAddition = getShorthandIndentAddition();
    advanceLexer();
    eolSeen = false;
    passJunk();

    parseBlockNode(indent + indentAddition, true);
    rollBackToEol();
    return YamlElementTypes.SEQUENCE_ITEM();
  }

  @NotNull
  private IElementType parseHash() {
    assert getTokenType() == YAMLTokenTypes.LBRACE;
    advanceLexer();
    myStopTokensStack.add(HASH_STOP_TOKENS);

    while (!eof()) {
      if (getTokenType() == YAMLTokenTypes.RBRACE) {
        advanceLexer();
        break;
      }
      parseSingleStatement(0);
    }

    myStopTokensStack.pop();
    dropEolMarker();
    return YamlElementTypes.HASH();
  }

  @NotNull
  private IElementType parseArray() {
    assert getTokenType() == YAMLTokenTypes.LBRACKET;
    advanceLexer();
    myStopTokensStack.add(ARRAY_STOP_TOKENS);

    while (!eof()) {
      if (getTokenType() == YAMLTokenTypes.RBRACKET) {
        advanceLexer();
        break;
      }
      if (isJunk()) {
        advanceLexer();
        continue;
      }

      final PsiBuilder.Marker marker = mark();
      final IElementType parsedElement = parseSingleStatement(0);
      if (parsedElement != null) {
        marker.done(YamlElementTypes.SEQUENCE_ITEM());
      }
      else {
        marker.error("Sequence item expected");
      }

      if (getTokenType() == YAMLTokenTypes.COMMA) {
        advanceLexer();
      }
    }

    myStopTokensStack.pop();
    dropEolMarker();
    return YamlElementTypes.ARRAY();
  }

  private boolean eof() {
    return myBuilder.eof() || myBuilder.getTokenType() == YAMLTokenTypes.DOCUMENT_MARKER;
  }

  @Nullable
  private IElementType getTokenType() {
    return eof() ? null : myBuilder.getTokenType();
  }

  private void dropEolMarker() {
    if (myAfterLastEolMarker != null) {
      myAfterLastEolMarker.drop();
      myAfterLastEolMarker = null;
    }
  }

  private void rollBackToEol() {
    if (eolSeen && myAfterLastEolMarker != null) {
      eolSeen = false;
      myAfterLastEolMarker.rollbackTo();
      myAfterLastEolMarker = null;
    }
  }

  private PsiBuilder.Marker mark() {
    dropEolMarker();
    return myBuilder.mark();
  }

  private void advanceLexer() {
    if (myBuilder.eof()) {
      return;
    }
    final IElementType type = myBuilder.getTokenType();
    eolSeen = eolSeen || type == YAMLTokenTypes.EOL;
    if (type == YAMLTokenTypes.EOL) {
      // Drop and create new eolMarker
      myAfterLastEolMarker = mark();
      myIndent = 0;
    }
    else if (type == YAMLTokenTypes.INDENT) {
      myIndent = getCurrentTokenLength();
    }
    else {
      // Drop Eol Marker if other token seen
      dropEolMarker();
    }
    myBuilder.advanceLexer();
  }

  private int getCurrentTokenLength() {
    return myBuilder.rawTokenTypeStart(1) - myBuilder.getCurrentOffset();
  }

  private void passJunk() {
    while (!eof() && isJunk()) {
      advanceLexer();
    }
  }

  private boolean isJunk() {
    final IElementType type = getTokenType();
    return type == YAMLTokenTypes.INDENT || type == YAMLTokenTypes.EOL;
  }
}