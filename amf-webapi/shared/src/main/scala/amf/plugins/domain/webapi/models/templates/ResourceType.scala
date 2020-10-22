package amf.plugins.domain.webapi.models.templates

import amf.core.errorhandling.{ErrorHandler, UnhandledErrorHandler}
import amf.core.metamodel.Obj
import amf.core.model.document.BaseUnit
import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.model.domain.{DataNode, DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.templates.ResourceTypeModel
import amf.plugins.domain.webapi.models.EndPoint
import amf.plugins.domain.webapi.resolution.ExtendsHelper
import amf.{ProfileName, RamlProfile}
import org.yaml.model.{YMapEntry, YPart}

class ResourceType(override val fields: Fields, override val annotations: Annotations)
    extends AbstractDeclaration(fields, annotations) {

  override def linkCopy(): ResourceType = ResourceType().withId(id)

  override def meta: ResourceTypeModel.type = ResourceTypeModel

  def asEndpoint[T <: BaseUnit](unit: T,
                                profile: ProfileName = RamlProfile,
                                errorHandler: ErrorHandler = UnhandledErrorHandler): EndPoint = {
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
                                     errorHandler: ErrorHandler = UnhandledErrorHandler,
                                     profile: ProfileName = RamlProfile): EndPoint = {
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
