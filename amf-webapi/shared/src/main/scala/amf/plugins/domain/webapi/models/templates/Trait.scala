package amf.plugins.domain.webapi.models.templates

import amf.core.errorhandling.{ErrorHandler, UnhandledErrorHandler}
import amf.core.metamodel.Obj
import amf.core.metamodel.domain.templates.AbstractDeclarationModel
import amf.core.model.document.BaseUnit
import amf.core.model.domain.templates.AbstractDeclaration
import amf.core.model.domain.{DomainElement, Linkable}
import amf.core.parser.{Annotations, Fields}
import amf.plugins.domain.webapi.metamodel.templates.TraitModel
import amf.plugins.domain.webapi.models.Operation
import amf.plugins.domain.webapi.resolution.ExtendsHelper
import amf.{ProfileName, RamlProfile}
import org.yaml.model.{YMapEntry, YPart}

class Trait(override val fields: Fields, override val annotations: Annotations)
    extends AbstractDeclaration(fields, annotations) {
  override def linkCopy(): Trait = {
    Trait().withId(id)
  }

  override def meta: Obj = TraitModel

  /** Get this trait as an operation. No variables will be replaced. Pass the BaseUnit that contains this trait to use its declarations and the profile ProfileNames.RAML08 if this is from a raml08 unit. */
  def asOperation[T <: BaseUnit](unit: T,
                                 profile: ProfileName = RamlProfile,
                                 errorHandler: ErrorHandler = UnhandledErrorHandler): Operation = {
    linkTarget match {
      case Some(_) =>
        effectiveLinkTarget().asInstanceOf[Trait].asOperation(unit, profile, errorHandler)
      case _ =>
        Option(dataNode)
          .map(
            ExtendsHelper.asOperation(
              profile,
              _,
              unit,
              name.option().getOrElse(""),
              annotations,
              id,
              ExtendsHelper.findUnitLocationOfElement(id, unit),
              keepEditingInfo = false,
              errorHandler = errorHandler
            ))
          .orNull // TODO should we return null or an empty operation?
    }
  }

  def entryAsOperation[T <: BaseUnit](unit: T,
                                      entry: YMapEntry,
                                      annotations: Annotations,
                                      profile: ProfileName = RamlProfile,
                                      errorHandler: ErrorHandler = UnhandledErrorHandler): Operation = {
    ExtendsHelper.entryAsOperation(profile,
                                   unit,
                                   name.option().getOrElse(""),
                                   id,
                                   keepEditingInfo = false,
                                   entry,
                                   annotations = annotations,
                                   errorHandler = errorHandler)
  }

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = Trait.apply
}

object Trait {
  def apply(): Trait = apply(Annotations())

  def apply(ast: YPart): Trait = apply(Annotations(ast))

  def apply(annotations: Annotations): Trait = Trait(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Trait = new Trait(fields, annotations)
}
