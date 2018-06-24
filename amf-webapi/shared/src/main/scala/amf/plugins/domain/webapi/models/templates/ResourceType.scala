package amf.plugins.domain.webapi.models.templates

import amf.ProfileNames
import amf.ProfileNames.ProfileName
import amf.core.metamodel.domain.templates.AbstractDeclarationModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.parser.{Annotations, DefaultUnhandledError, ErrorHandler, Fields}
import amf.plugins.domain.webapi.metamodel.templates.ResourceTypeModel
import amf.plugins.domain.webapi.models.EndPoint
import amf.plugins.domain.webapi.resolution.ExtendsHelper
import org.yaml.model.YPart

class ResourceType(override val fields: Fields, override val annotations: Annotations)
    extends AbstractDeclaration(fields, annotations) {

  override def linkCopy(): ResourceType = ResourceType().withId(id)

  override def meta: AbstractDeclarationModel = ResourceTypeModel

  def asEndpoint[T <: BaseUnit](unit: T,
                                profile: ProfileName = ProfileNames.RAML,
                                errorHandler: ErrorHandler = DefaultUnhandledError): EndPoint = {
    linkTarget match {
      case Some(_) =>
        effectiveLinkTarget.asInstanceOf[ResourceType].asEndpoint(unit, profile, errorHandler)
      case _ =>
        ExtendsHelper.asEndpoint(unit,
                                 profile,
                                 dataNode,
                                 name.value(),
                                 id,
                                 ExtendsHelper.findUnitLocationOfElement(id, unit),
                                 keepEditingInfo = false,
                                 errorHandler = errorHandler)
    }
  }
}

object ResourceType {
  def apply(): ResourceType = apply(Annotations())

  def apply(ast: YPart): ResourceType = apply(Annotations(ast))

  def apply(annotations: Annotations): ResourceType = ResourceType(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): ResourceType = new ResourceType(fields, annotations)
}
