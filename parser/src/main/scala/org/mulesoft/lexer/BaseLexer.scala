package org.mulesoft.lexer

import org.mulesoft.lexer.LexerInput.{EofChar, Mark}

import scala.collection.mutable.ListBuffer

abstract class BaseLexer[T <: Token](var input: LexerInput) extends Lexer[T] {

    private val tokenBuffer = ListBuffer.empty[TokenData[T]]
    private var mark = input.position

    private var _tokenData: TokenData[T] = _
//    var stack: List[S] = Nil

    /** initialize the stack and the current _tokenData (may be invoking advance) */
    initialize()

    /** Get the current state of the Lexer */
  //  override def state: S = stack.head

    /** Set (replace) the current state of the Lexer */
//    protected def state_=(newState: S): Unit = {
//        state.end()
//        stack = newState :: stack.tail
//        newState.begin()
//    }
//
//    /** Push a new Lexer State */
//    def pushState(newState: S): Unit = {
//        newState.begin()
//        stack = newState :: stack
//    }
//
//    /** Pop an older Lexer State */
//    def popState(): S = {
//        val r = stack.head
//        stack = stack.tail
//        r.end()
//        if (stack.nonEmpty) stack.head.restore()
//        r
//    }

    /** Check if there are emitted tokens if the specified state exists in the stack */
    def nonTokenEmitted: Boolean = tokenBuffer.isEmpty

    /** Init must initialize the stack and the current _tokenData (may be invoking advance) */
    protected def initialize()

    /** get the current token in the input stream.  */
    override def token: T = _tokenData.token

    /** All the token data.  */
    override def tokenData: TokenData[T] = _tokenData

    /** Get the current Token Char Sequence.  */
    override def tokenText: CharSequence = input.subSequence(_tokenData.start, _tokenData.end)

    /** Emit a Token */
    @failfast def emit(token: T): Boolean = {
        val newMark = input.position
        tokenBuffer += TokenData(token, InputRange(mark._1, mark._2, newMark._1, newMark._2), mark._3, newMark._3)
        mark = newMark
        true
    }

    /** Advance the lexer to the next token.  */
    override def advance(): Unit = {
        if (nonTokenEmitted) findToken()
        _tokenData = tokenBuffer.remove(0)
    }

    protected final def currentChar: Int = input.current

    final def lookAhead(n: Int): Int = input.lookAhead(n)

    final def consume(): Unit = input.consume()

    protected final def consume(n:Int): Unit = input.consume(n)
    protected def consumeWhile(p: (Int => Boolean)): Unit = input.consumeWhile(p)

    final def consumeAndEmit(token: T): Boolean = {
        consume()
        emit(token)
    }
    final def consumeAndEmit(n:Int, token: T): Boolean = if (n <= 0) true
    else {
        consume(n)
        emit(token)
    }
    final def matches(p: => Boolean): Boolean = {
        val s      = saveState
        val result = p
        if (!result) restoreState(s)
        result
    }

    final def zeroOrMore(p: => Boolean): Boolean = {
        var s = saveState
        while (nonEof && p) s = saveState
        restoreState(s)
        true
    }
    final def oneOrMore(p: => Boolean): Boolean = {
        var s      = saveState
        val result = p
        if (result) {
            do s = saveState
            while (nonEof && p)
        }
        restoreState(s)
        result
    }

    /** We're not at the Eof */
    def nonEof:Boolean = input.nonEof

    final def optional(p: Boolean) : Boolean = true

    @inline final def beginOfLine: Boolean = input.column == 0
    /**
      * Process all pending tokens. Trivial implementation just emit the EofToken
      * More complex ones can continue returning pending tokens until they emit the EofToken
      */
    protected def processPending(): Unit

    protected final def findToken(): Unit = {
//        currentChar match {
//            case EofChar => processPending()
//            case chr => state(chr)
//        }
        if (nonTokenEmitted) processPending()
    }

    def restoreState(s: (Int, (Int, Int, Int), Mark)): Unit = {
        tokenBuffer.remove(s._1, tokenBuffer.size - s._1)
        mark  = s._2
        input.reset(s._3)
    }

    def saveState: (Int, (Int, Int, Int), Mark) =
        (tokenBuffer.size, mark, input.createMark())
}

