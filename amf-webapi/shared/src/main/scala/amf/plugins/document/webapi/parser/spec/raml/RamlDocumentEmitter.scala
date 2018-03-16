package amf.plugins.document.webapi.parser.spec.raml

import amf.core.annotations._
import amf.core.emitter.BaseEmitters.{ValueEmitter, _}
import amf.core.emitter._
import amf.core.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.model.document._
import amf.core.model.domain._
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.parser.Position.ZERO
import amf.core.parser.{Annotations, EmptyFutureDeclarations, FieldEntry, Fields, Position, Value}
import amf.core.remote._
import amf.core.utils.TSort.tsort
import amf.plugins.document.webapi.contexts.{RamlScalarEmitter, RamlSpecEmitterContext, SpecEmitterContext}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.common.IdCounter
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.oas.{OasDeclaredResponsesEmitter, TagsEmitter}
import amf.plugins.domain.shapes.models.{AnyShape, CreativeWork}
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.models._
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YDocument, YNode}
import org.yaml.render.YamlRender

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class Raml08RootLevelEmitters(document: BaseUnit with DeclaresModel, ordering: SpecOrdering)(
    implicit override val spec: RamlSpecEmitterContext)
    extends RamlRootLevelEmitters(document, ordering) {
  override def emitters: Seq[EntryEmitter] =
    declarationsEmitter()

  override def declarationsEmitter(): Seq[EntryEmitter] = {

    val declarations = WebApiDeclarations(document.declares, None, EmptyFutureDeclarations())

    val result = ListBuffer[EntryEmitter]()

    if (declarations.shapes.nonEmpty)
      result += DeclaredTypesEmitters(declarations.shapes.values.toSeq, document.references, ordering)

//    if (declarations.annotations.nonEmpty)
//      result += AnnotationsTypesEmitter(declarations.annotations.values.toSeq, document.references, ordering)

    if (declarations.resourceTypes.nonEmpty)
      result += AbstractDeclarationsEmitter("resourceTypes",
                                            declarations.resourceTypes.values.toSeq,
                                            ordering,
                                            document.references)

    if (declarations.traits.nonEmpty)
      result += AbstractDeclarationsEmitter("traits", declarations.traits.values.toSeq, ordering, document.references)

    if (declarations.securitySchemes.nonEmpty)
      result += RamlSecuritySchemesEmitters(declarations.securitySchemes.values.toSeq,
                                            document.references,
                                            ordering,
                                            spec.factory.namedSecurityEmitter)
//    if (declarations.parameters.nonEmpty)
//      result += DeclaredParametersEmitter(declarations.parameters.values.toSeq, ordering, document.references) // todo here or move to 1.0 only?

    if (declarations.responses.nonEmpty)
      result += OasDeclaredResponsesEmitter("(responses)",
                                            declarations.responses.values.toSeq,
                                            ordering,
                                            document.references)(toOas(spec))

    result
  }
}
case class Raml10RootLevelEmitters(document: BaseUnit with DeclaresModel, ordering: SpecOrdering)(
    implicit override val spec: RamlSpecEmitterContext)
    extends RamlRootLevelEmitters(document, ordering) {

  override def emitters: Seq[EntryEmitter] = {
    val declares   = declarationsEmitter()
    val references = ReferencesEmitter(document.references, ordering)
    val extension  = extensionEmitter()
    val usage: Option[ValueEmitter] =
      document.fields.entry(BaseUnitModel.Usage).map(f => ValueEmitter("usage", f))

    declares ++ extension ++ usage :+ references
  }

  def extensionEmitter(): Option[EntryEmitter] =
    document.fields
      .entry(ExtensionLikeModel.Extends)
      .map(f => MapEntryEmitter("extends", f.scalar.toString, position = pos(f.value.annotations)))

  case class AnnotationsTypesEmitter(properties: Seq[CustomDomainProperty],
                                     references: Seq[BaseUnit],
                                     ordering: SpecOrdering)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry("annotationTypes", _.obj { b =>
        traverse(ordering.sorted(properties.map(p => NamedPropertyTypeEmitter(p, references, ordering))), b)
      })
    }
    override def position(): Position = properties.headOption.map(p => pos(p.annotations)).getOrElse(ZERO)
  }

  case class NamedPropertyTypeEmitter(annotation: CustomDomainProperty,
                                      references: Seq[BaseUnit],
                                      ordering: SpecOrdering)
      extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      val name = annotation.name.option().orElse(throw new Exception(s"Annotation type without name $annotation")).get
      b.entry(name, if (annotation.isLink) emitLink _ else emitInline _)
    }

    private def emitLink(b: PartBuilder): Unit = {
      annotation.linkTarget.foreach { l =>
        spec.factory.tagToReferenceEmitter(l, annotation.linkLabel, references).emit(b)
      }
    }

    private def emitInline(b: PartBuilder): Unit = {
      spec.factory.annotationTypeEmitter(annotation, ordering).emitters() match {
        case Left(emitters) =>
          b.obj { e =>
            traverse(ordering.sorted(emitters), e)
          }
        case Right(part) =>
          part.emit(b)
      }
    }

    override def position(): Position = pos(annotation.annotations)
  }

  override def declarationsEmitter(): Seq[EntryEmitter] = {

    val declarations = WebApiDeclarations(document.declares, None, EmptyFutureDeclarations())

    val result = ListBuffer[EntryEmitter]()

    if (declarations.shapes.nonEmpty)
      result += DeclaredTypesEmitters(declarations.shapes.values.toSeq, document.references, ordering)

    if (declarations.annotations.nonEmpty)
      result += AnnotationsTypesEmitter(declarations.annotations.values.toSeq, document.references, ordering)

    if (declarations.resourceTypes.nonEmpty)
      result += AbstractDeclarationsEmitter("resourceTypes",
                                            declarations.resourceTypes.values.toSeq,
                                            ordering,
                                            document.references)

    if (declarations.traits.nonEmpty)
      result += AbstractDeclarationsEmitter("traits", declarations.traits.values.toSeq, ordering, document.references)

    if (declarations.securitySchemes.nonEmpty)
      result += RamlSecuritySchemesEmitters(declarations.securitySchemes.values.toSeq,
                                            document.references,
                                            ordering,
                                            spec.factory.namedSecurityEmitter)
    if (declarations.parameters.nonEmpty)
      result += DeclaredParametersEmitter(declarations.parameters.values.toSeq, ordering, document.references) // todo here or move to 1.0 only?

    if (declarations.responses.nonEmpty)
      result += OasDeclaredResponsesEmitter("(responses)",
                                            declarations.responses.values.toSeq,
                                            ordering,
                                            document.references)(toOas(spec))

    result
  }

  case class DeclaredParametersEmitter(parameters: Seq[Parameter], ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        "(parameters)",
        _.obj(traverse(ordering.sorted(parameters.map(NamedParameterEmitter(_, ordering, references))), _))
      )
    }

    override def position(): Position = parameters.headOption.map(a => pos(a.annotations)).getOrElse(ZERO)
  }

  case class NamedParameterEmitter(parameter: Parameter, ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      parameter.fields.get(ParameterModel.Binding).annotations += ExplicitField()
      Raml10ParameterEmitter(parameter, ordering, references).emit(b) // todo, only 1.0 parametersdeclared?? move to 1.0 only, with DeclaredParametersEmitters?
    }

    override def position(): Position = pos(parameter.annotations)
  }
}

