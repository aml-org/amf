package amf.yaml

import java.lang.Character.isWhitespace

import amf.lexer.CharStream.EOF_CHAR
import amf.lexer.{AbstractLexer, CharSequenceStream, CharStream}
import amf.yaml.YamlLexer._
import amf.yaml.YamlToken._


/**
  * A Yaml simple lexer
  */
class YamlLexer(stream: CharStream = new CharSequenceStream()) extends AbstractLexer[YamlToken](stream) {
  // The number of unclosed '{' and '['. flow_level 0 means block context.
  private var flowLevel = 0
  // Current indentColumn
  private var indentColumn = -1
  // The start column of last scalar
  private var scalarStartColumn = -1
  // The Stack of previous indentations
  private var indentStack: List[(Int, YamlToken)] = Nil

  override protected val eofToken = Eof
  override protected val whiteSpaceToken = WhiteSpace
  override protected val commentToken = Comment
  override protected val badCharToken = BadChar

  /** Is this character consider a whitespace */
  override protected def isWhiteSpace(chr: Int): Boolean = chr == ' ' || chr == '\t'

  private def directive(): YamlToken = {
    while (Character.isLetter(currentChar)) {
      consume()
    }
    processPending(directives(currentTokenText))
  }

  /** Get a Token at the topLevel State */
  private val topLevel: Int => YamlToken = {
    // WhiteSpace
    case ' ' | '\t' =>
      whiteSpace()

    // Newline Just consume one whiteSpace
    case '\n' =>
      consume()
      whiteSpaceToken

    // A Comment
    case '#' =>
      lineComment()

    // A Yaml Directive
    case '^' =>
      directive()

    // It can be a new list item, the start of a document or just a plain scalar
    case '-' =>
      val c1 = lookAhead(1)
      if (isWhiteSpace(c1) || c1 == '\n') {
        val c = column
        consume(2)
        startItem(StartSequence, c)
      }
      else if (c1 == '-' && lookAhead(2) == '-') startDocument()
      else startPlainScalar()

    // It can be a the end of a document or just a plain scalar
    case '.' =>
      if (lookAhead(1) == '.' && lookAhead(2) == '.') endDocument()
      else startPlainScalar()

    // Flow sequence start
    case '[' | '{' =>
      startState(State.Flow)

    case _ =>
      scalar()
    // startPlainScalar()

  }

    private var inValue = true
  private def scalarToken: YamlToken = {
        inValue = false
        // Quick and dirty fix to recognize types in scalars
        currentTokenText match {
            case IntPattern() => IntToken
            case FloatPattern() => FloatToken
            case "true" | "false" => BooleanToken
       //     case "" => EmptyToken
            case _ => StringToken
        }
    }  private def scalar(): YamlToken = {
        val startColumn = column



        while (true) {
            currentChar match {
                case '\n' | EOF_CHAR => return scalarToken
                case ':' if isWhitespace(lookAhead(1)) && flowLevel > 0 =>
          consume()
          return endPlainScalar
        case ':' if isWhitespace(lookAhead(1))=>
                    pushToken(endPlainScalar)
                    val result = startItem(StartMap, startColumn)
                    consume(2)
                    inValue = true
                    return result
                case ' ' =>
                    if (lookAhead(1) == '#') return scalarToken else consume()
                case '[' | ']' | '{' | '}' | ',' if flowLevel > 0 => return scalarToken
                case '!' => // Belongs to RAML lexer. Proof of concept...
                    consume(9)
                    return Link
                case _ => consume()
            }
        }
        scalarToken
    }

  private def startPlainScalar() = {
    scalarStartColumn = column
    startState(State.PlainScalar)
  }

  /** Get a Token at the Plain Scalar State */
  private val plainScalar: Int => YamlToken = {
    case '\n' => endPlainScalar
    case EOF_CHAR => endPlainScalar
    case ':' if isWhitespace(lookAhead(1)) =>
      val result = startItem(StartMap, scalarStartColumn)
      pushToken(endPlainScalar)
      consume(2)
      result
    case ' ' if lookAhead(1) == '#' => endPlainScalar
    case '[' | ']' | '{' | '}' | ',' if flowLevel > 0 => endPlainScalar
    case _ => Consume
  }

  /** Get a Token at the Flow State */
  private val flow: Int => YamlToken = {
    case c@('[' | '{') =>
      flowLevel += 1
      consume()
      if (c == '[') StartSequence else StartMap
    case c@(']' | '}') =>
      flowLevel -= 1
      consume()
      if (flowLevel == 0) state = State.TopLevel
      if (c == ']') EndSequence else EndMap
    case ',' =>
      consume()
      Comma
    case ' ' | '\t' | '\n' =>
      whiteSpace()
    case _ =>
      scalar()

  }

  private def endPlainScalar = {
    state = if (flowLevel == 0) State.TopLevel else State.Flow
    StringToken
  }

  override protected val states: Array[Int => YamlToken] = Array(topLevel, plainScalar, flow)


  private def startState(newState: Int) = {
    state = newState
    findToken()
  }

  override protected def processPending(finalToken: YamlToken): YamlToken = {
    if (indentStack == Nil) finalToken
    else {
      val (prevCol, endToken) = indentStack.head
      indentColumn = prevCol
      indentStack = indentStack.tail
      endToken
    }
  }

  private def startItem(startToken: YamlToken, col: Int): YamlToken = {
    val endToken = if (startToken == StartSequence) EndSequence else EndMap
    if (col > indentColumn) {
      indentStack = (indentColumn, endToken) :: indentStack
      indentColumn = col
      return startToken

    } else if (col == indentColumn) {
      return Comma
    }

    var tokens: List[YamlToken] = Nil

    while (col <= indentStack.head._1) {
      val (_, actualEnd) = indentStack.head
      indentStack = indentStack.tail
      tokens = actualEnd :: tokens
    }

    indentColumn = col
    pushToken(Comma)

    while (tokens.nonEmpty) {
      tokens match {
        case x :: Nil => return x
        case x :: xs =>
          pushToken(x)
          tokens = xs
      }
    }

    throw new Exception()
  }

  private def startDocument(): YamlToken = {
    consume(3)
    processPending(StartDocument)
  }

  private def endDocument(): YamlToken = {
    consume(3)
    processPending(EndDocument)
  }

  private val directives: Map[CharSequence, YamlToken] = Map()

  object State {
    val TopLevel = 0
    val PlainScalar = 1
    val Flow = 2
  }

}

object YamlLexer {
  def apply(input: String) = new YamlLexer(new CharSequenceStream(input))

  def apply(stream: CharStream) = new YamlLexer(stream)

    private final val num = "(?:0|-?[1-9][0-9]*)"
    private final val IntPattern = num.r
    private final val FloatPattern =  (num + "(?:\\.[0-9]*[1-9])?(?:e[-+][1-9][0-9]*)?").r
}

