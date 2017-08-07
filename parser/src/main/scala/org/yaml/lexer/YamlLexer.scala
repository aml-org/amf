package org.yaml.lexer

import java.io.{File, FileReader}
import java.lang.Integer.MAX_VALUE

import org.mulesoft.common.core.countWhile
import org.mulesoft.lexer.LexerInput.EofChar
import org.mulesoft.lexer.{BaseLexer, CharSequenceLexerInput, LexerInput, failfast}
import org.yaml.lexer.YamlCharRules._
import org.yaml.lexer.YamlToken._

import scala.annotation.tailrec

/**
  * Yaml Lexer for 1.2 Specification
  */
final class YamlLexer(input: LexerInput = new CharSequenceLexerInput()) extends BaseLexer[YamlToken](input) {

  //~ Methods ..........................................................................................................

  override protected def initialize(): Unit = {
//    stack = List(new InitialState()(this))
    yamlStream()
    advance()
  }

  override protected def processPending(): Unit = emit(EndStream)

  protected def countSpaces(offset: Int = 0, max: Int = MAX_VALUE): Int =
    countWhile(i => lookAhead(offset + i) == ' ' && i < max)

  def emitIndicator(): Boolean = consumeAndEmit(Indicator)

  /** Check that the current char is the specified one and if so emit it as an Indicator */
  @failfast def indicator(chr: Int): Boolean = currentChar == chr && consumeAndEmit(Indicator)

  /** Count the number of whiteSpaces from the specified offset */
  protected def countWhiteSpaces(offset: Int = 0): Int = countWhile(i => isWhite(lookAhead(i + offset)))

//  def ensureDocumentStarted(): Boolean =
//    if (stack.exists(s => s.isInstanceOf[InDocument])) false
//    else {
//      InDocument()(this)
//      true
//    }

  /**
    * Recognition of lineBreak Sequence<p>
    *
    * [28] b-break	::=	( b-carriage-return b-line-feed ) | b-line-feed
    */
  def lineBreakSequence(offset: Int = 0): Int = {
    val current = lookAhead(offset)
    if (current == '\n') 1 else if (current == '\r' && lookAhead(offset + 1) == '\n') 2 else 0
  }

  /** Utility method to Process the lineBreak */
  @failfast private def lineBreak(t: YamlToken): Boolean = {
    val n = lineBreakSequence()
    n != 0 && { consume(n); emit(t) }
  }

  /**
    * Line Break normalized to lineFeed<p>
    *
    * [29] b-as-line-feed	::=	b-break
    */
  def breakAsLineFeed(): Boolean = lineBreak(LineFeed)

  /**
    * Outside scalar content, YAML allows any line break to be used to terminate lines.<p>
    * [30]	b-non-content	::=	b-break
    */
  def breakNonContent(): Boolean = lineBreak(LineBreak)

  /**
    * URI characters for tags, as specified in RFC2396,
    *  with the addition of the “[” and “]” for presenting IPv6 addresses as proposed in RFC2732.<p>
    *  [39]	ns-uri-char	::=	  “%” ns-hex-digit ns-hex-digit | ns-word-char
    *                     | “#” | “;” | “/” | “?” | “:” | “@” | “&” | “=” | “+” | “$”
    *                     | “,” | “_” | “.” | “!” | “~” | “*” | “'” | “(” | “)” | “[” | “]”
    */
  @failfast private def uriChar() =
    if ("#;/?:@&=+$,_.!~*'()[]".indexOf(currentChar) != -1 || isWordChar(currentChar)) {
      consume()
      true
    } else if (currentChar == '%' && isNsHexDigit(lookAhead(1)) && isNsHexDigit(lookAhead(2))) {
      consume(3)
      true
    } else false

  /**
    * A Tag char cannot contain the “!” character because is used to indicate the end of a named tag handle.
    * In addition, the “[”, “]”, “{”, “}” and “,” characters are excluded
    * because they would cause ambiguity with flow collection structures.<p>
    *
    * [40] ns-tag-char ::= [[uriChar ns-uri-char]] - “!” - [[isFlowIndicator c-flow-indicator]]
    */
  @failfast private def tagChar() = currentChar != '|' && !isFlowIndicator(currentChar) && uriChar()

  /**
    * Process an indentation exactly as the current one<p>
    *  [63]	s-indent(n)	::=	s-space × n
    */
  def indent(n: Int): Boolean = n <= 0 || n == countSpaces(0, n) && consumeAndEmit(n, Indent)

  /**
    * Detect an indentation lower than the current one<p>
    *
    *  [64]	s-indent(&lt;n)	::=	s-space × m (Where m < n)
    */
  def indentLess(n: Int): Boolean = {
    val m = countSpaces()
    m < n && consumeAndEmit(m, Indent)
  }

  /**
    * Detect an indentation lower or equal to the current one<p>
    *
    *  [64]	s-indent(&le;n)	::=	s-space × m (Where m ≤ n)
    */
  def indentLessOrEqual(n: Int): Boolean = {
    val m = countSpaces()
    m <= n && consumeAndEmit(m, Indent)
  }

  /**
    * [66]	s-separate-in-line	::=	s-white+ | /* Start of line */
    */
  @failfast private def separateInLine(): Boolean = {
    val n = countWhiteSpaces()
    n > 0 && consumeAndEmit(n, WhiteSpace) || beginOfLine
  }

  /**
    * <blockquote><pre>
    *     [67]	s-line-prefix(n,c)	::=
    *                                   c = block-out ⇒ s-block-line-prefix(n)
    *                                   c = block-in  ⇒ s-block-line-prefix(n)
    *                                   c = flow-out  ⇒ s-flow-line-prefix(n)
    *                                   c = flow-in   ⇒ s-flow-line-prefix(n)
    *     [68]	s-block-line-prefix(n)	::=	s-indent(n)
    *     [69]	s-flow-line-prefix(n)	::=	s-indent(n) s-separate-in-line?
    *     [70]	l-empty(n,c)	::=	( s-line-prefix(n,c) | s-indent(&lt;n) ) b-as-line-feed
    *
    * </blockquote></pre>
    */
  def emptyLine(n: Int, ctx: YamlContext): Boolean =
    linePrefix(n, ctx == FlowOut || ctx == FlowIn, emptyLine = true) && breakAsLineFeed()

  private def linePrefix(n: Int, flow: Boolean, emptyLine: Boolean = false): Boolean =
    if (!beginOfLine) false
    else {
      if (directivesEnd(emit = false) || documentEnd(emit = false)) return false
      val spaces = countSpaces(0, n)
      if (!emptyLine && spaces < n) return false
      val whiteSpaces = if (flow) countWhiteSpaces(spaces) else 0
      if (emptyLine && !isBBreak(lookAhead(spaces + whiteSpaces))) false
      else {
        consumeAndEmit(spaces, Indent)
        consumeAndEmit(whiteSpaces, WhiteSpace)
      }
    }

  /**
    * If a line break is followed by an empty line, it is trimmed.
    * The first line break is discarded and the rest are retained as content.<p>
    * [71]	b-l-trimmed(n,c)	::=	[[breakNonContent b-non-content]] [[emptyLine l-empty(n,c)]]+
    */
  private def trimmed(n: Int, ctx: YamlContext): Boolean = matches(breakNonContent() && oneOrMore(emptyLine(n, ctx)))

  /**
    * Convert a lineBreak to an space (fold it)<p>
    * [72]   	b-as-space	::=	b-break
    */
  def breakAsSpace(): Boolean = lineBreak(LineFold)

