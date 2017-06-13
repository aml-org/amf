package amf.yaml

import java.lang.Character.isWhitespace

import amf.lexer.CharStream.EOF_CHAR
import amf.lexer.{AbstractLexer, CharSequenceStream, CharStream}
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
            if (c1 == ' ') {
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
    private def scalar(): YamlToken = {
        val startColumn = column

        def scalarToken = {
            inValue = false
            StringToken
        }

        while (true) {
            currentChar match {
                case '\n' | EOF_CHAR => return scalarToken
                case ':' if isWhitespace(lookAhead(1)) =>
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
            if (c == '[') StartSequence else StartMap
        case c@(']' | '}') =>
            flowLevel -= 1
            if (flowLevel == 0) state = State.TopLevel
            if (c == ']') EndSequence else EndMap
        case ',' =>
            Comma
        case ' ' | '\t' | '\n' =>
            whiteSpace()

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
            if (indentStack.tail == Nil || inValue) return startToken
            pushToken(startToken)
            return Comma
        }

        val (prevCol, prevEnd) = indentStack.head
        if (col != prevCol) {
            if (prevEnd == endToken) return Comma
            pushToken(prevEnd)
            return Comma
        }

        indentStack = indentStack.tail
        indentColumn = col

        pushToken(Comma)
        prevEnd
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
}

