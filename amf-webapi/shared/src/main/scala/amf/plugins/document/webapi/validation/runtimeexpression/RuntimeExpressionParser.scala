package amf.plugins.document.webapi.validation.runtimeexpression

import scala.collection.mutable
import scala.language.postfixOps
import scala.util.matching.Regex

trait RuntimeExpressionParser extends RuntimeParsingToken {
  lazy val completeStack: Seq[RuntimeParsingToken] = {
    val aux: mutable.ListBuffer[RuntimeParsingToken] = mutable.ListBuffer()
    var token: Option[RuntimeParsingToken]           = Some(this)
    do {
      aux.append(token.get)
      token = token.get.next
    } while (token.isDefined)
    aux
  }
}

trait RuntimeParsingToken {
  val value: String
  val rx: Regex
  val followedBy: Seq[String => RuntimeParsingToken]
  lazy val extraValue: String = value match {
    case rx(e) => value.stripPrefix(e)
    case _     => value
  }

  lazy val possibleApplications: Seq[String] = followedBy.flatMap(pa =>
    pa("") match {
      case l: LabeledExpressionToken => Some(l.label)
      case _                         => None
  })

  lazy val next: Option[RuntimeParsingToken] =
    if (extraValue.isEmpty) None
    else {
      Some(
        followedBy
          .find(rp => rp(extraValue).nodeIsValid)
          .map(rp => rp(extraValue))
          .getOrElse(InvalidExpressionToken(extraValue)))
    }

  def nodeIsValid: Boolean = {
    value match {
      case rx(e) => true
      case _     => false
    }
  }

  def completelyValid: Boolean = {
    if (followedBy.isEmpty) nodeIsValid
    else next.exists(_.completelyValid)
  }
}

case class InvalidExpressionToken(override val value: String) extends RuntimeParsingToken {
  override val rx: Regex                                      = "(.*)" r
  override val followedBy: Seq[String => RuntimeParsingToken] = Nil

  override lazy val next: Option[RuntimeParsingToken] = None

  override def nodeIsValid = false

  override def completelyValid: Boolean = false
}

trait LabeledExpressionToken extends RuntimeParsingToken {
  val label: String
}

trait BaseLabeledExpressionToken extends LabeledExpressionToken {
  override val followedBy: Seq[String => RuntimeParsingToken] = Nil
}