  /**
    * A folded non-empty line may end with either of the above line breaks.<p>
    * [73]	b-l-folded(n,c)	::=	[[trimmed b-l-trimmed(n,c)]] | [[breakAsSpace b-as-space]]
    */
  private def folded(n: Int, ctx: YamlContext): Boolean =
    isBBreak(currentChar) && (matches(trimmed(n, ctx)) || breakAsSpace())

  /**
    * The combined effect of the flow line folding rules is that each “paragraph” is interpreted as a line,
    * empty lines are interpreted as line feeds,
    * and text can be freely more-indented without affecting the content information<p>
    *
    *  [74]	s-flow-folded(n)	::=	[[separateInLine s-separate-in-line]]?
    *                               [[folded b-l-folded(n,flow-in)]]
    *                               [[linePrefix s-flow-line-prefix(n)]]
    */
  private def flowFolded(n: Int): Boolean =
    matches({
      separateInLine()
      folded(n, FlowIn) && linePrefix(n, flow = true)
    })

  /**
    * [75]	c-nb-comment-text	::=	“#” nb-char*
    *
    * Actually it is doing:
    *  commentText ::= '#' nb-char* b-comment
    */
  def commentText(): Boolean = {
    if (currentChar != '#') return false
    emit(BeginComment)
    consumeAndEmit(Indicator)
    while (!isBreakComment(currentChar)) consume()
    emit(MetaText)
    emit(EndComment)
    breakComment()
  }

  /**
    * [76]	b-comment	::=	[[breakNonContent b-non-content]] | EofChar
    */
  def breakComment(): Boolean = currentChar == EofChar || breakNonContent()

  /**
    * [77]	s-b-comment	::=	( s-separate-in-line c-nb-comment-text? )? b-comment*
    * Actually refactored to:
    *   b-comment | l-comment
    */
  def spaceBreakComment(): Boolean = breakComment() || lineComment()

  /**
    * <blockquote><pre>
    *
    * [78]	l-comment	::=	s-separate-in-line c-nb-comment-text? b-comment
    *
    * Actually refactored to:
    *   [[separateInLine s-separate-in-line]] ([[breakComment b-non-content]] | [[commentText]])
    * </blockquote></pre>
    *
    */
  def lineComment(): Boolean = matches { // can be made failfast...
    separateInLine() && (breakComment() || commentText())
  }

