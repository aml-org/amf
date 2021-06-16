package amf.apicontract.client.scala.model.domain.templates

import amf.apicontract.client.scala.model.domain.Operation
import amf.core.client.common.validation.{ProfileName, Raml10Profile}
import amf.core.client.scala.errorhandling.{AMFErrorHandler, UnhandledErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.templates.AbstractDeclaration
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.parser.domain.{Annotations, Fields}
import amf.apicontract.internal.metamodel.domain.templates.TraitModel
import amf.apicontract.internal.spec.common.transformation.ExtendsHelper
import org.yaml.model.{YMapEntry, YPart}

class Trait(override val fields: Fields, override val annotations: Annotations)
    extends AbstractDeclaration(fields, annotations) {
  override def linkCopy(): Trait = {
    Trait().withId(id)
  }

  override def meta: TraitModel.type = TraitModel

  /** Get this trait as an operation. No variables will be replaced. Pass the BaseUnit that contains this trait to use its declarations and the profile ProfileNames.RAML08 if this is from a raml08 unit. */
  def asOperation[T <: BaseUnit](unit: T,
                                 profile: ProfileName = Raml10Profile,
                                 errorHandler: AMFErrorHandler = UnhandledErrorHandler): Operation = {
    linkTarget match {
      case Some(_) =>
        effectiveLinkTarget().asInstanceOf[Trait].asOperation(unit, profile, errorHandler)
      case _ =>
        Option(dataNode)
          .map { dataNode =>
            val extendsHelper = ExtendsHelper(profile, keepEditingInfo = false, errorHandler)
            extendsHelper.asOperation(
              dataNode,
              unit,
              name.option().getOrElse(""),
              annotations,
              id
            )
          }
          .getOrElse(Operation())
    }
  }

  def entryAsOperation[T <: BaseUnit](unit: T,
                                      entry: YMapEntry,
                                      annotations: Annotations,
                                      profile: ProfileName = Raml10Profile,
                                      errorHandler: AMFErrorHandler = UnhandledErrorHandler): Operation = {
    val extendsHelper = ExtendsHelper(profile, keepEditingInfo = false, errorHandler)
    extendsHelper.entryAsOperation(unit, name.option().getOrElse(""), id, entry)
  }

  /** apply method for create a new instance with fields and annotations. Aux method for copy */
  override protected def classConstructor: (Fields, Annotations) => Linkable with DomainElement = Trait.apply

  override protected def declarationComponent: String = "trait"
}

object Trait {
  def apply(): Trait = apply(Annotations())

  def apply(ast: YPart): Trait = apply(Annotations(ast))

  def apply(annotations: Annotations): Trait = Trait(Fields(), annotations)

  def apply(fields: Fields, annotations: Annotations): Trait = new Trait(fields, annotations)
}
