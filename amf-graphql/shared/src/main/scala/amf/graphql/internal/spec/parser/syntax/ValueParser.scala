package amf.graphql.internal.spec.parser.syntax

import amf.core.client.scala.model.domain.DataNode
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.parser.syntax.RichSeq.seq2RichSeq
import amf.graphql.internal.spec.parser.syntax.value.{AbstractValueParser, ListValueParser, ObjectValueParser}
import amf.graphql.internal.spec.parser.syntax.value.scalar._
import org.mulesoft.antlrast.ast.Node

import scala.language.implicitConversions

object ValueParser {
  private val parsers: Seq[AbstractValueParser[_ <: DataNode]] =
    Seq(ObjectValueParser, ListValueParser, IntValueParser, FloatValueParser, StringValueParser, BooleanValueParser, EnumValueParser)

  def parseValue(node: Node, path: Seq[String] = Nil)(implicit ctx: GraphQLBaseWebApiContext): Option[DataNode] =
    parsers.firstThatCan(_.parse(node, path))
}

sealed case class RichSeq[A](seq: Seq[A]) {
  def firstThatCan[B](fn: (A) => Option[B]): Option[B] = {
    val it = seq.iterator
    while (it.hasNext) {
      fn(it.next()) match {
        case Some(value) => return Some(value)
        case None        => // nothing
      }
    }
    None
  }
}

object RichSeq {
  implicit def seq2RichSeq[A](s: Seq[A]): RichSeq[A] = RichSeq(s)
}
