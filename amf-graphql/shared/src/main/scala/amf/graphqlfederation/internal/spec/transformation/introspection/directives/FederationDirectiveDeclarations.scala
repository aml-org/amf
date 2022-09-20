package amf.graphqlfederation.internal.spec.transformation.introspection.directives

import amf.core.client.scala.model.document.DeclaresModel
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty

case class FederationDirectiveDeclarations(
    `@key`: CustomDomainProperty,
    `@external`: CustomDomainProperty,
    `@requires`: CustomDomainProperty,
    `@provides`: CustomDomainProperty,
    `@shareable`: CustomDomainProperty,
    `@inaccessible`: CustomDomainProperty,
    `@override`: CustomDomainProperty
)

object FederationDirectiveDeclarations {
  def extractFrom(d: DeclaresModel): FederationDirectiveDeclarations = {
    val index =
      d.declares.flatMap {
        case cdp: CustomDomainProperty => Some(cdp.name.value() -> cdp)
        case _                         => None
      }.toMap

    FederationDirectiveDeclarations(
      index("key"),
      index("external"),
      index("requires"),
      index("provides"),
      index("shareable"),
      index("inaccessible"),
      index("override")
    )
  }
}
