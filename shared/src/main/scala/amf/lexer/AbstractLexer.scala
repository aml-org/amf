package amf.lexer

import amf.lexer.CharStream.EOF_CHAR
import amf.lexer.Token.Consume
import amf.parser.Position
import amf.parser.Position.ZERO
import amf.common.Strings.escape

/**
  * Abstract common lexer
  */
abstract class AbstractLexer[T <: Token](var stream: CharStream) extends Lexer[T] {
    private var _state: Int = 0
    private var _currentTokenStart: Int = 0
    private var _currentTokenPosition: Position = ZERO
    private var _currentToken: T = _
    private var _currentTokenEnd: Int = 0
    private var tokenStack:List[(T, Position, Int, Int)] = Nil

    /** Advance the lexer to the next token.  */
    override def advance() {
        if (tokenStack == Nil) {
            _currentTokenStart = stream.index()
            _currentTokenPosition = stream.position()
            _currentToken = findToken()
            _currentTokenEnd = stream.index()
        }
        else {
            val (t, p, s, e) = tokenStack.head
            _currentTokenStart = s; _currentTokenEnd = e; _currentToken = t; _currentTokenPosition = p
            tokenStack = tokenStack.tail
        }
    }
    protected def pushToken(token:T):Unit = {
        tokenStack = (token, _currentTokenPosition, _currentTokenStart, stream.index()) :: tokenStack
        _currentTokenStart = stream.index()
    }

    /** get the current token in the input stream.  */
    override def currentToken: T = {
        if (_currentToken == null) advance()
        _currentToken
    }

    /** Get the Position (Line, Column) the current token.  */
    override def currentStart: Position = _currentTokenPosition

    /** Get the index of the end position if the current token.  */
    override def currentTokenEnd: Int = _currentTokenEnd

    /** Get the index of the start position if the current token.  */
    override def currentTokenStart: Int = _currentTokenStart

    /** Get the current Token String.  */
    override def currentTokenText: CharSequence = stream.subSequence(_currentTokenStart, _currentTokenEnd)

    /** Ge the current state of the Lexer (0 = default state).  */
    override def state: Int = _state

    /** Set the current state of the Lexer (0 = default state).  */
    override def state_=(s: Int): Unit = _state = s

    protected def currentChar: Int = stream.currentChar()

    protected def column: Int = stream.column

    protected def lookAhead(i: Int): Int = stream.lookAhead(i)

    protected def consume(): Unit = stream.consume()

    protected def consume(n: Int): Unit = {
        var i = n
        while (i > 0) {
            stream.consume()
            i -= 1
        }
    }

    protected def matchAny(cs: Int*): Boolean = stream.matchAny(cs: _*)

    protected def matches(c: Int): Boolean = stream.matches(c)

    protected def matchDecimalDigits() {
        while (Character.isDigit(currentChar)) consume()
    }

    protected def whiteSpace(): T = {
        do {
            consume()
        }
        while (isWhiteSpace(currentChar))
        whiteSpaceToken
    }

    protected def lineComment(): T = {
        while (!matchAny('\n', EOF_CHAR)) consume()
        commentToken
    }

    protected def badChar(): T = {
        consume()
        badCharToken
    }

    /** The eof token */
    protected val eofToken: T
    /** The whiteSpace token */
    protected val whiteSpaceToken: T
    /** The commentToken */
    protected val commentToken: T
    /** The bad char token */
    protected val badCharToken: T

    /** The processing functions for each state */
    protected val states: Array[Int => T]

    /** Is this character consider a whitespace */
    protected def isWhiteSpace(chr: Int): Boolean

    protected def findToken(): T = {
        val chr = currentChar
        if (chr == CharStream.EOF_CHAR && state == 0)
            processPending(eofToken)
        else
            states(state)(chr) match {
                case _:Consume =>
                    consume()
                    findToken()
                case token => token
            }

    }

    /** Process all pending tokens */
    protected def processPending(finalToken:T): T = finalToken

    def lex(): List[(T,String)] = {
        var result =List[(T,String)]()
        while (true) {
            val t = currentToken
            if (t == eofToken) {
                result= result :+ (t,"")
                return result
            }
            result = result :+ (t,escape(currentTokenText.toString))
            advance()
        }
        result
    }
}
