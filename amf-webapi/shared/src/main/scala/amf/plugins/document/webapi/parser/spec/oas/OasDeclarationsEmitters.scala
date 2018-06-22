package amf.plugins.document.webapi.parser.spec.oas

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.model.domain.{DomainElement, Shape}
import amf.core.parser.Position.ZERO
import amf.core.parser.{EmptyFutureDeclarations, FieldEntry, Position}
import amf.core.unsafe.PlatformSecrets
import amf.plugins.document.webapi.contexts.OasSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain.{OasResponseEmitter, ParameterEmitter}
import amf.plugins.domain.shapes.models.CreativeWork
import amf.plugins.domain.webapi.models.{Parameter, Response}
import org.yaml.model.YDocument.EntryBuilder
import amf.core.utils.Strings

import scala.collection.mutable.ListBuffer

case class OasDeclarationsEmitter(declares: Seq[DomainElement], ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends PlatformSecrets {
  val emitters: Seq[EntryEmitter] = {

    val declarations = WebApiDeclarations(declares, None, EmptyFutureDeclarations())

    val result = ListBuffer[EntryEmitter]()

    if (declarations.shapes.nonEmpty)
      result += OasDeclaredTypesEmitters(declarations.shapes.values.toSeq, ordering, references)

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
      result += OasSecuritySchemesEmitters(declarations.securitySchemes.values.toSeq, ordering)

    if (declarations.parameters.nonEmpty)
      result += OasDeclaredParametersEmitter(declarations.parameters.values.toSeq, ordering, references)

    if (declarations.responses.nonEmpty)
      result += OasDeclaredResponsesEmitter("responses", declarations.responses.values.toSeq, ordering, references)
    result
  }
}

case class OasDeclaredTypesEmitters(types: Seq[Shape], ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry("definitions",
            _.obj(traverse(ordering.sorted(types.map(OasNamedTypeEmitter(_, ordering, references))), _)))
  }

  override def position(): Position = types.headOption.map(a => pos(a.annotations)).getOrElse(ZERO)
}

case class OasDeclaredParametersEmitter(parameters: Seq[Parameter], ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "parameters",
      _.obj(traverse(ordering.sorted(parameters.map(OasNamedParameterEmitter(_, ordering, references))), _))
    )
  }

  override def position(): Position = parameters.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)
}

case class OasNamedParameterEmitter(parameter: Parameter, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def position(): Position = pos(parameter.annotations)

  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      parameter.name.option().getOrElse(throw new Exception(s"Cannot declare shape without name $parameter")),
      b => {
        if (parameter.isLink) OasTagToReferenceEmitter(parameter, parameter.linkLabel.option(), Nil).emit(b)
        else ParameterEmitter(parameter, ordering, references).emit(b)
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
          OasTagToReferenceEmitter(annotationType, annotationType.linkLabel.option(), Nil).emit(b)
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

case class OasUserDocumentationsEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit spec: OasSpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {

    val documents: List[CreativeWork] = f.array.values.collect({ case c: CreativeWork => c }).toList

    documents match {
      case head :: Nil => Seq(OasEntryCreativeWorkEmitter("externalDocs", head, ordering))
      case head :: tail =>
        Seq(OasEntryCreativeWorkEmitter("externalDocs", head, ordering), OasCreativeWorkEmitters(tail, ordering))
      case _ => Nil
    }

  }
}

case class OasCreativeWorkEmitters(documents: Seq[CreativeWork], ordering: SpecOrdering)(
    implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "userDocumentation".asOasExtension,
      _.list(traverse(ordering.sorted(documents.map(RamlCreativeWorkEmitter(_, ordering, withExtension = false))), _))
    )
  }

  override def position(): Position = documents.headOption.map(_.annotations).map(pos).getOrElse(Position.ZERO)
}

case class OasDeclaredResponsesEmitter(key: String,
                                       responses: Seq[Response],
                                       ordering: SpecOrdering,
                                       references: Seq[BaseUnit])(implicit spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      key,
      _.obj(traverse(ordering.sorted(responses.map(OasResponseEmitter(_, ordering, references: Seq[BaseUnit]))), _)))
  }

  override def position(): Position = responses.headOption.map(a => pos(a.annotations)).getOrElse(ZERO)
}
