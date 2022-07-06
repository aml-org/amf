package amf.graphql.internal.spec.parser.validation
import amf.core.client.scala.model.domain.NamedDomainElement
import amf.core.internal.validation.core.ValidationSpecification
import amf.graphql.internal.spec.context.GraphQLWebApiContext

object ParsingValidationsHelper {

  def checkDuplicates(
      s: Seq[NamedDomainElement],
      violation: ValidationSpecification,
      message: String => String
  )(implicit ctx: GraphQLWebApiContext): Unit = {
    s.foreach({ elem =>
      val elemName = elem.name.value()
      if (s.count(_.name.value() == elemName) > 1)
        ctx.eh.violation(
          violation,
          elem,
          message(elemName),
          elem.annotations
        )
    })
  }
}
