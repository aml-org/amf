package amf.graphql.internal.spec.parser.syntax.value.scalar

import amf.graphql.internal.spec.parser.syntax.TokenTypes.{FLOAT, FLOAT_TERMINAL, FLOAT_VALUE}

object FloatValueParser extends AbstractScalarValueParser(Seq(FLOAT_VALUE, FLOAT_TERMINAL), FLOAT)
