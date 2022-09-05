package amf.graphql.internal.spec.parser.syntax.value.scalar

import amf.graphql.internal.spec.parser.syntax.TokenTypes.{STRING, STRING_TERMINAL, STRING_VALUE}

object StringValueParser extends AbstractScalarValueParser(Seq(STRING_VALUE, STRING_TERMINAL), STRING)
