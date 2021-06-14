package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.client.common.position.Position
import amf.core.client.common.position.Position.ZERO
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.DomainElement
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.client.scala.parse.document.EmptyFutureDeclarations
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.render.BaseEmitters.{EntryPartEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.utils.AmfStrings
import amf.core.internal.validation.CoreValidations.ResolutionValidation
import amf.plugins.document.apicontract.contexts.emitter.oas.OasSpecEmitterContext
import amf.plugins.document.apicontract.parser.spec.WebApiDeclarations
import amf.plugins.document.apicontract.parser.spec.declaration._
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{
  AgnosticShapeEmitterContextAdapter,
  OasLikeShapeEmitterContextAdapter,
  ShapeEmitterContext
}
import amf.plugins.document.apicontract.parser.spec.domain._
import amf.plugins.domain.apicontract.models.{Parameter, Payload, Response}
import org.yaml.model.YDocument.EntryBuilder

import scala.collection.mutable.ListBuffer

case class OasDeclarationsEmitter(declares: Seq[DomainElement], ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends PlatformSecrets {

  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)

  val emitters: Seq[EntryEmitter] = {

    val declarations = WebApiDeclarations(declares, UnhandledErrorHandler, EmptyFutureDeclarations())

    val result = ListBuffer[EntryEmitter]()
    result ++= OasDeclaredShapesEmitter(declarations.shapes.values.toSeq, ordering, references)(
      OasLikeShapeEmitterContextAdapter(spec))

    if (declarations.annotations.nonEmpty)
      result += OasAnnotationsTypesEmitter(declarations.annotations.values.toSeq, ordering)

    if (declarations.resourceTypes.nonEmpty)
      result += AbstractDeclarationsEmitter("resourceTypes".asOasExtension,
                                            declarations.resourceTypes.values.toSeq,
                                            ordering,
                                            Nil)

    if (declarations.traits.nonEmpty)
      result += AbstractDeclarationsEmitter("traits".asOasExtension, declarations.traits.values.toSeq, ordering, Nil)

    if (declarations.securitySchemes.nonEmpty)
      result += spec.factory.securitySchemesEmitters(declarations.securitySchemes.values.toSeq, ordering)
    val oasParams = declarations.parameters.values.map(OasParameter(_)) ++ declarations.payloads.values
      .map(OasParameter(_))
    if (oasParams.nonEmpty)
      result += OasDeclaredParametersEmitter(oasParams.toSeq, ordering, references)

    if (declarations.headers.nonEmpty)
      result += OasDeclaredHeadersEmitter(declarations.headers.values.toSeq, ordering, references)

    if (declarations.responses.nonEmpty)
      result += OasDeclaredResponsesEmitter("responses", declarations.responses.values.toSeq, ordering, references)

    if (declarations.examples.nonEmpty)
      result += OasResponseExamplesEmitter("examples", examples = declarations.examples.values.toSeq, ordering)

    if (declarations.requests.nonEmpty)
      result += Oas3RequestBodyDeclarationsEmitter(declarations.requests.values.toSeq, ordering, references)

    if (declarations.links.nonEmpty)
      result += Oas3LinkDeclarationEmitter(declarations.links.values.toSeq, ordering, references)

    if (declarations.callbacks.nonEmpty) {
      val callbacks   = declarations.callbacks.values.flatten.toSeq
      val annotations = callbacks.headOption.map(_.annotations).getOrElse(Annotations())
      result += EntryPartEmitter("callbacks",
                                 OasCallbacksEmitter(callbacks, ordering, references, annotations),
                                 position = pos(annotations))
    }
    result
  }
}

case class OasDeclaredParametersEmitter(oasParameters: Seq[OasParameter],
                                        ordering: SpecOrdering,
                                        references: Seq[BaseUnit],
                                        key: String = "parameters")(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      key,
      _.obj(traverse(ordering.sorted(oasParameters.map(OasNamedParameterEmitter(_, ordering, references))), _))
    )
  }

  override def position(): Position =
    oasParameters.headOption.map(o => pos(o.domainElement.annotations)).getOrElse(Position.ZERO)
}

case class OasNamedParameterEmitter(oasParameter: OasParameter, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {

  override def position(): Position = pos(oasParameter.domainElement.annotations)

  override def emit(b: EntryBuilder): Unit = {
    val name = oasParameter.domainElement.name.option() match {
      case Some(n) => n
      case _ =>
        spec.eh.violation(
          ResolutionValidation,
          oasParameter.domainElement.id,
          None,
          s"Cannot declare parameter without name ${oasParameter.domainElement}",
          oasParameter.domainElement.position(),
          oasParameter.domainElement.location()
        )
        "default-name"
    }
    b.entry(
      name,
      p => {
        oasParameter.domainElement match {
          case param: Parameter => ParameterEmitter(param, ordering, references, asHeader = false).emit(p)
          case payload: Payload => PayloadAsParameterEmitter(payload, ordering, references).emit(p)
          case _                => // ignore
        }
      }
    )
  }
}

case class OasNamedRefEmitter(key: String, url: String, pos: Position = ZERO)(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      key,
      spec.ref(_, url)
    )
  }

  override def position(): Position = pos
}

case class OasAnnotationsTypesEmitter(properties: Seq[CustomDomainProperty], ordering: SpecOrdering)(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry("annotationTypes".asOasExtension,
            _.obj(traverse(ordering.sorted(properties.map(OasNamedPropertyTypeEmitter(_, ordering))), _)))
  }

  override def position(): Position = properties.headOption.map(p => pos(p.annotations)).getOrElse(ZERO)
}

case class OasNamedPropertyTypeEmitter(annotationType: CustomDomainProperty, ordering: SpecOrdering)(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      annotationType.name
        .option()
        .orElse(throw new Exception(s"Cannot declare annotation type without name $annotationType"))
        .get,
      b => {
        if (annotationType.isLink)
          OasTagToReferenceEmitter(annotationType).emit(b)
        else
          spec.factory.annotationTypeEmitter(annotationType, ordering).emitters() match {
            case Left(emitters) =>
              b.obj { b =>
                traverse(ordering.sorted(emitters), b)
              }
            case Right(part) => part.emit(b)
          }

      }
    )
  }

  def emitAnnotationFields(): Unit = {}

  override def position(): Position = pos(annotationType.annotations)
}

case class OasDeclaredResponsesEmitter(key: String,
                                       responses: Seq[Response],
                                       ordering: SpecOrdering,
                                       references: Seq[BaseUnit])(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      key,
      _.obj(
        traverse(ordering.sorted(
                   responses.map(OasResponseEmitter(_, ordering, references: Seq[BaseUnit], isDeclaration = true))),
                 _)))
  }

  override def position(): Position = responses.headOption.map(a => pos(a.annotations)).getOrElse(ZERO)
}
