package org.mulesoft.lexer

import scala.collection.mutable.ListBuffer

abstract class BaseLexer[T <: Token](var input: LexerInput) extends Lexer[T] {

    private var _tokenData: TokenData[T] = _
    private var _state: LexerState = LexerState.Initial

    private val tokenBuffer = ListBuffer.empty[TokenData[T]]

    private var mark = input.position

    /** The processing functions for each state */
    private val states: Array[(Int) => Boolean] = {
        val m = stateMap
        val stateArray = new Array[Int => Boolean](m.size)
        for ((s, f) <- m)
            stateArray(s.ordinal) = f
        stateArray
    }

    // Advance to first token
    advance()

    /** get the current token in the input stream.  */
    override def token: T = _tokenData.token

    /** All the token data.  */
    override def tokenData: TokenData[T] = _tokenData

    /** Ge the current state of the Lexer.  */
    override def state: LexerState = _state

    def state_=(newState: LexerState): Unit = _state = newState

    /** Get the current Token Char Sequence.  */
    override def tokenText: CharSequence = input.subSequence(_tokenData.start, _tokenData.end)

    /** Emit a Token */
    protected def emit(token: T): Boolean = {
        val newMark = input.position
        tokenBuffer += TokenData(token,
            InputRange(mark._1, mark._2, newMark._1, newMark._2),
            mark._3,
            newMark._3)
        mark = newMark
        true
    }

    /** Advance the lexer to the next token.  */
    override def advance(): Unit = {
        if (tokenBuffer.isEmpty)
            findToken()

        _tokenData = tokenBuffer.remove(0)
    }

    protected final def currentChar: Int = input.current

    protected final def lookAhead(n:Int): Int = input.lookAhead(n)

    protected final def consume(): Unit = input.consume()
    protected final def consume(n:Int): Unit = for (_ <- 0 until n) input.consume()

    /**
      * Process all pending tokens. Trivial implementation just emit the EofToken
      * More complex ones can continue returning pending tokens until they emit the EofToken
      */
    protected def processPending(): Unit

    /**
      * Return the Map of function of processing for each state
      */
    protected def stateMap: Map[LexerState, Int => Boolean]

    protected final def findToken(): Unit = {
        currentChar match {
            case LexerInput.EofChar if state == LexerState.Initial =>
                processPending()
            case chr =>
                while (!states(state.ordinal)(chr)) consume()
        }
        if (tokenBuffer.isEmpty) processPending()
    }
}
