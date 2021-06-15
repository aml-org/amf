package amf.plugins.domain.apicontract.models.templates

import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.templates.AbstractDeclaration
import amf.core.client.scala.model.domain.{DataNode, DomainElement, Linkable}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.plugins.domain.apicontract.metamodel.templates.ResourceTypeModel
import amf.plugins.domain.apicontract.models.EndPoint
import amf.plugins.domain.apicontract.resolution.ExtendsHelper
import org.yaml.model.{YMapEntry, YPart}

class ResourceType(override val fields: Fields, override val annotations: Annotations)
    extends AbstractDeclaration(fields, annotations) {

  override def linkCopy(): ResourceType = ResourceType().withId(id)

  override def meta: ResourceTypeModel.type = ResourceTypeModel

  def asEndpoint[T <: BaseUnit](unit: T,
                                profile: ProfileName = Raml10Profile,
                                errorHandler: AMFErrorHandler = UnhandledErrorHandler): EndPoint = {
    linkTarget match {
      case Some(_) =>
        effectiveLinkTarget().asInstanceOf[ResourceType].asEndpoint(unit, profile, errorHandler)
      case _ =>
        Option(dataNode)
          .map { dataNode =>
            val extendsHelper = ExtendsHelper(profile, keepEditingInfo = false, errorHandler)
            extendsHelper.asEndpoint(unit, dataNode, annotations, name.value(), id)
          }
          .getOrElse(EndPoint())
    }
  }

  def entryAsEndpoint[T <: BaseUnit](unit: T,
                                     node: DataNode,
                                     entry: YMapEntry,
                                     annotations: Annotations,
                                     errorHandler: AMFErrorHandler = UnhandledErrorHandler,
                                     profile: ProfileName = Raml10Profile): EndPoint = {
    val helper = ExtendsHelper(profile, keepEditingInfo = false, errorHandler)
    helper.entryAsEndpoint(unit, node, name.option().getOrElse(""), id, entry)
  }

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
