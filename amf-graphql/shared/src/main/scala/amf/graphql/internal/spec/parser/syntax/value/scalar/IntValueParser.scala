package amf.graphql.internal.spec.parser.syntax.value.scalar

import amf.core.client.scala.model.DataType
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{INT_TERMINAL, INT_VALUE}

object IntValueParser extends AbstractScalarValueParser(Seq(INT_VALUE, INT_TERMINAL), DataType.Integer)
