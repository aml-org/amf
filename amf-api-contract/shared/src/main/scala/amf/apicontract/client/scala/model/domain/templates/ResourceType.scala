package amf.apicontract.client.scala.model.domain.templates

import amf.apicontract.client.scala.model.domain.EndPoint
import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.templates.AbstractDeclaration
import amf.core.client.scala.model.domain.{DataNode, DomainElement, Linkable}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.templates.ResourceTypeModel
import amf.apicontract.internal.spec.common.transformation.ExtendsHelper
import org.yaml.model.{YMapEntry, YPart}

class ResourceType(override val fields: Fields, override val annotations: Annotations)
    extends AbstractDeclaration(fields, annotations) {

  override def linkCopy(): ResourceType = ResourceType().withId(id)

  override def meta: ResourceTypeModel.type = ResourceTypeModel

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = ResourceType.apply

  override protected def declarationComponent: String = "resourceType"
}

object ResourceType {
  def apply(): ResourceType = apply(Annotations())

  def apply(ast: YPart): ResourceType = apply(Annotations(ast))

  def apply(annotations: Annotations): ResourceType = ResourceType(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): ResourceType = new ResourceType(fields, annotations)
}
