package amf.plugins.document.webapi.parser.spec.raml

import amf.core.annotations._
import amf.core.emitter.BaseEmitters.{ValueEmitter, _}
import amf.core.emitter._
import amf.core.metamodel.document.{BaseUnitModel, ExtensionLikeModel}
import amf.core.model.document._
import amf.core.model.domain._
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.parser.Position.ZERO
import amf.core.parser.{EmptyFutureDeclarations, FieldEntry, Position}
import amf.core.remote._
import amf.core.utils.TSort.tsort
import amf.core.utils.{IdCounter, AmfStrings}
import amf.plugins.document.webapi.contexts.{RamlScalarEmitter, RamlSpecEmitterContext, SpecEmitterContext}
import amf.plugins.document.webapi.parser.spec._
import amf.plugins.document.webapi.parser.spec.declaration._
import amf.plugins.document.webapi.parser.spec.domain._
import amf.plugins.document.webapi.parser.spec.oas.{
  OasDeclaredParametersEmitter,
  OasDeclaredResponsesEmitter,
  TagsEmitter
}
import amf.plugins.domain.shapes.models.CreativeWork
import amf.plugins.domain.webapi.metamodel._
import amf.plugins.domain.webapi.models._
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
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
      result += spec.factory.declaredTypesEmitter(declarations.shapes.values.toSeq, document.references, ordering)

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
      result += OasDeclaredResponsesEmitter("responses".asRamlAnnotation,
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
    val references = ReferencesEmitter(document, ordering)
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
      val name = annotation.name.option() match {
        case Some(n) => n
        case _ =>
          spec.eh.violation(ResolutionValidation,
                            annotation.id,
                            None,
                            s"Annotation type without name $annotation",
                            annotation.position(),
                            annotation.location())
          "default-name"
      }
      b.entry(name, if (annotation.isLink) emitLink _ else emitInline _)
    }

    private def emitLink(b: PartBuilder): Unit = {
      annotation.linkTarget.foreach { l =>
        spec.factory.tagToReferenceEmitter(l, annotation.linkLabel.option(), references).emit(b)
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
      result += spec.factory.declaredTypesEmitter(declarations.shapes.values.toSeq, document.references, ordering)

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

    val oasParams = declarations.parameters.values.map(OasParameter(_)) ++ declarations.payloads.values
      .map(OasParameter(_))
    if (oasParams.nonEmpty)
      result += OasDeclaredParametersEmitter(oasParams.toSeq,
                                             ordering,
                                             document.references,
                                             "parameters".asRamlAnnotation)(toOas(spec))

    if (declarations.responses.nonEmpty)
      result += OasDeclaredResponsesEmitter("responses".asRamlAnnotation,
                                            declarations.responses.values.toSeq,
                                            ordering,
                                            document.references)(toOas(spec))

    result
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
}

case class ReferenceEmitter(reference: BaseUnit,
                            aliases: Option[Aliases],
                            ordering: SpecOrdering,
                            aliasGenerator: () => String)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val aliasesMap = aliases.getOrElse(Aliases(Set())).aliases
    val effectiveAlias = aliasesMap.find { case (_, (f, _)) => f == reference.id } map { case (a, (_, r)) => (a, r) } getOrElse {
      (aliasGenerator(), name)
    }
    MapEntryEmitter(effectiveAlias._1, effectiveAlias._2).emit(b)
  }

  private def name: String = reference.location().getOrElse(reference.id)

  override def position(): Position = ZERO
}