abstract class RamlRootLevelEmitters(doc: BaseUnit with DeclaresModel, ordering: SpecOrdering)(
    implicit val spec: RamlSpecEmitterContext) {

  def emitters: Seq[EntryEmitter]

  def declarationsEmitter(): Seq[EntryEmitter]

  case class DeclaredTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        spec.factory.typesKey,
        _.obj { b =>
          traverse(
            ordering.sorted(types.map {
              case s: AnyShape => RamlNamedTypeEmitter(s, ordering, references, spec.factory.typesEmitter)
              case _           => throw new Exception("Cannot emit non WebApi shape")
            }),
            b
          )
        }
      )
    }

    override def position(): Position = types.headOption.map(a => pos(a.annotations)).getOrElse(ZERO)
  }
}

case class ReferenceEmitter(reference: BaseUnit, ordering: SpecOrdering, aliasGenerator: () => String)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val aliases = reference.annotations.find(classOf[Aliases])

    def entry(tuple: (String, String)): Unit = tuple match {
      case (alias, path) =>
        val ref = path match {
          case "" => name
          case _  => path
        }
        MapEntryEmitter(alias, ref).emit(b)
    }

    aliases.fold {
      entry(aliasGenerator() -> "")
    } { _ =>
      aliases.foreach(_.aliases.foreach(entry))
    }
  }

  private def name: String = {
    Option(reference.location) match {
      case Some(location) => location
      case None           => reference.id
    }
  }

  override def position(): Position = ZERO
}

