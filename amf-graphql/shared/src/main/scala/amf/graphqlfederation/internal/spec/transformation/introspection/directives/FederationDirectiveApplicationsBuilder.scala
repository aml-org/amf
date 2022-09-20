package amf.graphqlfederation.internal.spec.transformation.introspection.directives

import amf.core.client.scala.model.domain.ObjectNode
import amf.core.client.scala.model.domain.extensions.DomainExtension

case class FederationDirectiveApplicationsBuilder(declarations: FederationDirectiveDeclarations) extends Utils {
  def `@key`(fieldSet: String, resolvable: Boolean): DomainExtension = {
    val extension = ObjectNode()
      .addProperty("fields", asScalarNode(fieldSet))
    if (!resolvable) extension.addProperty("resolvable", asScalarNode(resolvable))

    val definedBy = declarations.`@key`

    DomainExtension()
      .withName("key")
      .withExtension(extension)
      .withDefinedBy(definedBy)
  }

  def `@requires`(fieldSet: String): DomainExtension = {
    val extension = ObjectNode()
      .addProperty("fields", asScalarNode(fieldSet))

    val definedBy = declarations.`@requires`

    DomainExtension()
      .withName("requires")
      .withExtension(extension)
      .withDefinedBy(definedBy)
  }

  def `@provides`(fieldSet: String): DomainExtension = {
    val extension = ObjectNode()
      .addProperty("fields", asScalarNode(fieldSet))

    val definedBy = declarations.`@provides`

    DomainExtension()
      .withName("provides")
      .withExtension(extension)
      .withDefinedBy(definedBy)
  }

  def `@shareable`: DomainExtension = {
    val extension = ObjectNode()
    val definedBy = declarations.`@shareable`
    DomainExtension()
      .withName("shareable")
      .withExtension(extension)
      .withDefinedBy(definedBy)
  }

  def `@inaccessible`: DomainExtension = {
    val extension = ObjectNode()
    val definedBy = declarations.`@inaccessible`
    DomainExtension()
      .withName("inaccessible")
      .withExtension(extension)
      .withDefinedBy(definedBy)
  }

  def `@override`(from: String): DomainExtension = {
    val extension = ObjectNode()
      .addProperty("from", asScalarNode(from))
    val definedBy = declarations.`@override`
    DomainExtension()
      .withName("override")
      .withExtension(extension)
      .withDefinedBy(definedBy)
  }

  def `@external`: DomainExtension = {
    val extension = ObjectNode()
    val definedBy = declarations.`@external`
    DomainExtension()
      .withName("external")
      .withExtension(extension)
      .withDefinedBy(definedBy)
  }

}
