package amf.shapes.internal.spec.raml.parser.expression

private[expression] trait AbstractParser {
  protected var current = 0
  protected val tokens: Seq[Token]

  protected def advance(): Token = {
    if (!isAtEnd) current += 1
    previous()
  }

  protected def expect(token: String) = {
    if (isAtEnd) false
    else peek().exists(_.token == token)
  }

  protected def peek() = tokens.lift(current)

  protected def previous(): Token = tokens(current - 1)

  protected def expectPrevious(token: String) = tokens.lift(current - 2).exists(_.token == token)

  protected def consume(token: String) = {
    if (expect(token)) Some(advance())
    else None
  }

  protected def consumeToEnd(): Seq[Token] = {
    val (_, rest) = tokens.splitAt(current)
    current = tokens.size
    rest
  }

  protected def consumeUntil(token: String): Seq[Token] = {
    var accumulated = Seq[Token]()
    while (!expect(token) && !isAtEnd) {
      accumulated = accumulated :+ advance()
    }
    accumulated
  }

  protected def isAtEnd = current >= tokens.size
}