case class ReferencesEmitter(references: Seq[BaseUnit], ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val modules = references.collect({ case m: Module => m })
    if (modules.nonEmpty) {
      val idCounter = new IdCounter()
      b.entry("uses", _.obj { b =>
        traverse(ordering.sorted(modules.map(r => ReferenceEmitter(r, ordering, () => idCounter.genId("uses")))), b)
      })
    }
  }

  override def position(): Position = ZERO
}

case class RamlDocumentEmitter(document: BaseUnit)(implicit val spec: RamlSpecEmitterContext) {

  private def retrieveWebApi(): WebApi = document match {
    case document: Document => document.encodes.asInstanceOf[WebApi]
    case _                  => throw new Exception("BaseUnit doesn't encode a WebApi.")
  }
  def apiEmitters(ordering: SpecOrdering): Seq[EntryEmitter] = {
    val model  = retrieveWebApi()
    val vendor = model.annotations.find(classOf[SourceVendor]).map(_.vendor)
    WebApiEmitter(model, ordering, vendor, document.references).emitters
  }

  def emitDocument(): YDocument = {
    val doc                    = document.asInstanceOf[Document]
    val ordering: SpecOrdering = SpecOrdering.ordering(Raml, doc.encodes.annotations)

    val content = spec.factory.rootLevelEmitters(doc, ordering).emitters ++ apiEmitters(ordering)

    YDocument(b => {
      b.comment(spec.factory.retrieveHeader(document))
      b.obj { b =>
        traverse(ordering.sorted(content), b)
      }
    })
  }

  case class WebApiEmitter(api: WebApi,
                           ordering: SpecOrdering,
                           vendor: Option[Vendor],
                           references: Seq[BaseUnit] = Seq())(implicit val spec: RamlSpecEmitterContext) {

    val emitters: Seq[EntryEmitter] = {
      val fs     = api.fields
      val result = mutable.ListBuffer[EntryEmitter]()

      fs.entry(WebApiModel.Name).map(f => result += RamlScalarEmitter("title", f))

      fs.entry(WebApiModel.BaseUriParameters)
        .map(f => result += RamlParametersEmitter("baseUriParameters", f, ordering, references))

      fs.entry(WebApiModel.Description).map(f => result += RamlScalarEmitter("description", f))

      fs.entry(WebApiModel.ContentType).map(f => result += ArrayEmitter("mediaType", f, ordering))

      fs.entry(WebApiModel.Version).map(f => result += RamlScalarEmitter("version", f))

      fs.entry(WebApiModel.TermsOfService).map(f => result += ValueEmitter("(termsOfService)", f))

      fs.entry(WebApiModel.Schemes)
        .filter(!_.value.annotations.contains(classOf[SynthesizedField]))
        .map(f => result += ArrayEmitter("protocols", f, ordering))

      fs.entry(WebApiModel.Provider).map(f => result += OrganizationEmitter("(contact)", f, ordering))

      fs.entry(WebApiModel.Tags)
        .map(f => result += TagsEmitter("(tags)", f.array.values.asInstanceOf[Seq[Tag]], ordering)(toOas(spec)))

      fs.entry(WebApiModel.Documentations).map(f => result += UserDocumentationsEmitter(f, ordering))

      fs.entry(WebApiModel.License).map(f => result += LicenseEmitter("(license)", f, ordering))

      fs.entry(WebApiModel.EndPoints).map(f => result ++= endpoints(f, ordering, vendor))

      result += BaseUriEmitter(fs, api.annotations.find(classOf[BaseUriAnnotation]))

      result ++= AnnotationsEmitter(api, ordering).emitters

      fs.entry(WebApiModel.Security).map(f => result += ParametrizedSecuritiesSchemeEmitter("securedBy", f, ordering))

      ordering.sorted(result)
    }

    private def endpoints(f: FieldEntry, ordering: SpecOrdering, vendor: Option[Vendor]): Seq[EntryEmitter] = {

      def defaultOrder(emitters: Seq[RamlEndPointEmitter]): Seq[RamlEndPointEmitter] = {
        emitters.sorted((x: RamlEndPointEmitter, y: RamlEndPointEmitter) =>
          x.endpoint.path.value().count(_ == '/') compareTo y.endpoint.path.value().count(_ == '/'))
      }

      val endpoints = f.array.values
        .asInstanceOf[Seq[EndPoint]]

      val notOas = !vendor.contains(Oas)

      if (notOas) {
        val graph                                               = endpoints.map(e => (e, e.parent.toSet)).toMap
        val all: mutable.ListMap[EndPoint, RamlEndPointEmitter] = mutable.ListMap[EndPoint, RamlEndPointEmitter]()
        tsort(graph, Seq()).foreach(e => {
          val emitter = spec.factory.endpointEmitter(e, ordering, ListBuffer(), references)
          e.parent match {
            case Some(parent) =>
              all(parent) += emitter
              all += (e -> emitter)
            case _ => all += (e -> emitter)
          }
        })
        defaultOrder(
          all
            .filterKeys(_.parent.isEmpty)
            .values
            .toSeq)

      } else {
        endpoints.map(spec.factory.endpointEmitter(_, ordering, ListBuffer(), references))
      }

    }

    private case class BaseUriEmitter(fs: Fields, annotation: Option[BaseUriAnnotation]) extends EntryEmitter {
      override def emit(b: EntryBuilder): Unit = {
        val protocol: String = fs
          .entry(WebApiModel.Schemes)
          .find(_.value.annotations.contains(classOf[SynthesizedField]))
          .flatMap(_.array.scalars.headOption)
          .map(_.toString)
          .getOrElse("")

        val domain: String = fs
          .entry(WebApiModel.Host)
          .map(_.scalar.value)
          .map(_.toString)
          .getOrElse("")

        val basePath: String = fs
          .entry(WebApiModel.BasePath)
          .map(_.scalar.value)
          .map(_.toString)
          .getOrElse("")

        val uri = BaseUriSplitter(protocol, domain, basePath)

        if (uri.nonEmpty) {
          val Empty = Annotations()
          RamlScalarEmitter("baseUri",
                            FieldEntry(WebApiModel.BasePath, // Ignore :S
                                       Value(AmfScalar(uri.url()), annotation.map(_.extensions).getOrElse(Empty))))
            .emit(b)
        }
      }

      override def position(): Position =
        fs.entry(WebApiModel.BasePath)
          .flatMap(f => f.value.annotations.find(classOf[LexicalInformation]))
          .orElse(fs.entry(WebApiModel.Host).flatMap(f => f.value.annotations.find(classOf[LexicalInformation])))
          .orElse(
            fs.entry(WebApiModel.Schemes)
              .find(_.value.annotations.contains(classOf[SynthesizedField]))
              .flatMap(f => f.value.annotations.find(classOf[LexicalInformation])))
          .map(_.range.start)
          .getOrElse(ZERO)

    }

  }

  case class LicenseEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        f.value,
        b.entry(
          key,
          _.obj { b =>
            val fs     = f.obj.fields
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(LicenseModel.Url).map(f => result += ValueEmitter("url", f))
            fs.entry(LicenseModel.Name).map(f => result += ValueEmitter("name", f))

            result ++= AnnotationsEmitter(f.domainElement, ordering).emitters

            traverse(ordering.sorted(result), b)
          }
        )
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class OrganizationEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        f.value,
        b.entry(
          key,
          _.obj { b =>
            val fs     = f.obj.fields
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(OrganizationModel.Url).map(f => result += ValueEmitter("url", f))
            fs.entry(OrganizationModel.Name).map(f => result += ValueEmitter("name", f))
            fs.entry(OrganizationModel.Email).map(f => result += ValueEmitter("email", f))

            result ++= AnnotationsEmitter(f.domainElement, ordering).emitters

            traverse(ordering.sorted(result), b)
          }
        )
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

}

case class UserDocumentationsEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "documentation",
      _.list { b =>
        f.array.values
          .collect({ case c: CreativeWork => c })
          .foreach(
            c =>
              if (c.isLink)
                raw(b, c.linkLabel.getOrElse(c.linkTarget.get.id))
              else
                RamlCreativeWorkEmitter(c, ordering, withExtension = true).emit(b))
      }
    )
  }

  override def position(): Position = pos(f.array.values.head.annotations)
}

case class OasExtCreativeWorkEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit val spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    sourceOr(
      f.value.annotations,
      b.entry(
        "(externalDocs)",
        OasCreativeWorkEmitter(f.value.value.asInstanceOf[CreativeWork], ordering).emit(_)
      )
    )
  }

  override def position(): Position = pos(f.value.annotations)
}

case class CommentEmitter(element: AmfElement, message: String) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    b += YNode.Empty
    b.comment(message)
    element.annotations.find(classOf[SourceAST]).map(_.ast).foreach(a => b.comment(YamlRender.render(a)))
  }

  override def position(): Position = Position.ZERO
}
