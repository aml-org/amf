package amf.graphql.internal.spec.parser.syntax.value.scalar

import amf.core.client.scala.model.DataType
import amf.graphql.internal.spec.parser.syntax.TokenTypes.{ENUM_VALUE, NAME, NAME_TERMINAL}

object EnumValueParser extends AbstractScalarValueParser(Seq(ENUM_VALUE, NAME, NAME_TERMINAL), DataType.Any)
