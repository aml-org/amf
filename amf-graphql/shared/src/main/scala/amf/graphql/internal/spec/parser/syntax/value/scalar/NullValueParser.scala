package amf.graphql.internal.spec.parser.syntax.value.scalar

import amf.graphql.internal.spec.parser.syntax.TokenTypes.{NULL_TERMINAL, NULL_VALUE}

object NullValueParser extends AbstractScalarValueParser(Seq(NULL_VALUE, NULL_TERMINAL), NULL_TERMINAL)
