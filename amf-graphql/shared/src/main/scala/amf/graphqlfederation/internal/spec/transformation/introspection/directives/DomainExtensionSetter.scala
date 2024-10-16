package amf.graphqlfederation.internal.spec.transformation.introspection.directives

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.core.client.scala.model.domain.federation.{FederationMetadata, HasFederationMetadata}
import amf.shapes.client.scala.model.domain.NodeShape

case class DomainExtensionSetter(build: FederationDirectiveApplicationsBuilder) {

  def fromKeysIn(node: NodeShape): this.type = {
    node.keys match {
      case null => // skip
      case keys =>
        val extensions = keys.map { key =>
          val fs = FieldSet.parse(key.components)
          build.`@key`(fs.toString, key.isResolvable.value())
        }
        node.withCustomDomainProperties(node.customDomainProperties ++ extensions)
    }
    this
  }

  def fromRequiresIn(prop: PropertyShape): this.type = {
    if (prop.requires.nonEmpty) {
      val extension = {
        val fs = FieldSet.parse(prop.requires)
        build.`@requires`(fs.toString)
      }
      prop.withCustomDomainProperties(prop.customDomainProperties :+ extension)
    }
    this
  }

  def fromProvidesIn(prop: PropertyShape): this.type = {
    if (prop.provides.nonEmpty) {
      val extension = {
        val fs = FieldSet.parse(prop.provides)
        build.`@provides`(fs.toString)
      }
      prop.withCustomDomainProperties(prop.customDomainProperties :+ extension)
    }
    this
  }

  def fromShareableIn[T <: FederationMetadata](elem: HasFederationMetadata[T]): this.type = {
    elem.federationMetadata match {
      case null => // skip
      case federationMetadata if federationMetadata.shareable.value() =>
        val extension = build.`@shareable`
        elem.withCustomDomainProperties(elem.customDomainProperties :+ extension)
      case _ => // skip
    }
    this
  }

  def fromInaccessibleIn[T <: FederationMetadata](elem: HasFederationMetadata[T]): this.type = {
    elem.federationMetadata match {
      case null => // skip
      case federationMetadata if federationMetadata.inaccessible.value() =>
        val extension = build.`@inaccessible`
        elem.withCustomDomainProperties(elem.customDomainProperties :+ extension)
      case _ => // skip
    }
    this
  }

  def fromOverrideIn[T <: FederationMetadata](elem: HasFederationMetadata[T]): this.type = {
    elem.federationMetadata match {
      case null => // skip
      case federationMetadata if federationMetadata.overrideFrom.nonEmpty =>
        val extension = build.`@override`(federationMetadata.overrideFrom.value())
        elem.withCustomDomainProperties(elem.customDomainProperties :+ extension)
      case _ => // skip
    }
    this
  }

  def fromExternalIn(prop: PropertyShape): this.type = {
    if (prop.isStub.value()) {
      val extension = build.`@external`
      prop.withCustomDomainProperties(prop.customDomainProperties :+ extension)
    }
    this
  }
}
