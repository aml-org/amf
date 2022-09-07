package amf.graphql.internal.spec.parser.syntax.value.scalar

import amf.graphql.internal.spec.parser.syntax.TokenTypes.{ENUM, ENUM_VALUE, NAME, NAME_TERMINAL}

object EnumValueParser extends AbstractScalarValueParser(Seq(ENUM_VALUE, NAME, NAME_TERMINAL), ENUM)
