package amf.plugins.document.apicontract.parser.spec.raml.expression

private[expression] trait AbstractParser {
  protected var current = 0
  protected val tokens: Seq[Token]

  protected def advance(): Token = {
    if (!isAtEnd) current += 1
    previous()
  }

  protected def check(token: String) = {
    if (isAtEnd) false
    else peek().token == token
  }

  protected def peek() = tokens(current)

  protected def previous(): Token = tokens(current - 1)

  protected def consume(token: String) = {
    if (check(token)) Some(advance())
    else None
  }

  protected def consumeToEnd(): Seq[Token] = {
    val rest = tokens.splitAt(current)._2
    current = tokens.size
    rest
  }

  protected def consumeUntil(token: String): Seq[Token] = {
    var accumulated = Seq[Token]()
    while (!check(token) && !isAtEnd) {
      accumulated = accumulated :+ advance()
    }
    accumulated
  }

  protected def isAtEnd = current >= tokens.size
}