  /**
    * [79]	s-l-comments	::=	( s-b-comment | /* Start of line */ ) l-comment*
    */
  def multilineComment(): Boolean = (beginOfLine || spaceBreakComment()) && zeroOrMore(lineComment())

  /**
    * [80]	s-separate(n,c)	::=	c = block-out ⇒ s-separate-lines(n)
    *                           c = block-in  ⇒ s-separate-lines(n)
    *                           c = flow-out  ⇒ s-separate-lines(n)
    *                           c = flow-in   ⇒ s-separate-lines(n)
    *                           c = block-key ⇒ s-separate-in-line
    *                           c = flow-key  ⇒ s-separate-in-line
    * [81]	s-separate-lines(n)	::=	  ( s-l-comments s-flow-line-prefix(n) ) | s-separate-in-line
    */
  def separate(n: Int, ctx: YamlContext): Boolean = ctx match {
    case FlowKey | BlockKey => separateInLine()
    case _ =>
      matches(multilineComment() && linePrefix(n, flow = true)) || separateInLine()
  }

  /**
    * Directives are instructions to the YAML processor.
    * [82]	l-directive	::=	“%”
    *                     ( [[yamlDirective ns-yaml-directive]]
    *                     | [[tagDirective ns-tag-directive]]
    *                     | [[reservedDirective ns-reserved-directive]] )
    *                     s-l-comments
    */
  private def directive() =
    currentChar == '%' && emit(BeginDirective) && emitIndicator() && (
        matches(yamlDirective()) || matches(tagDirective()) || matches(reservedDirective())
    ) && emit(EndDirective) && multilineComment()

  /**
    * Each directive is specified on a separate non-indented line starting with the “%” indicator,
    * followed by the directive name and a list of parameters.
    * <blockquote><pre>
    *
    * [83]	ns-reserved-directive	::=	ns-directive-name ( s-separate-in-line ns-directive-parameter )*
    * [84]	ns-directive-name	    ::=	ns-char+
    * [85]	ns-directive-parameter	::=	ns-char+
    *
    * </blockquote></pre>
    */
  private def reservedDirective() = {
    def nameOrParameter =
      if (!isNsChar(currentChar)) false
      else {
        do consume() while (isNsChar(currentChar))
        emit(MetaText)
      }
    nameOrParameter && zeroOrMore(separateInLine() && nameOrParameter)
  }

  /**
    * [86]	ns-yaml-directive	::=	“Y” “A” “M” “L” s-separate-in-line ns-yaml-version	
    * [87]	ns-yaml-version	::=	ns-dec-digit+ “.” ns-dec-digit+
    */
  private def yamlDirective() =
    if (!consume("YAML")) false
    else {
      emit(MetaText)
      if (!separateInLine() || !isNsDecDigit(currentChar)) false
      else {
        consumeWhile(isNsDecDigit)
        if (currentChar != '.' || !isNsDecDigit(lookAhead(1))) false
        else {
          consume()
          consumeWhile(isNsDecDigit)
          emit(MetaText)
        }
      }
    }

  /**
    * The “TAG” directive establishes a tag shorthand notation for specifying node tags.
    * Each “TAG” directive associates a handle with a prefix.
    * This allows for compact and readable tag notation.<p>
    * [88]	ns-tag-directive	::=	“T” “A” “G”
    *                               [[separateInLine s-separate-in-line]]
    *                               [[tagHandle c-tag-handle]]
    *                               [[separateInLine s-separate-in-line]]
    *                               [[tagPrefix ns-tag-prefix]]
    */
  private def tagDirective() =
    if (!consume("TAG")) false
    else {
      emit(MetaText)
      separateInLine() && tagHandle() && separateInLine() && tagPrefix()
    }

  /**
    * The tag handle exactly matches the prefix of the affected tag shorthand.
    * There are three tag handle variants:<p>
    *
    * [89]	c-tag-handle	::=	  c-named-tag-handle | c-secondary-tag-handle | c-primary-tag-handle
    * [91]	c-secondary-tag-handle	::=	“!” “!”
    * [92]	c-named-tag-handle	::=	“!” ns-word-char+ “!”
    */
  private def tagHandle(): Boolean =
    emit(BeginHandle) && {
      // secondary tag handle
      lookAhead(1) == '!' && indicator('!') && indicator('!') ||
      // named tag handle
      isWordChar(lookAhead(1)) && matches { // named tag handle
        indicator('!')
        consumeWhile(isWordChar)
        emit(MetaText) && indicator('!')
      } ||
      indicator('!')
    } && emit(EndHandle)

  /**
    * There are two tag prefix variants:<p>
    * Local Tag Prefix:
    *    If the prefix begins with a “!” character, shorthands using the handle are expanded to a local tag.<p>
    * Global Tag Prefix:
    *    If the prefix begins with a character other than “!”, it must to be a valid URI prefix<p>
    *<blockquote><pre>
    *
    *
    * [93]	ns-tag-prefix	        ::=	c-ns-local-tag-prefix | ns-global-tag-prefix
    * [94]	c-ns-local-tag-prefix	::=	“!” ns-uri-char*
    * [95]	ns-global-tag-prefix	::=	ns-tag-char ns-uri-char*
    *
    *</blockquote></pre>
    */
  private def tagPrefix() = matches {
    emit(BeginTag) && {
      indicator('!') && zeroOrMore(uriChar()) || tagChar() && zeroOrMore(uriChar())
    } && emit(MetaText) && emit(EndTag)
  }

  /** Each node may have two optional properties, anchor and tag,
    *  in addition to its content.
    *  Node properties may be specified in any order before the node’s content.
    *  Either or both may be omitted.<p>
    *<blockquote><pre>
    *
    * [96]	c-ns-properties(n,c)	::=
    *                                 ( [[tagProperty c-ns-tag-property]]
    *                                    ( [[separate s-separate(n,c)]] [[anchorProperty c-ns-anchor-property]] )? )
    *                                 |  ( [[anchorProperty c-ns-anchor-property]]
    *                                      ( [[separate s-separate(n,c)]] [[tagProperty c-ns-tag-property]] )? )
    *</blockquote></pre>
    */
  private def nodeProperties(n: Int, c: YamlContext) = matches {
    emit(BeginProperties) && (
        tagProperty() && optional(matches {
          val b1 = separate(n, c)
          b1 && anchorProperty()
        }) ||
        anchorProperty() && optional(matches(separate(n, c) && tagProperty()))
    ) &&
    emit(EndProperties)
  }

  /**
    * The tag property identifies the type of the native data structure presented by the node.
    * A tag is denoted by the “!” indicator.
    *<blockquote><pre>
    *
    * [97]	c-ns-tag-property	::=	  c-verbatim-tag | c-ns-shorthand-tag | c-non-specific-tag
    * [98]	c-verbatim-tag	    ::=	“!” “<” [[uriChar ns-uri-char]]+ “>”
    * [99]	c-ns-shorthand-tag	::=	[[tagHandle c-tag-handle]] [[tagChar ns-tag-char]]+
    * [100]	c-ns-shorthand-tag	::=	“!”
    *
    *</blockquote></pre>
    */
  private def tagProperty() =
    currentChar == '!' && emit(BeginTag) && {
      // c-ns-shorthand-tag
      lookAhead(1) == ' ' && indicator('!') ||
      matches {
        // c-verbatim-tag
        indicator('!') &&
        indicator('<') && oneOrMore(uriChar()) && emit(MetaText) && indicator('>')
      } ||
      matches {
        tagHandle() && (
            matches {
              tagChar() && {
                while (tagChar()) {}
                emit(MetaText)
              }
            } ||
            emit(Error)
        )
      }
    } &&
      emit(EndTag)

  /**
    * An anchor is denoted by the “&” indicator.
    * It marks a node for future reference.
    * An alias node can then be used to indicate additional inclusions of the anchored node.
    * An anchored node need not be referenced by any alias nodes; in particular, it is valid for all nodes to be anchored.<p>
    *
    * [101]	c-ns-anchor-property	::=	“&” [[anchorName ns-anchor-name]]
    *
    *</blockquote></pre>
    *
    */
  @failfast private def anchorProperty() =
    currentChar == '&' && isNsAnchorChar(lookAhead(1)) &&
      emit(BeginAnchor) && emitIndicator() && anchorName() && emit(EndAnchor)

  /** [102]	ns-anchor-char	::=	[[isNsChar ns-char]] - [[isFlowIndicator c-flow-indicator]] */
  private def isNsAnchorChar(c: Int) = isNsChar(c) && !isFlowIndicator(c)

  /**
    * An anchor name<p>
    * [103]	ns-anchor-name	::=	[[isNsAnchorChar ns-anchor-char]]+
    */
  private def anchorName() = {
    consumeWhile(isNsAnchorChar)
    emit(MetaText)
  }

  /**
    * An alias node is denoted by the “*” indicator.
    * The alias refers to the most recent preceding node having the same anchor.<p>
    *
    * [104]	c-ns-alias-node	::=	“*” [[anchorName ns-anchor-name]]
    *
    */
  @failfast private def aliasNode() =
    currentChar == '*' && isNsAnchorChar(lookAhead(1)) &&
      emit(BeginAlias) && emitIndicator() && anchorName() && emit(EndAlias)

  /**
    * [105]	e-scalar	::=	/* Empty */
    */
  def emptyScalar(): Boolean = {
    emit(BeginNode, BeginScalar)
    emit(EndScalar, EndNode)
  }

  /**
    * Process either simple or double quoted scalars
    */
  private def quotedScalar(n: Int, c: YamlContext, quoteChar: Char) = currentChar == quoteChar && {
    var inText     = false
    def emitText() = if (inText) { emit(Text); inText = false }
    def allHexa(chars: Int) = {
      val i = countWhile(n => isNsHexDigit(lookAhead(n)))
      i >= chars
    }
    def processHexa(chars: Int) = emitIndicator() && consumeAndEmit(chars, if (allHexa(chars)) MetaText else Error)

    emit(BeginScalar)
    emitIndicator()
    var done = false

    while (!done) {
      currentChar match {
        case '"' if quoteChar == '"' =>
          done = true
        case '\'' if quoteChar == '\'' =>
          if (lookAhead(1) != '\'') done = true
          else {
            emitText()
            emit(BeginEscape)
            emitIndicator()
            consumeAndEmit(MetaText)
            emit(EndEscape)
          }

        case '\r' | '\n' =>
          if (c != BlockKey && c != FlowKey) {
            emitText()
            matches(breakNonContent() && emptyLine(n, c)) || consumeAndEmit(LineFold)
            indent(n)
            consumeAndEmit(countWhiteSpaces(), WhiteSpace)
          } else {
            inText = false
            emit(Error)
            done = true
          }
        case '\\' if quoteChar == '"' =>
          emitText()
          emit(BeginEscape)
          emitIndicator()
          if (currentChar == '\n') breakNonContent()
          else
            escapeSeq(currentChar) match {
              case -1 =>
                consumeAndEmit(Error)
              case 'x' => processHexa(2)
              case 'u' => processHexa(4)
              case 'U' => processHexa(8)
              case _ =>
                consumeAndEmit(MetaText)
            }
          emit(EndEscape)
        case ' ' | '\t' =>
          val spaces = countWhiteSpaces()
          if (isBBreak(lookAhead(spaces))) {
            emitText()
            consumeAndEmit(spaces, WhiteSpace)
          } else {
            inText = true
            consume(spaces)
          }

        case _ =>
          inText = true
          consume()
      }
    }
    emitText()
    emitIndicator()
    emit(EndScalar)
  }

  /** todo deprecate */
  def isPlainFirst(chr: Int, nextChar: Int, ctx: YamlContext): Boolean =
    isNsChar(chr) && !isIndicator(chr) || (chr == '-' || chr == ':' || chr == '?') && isPlainSafe(nextChar, ctx)

  /**
    * [126]	ns-plain-first(c) ::=	  ( ns-char - c-indicator )
    *                             |   ( ( “?” | “:” | “-” )    Followed by an ns-plain-safe(c))
    */
  @failfast private def plainFirst(ctx: YamlContext): Boolean = {
    val chr = currentChar
    val result = isNsChar(chr) && !isIndicator(chr) || (chr == '-' || chr == ':' || chr == '?') && isPlainSafe(
        lookAhead(1),
        ctx)
    if (result) consume()
    result
  }

  /**
    * [127]	ns-plain-safe(c)	::=	c = flow-out  ⇒ ns-plain-safe-out
    *                               c = flow-in   ⇒ ns-plain-safe-in
    *                               c = block-key ⇒ ns-plain-safe-out
    *                               c = flow-key  ⇒ ns-plain-safe-in
    * [128]	ns-plain-safe-out	::=	ns-char
    * [129]	ns-plain-safe-in	::=	ns-char - c-flow-indicator
    */
  private def isPlainSafe(chr: Int, ctx: YamlContext): Boolean = ctx match {
    case FlowIn | FlowKey => isNsChar(chr) && !isFlowIndicator(chr)
    case _                => isNsChar(chr)
  }

  /**
    * [130]	ns-plain-char(c)	::=	  ( ns-plain-safe(c) - “:” - “#” )
    *                               |   ( /* An ns-char preceding */ “#” )
    *                               |   ( “:” /* Followed by an ns-plain-safe(c) */ )
    */
  def isPlainChar(offset: Int, ctx: YamlContext): Boolean = lookAhead(offset) match {
    case '#'                                => lookAhead(offset - 1) != ' '
    case ':'                                => isPlainSafe(lookAhead(offset + 1), ctx)
    case ' ' | '\t' | '\r' | '\n' | EofChar => false
    case _                                  => isPlainSafe(lookAhead(offset), ctx)
  }
  private def plainChar(ctx: YamlContext): Boolean = {
    val result = isPlainChar(0, ctx)
    if (result) consume()
    result
  }

  /**
    *
    *    [131]	ns-plain(n,c)	::=	c = flow-out  ⇒ ns-plain-multi-line(n,c)
    *                               c = flow-in   ⇒ ns-plain-multi-line(n,c)
    *                               c = block-key ⇒ ns-plain-one-line(c)
    *                               c = flow-key  ⇒ ns-plain-one-line(c)
    */
  def plainScalar(n: Int, ctx: YamlContext): Boolean =
    matches({
      emit(BeginScalar)
      ctx match {
        case FlowKey | BlockKey => plainOneLine(ctx)
        case _                  => plainMultiline(n, ctx)
      }
      emit(EndScalar)
    })

  /**
    * [132]	nb-ns-plain-in-line(c)	::=	( s-white* ns-plain-char(c) )*
    */
  @failfast private def plainInLine(ctx: YamlContext): Boolean = {
    while ({
      val spaces = countWhiteSpaces()
      if (!isPlainChar(spaces, ctx)) false
      else {
        consume(spaces + 1)
        true
      }
    }) {}
    true
  }

  /** [133]	ns-plain-one-line(c)	::=	ns-plain-first(c) nb-ns-plain-in-line(c) */
  @failfast private def plainOneLine(ctx: YamlContext): Boolean = plainFirst(ctx) && plainInLine(ctx) && emit(Text)

  /**
    * [134]	s-ns-plain-next-line(n,c)	::=	s-flow-folded(n) ns-plain-char(c) nb-ns-plain-in-line(c)
    * [135]	ns-plain-multi-line(n,c)	::=	ns-plain-one-line(c) s-ns-plain-next-line(n,c)*
    */
  private def plainMultiline(n: Int, ctx: YamlContext): Boolean =
    plainOneLine(ctx) && zeroOrMore({
      val b1 = flowFolded(n)
      if (!b1) false
      else {
        val b2 = plainChar(ctx)
        if (!b2) false
        else {
          val b3 = plainInLine(ctx)
          if (b3) emit(Text)
          b3
        }
      }

    })

  /**
    * Flow sequence content is denoted by surrounding “[” and “]” characters.<p>
    *
    * <blockquote><pre>
    * [136]	in-flow(c)	::=
    *                     c = flow-out  ⇒ flow-in
    *                     c = flow-in   ⇒ flow-in
    *                     c = block-key ⇒ flow-key
    *                     c = flow-key  ⇒ flow-key
    */
  private def inFlow(ctx: YamlContext) =
    if (ctx == BlockKey || ctx == FlowKey) FlowKey else FlowIn

  /**
    * Flow sequence content is denoted by surrounding “[” and “]” characters.<p>
    *
    * [137]	c-flow-sequence(n,c)	::=	“[”
    *                                   [[separate s-separate(n,c)]]?
    *                                   [[flowSequenceEntries ns-s-flow-seq-entries(n,in-flow(c))]]?
    *                                   “]”
    */
  def flowSequence(n: Int, ctx: YamlContext): Boolean = currentChar == '[' && matches {
    emit(BeginSequence) && emitIndicator() &&
    optional(separate(n: Int, ctx)) &&
    optional(flowSequenceEntries(n, inFlow(ctx))) &&
    currentChar == ']' && emitIndicator() && emit(EndSequence)
  }

  /**
    * Sequence entries are separated by a “,” character.<p>
    *
    * [138]	ns-s-flow-seq-entries(n,c)	::=	[[flowSequenceEntry ns-flow-seq-entry(n,c)]]
    *                                       [[separate s-separate(n,c)]]?
    *                                       ( “,”
    *                                         [[separate s-separate(n,c)]]?
    *                                         [[flowSequenceEntries ns-s-flow-seq-entries(n,c)]]?
    *                                       )?
    */
  @tailrec private def flowSequenceEntries(n: Int, ctx: YamlContext): Boolean = {
    flowSequenceEntry(n, ctx) && {
      separate(n, ctx)
      indicator(',') && {
        separate(n, ctx)
        flowSequenceEntries(n, ctx)
      }
    }
  }

  /**
    * Any flow node may be used as a flow sequence entry.
    * In addition, YAML provides a compact notation
    * for the case where a flow sequence entry is a mapping with a single key: value pair.<p>
    *
    * [139]	ns-flow-seq-entry(n,c)	::=	[[flowPair ns-flow-pair(n,c)]]
    *                                 | [[flowNode ns-flow-node(n,c)]]
    */
  def flowSequenceEntry(n: Int, ctx: YamlContext): Boolean = {
    // todo flow-pair
    matches(emit(BeginNode) && flowNode(n, ctx) && emit(EndNode))
  }

  /**
    * Flow mappings are denoted by surrounding “{” and “}” characters.<p>
    *
    *[140]	c-flow-mapping(n,c)	::=	“{”
    *                                   [[separate s-separate(n,c)]]?
    *                                   [[flowMapEntries ns-s-flow-map-entries(n,in-flow(c))]]?
    *                               “}”
    */
  private def flowMapping(n: Int, c: YamlContext) = currentChar == '{' && matches {
    emit(BeginMapping) && emitIndicator() &&
    optional(separate(n: Int, c)) &&
    optional(flowMapEntries(n, inFlow(c))) &&
    currentChar == '}' && emitIndicator() && emit(EndMapping)
  }

  /**
    * Mapping entries are separated by a “,” character.<p>
    *
    *[141]	ns-s-flow-map-entries(n,c)	::=	ns-flow-map-entry(n,c) s-separate(n,c)?
    *                                     ( “,” s-separate(n,c)? ns-s-flow-map-entries(n,c)? )?
    */
  @tailrec private def flowMapEntries(n: Int, ctx: YamlContext): Boolean = {
    flowMapEntry(n, ctx) && {
      separate(n, ctx)
      indicator(',') && {
        separate(n, ctx)
        flowMapEntries(n, ctx)
      }
    }
  }

  /**
    * A Flow Map Entry<p>
    * If the optional “?” mapping key indicator is specified, the rest of the entry may be completely empty.<p>
    *
    * <blockquote><pre>
    * [142]	ns-flow-map-entry(n,c)	::=	  ( “?” [[separate s-separate(n,c)]]
    *                                        ns-flow-map-explicit-entry(n,c)
    *                                     )
    *                                 |     ns-flow-map-implicit-entry(n,c)
    *
    *[143]	ns-flow-map-explicit-entry(n,c)	::=	  ns-flow-map-implicit-entry(n,c)
    *                                        |   ( [[emptyScalar e-node]] [[emptyScalar e-node]])
    * </blockquote></pre>
    */
  private def flowMapEntry(n: Int, c: YamlContext) =
    currentChar == '?' && separate(n, c) && {
      {
        val c = lookAhead(1)
        (c == ',' || c == '}') && emptyScalar() && emptyScalar()
      } || flowMapImplicitEntry(n, c)
    } || flowMapImplicitEntry(n, c)

  /**
    * <blockquote><pre>
    *  [144]	ns-flow-map-implicit-entry(n,c)	   ::=	ns-flow-map-yaml-key-entry(n,c)
    *                                             |  c-ns-flow-map-empty-key-entry(n,c)
    *                                             |  c-ns-flow-map-json-key-entry(n,c)
    *
    *  [145]	ns-flow-map-yaml-key-entry(n,c)	   ::=	[[flowYamlNode ns-flow-yaml-node(n,c)]]
    *                                             ( ([[separate s-separate(n,c)]]?
    *                                                c-ns-flow-map-separate-value(n,c) )
    *                                             |  [[emptyScalar e-node]]
    *                                             )
    *  [146]	c-ns-flow-map-empty-key-entry(n,c)	::=	[[emptyScalar e-node]]
    *                                                c-ns-flow-map-separate-value(n,c)
    *
    *  [147]	c-ns-flow-map-separate-value(n,c)	::=	“:” (Not followed by an ns-plain-safe(c) )
    *                                                 ( ( [[separate s-separate(n,c)]]
    *                                                    [[flowNode ns-flow-node(n,c)]])
    *                                                 | [[emptyScalar e-node]]
    *                                                )
    * </blockquote></pre>
    * // todo complete
    */
  private def flowMapImplicitEntry(n: Int, c: YamlContext) = matches {
    emit(BeginPair, BeginNode)
    flowYamlNode(n, c) &&
    emit(EndNode) &&
    optional(separate(n, c)) &&
    indicator(':') &&
    matches {
      separate(n, c)
      emit(BeginNode)
      flowNode(n, c) &&
      emit(EndNode)
    } &&
    emit(EndPair)
  }

  /**
    * If the “?” indicator is explicitly specified, parsing is unambiguous,
    * and the syntax is identical to the general case. <p>
    *
    * [150]	ns-flow-pair(n,c)	::=	  ( “?” s-separate(n,c) ns-flow-map-explicit-entry(n,c) )
    *                                      | ns-flow-pair-entry(n,c)
    */
  def flowPair(n: Int, ctx: YamlContext): Boolean = ???

  /** [154]	implicit-yaml-key(c)	::=	ns-flow-yaml-node(n/a,c) s-separate-in-line? */
  def implicitYamlKey(ctx: YamlContext): Boolean =
    emit(BeginNode) && flowYamlNode(0, ctx) && emit(EndNode) && optional(separateInLine())

  /** [155]	c-s-implicit-json-key(c)	::=	c-flow-json-node(n/a,c) s-separate-in-line? */
  def implicitJsonKey(ctx: YamlContext): Boolean = flowJsonNode(0, ctx) && optional(separateInLine())

  /**
    *      [156] ns-flow-yaml-content(n,c)	::=	ns-plain(n,c)
    */
  @failfast private def flowYamlContent(n: Int, ctx: YamlContext): Boolean =
    isPlainFirst(currentChar, lookAhead(1), ctx) && plainScalar(n, ctx)

  /**
    * JSON-like flow styles all have explicit start and end indicators.<p>
    *  <blockquote><pre>
    *      [157] c-flow-json-content(n,c)	::=
    *                                           [[flowSequence c-flow-sequence(n,c)]]
    *                                         | [[flowMapping c-flow-mapping(n,c)]]
    *                                         | [[quotedScalar c-single-quoted(n,c)]]
    *                                         | [[quotedScalar c-double-quoted(n,c)]]
    * </blockquote></pre>
    */
  @failfast def flowJsonContent(n: Int, ctx: YamlContext): Boolean = currentChar match {
    case '['  => flowSequence(n, ctx)
    case '{'  => flowMapping(n, ctx)
    case '\'' => quotedScalar(n, ctx, '\'')
    case '"'  => quotedScalar(n, ctx, '"')
    case _    => false
  }

  /**
    * [158] ns-flow-content(n,c)	    ::=	ns-flow-yaml-content(n,c) | c-flow-json-content(n,c)
    */
  def flowContent(n: Int, ctx: YamlContext): Boolean = flowJsonContent(n, ctx) || flowYamlContent(n, ctx)

  /**
    *[159]	ns-flow-yaml-node(n,c)	::=	  [[aliasNode c-ns-alias-node]]
    *                                  |   [[flowYamlContent ns-flow-yaml-content(n,c)]]
    *                                  | ( [[nodeProperties c-ns-properties(n,c)]]
    *                                          ( ( [[separate s-separate(n,c)]] [[flowYamlContent ns-flow-yaml-content(n,c)]] )
    *                                          | [[emptyScalar e-scalar]] )
    *                                         )
    */
  def flowYamlNode(n: Int, c: YamlContext): Boolean =
    aliasNode() ||
      flowYamlContent(n, c) ||
      nodeProperties(n, c) && (separate(n, c) && flowYamlContent(n, c) || emptyScalar())

  /**
    *[160]	c-flow-json-node(n,c)	::=	( c-ns-properties(n,c) s-separate(n,c) )? c-flow-json-content(n,c)
    */
  def flowJsonNode(n: Int, ctx: YamlContext): Boolean =
    emit(BeginNode) &&
      optional(nodeProperties(n, ctx) && separate(n, ctx)) &&
      flowJsonContent(n, ctx) &&
      emit(EndNode)
  // todo complete

  /**
    * A complete flow node also has optional node properties,
    *  except for alias nodes which refer to the anchored node properties.<p>
    *
    *  [161] ns-flow-node(n,c)	::=	  [[aliasNode c-ns-alias-node]]
    *                            |    [[flowContent ns-flow-content(n,c)]]
    *                            |  ( [[nodeProperties c-ns-properties(n,c)]]
    *                                  ( ( [[separate s-separate(n,c)]] [[flowContent ns-flow-content(n,c) )]])
    *                                  | [[emptyScalar e-scalar]]
    *                                  )
    */
  private def flowNode(n: Int, c: YamlContext): Boolean = {
    aliasNode() ||
    matches(flowContent(n, c)) ||
    nodeProperties(n, c) &&
    (matches(separate(n, c) && flowContent(n, c)) || emptyScalar())
  }

  /**
    * Returns the new defined indentation or MAX_VALUE if autodetect
    *  <blockquote><pre>
    * [162] c-b-block-header(m,t)	::=
    *                                 ( ( c-indentation-indicator(m) c-chomping-indicator(t) )
    *                                 | ( c-chomping-indicator(t) c-indentation-indicator(m) )
    *                                 )
    *                                 s-b-comment
    *
    * [163] c-indentation-indicator(m) ::=	ns-dec-digit ⇒ m = ns-dec-digit - #x30
    *                                    | Empty
    *
    * [164] c-chomping-indicator(t) ::=
    *                                     “-” ⇒ t = strip
    *                                 |   “+” ⇒ t = keep
    *                                 |   “ ” ⇒ t = clip
    *
    *  </blockquote></pre>
    */
  def blockHeader(): (Int, Char) = {
    def chompingIndicator = currentChar match {
      case '+' =>
        emitIndicator()
        '+'
      case '-' =>
        emitIndicator()
        '-'
      case _ => ' '
    }
    var t = chompingIndicator

    val c = currentChar
    val m =
      if (!isNsDecDigit(c) || c == '0') -1
      else {
        emitIndicator()
        c - '0'
      }
    if (t == ' ') t = chompingIndicator

    if (!spaceBreakComment()) {
      error()
      breakComment()
    }
    (m, t)
  }

  /**
    * Process the last block of an Scalar based on the chomp indicator
    *
    *   <blockquote><pre>
    *
    * [165]	b-chomped-last(t) ::=
    *                            t = strip(-) ⇒ b-non-content  | EOF
    *                            t = clip     ⇒ b-as-line-feed | EOF
    *                            t = keep (+) ⇒ b-as-line-feed | EOF
    *   </blockquote></pre>
    */
  def chompedLast(t: Char): Boolean =
    currentChar == EofChar || t != '-' && breakAsLineFeed() || emit(EndScalar) && breakNonContent()

  /**
    *   <blockquote><pre>
    *
    * [166]	l-chomped-empty(n,t) ::=
    *                                   t = strip(-) ⇒ l-strip-empty(n)
    *                                   t = clip     ⇒ l-strip-empty(n)
    *                                   t = keep (+) ⇒ l-keep-empty(n)
    *
    * [167]	l-strip-empty(n)	::=	([[indentLessOrEqual s-indent(≤n)]] [[breakNonContent b-non-content]])*
    *                               [[trailComments l-trail-comments(n)]]?
    *
    * [168]	l-keep-empty(n)	    ::=	[[emptyLine l-empty(n,block-in)]]*
    *                               [[trailComments l-trail-comments(n)]]?
    *   </blockquote></pre>
    */
  def chompedEmpty(n: Int, t: Char): Boolean = {
    if (t != '+') {
      if (t != '-') emit(EndScalar)
      zeroOrMore(indentLessOrEqual(n) && breakNonContent()) //strip empty
    } else {
      zeroOrMore(emptyLine(n, BlockIn)) // keep empty
      emit(EndScalar)
    }
    trailComments(n)
    true
  }

  /**
    * Explicit comment lines may follow the trailing empty lines.
    * To prevent ambiguity, the first such comment line must be less indented than the block scalar content.
    * Additional comment lines, if any, are not so restricted.
    * This is the only case where the indentation of comment lines is constrained.<p>
    *
    * [169]	l-trail-comments(n)	::=	[[indentLess s-indent(&lt;n)]]
    *                               [[commentText c-nb-comment-text b-comment]]
    *                               [[lineComment l-comment]]*
    */
  def trailComments(n: Int): Boolean = matches(indentLess(n) && commentText() && zeroOrMore(lineComment()))

  /**
    * [170]	c-l+literal(n)	::=	 “|” [[blockHeader c-b-block-header(m,t)]] [[literalContent l-literal-content(n+m,t)]]
    *
    */
  def literalScalar(n: Int): Boolean =
    if (currentChar != '|') false
    else {
      emit(BeginScalar)
      emitIndicator()
      val (m, t) = blockHeader()
      literalContent(if (m == -1) -1 else n + m, t)
    }

  /**
    *        [171]	l-nb-literal-text(n)	::=	l-empty(n,block-in)* s-indent(n) nb-char+
    */
  def literalContentLine(n: Int): (Boolean, Int) = {
    zeroOrMore(emptyLine(n, BlockIn))
    // Detect Indentation if necessary
    val m = if (n == -1) countSpaces() else n
    val b = indent(m) && !isBreakOrEof(currentChar) && {
      consumeWhile(!isBreakOrEof(_))
      emit(Text)
    }
    (b, m)
  }

  /**
    * <blockquote><pre>
    *    [173]	l-literal-content(n,t)	::=
    *         ( [[literalContentLine l-nb-literal-text(n)]]
    *           b-nb-literal-next(n) *
    *           [[chompedLast b-chomped-last(t)]]
    *         )?
    *         [[chompedEmpty l-chomped-empty(n,t)]]
    *
    * [172]	b-nb-literal-next(n) ::= [[breakAsLineFeed b-as-line-feed]] [[literalContentLine l-nb-literal-text(n)]]
    *
    * </blockquote></pre>
    */
  def literalContent(n: Int, t: Char): Boolean = {
    var m = n
    matches {
      val r = literalContentLine(n)
      if (!r._1) false
      else {
        m = r._2
        zeroOrMore {
          breakAsLineFeed() && literalContentLine(m)._1
        }
        chompedLast(t)
      }
    }
    chompedEmpty(m, t)
  }

  /**
    * The folded style is denoted by the “>” indicator.
    * It is similar to the literal style; however, folded scalars are subject to line folding.<p>
    * [174]	c-l+folded(n)	::=	“>” [[blockHeader c-b-block-header(m,t)]]
    *                                 [[foldedContent l-folded-content(n+m,t)]]
    */
  def foldedScalar(n: Int): Boolean = currentChar == '>' && emit(BeginScalar) && emitIndicator() && {
    val (m, t) = blockHeader()
    foldedContent(if (m == -1) -1 else n + m, t)
  }

  /**
    * Folding allows long lines to be broken anywhere a single space character separates two non-space characters.<p>
    *  [175]	s-nb-folded-text(n)	  ::=	[[indent s-indent(n)]] [[isNsChar ns-char]] [[isNBChar nb-char]]*
    */
  private def foldedText(n: Int) =
    if (countSpaces() != n || !isNsChar(lookAhead(n + 1))) false
    else {
      consumeAndEmit(n, Indent)
      consumeWhile(isNBChar)
      emit(Text)
    }

  /**
    *  [176]	l-nb-folded-lines(n)  ::=	[[foldedText s-nb-folded-text(n)]]
    *                                     ([[folded b-l-folded(n,block-in)]] [[foldedText s-nb-folded-text(n)]] )*
    */
  private def foldedLines(n: Int) = foldedText(n) && zeroOrMore(folded(n, BlockIn) && foldedText(n))

  /**
    * Lines starting with white space characters (more-indented lines) are not folded.<p>
    *  [177]	s-nb-spaced-text(n)	::=	[[indent s-indent(n)]] [[isWhite s-white]] [[isNBChar nb-char]]*
    */
  private def spacedText(n: Int): Boolean = {
    val m = countSpaces()
    if (m < n || m == n && !isWhite(lookAhead(n))) false
    else {
      consumeAndEmit(n, Indent)
      consumeWhile(isNBChar)
      emit(Text)
    }
  }

  /**
    *<blockquote><pre>
    *  [178]	b-l-spaced(n)	::=	[[breakAsLineFeed b-as-line-feed]] [[emptyLine l-empty(n,block-in)]]*
    *  [179]	l-nb-spaced-lines(n)	::=	[[spacedText s-nb-spaced-text(n)]]
    *                                     ( b-l-spaced(n) [[spacedText s-nb-spaced-text(n)]] )*
    *</blockquote></pre>
    */
  private def spacedLines(n: Int) =
    spacedText(n) && zeroOrMore(breakAsLineFeed() && zeroOrMore(emptyLine(n, BlockIn)) && spacedText(n))

  /**
    * Line breaks and empty lines separating folded and more-indented lines are also not folded.<p>
    *     [180]	l-nb-same-lines(n)	::=	[[emptyLine l-empty(n,block-in)]]*
    *                                   ( [[foldedLines l-nb-folded-lines(n)]]
    *                                   | [[spacedLines l-nb-spaced-lines(n)]]
    *                                   )
    */
  private def sameLines(n: Int): (Boolean, Int) = {
    zeroOrMore(emptyLine(n, BlockIn))
    // Detect Indentation if necessary
    val m = if (n == -1) countSpaces() else n
    val b = matches(foldedLines(m)) || matches(spacedLines(m))
    (b, m)
  }

  /**
    * The final line break, and trailing empty lines if any, are subject to chomping and are never folded.<p>
    *<blockquote><pre>
    * [181]	l-nb-diff-lines(n)	    ::=	[[sameLines l-nb-same-lines(n)]]
    *                                    ( [[breakAsLineFeed b-as-line-feed]] [[sameLines l-nb-same-lines(n)]])*
    *
    * [182]	l-folded-content(n,t)	::=	( l-nb-diff-lines(n) b-chomped-last(t) )? l-chomped-empty(n,t)
    *
    * </blockquote></pre>
    */
  private def foldedContent(n: Int, t: Char): Boolean = {
    var m = n
    matches {
      val r = sameLines(n)
      if (!r._1) false
      else {
        m = r._2
        zeroOrMore {
          val b = breakAsLineFeed()
          b && sameLines(m)._1
        }
        chompedLast(t)
      }
    }
    chompedEmpty(m, t)
  }

  /**
    * A block sequence is simply a series of nodes, each denoted by a leading “-” indicator.
    * The “-” indicator must be separated from the node by white space.
    * This allows “-” to be used as the first character
    * in a plain scalar if followed by a non-space character (e.g. “-1”).<p>
    *
    * <blockquote><pre>
    * [183]	l+block-sequence(n)	::=	( [[indent s-indent(n+m)]] [[blockSeqEntry c-l-block-seq-entry(n+m)]] )+
    *                                      (For some fixed auto-detected m > 0)
    * </blockquote></pre>
    */
  def blockSequence(n: Int): Boolean = beginOfLine && {
    val m: Int = detectSequenceStart(n)
    m > 0 &&
    emit(BeginSequence) &&
    oneOrMore {
      val b1 = indent(n + m)
      b1 && blockSeqEntry(n + m)
    } &&
    emit(EndSequence)
  }

  private def detectSequenceStart(n: Int) = {
    val spaces = countSpaces()
    if (spaces > n && lookAhead(spaces) == '-' && !isNsChar(lookAhead(spaces + 1))) spaces - n else 0
  }

  /**
    * [184]	c-l-block-seq-entry(n)	::=	“-” (Not followed by an ns-char)
    *                                [[blockIndented s-l+block-indented(n,block-in)]]
    * </blockquote></pre>
    */
  private def blockSeqEntry(n: Int) = indicator('-') && blockIndented(n, BlockIn)

  /**
    * The entry node may be either completely empty, be a nested block node, or use a compact in-line notation.<p>
    * The compact notation may be used when the entry is itself a nested block collection.<p>
    *
    * <blockquote><pre>
    * [185]	s-l+block-indented(n,c)	 ::=
    *                                  ([[indent s-indent(m)]]
    *                                      ( [[compactSequence ns-l-compact-sequence(n+1+m)]]
    *                                      | [[compactMapping  ns-l-compact-mapping(n+1+m)]]
    *                                      )
    *                                  )
    *                                  | [[blockNode s-l+block-node(n,c) ]]
    *                                  | ( [[emptyScalar e-node]] [[multilineComment s-l-comments]] )
    *
    * </blockquote></pre>
    */
  private def blockIndented(n: Int, ctx: YamlContext) = {
    val m = detectSequenceStart(n) + n
    m > 0 && indent(m) && emit(BeginNode) && compactSequence(n + 1 + m) && emit(EndNode)
  } || {
    val m = detectMapStart(n) + n
    m > 0 && indent(m) && emit(BeginNode) && compactMapping(n + 1 + m) && emit(EndNode)
  } || {
    blockNode(n, ctx) ||
    matches(emptyScalar() && multilineComment())
  }

  /**
    * [186]	ns-l-compact-sequence(n) ::=	[[blockSeqEntry c-l-block-seq-entry(n)]]
    *                                      ( [[indent s-indent(n)]] [[blockSeqEntry c-l-block-seq-entry(n)]] )*
    */
  private def compactSequence(n: Int): Boolean =
    emit(BeginSequence) && blockSeqEntry(n) && zeroOrMore(indent(n) && blockSeqEntry(n)) && emit(EndSequence)

  private def detectMapStart(n: Int) = {
    val spaces = countSpaces()
    if (spaces > n && lookAhead(spaces) == '?' || lineContainsMapIndicator()) spaces - n else 0
  }

  /**
    * A Block mapping is a series of entries, each presenting a key: value pair.<p>
    * [187]	l+block-mapping(n)	    ::= ( [[indent s-indent(n+m)]]
    *                                     [[blockMapEntry ns-l-block-map-entry(n+m)]]
    *                                   )+
    * <p> For some fixed auto-detected m greater than 0
    */
  def blockMapping(n: Int): Boolean = beginOfLine && {
    val m = detectMapStart(n)
    (m > 0) && emit(BeginMapping) && oneOrMore {
      indent(n + m) && blockMapEntry(n + m)
    } && emit(EndMapping)
  }

  /**
    * [188]	ns-l-block-map-entry(n)	::=
    *                                   [[mapExplicitEntry c-l-block-map-explicit-entry(n)]]
    *                                 | [[mapImplicitEntry ns-l-block-map-implicit-entry(n)]]
    */
  @failfast def blockMapEntry(n: Int): Boolean = {
    val explicit = currentChar == '?'
    val entry    = explicit || lineContainsMapIndicator()
    if (!entry) false
    else {
      emit(BeginPair)
      if (explicit) mapExplicitEntry(n) else mapImplicitEntry(n)
      emit(EndPair)
    }
  }

  /**
    * Explicit map entries are denoted by the “?” mapping key indicator<p>
    * <blockquote><pre>
    * [189]	c-l-block-map-explicit-entry(n)	::=	c-l-block-map-explicit-key(n)
    *                                         ( l-block-map-explicit-value(n) | [[emptyScalar e-node]] )
    *
    * [190]	c-l-block-map-explicit-key(n)	::=	“?” [[blockIndented s-l+block-indented(n,block-out)]]
    *
    * </blockquote></pre>
    */
  @failfast def mapExplicitEntry(n: Int): Boolean = indicator('?') && blockIndented(n, BlockOut) && matches {
    mapExplicitValue(n) || emptyScalar()
  }

  /**
    * [191]	l-block-map-explicit-value(n)	::=	[[indent s-indent(n)]] “:”
    *                                           [[blockIndented s-l+block-indented(n,block-out)]]
    */
  def mapExplicitValue(n: Int): Boolean = matches {
    indent(n) && indicator(':') && blockIndented(n, BlockOut)
  }

  /**
    * Detect a Map implicit Entry
    *
    *  <blockquote><pre>
    *
    * [192]	ns-l-block-map-implicit-entry(n) ::=
    *       ( ns-s-block-map-implicit-key | [[emptyScalar e-node]] )
    *       [[mapImplicitValue c-l-block-map-implicit-value(n)]]
    *
    * [193]	ns-s-block-map-implicit-key	::=	  [[implicitJsonKey c-s-implicit-json-key(block-key)]]
    *                                     |   [[implicitYamlKey ns-s-implicit-yaml-key(block-key)]]
    *
    *
    * </blockquote></pre>
    */
  def mapImplicitEntry(n: Int): Boolean = {
    (matches(implicitJsonKey(BlockKey)) || matches(implicitYamlKey(BlockKey))) || matches(emptyScalar())
  } && mapImplicitValue(n)

  /**
    *  [194] c-l-block-map-implicit-value(n)	::=	“:”
    *  ( [[blockNode s-l+block-node(n,block-out)]]
    *  | ( [[emptyScalar e-node]] [[multilineComment s-l-comments]] )
    *  )
    */
  def mapImplicitValue(n: Int): Boolean = {
    emitIndicator()
    blockNode(n, BlockOut) ||
    emptyScalar() && (matches(multilineComment()) || matches(error() && multilineComment()))
  }

  /**
    * [195]	ns-l-compact-mapping(n)	::=	[[blockMapEntry ns-l-block-map-entry(n)]]
    *                                   ( [[indent s-indent(n)]] [[blockMapEntry ns-l-block-map-entry(n)]] )*
    */
  def compactMapping(n: Int): Boolean =
    emit(BeginMapping) && blockMapEntry(n) && zeroOrMore(indent(n) && blockMapEntry(n)) && emit(EndMapping)

  /**
    *   [196] s-l+block-node(n,c)  ::=	s-l+block-in-block(n,c) | s-l+flow-in-block(n)
    *   [197] s-l+flow-in-block(n) ::=	s-separate(n+1,flow-out) ns-flow-node(n+1,flow-out) s-l-comments
    */
  def blockNode(n: Int, ctx: YamlContext): Boolean =
    matches(blockInBlock(n, ctx)) || matches {
      separate(n + 1, FlowOut) && emit(BeginNode) && flowNode(n + 1, FlowOut) && emit(EndNode) && multilineComment()
    }

  /*
   *  [198]	s-l+block-in-block(n,c)	::=	s-l+block-scalar(n,c) | s-l+block-collection(n,c)
   */
  def blockInBlock(n: Int, ctx: YamlContext): Boolean =
    emit(BeginNode) && (blockScalar(n, ctx) || blockCollection(n, ctx)) && emit(EndNode)

  /**
    * A Block Scalar Node<p>
    *  [199]	s-l+block-scalar(n,c)	::=	[[separate s-separate(n+1,c)]]
    *                                      ([[nodeProperties c-ns-properties(n+1,c)]] [[separate s-separate(n+1,c)]])?
    *                                      ( [[literalScalar c-l+literal(n)]] | [[foldedScalar c-l+folded(n)]] )
    */
  private def blockScalar(n: Int, ctx: YamlContext) = matches {
    separate(n + 1, ctx) && {
      if (nodeProperties(n + 1, ctx)) separate(n + 1, ctx)
      literalScalar(n) || foldedScalar(n)
    }
  }

  /**
    *
    * A nested Block Collection<p>
    *<blockquote><pre>
    * [200]	s-l+block-collection(n,c)	::=	( [[separate s-separate(n+1,c)]] c-ns-properties(n+1,c) )?
    *                                       [[multilineComment s-l-comments]]
    *                                       ( [[blockSequence l+block-sequence(seq-spaces(n,c))]]
    *                                       | [[blockMapping]] l+block-mapping(n) )
    *
    * [201]	seq-spaces(n,c)	::=
    *                           c = block-out ⇒ n-1
    *                           c = block-in  ⇒ n
    *
    *</blockquote></pre>
    *
    */
  def blockCollection(n: Int, ctx: YamlContext): Boolean = {
    def bc() = multilineComment() && (blockSequence(if (ctx == BlockOut) n - 1 else n) || blockMapping(n))

    matches(separate(n + 1, ctx) && nodeProperties(n + 1, ctx) && bc()) || matches(bc())
  }

  /**  A document may be preceded by a prefix specifying the character encoding, and optional comment lines.
    *  Note that all documents in a stream must use the same character encoding.<p>
    *
    *  [202]	l-document-prefix	::=	c-byte-order-mark? l-comment*
    */
  private def documentPrefix() =
    nonEof && //
      (currentChar == BomMark && emit(Bom) && zeroOrMore(lineComment()) || oneOrMore(lineComment()))

  /**
    * The Directive End marker. Specifying false for the parameter will only test without emitting<p>
    *
    * [203]	c-directives-end	::=	“-” “-” “-”
    */
  @failfast private def directivesEnd(emit: Boolean = true) =
    currentChar == '-' && lookAhead(1) == '-' && lookAhead(2) == '-' && (!emit || consumeAndEmit(3, DirectivesEnd))

  /**
    *     * The Document End marker. Specifying false for the parameter will only test without emitting<p>
    * [204]	c-document-end	::=	“.” “.” “.”
    */
  @failfast private def documentEnd(emit: Boolean = true) =
    currentChar == '.' && lookAhead(1) == '.' && lookAhead(2) == '.' && (!emit || consumeAndEmit(3, DocumentEnd))

  /** [205]	l-document-suffix	::=	c-document-end s-l-comments */
  private def documentSuffix() = matches(documentEnd() && multilineComment())

  /**
    * A bare document does not begin with any directives or marker lines.<p>
    *
    * [207]	l-bare-document	::=	s-l+block-node(-1,block-in)
    */
  private def bareDocument() = blockNode(-1, BlockIn)

  /**
    * An explicit document begins with an explicit directives end marker line but no directives.
    * Since the existence of the document is indicated by this marker, the document itself may be completely empty.<p>
    *
    * [208]	l-explicit-document	::=
    *                                [[directivesEnd c-directives-end]]
    *                                ( [[bareDocument l-bare-document]]
    *                                | ( [[emptyScalar e-node]] [[multilineComment s-l-comments]] )
    *                                )
    */
  private def explicitDocument() = directivesEnd(emit = false) && matches {
    directivesEnd()
    matches(bareDocument()) || matches(emptyScalar() && multilineComment())
  }

  /**
    * A directives document begins with some directives followed by an explicit directives end marker line.<p>
    *
    * [209]	l-directive-document	::=	l-directive+ l-explicit-document
    */
  private def directiveDocument() = currentChar == '%' && oneOrMore(directive()) && explicitDocument()

  /** [210]	l-any-document	::=	  l-directive-document | l-explicit-document | l-bare-document */
  private def anyDocument() = nonEof && matches {
    emit(BeginDocument) &&
    (directiveDocument() || explicitDocument() || bareDocument()) &&
    emit(EndDocument)
  }

  /**
    * A YAML stream consists of zero or more documents.
    * Subsequent documents require some sort of separation marker line.
    * If a document is not terminated by a document end marker line,
    * then the following document must begin with a directives end marker line.
    * The stream format is intentionally “sloppy” to better support common use cases, such as stream concatenation.
    *
    *<blockquote><pre>
    *
    *  [211]	l-yaml-stream	::=
    *                                 [[documentPrefix l-document-prefix]]*
    *                                 [[anyDocument l-any-document]]?
    *                                 (
    *                                   l-document-suffix+ l-document-prefix* l-any-document?
    *                                 | l-document-prefix* l-explicit-document?
    *                                 )*
    *</blockquote></pre>
    */
  def yamlStream(): Unit = {
    while (documentPrefix()) {}
    anyDocument()
    while (nonEof) {
      matches {
        oneOrMore(documentSuffix()) && zeroOrMore(documentPrefix()) && optional(anyDocument())
      } || matches {
        zeroOrMore(documentPrefix())
        emit(BeginDocument)
        explicitDocument()
        emit(EndDocument)
      }
    }
    emit(EndStream)
  }

  /** Check if the line contains a map Indicator (":" plus space or end of text) */
  def lineContainsMapIndicator(): Boolean = {
    var i        = 0
    var chr: Int = 0
    var inQuotes = false
    do {
      chr = lookAhead(i)
      if (inQuotes) {
        if (chr == '"' && lookAhead(i - 1) != '\\') inQuotes = false
      } else if (chr == '"') inQuotes = true
      else if (isMappingIndicator(chr, lookAhead(i + 1))) return true
      i += 1
    } while (!isBreakComment(chr))
    false
  }

  /** Emit an error and Consume until end of line or file */
  def error(): Boolean =
    if (isBreakComment(currentChar)) true
    else {
      do consume() while (!isBreakComment(currentChar))
      emit(Error)
    }

}

object YamlLexer {

  def isText(c: Int): Boolean = c != '\n' && c != '\r' && c != EofChar

  def apply(input: LexerInput = new CharSequenceLexerInput()): YamlLexer = new YamlLexer(input)

  def apply(s: String): YamlLexer = YamlLexer(new CharSequenceLexerInput(s))

  def apply(file: File): YamlLexer = {
    val fis    = new FileReader(file)
    val data   = new Array[Char](file.length.toInt)
    val length = fis.read(data)
    fis.close()
    new YamlLexer(new CharSequenceLexerInput(data, 0, length, file.getName))
  }
}
