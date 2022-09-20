package amf.graphql.internal.spec.parser.syntax.value.scalar

import amf.core.client.scala.model.DataType
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{FLOAT_TERMINAL, FLOAT_VALUE}

object FloatValueParser extends AbstractScalarValueParser(Seq(FLOAT_VALUE, FLOAT_TERMINAL), DataType.Float)