case class ReferencesEmitter(baseUnit: BaseUnit, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val aliases    = baseUnit.annotations.find(classOf[Aliases]).getOrElse(Aliases(Set()))
    val references = baseUnit.references
    val modules    = references.collect({ case m: Module => m })
    if (modules.nonEmpty) {
      var modulesEmitted = Map[String, Module]()
      val idCounter      = new IdCounter()
      val aliasesEmitters: Seq[Option[EntryEmitter]] = aliases.aliases.map {
        case (alias, (fullUrl, localUrl)) =>
          modules.find(_.id == fullUrl) match {
            case Some(module) =>
              modulesEmitted += (module.id -> module)
              Some(
                ReferenceEmitter(module,
                                 Some(Aliases(Set(alias -> (fullUrl, localUrl)))),
                                 ordering,
                                 () => idCounter.genId("uses")))
            case _ => None
          }
      }.toSeq
      val missingModuleEmitters = modules.filter(m => modulesEmitted.get(m.id).isEmpty).map { module =>
        Some(ReferenceEmitter(module, Some(Aliases(Set())), ordering, () => idCounter.genId("uses")))
      }
      val finalEmitters = (aliasesEmitters ++ missingModuleEmitters).collect { case Some(e) => e }
      b.entry("uses", _.obj { b =>
        traverse(ordering.sorted(finalEmitters), b)
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
      spec.factory.retrieveHeader(document).foreach(b.comment)
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

      fs.entry(WebApiModel.Servers).map(f => result ++= RamlServersEmitter(f, ordering, references).emitters())

      fs.entry(WebApiModel.Description).map(f => result += RamlScalarEmitter("description", f))

      fs.entry(WebApiModel.ContentType).map(f => result += ArrayEmitter("mediaType", f, ordering))

      fs.entry(WebApiModel.Version).map(f => result += RamlScalarEmitter("version", f))

      fs.entry(WebApiModel.TermsOfService).map(f => result += ValueEmitter("termsOfService".asRamlAnnotation, f))

      fs.entry(WebApiModel.Schemes).map(f => result += ArrayEmitter("protocols", f, ordering))

      fs.entry(WebApiModel.Provider).map(f => result += OrganizationEmitter("contact".asRamlAnnotation, f, ordering))

      fs.entry(WebApiModel.Tags)
        .map(f =>
          result += TagsEmitter("tags".asRamlAnnotation, f.array.values.asInstanceOf[Seq[Tag]], ordering)(toOas(spec)))

      fs.entry(WebApiModel.Documentations).map(f => result += UserDocumentationsEmitter(f, ordering))

      fs.entry(WebApiModel.License).map(f => result += LicenseEmitter("license".asRamlAnnotation, f, ordering))

      fs.entry(WebApiModel.EndPoints).map(f => result ++= endpoints(f, ordering, vendor))

      result ++= AnnotationsEmitter(api, ordering).emitters

      fs.entry(WebApiModel.Security).map(f => result += ParametrizedSecuritiesSchemeEmitter("securedBy", f, ordering))

      ordering.sorted(result)
    }

    private def endpoints(f: FieldEntry, ordering: SpecOrdering, vendor: Option[Vendor]): Seq[EntryEmitter] = {

      def defaultOrder(emitters: Seq[RamlEndPointEmitter]): Seq[RamlEndPointEmitter] = {
        emitters.sorted((x: RamlEndPointEmitter, y: RamlEndPointEmitter) => {
          x.endpoint.path.value().count(_ == '/') compareTo y.endpoint.path.value().count(_ == '/') match {
            case 0 =>
              x.endpoint.path.value() compareTo y.endpoint.path.value()
            case n => n
          }
        })
      }

      val endpoints = f.array.values
        .asInstanceOf[Seq[EndPoint]]

      val notOas = !vendor.contains(Oas)

      if (notOas) {
        val graph                                               = endpoints.map(e => (e, e.parent.toSet)).toMap
        val all: mutable.ListMap[EndPoint, RamlEndPointEmitter] = mutable.ListMap[EndPoint, RamlEndPointEmitter]()
        tsort(graph, Seq()).foreach(_.foreach(e => {
          val emitter = spec.factory.endpointEmitter(e, ordering, ListBuffer(), references)
          e.parent match {
            case Some(parent) =>
              all(parent) += emitter
              all += (e -> emitter)
            case _ => all += (e -> emitter)
          }
        }))
        defaultOrder(
          all
            .filterKeys(_.parent.isEmpty)
            .values
            .toSeq)

      } else {
        endpoints.map(spec.factory.endpointEmitter(_, ordering, ListBuffer(), references))
      }

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
                raw(b, c.linkLabel.option().getOrElse(c.linkTarget.get.id))
              else
                RamlCreativeWorkEmitter(c, ordering, withExtension = true).emit(b))
      }
    )
  }

  override def position(): Position = f.array.values.headOption.map(_.annotations).map(pos).getOrElse(Position.ZERO)
}

case class OasExtCreativeWorkEmitter(f: FieldEntry, ordering: SpecOrdering)(implicit val spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    sourceOr(
      f.value.annotations,
      b.entry(
        "externalDocs".asRamlAnnotation,
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
