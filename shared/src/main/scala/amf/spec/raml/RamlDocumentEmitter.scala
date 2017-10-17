package amf.spec.raml

import amf.common.TSort.tsort
import amf.compiler.RamlHeader
import amf.document.Fragment.{ExtensionFragment, Fragment, OverlayFragment}
import amf.document.{BaseUnit, Document, Module}
import amf.domain.Annotation._
import amf.domain._
import amf.domain.extensions.{
  ArrayNode => DataArrayNode,
  ObjectNode => DataObjectNode,
  ScalarNode => DataScalarNode,
  _
}
import amf.metadata.Field
import amf.metadata.domain._
import amf.metadata.domain.extensions.CustomDomainPropertyModel
import amf.metadata.shape._
import amf.model.{AmfArray, AmfScalar}
import amf.parser.Position
import amf.parser.Position.ZERO
import amf.remote.{Oas, Raml, Vendor}
import amf.shape._
import amf.spec._
import amf.spec.common.BaseSpecEmitter
import amf.vocabulary.VocabularyMappings
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YDocument, YNode, YScalar, YType}

import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class RamlDocumentEmitter(document: BaseUnit) extends RamlSpecEmitter {

  private def retrieveWebApi(): WebApi = document match {
    case document: Document           => document.encodes.asInstanceOf[WebApi]
    case extension: ExtensionFragment => extension.encodes
    case overlay: OverlayFragment     => overlay.encodes
    case _                            => throw new Exception("BaseUnit doesn't encode a WebApi.")
  }

  def emitDocument(): YDocument = {
    val doc                    = document.asInstanceOf[Document]
    val ordering: SpecOrdering = SpecOrdering.ordering(Raml, doc.encodes.annotations)

    val api        = apiEmitters(ordering, doc.references)
    val declares   = DeclarationsEmitter(doc.declares, doc.references, ordering).emitters
    val references = ReferencesEmitter(doc.references, ordering)

    YDocument(b => {
      b.comment(RamlHeader.Raml10.text)
      b.map { b =>
        traverse(ordering.sorted(api ++ declares :+ references), b)
      }
    })
  }

  def apiEmitters(ordering: SpecOrdering, references: Seq[BaseUnit]): Seq[EntryEmitter] = {
    val model  = retrieveWebApi()
    val vendor = model.annotations.find(classOf[SourceVendor]).map(_.vendor)
    WebApiEmitter(model, ordering, vendor, references).emitters
  }

  case class WebApiEmitter(api: WebApi,
                           ordering: SpecOrdering,
                           vendor: Option[Vendor],
                           references: Seq[BaseUnit] = Seq()) {

    val emitters: Seq[EntryEmitter] = {
      val fs     = api.fields
      val result = mutable.ListBuffer[EntryEmitter]()

      fs.entry(WebApiModel.Name).map(f => result += ValueEmitter("title", f))

      fs.entry(WebApiModel.BaseUriParameters)
        .map(f => result += ParametersEmitter("baseUriParameters", f, ordering, references))

      fs.entry(WebApiModel.Description).map(f => result += ValueEmitter("description", f))

      fs.entry(WebApiModel.ContentType).map(f => result += ArrayEmitter("mediaType", f, ordering))

      fs.entry(WebApiModel.Version).map(f => result += ValueEmitter("version", f))

      fs.entry(WebApiModel.TermsOfService).map(f => result += ValueEmitter("(termsOfService)", f))

      fs.entry(WebApiModel.Schemes)
        .filter(!_.value.annotations.contains(classOf[SynthesizedField]))
        .map(f => result += ArrayEmitter("protocols", f, ordering))

      fs.entry(WebApiModel.Provider).map(f => result += OrganizationEmitter("(contact)", f, ordering))

      fs.entry(WebApiModel.Documentations).map(f => result += UserDocumentationsEmitter(f, ordering))

      fs.entry(WebApiModel.License).map(f => result += LicenseEmitter("(license)", f, ordering))

      fs.entry(WebApiModel.EndPoints).map(f => result ++= endpoints(f, ordering, vendor))

      result += BaseUriEmitter(fs)

      result ++= RamlAnnotationsEmitter(api, ordering).emitters

      ordering.sorted(result)
    }

    private def endpoints(f: FieldEntry, ordering: SpecOrdering, vendor: Option[Vendor]): Seq[EntryEmitter] = {

      def defaultOrder(emitters: Seq[EndPointEmitter]): Seq[EndPointEmitter] = {
        emitters.sorted((x: EndPointEmitter, y: EndPointEmitter) =>
          x.endpoint.path.count(_ == '/') compareTo y.endpoint.path.count(_ == '/'))
      }

      val endpoints = f.array.values
        .asInstanceOf[Seq[EndPoint]]

      val notOas = !vendor.contains(Oas)

      if (notOas) {
        val graph                                           = endpoints.map(e => (e, e.parent.toSet)).toMap
        val all: mutable.ListMap[EndPoint, EndPointEmitter] = mutable.ListMap[EndPoint, EndPointEmitter]()
        tsort(graph, Seq()).foreach(e => {
          val emitter = EndPointEmitter(e, ordering, ListBuffer(), references)
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
        endpoints.map(EndPointEmitter(_, ordering, ListBuffer(), references))
      }

    }

    private case class BaseUriEmitter(fs: Fields) extends EntryEmitter {
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

        if (uri.nonEmpty) b.entry("baseUri", uri.url())
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

  case class EndPointEmitter(endpoint: EndPoint,
                             ordering: SpecOrdering,
                             children: mutable.ListBuffer[EndPointEmitter] = mutable.ListBuffer(),
                             references: Seq[BaseUnit] = Seq())
      extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      val fs = endpoint.fields

      sourceOr(
        endpoint.annotations,
        b.complexEntry(
          b => {
            endpoint.parent.fold(ScalarEmitter(fs.entry(EndPointModel.Path).get.scalar).emit(b))(_ =>
              ScalarEmitter(AmfScalar(endpoint.relativePath)).emit(b))
          },
          _.map { b =>
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(EndPointModel.Name).map(f => result += ValueEmitter("displayName", f))

            fs.entry(EndPointModel.Description).map(f => result += ValueEmitter("description", f))

            fs.entry(EndPointModel.UriParameters)
              .map(f => result += ParametersEmitter("uriParameters", f, ordering, references))

            fs.entry(DomainElementModel.Extends).map(f => result ++= ExtendsEmitter("", f, ordering).emitters())

            fs.entry(EndPointModel.Operations).map(f => result ++= operations(f, ordering))

            result ++= RamlAnnotationsEmitter(endpoint, ordering).emitters

            result ++= children

            map { () =>
              traverse(ordering.sorted(result), b)
            }
          }
        )
      )
    }

    def +=(child: EndPointEmitter): Unit = children += child

    private def operations(f: FieldEntry, ordering: SpecOrdering): Seq[EntryEmitter] = {
      f.array.values
        .map(e => OperationEmitter(e.asInstanceOf[Operation], ordering, references))
    }

    override def position(): Position = pos(endpoint.annotations)
  }

  case class OperationEmitter(operation: Operation, ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val fs = operation.fields
      sourceOr(
        operation.annotations,
        b.complexEntry(
          ScalarEmitter(fs.entry(OperationModel.Method).get.scalar).emit(_),
          _.map { b =>
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(OperationModel.Name).map(f => result += ValueEmitter("displayName", f))

            fs.entry(OperationModel.Description).map(f => result += ValueEmitter("description", f))

            fs.entry(OperationModel.Deprecated).map(f => result += ValueEmitter("(deprecated)", f))

            fs.entry(OperationModel.Summary).map(f => result += ValueEmitter("(summary)", f))

            fs.entry(OperationModel.Documentation)
              .map(
                f =>
                  result += OasEntryCreativeWorkEmitter("(externalDocs)",
                                                        f.value.value.asInstanceOf[CreativeWork],
                                                        ordering))

            fs.entry(OperationModel.Schemes).map(f => result += ArrayEmitter("protocols", f, ordering))

            fs.entry(OperationModel.Accepts).map(f => result += ArrayEmitter("(consumes)", f, ordering))

            fs.entry(OperationModel.ContentType).map(f => result += ArrayEmitter("(produces)", f, ordering))

            fs.entry(DomainElementModel.Extends).map(f => result ++= ExtendsEmitter("", f, ordering).emitters())

            result ++= RamlAnnotationsEmitter(operation, ordering).emitters

            Option(operation.request).foreach { req =>
              val fields = req.fields

              fields
                .entry(RequestModel.QueryParameters)
                .map(f => result += ParametersEmitter("queryParameters", f, ordering, references))

              fields
                .entry(RequestModel.Headers)
                .map(f => result += ParametersEmitter("headers", f, ordering, references))
              fields.entry(RequestModel.Payloads).map(f => result += PayloadsEmitter("body", f, ordering))
            }

            fs.entry(OperationModel.Responses)
              .map(f => result += ResponsesEmitter("responses", f, ordering, references))

            traverse(ordering.sorted(result), b)
          }
        )
      )
    }

    override def position(): Position = pos(operation.annotations)
  }

  case class ResponsesEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        f.value.annotations,
        b.entry(key, _.map { traverse(responses(f, ordering, references), _) })
      )
    }

    private def responses(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit]): Seq[EntryEmitter] = {
      val result = f.array.values.map(e => ResponseEmitter(e.asInstanceOf[Response], ordering, references))
      ordering.sorted(result)
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class ResponseEmitter(response: Response, ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val fs = response.fields
      sourceOr(
        response.annotations,
        b.complexEntry(
          ScalarEmitter(fs.entry(ResponseModel.StatusCode).get.scalar).emit(_),
          _.map { b =>
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(ResponseModel.Description).map(f => result += ValueEmitter("description", f))
            fs.entry(RequestModel.Headers).map(f => result += ParametersEmitter("headers", f, ordering, references))
            fs.entry(RequestModel.Payloads).map(f => result += PayloadsEmitter("body", f, ordering))

            result ++= RamlAnnotationsEmitter(response, ordering).emitters

            traverse(ordering.sorted(result), b)
          }
        )
      )
    }

    override def position(): Position = pos(response.annotations)
  }

  case class PayloadsEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        f.value.annotations,
        b.entry(key, _.map { b =>
          traverse(payloads(f, ordering), b)
        })
      )
    }

    private def payloads(f: FieldEntry, ordering: SpecOrdering): Seq[EntryEmitter] = {
      ordering.sorted(f.array.values.flatMap(e => Payloads(e.asInstanceOf[Payload], ordering).emitters()))
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class Payloads(payload: Payload, ordering: SpecOrdering) {
    def emitters(): Seq[EntryEmitter] = {
      val fs = payload.fields
      fs.entry(PayloadModel.MediaType)
        .fold(
          RamlTypeEmitter(payload.schema, ordering).emitters()
        )(_ => Seq(PayloadEmitter(payload, ordering)))
    }
  }

  case class PayloadEmitter(payload: Payload, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val fs = payload.fields
      fs.entry(PayloadModel.MediaType)
        .foreach(mediaType => {
          b.complexEntry(
            ScalarEmitter(mediaType.scalar).emit(_),
            _.map { b =>
              var result = RamlTypeEmitter(payload.schema, ordering)
                .emitters() ++= RamlAnnotationsEmitter(payload, ordering).emitters
              traverse(ordering.sorted(result), b)
            }
          )
        })
    }

    override def position(): Position = pos(payload.annotations)
  }

  case class ParametersEmitter(key: String, f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        f.value.annotations,
        b.entry(
          key,
          _.map(traverse(parameters(f, ordering, references), _))
        )
      )
    }

    private def parameters(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit]): Seq[EntryEmitter] = {
      val result = mutable.ListBuffer[EntryEmitter]()
      f.array.values
        .foreach(e => result += ParameterEmitter(e.asInstanceOf[Parameter], ordering, references))

      ordering.sorted(result)
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class LicenseEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        f.value,
        b.entry(
          key,
          _.map { b =>
            val fs     = f.obj.fields
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(LicenseModel.Url).map(f => result += ValueEmitter("url", f))
            fs.entry(LicenseModel.Name).map(f => result += ValueEmitter("name", f))

            result ++= RamlAnnotationsEmitter(f.domainElement, ordering).emitters

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
          _.map { b =>
            val fs     = f.obj.fields
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(OrganizationModel.Url).map(f => result += ValueEmitter("url", f))
            fs.entry(OrganizationModel.Name).map(f => result += ValueEmitter("name", f))
            fs.entry(OrganizationModel.Email).map(f => result += ValueEmitter("email", f))

            result ++= RamlAnnotationsEmitter(f.domainElement, ordering).emitters

            traverse(ordering.sorted(result), b)
          }
        )
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

}

class RamlSpecEmitter() extends BaseSpecEmitter {

  case class ReferencesEmitter(references: Seq[BaseUnit], ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val modules = references.collect({ case m: Module => m })
      if (modules.nonEmpty) {
        b.entry("uses", _.map { b =>
          idCounter.reset()
          traverse(ordering.sorted(modules.map(r => ReferenceEmitter(r, ordering, () => idCounter.genId("uses")))), b)
        })
      }
    }

    override def position(): Position = ZERO
  }

  case class ReferenceEmitter(reference: BaseUnit, ordering: SpecOrdering, aliasGenerator: () => String)
      extends EntryEmitter {

    // todo review with PEdro. We dont serialize location, so when parse amf to dump spec, we lose de location (we only have the id)
    override def emit(b: EntryBuilder): Unit = {
      val alias = reference.annotations.find(classOf[Aliases])

      def entry(alias: String) = MapEntryEmitter(alias, name).emit(b)

      alias.fold {
        entry(aliasGenerator())
      } { _ =>
        alias.foreach(_.aliases.foreach(entry))
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

  case class DeclarationsEmitter(declares: Seq[DomainElement], references: Seq[BaseUnit], ordering: SpecOrdering) {
    val emitters: Seq[EntryEmitter] = {
      val declarations = Declarations(declares)

      val result = ListBuffer[EntryEmitter]()

      if (declarations.shapes.nonEmpty)
        result += DeclaredTypesEmitters(declarations.shapes.values.toSeq, references, ordering)

      if (declarations.annotations.nonEmpty)
        result += AnnotationsTypesEmitter(declarations.annotations.values.toSeq, references, ordering)

      if (declarations.resourceTypes.nonEmpty)
        result += AbstractDeclarationsEmitter(
          "resourceTypes",
          declarations.resourceTypes.values.toSeq,
          ordering,
          (e: DomainElement, key: String) => TagToReferenceEmitter(e, key, references))

      if (declarations.traits.nonEmpty)
        result += AbstractDeclarationsEmitter(
          "traits",
          declarations.traits.values.toSeq,
          ordering,
          (e: DomainElement, key: String) => TagToReferenceEmitter(e, key, references))

      if (declarations.parameters.nonEmpty)
        result += DeclaredParametersEmitter(declarations.parameters.values.toSeq, ordering, references)

      result
    }
  }

  case class DeclaredTypesEmitters(types: Seq[Shape], references: Seq[BaseUnit], ordering: SpecOrdering)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry("types", _.map { b =>
        traverse(ordering.sorted(types.map(s => NamedTypeEmitter(s, references, ordering))), b)
      })
    }

    override def position(): Position = types.headOption.map(a => pos(a.annotations)).getOrElse(ZERO)
  }

  case class DeclaredParametersEmitter(parameters: Seq[Parameter], ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        "(parameters)",
        _.map(traverse(ordering.sorted(parameters.map(NamedParameterEmitter(_, ordering, references))), _))
      )
    }

    override def position(): Position = parameters.headOption.map(a => pos(a.annotations)).getOrElse(ZERO)
  }

  case class NamedTypeEmitter(shape: Shape, references: Seq[BaseUnit], ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val name = Option(shape.name).getOrElse(throw new Exception(s"Cannot declare shape without name $shape"))
      b.entry(name, if (shape.isLink) emitLink _ else emitInline _)
    }

    private def emitLink(b: PartBuilder): Unit = {
      shape.linkTarget.foreach { l =>
        TagToReferenceEmitter(l, shape.linkLabel.getOrElse(l.id), references).emit(b)
      }
    }

    private def emitInline(b: PartBuilder): Unit = {
      b.map { b =>
        traverse(ordering.sorted(RamlTypeEmitter(shape, ordering).emitters()), b)
      }
    }

    override def position(): Position = pos(shape.annotations)
  }

  case class NamedParameterEmitter(parameter: Parameter, ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      parameter.fields.get(ParameterModel.Binding).annotations += ExplicitField()
      ParameterEmitter(parameter, ordering, references).emit(b)
    }

    override def position(): Position = pos(parameter.annotations)
  }

  case class TagToReferenceEmitter(reference: DomainElement, text: String, references: Seq[BaseUnit])
      extends PartEmitter {
    override def emit(b: PartBuilder): Unit = {
      references
        .find {
          case m: Module   => m.declares.contains(reference)
          case f: Fragment => f.encodes == reference
        }
        .foreach({
          case _: Module => raw(b, text)
          case _         => ref(b, text)
        })
    }

    override def position(): Position = pos(reference.annotations)
  }

  case class AnnotationsTypesEmitter(properties: Seq[CustomDomainProperty],
                                     references: Seq[BaseUnit],
                                     ordering: SpecOrdering)
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry("annotationTypes", _.map { b =>
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
      val name = Option(annotation.name).orElse(throw new Exception(s"Annotation type without name $annotation")).get
      b.entry(name, if (annotation.isLink) emitLink _ else emitInline _)
    }

    private def emitLink(b: PartBuilder): Unit = {
      annotation.linkTarget.foreach { l =>
        TagToReferenceEmitter(l, annotation.linkLabel.getOrElse(l.id), references).emit(b)
      }
    }

    private def emitInline(b: PartBuilder): Unit = {
      b.map { b =>
        traverse(ordering.sorted(AnnotationTypeEmitter(annotation, ordering).emitters()), b)
      }
    }

    override def position(): Position = pos(annotation.annotations)
  }

  case class UserDocumentationsEmitter(f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
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

  case class OasExtCreativeWorkEmitter(f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
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

  case class RamlTypeEmitter(shape: Shape, ordering: SpecOrdering, ignored: Seq[Field] = Nil) {
    def emitters(): Seq[EntryEmitter] = {
      shape match {
        //        case _ if Option(shape).isDefined && shape.fromTypeExpression => Seq(TypeExpressionEmitter(shape))
        case _ if Option(shape).isDefined && shape.fromTypeExpression => {
          println("Grrrrr")
          ???
        }
        //        case l: Linkable if l.isLink                                  => Seq(LocalReferenceEmitter(shape))
        case l: Linkable if l.isLink => {
          println("Grrrrr")
          ???
        }
        case schema: SchemaShape => Seq(SchemaShapeEmitter(schema))
        case node: NodeShape =>
          val copiedNode = node.copy(fields = node.fields.filter(f => !ignored.contains(f._1)))
          NodeShapeEmitter(copiedNode, ordering).emitters()
        case union: UnionShape =>
          val copiedNode = union.copy(fields = union.fields.filter(f => !ignored.contains(f._1)))
          UnionShapeEmitter(copiedNode, ordering).emitters()
        case file: FileShape =>
          val copiedFile = file.copy(fields = file.fields.filter(f => !ignored.contains(f._1)))
          FileShapeEmitter(copiedFile, ordering).emitters()
        case scalar: ScalarShape =>
          val copiedScalar = scalar.copy(fields = scalar.fields.filter(f => !ignored.contains(f._1)))
          ScalarShapeEmitter(copiedScalar, ordering).emitters()
        case array: ArrayShape =>
          val copiedArray = array.copy(fields = array.fields.filter(f => !ignored.contains(f._1)))
          ArrayShapeEmitter(copiedArray, ordering).emitters()
        case tuple: TupleShape =>
          val copiedTuple = tuple.copy(fields = tuple.fields.filter(f => !ignored.contains(f._1)))
          TupleShapeEmitter(copiedTuple, ordering).emitters()
        case matrix: MatrixShape =>
          val copiedMatrix = matrix.copy(fields = matrix.fields.filter(f => !ignored.contains(f._1)))
          ArrayShapeEmitter(copiedMatrix.toArrayShape, ordering).emitters()
        case any: AnyShape =>
          val copiedNode = any.copy(fields = any.fields.filter(f => !ignored.contains(f._1)))
          AnyShapeEmitter(copiedNode, ordering).emitters()
        case nil: NilShape =>
          val copiedNode = nil.copy(fields = nil.fields.filter(f => !ignored.contains(f._1)))
          NilShapeEmitter(copiedNode, ordering).emitters()
        case _ => Seq()
      }
    }
  }

  abstract class ShapeEmitter(shape: Shape, ordering: SpecOrdering) {
    def emitters(): Seq[EntryEmitter] = {

      val result = ListBuffer[EntryEmitter]()
      val fs     = shape.fields

      Option(fs.getValue(ShapeModel.RequiredShape)) match {
        case Some(v) =>
          if (v.annotations.contains(classOf[ExplicitField])) {
            fs.entry(ShapeModel.RequiredShape).map(f => result += ValueEmitter("required", f))
          }
        case None => // ignore
      }

      fs.entry(ShapeModel.DisplayName).map(f => result += ValueEmitter("displayName", f))

      fs.entry(ShapeModel.Description).map(f => result += ValueEmitter("description", f))

      fs.entry(ShapeModel.Default).map(f => result += ValueEmitter("default", f))

      fs.entry(ShapeModel.Values).map(f => result += ArrayEmitter("enum", f, ordering))

      fs.entry(ShapeModel.Documentation)
        .map(f =>
          result += OasEntryCreativeWorkEmitter("(externalDocs)", f.value.value.asInstanceOf[CreativeWork], ordering))

      fs.entry(ShapeModel.XMLSerialization).map(f => result += XMLSerializerEmitter("xml", f, ordering))

      result ++= RamlAnnotationsEmitter(shape, ordering).emitters

      result
    }
  }

  case class SchemaShapeEmitter(schema: SchemaShape) extends EntryEmitter {
//    override def emit(b: EntryBuilder): Unit = raw(schema.raw)
    override def emit(b: EntryBuilder): Unit = {
      ??? //todo syaml raw...
    }

    override def position(): Position = pos(schema.annotations)
  }

  case class XMLSerializerEmitter(key: String, f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        f.value,
        b.entry(
          key,
          b => {
            val fs     = f.obj.fields
            val result = mutable.ListBuffer[EntryEmitter]()

            fs.entry(XMLSerializerModel.Attribute)
              .filter(_.value.annotations.contains(classOf[ExplicitField]))
              .map(f => result += ValueEmitter("attribute", f))

            fs.entry(XMLSerializerModel.Wrapped)
              .filter(_.value.annotations.contains(classOf[ExplicitField]))
              .map(f => result += ValueEmitter("wrapped", f))

            fs.entry(XMLSerializerModel.Name)
              .filter(_.value.annotations.contains(classOf[ExplicitField]))
              .map(f => result += ValueEmitter("name", f))

            fs.entry(XMLSerializerModel.Namespace).map(f => result += ValueEmitter("namespace", f))

            fs.entry(XMLSerializerModel.Prefix).map(f => result += ValueEmitter("prefix", f))

            result ++= RamlAnnotationsEmitter(f.domainElement, ordering).emitters

            b.map { b =>
              traverse(ordering.sorted(result), b)
            }
          }
        )
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class NodeShapeEmitter(node: NodeShape, ordering: SpecOrdering) extends ShapeEmitter(node, ordering) {
    override def emitters(): Seq[EntryEmitter] = {
      val result: ListBuffer[EntryEmitter] = ListBuffer()
      node.annotations.find(classOf[ParsedJSONSchema]) match {
        case Some(jsonParsedAnnotation) =>
          result += new EntryEmitter() {
            override def emit(b: EntryBuilder): Unit = {
//              raw(jsonParsedAnnotation.value)
              ???
            }

            override def position(): Position = pos(node.annotations)
          }
        case None =>
          result ++= super.emitters()

          val fs = node.fields

          // TODO annotation for original position?
          if (node.annotations.contains(classOf[ExplicitField]))
            result += MapEntryEmitter("type", "object")

          fs.entry(NodeShapeModel.Inherits).map(f => result += ShapeInheritsEmitter(f, ordering))

          fs.entry(NodeShapeModel.MinProperties).map(f => result += ValueEmitter("minProperties", f))

          fs.entry(NodeShapeModel.MaxProperties).map(f => result += ValueEmitter("maxProperties", f))

          fs.entry(NodeShapeModel.Closed)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(
              f =>
                result += MapEntryEmitter("additionalProperties",
                                          (!node.closed).toString,
                                          position = pos(f.value.annotations)))

          fs.entry(NodeShapeModel.Discriminator).map(f => result += ValueEmitter("discriminator", f))

          fs.entry(NodeShapeModel.DiscriminatorValue).map(f => result += ValueEmitter("discriminatorValue", f))

          fs.entry(NodeShapeModel.ReadOnly).map(f => result += ValueEmitter("(readOnly)", f))

          fs.entry(NodeShapeModel.Properties).map(f => result += PropertiesShapeEmitter(f, ordering))

          val propertiesMap = ListMap(node.properties.map(p => p.id -> p): _*)

          fs.entry(NodeShapeModel.Dependencies)
            .map(f => result += ShapeDependenciesEmitter(f, ordering, propertiesMap))

          result
      }
    }

  }

  case class ShapeInheritsEmitter(f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {

      val (declaredShapes, inlineShapes) =
        f.array.values.map(_.asInstanceOf[Shape]).partition(_.annotations.contains(classOf[DeclaredElement]))

      b.entry(
        "type",
        b => {
          if (inlineShapes.nonEmpty) {
            b.map(traverse(ordering.sorted(inlineShapes.flatMap(RamlTypeEmitter(_, ordering).emitters())), _))
          } else {
            b.list { b =>
              declaredShapes.foreach(s => raw(b, s.name))
            }
          }
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class AnyShapeEmitter(shape: AnyShape, ordering: SpecOrdering) extends ShapeEmitter(shape, ordering) {
    override def emitters(): Seq[EntryEmitter] = super.emitters() :+ MapEntryEmitter("type", "any")
  }

  case class NilShapeEmitter(shape: NilShape, ordering: SpecOrdering) extends ShapeEmitter(shape, ordering) {
    override def emitters(): Seq[EntryEmitter] = super.emitters() :+ MapEntryEmitter("type", "nil")
  }

  trait CommonOASFieldsEmitter {
    def emitOASFields(fs: Fields, result: ListBuffer[EntryEmitter]): Unit = {
      fs.entry(ScalarShapeModel.MinLength).map(f => result += ValueEmitter("minLength", f))

      fs.entry(ScalarShapeModel.MaxLength).map(f => result += ValueEmitter("maxLength", f))

      fs.entry(ScalarShapeModel.ExclusiveMinimum).map(f => result += ValueEmitter("(exclusiveMinimum)", f))

      fs.entry(ScalarShapeModel.ExclusiveMaximum).map(f => result += ValueEmitter("(exclusiveMaximum)", f))

    }
  }
  case class ScalarShapeEmitter(scalar: ScalarShape, ordering: SpecOrdering)
      extends ShapeEmitter(scalar, ordering)
      with CommonOASFieldsEmitter {
    override def emitters(): Seq[EntryEmitter] = {
      val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

      val fs = scalar.fields

      val (typeDef, format) = RamlTypeDefStringValueMatcher.matchType(TypeDefXsdMapping.typeDef(scalar.dataType)) // TODO Check this

      fs.entry(ScalarShapeModel.DataType)
        .map(
          f =>
            result += MapEntryEmitter(
              "type",
              typeDef,
              position =
                if (f.value.annotations.contains(classOf[Inferred])) ZERO
                else
                  pos(f.value.annotations))) // TODO check this  - annotations of typeDef in parser

      emitOASFields(fs, result)

      fs.entry(ScalarShapeModel.Pattern).map(f => result += ValueEmitter("pattern", f))

      fs.entry(ScalarShapeModel.Minimum).map(f => result += ValueEmitter("minimum", f))

      fs.entry(ScalarShapeModel.Maximum).map(f => result += ValueEmitter("maximum", f))

      fs.entry(ScalarShapeModel.MultipleOf).map(f => result += ValueEmitter("multipleOf", f))

      if (format.nonEmpty) result += MapEntryEmitter("(format)", format)
      else fs.entry(ScalarShapeModel.Format).map(f => result += ValueEmitter("format", f)) // todo mutually exclusive?

      result
    }
  }

  case class FileShapeEmitter(scalar: FileShape, ordering: SpecOrdering)
      extends ShapeEmitter(scalar, ordering)
      with CommonOASFieldsEmitter {
    override def emitters(): Seq[EntryEmitter] = {
      val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

      val fs = scalar.fields

      result += MapEntryEmitter("type", "file")

      emitOASFields(fs, result)

      fs.entry(FileShapeModel.FileTypes).map(f => result += ArrayEmitter("fileTypes", f, ordering))

      fs.entry(ScalarShapeModel.Pattern).map(f => result += ValueEmitter("(pattern)", f))

      fs.entry(ScalarShapeModel.Minimum).map(f => result += ValueEmitter("(minimum)", f))

      fs.entry(ScalarShapeModel.Maximum).map(f => result += ValueEmitter("(maximum)", f))

      fs.entry(ScalarShapeModel.MultipleOf).map(f => result += ValueEmitter("(multipleOf)", f))

      result
    }
  }

  case class ShapeDependenciesEmitter(f: FieldEntry, ordering: SpecOrdering, props: ListMap[String, PropertyShape])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        "(dependencies)",
        _.map { b =>
          val result =
            f.array.values.map(v => PropertyDependenciesEmitter(v.asInstanceOf[PropertyDependencies], ordering, props))
          traverse(ordering.sorted(result), b)
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class PropertyDependenciesEmitter(property: PropertyDependencies,
                                         ordering: SpecOrdering,
                                         properties: ListMap[String, PropertyShape])
      extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      properties
        .get(property.propertySource)
        .foreach(p => {
          b.entry(
            p.name,
            b => {
              val targets = property.fields
                .entry(PropertyDependenciesModel.PropertyTarget)
                .map(f => {
                  f.array.scalars.flatMap(iri =>
                    properties.get(iri.value.toString).map(p => AmfScalar(p.name, iri.annotations)))
                })

              targets.foreach(target => {
                b.list { b =>
                  traverse(ordering.sorted(target.map(t => ScalarEmitter(t))), b)
                }
              })
            }
          )
        })
    }

    override def position(): Position = pos(property.annotations) // TODO check this
  }

  case class UnionShapeEmitter(shape: UnionShape, ordering: SpecOrdering) extends ShapeEmitter(shape, ordering) {
    override def emitters(): Seq[EntryEmitter] = {
      super.emitters() :+ MapEntryEmitter("type", "union") :+ AnyOfShapeEmitter(shape, ordering)
    }
  }

  case class AnyOfShapeEmitter(shape: UnionShape, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        "anyOf",
        _.list { b =>
          val emitters = shape.anyOf
            .map { shape =>
              ordering.sorted(RamlTypeEmitter(shape, ordering).emitters())
            }
            .map { emitters =>
              new EntryEmitter {
                override def position(): Position        = emitters.head.position()
                override def emit(b: EntryBuilder): Unit = emitters.foreach(_.emit(b))
              }
            }
          ordering.sorted(emitters).foreach { emitter =>
            b.map { emitter.emit }
          }
        }
      )
    }

    override def position(): Position = pos(shape.fields.getValue(UnionShapeModel.AnyOf).annotations)
  }

  case class ArrayShapeEmitter(array: ArrayShape, ordering: SpecOrdering) extends ShapeEmitter(array, ordering) {
    override def emitters(): Seq[EntryEmitter] = {
      val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

      val fs = array.fields

      if (array.annotations.contains(classOf[ExplicitField]))
        result += MapEntryEmitter("type", "array")

      result += ItemsShapeEmitter(array, ordering)

      fs.entry(ArrayShapeModel.MaxItems).map(f => result += ValueEmitter("maxItems", f))

      fs.entry(ArrayShapeModel.MinItems).map(f => result += ValueEmitter("minItems", f))

      fs.entry(ArrayShapeModel.UniqueItems).map(f => result += ValueEmitter("uniqueItems", f))

      result
    }
  }

  case class TupleShapeEmitter(tuple: TupleShape, ordering: SpecOrdering) extends ShapeEmitter(tuple, ordering) {
    override def emitters(): Seq[EntryEmitter] = {
      val result: ListBuffer[EntryEmitter] = ListBuffer(super.emitters(): _*)

      val fs = tuple.fields

      if (tuple.annotations.contains(classOf[ExplicitField]))
        result += MapEntryEmitter("type", "array")

      result += TupleItemsShapeEmitter(tuple, ordering)
      result += MapEntryEmitter("(tuple)", "true")

      fs.entry(ArrayShapeModel.MaxItems).map(f => result += ValueEmitter("maxItems", f))

      fs.entry(ArrayShapeModel.MinItems).map(f => result += ValueEmitter("minItems", f))

      fs.entry(ArrayShapeModel.UniqueItems).map(f => result += ValueEmitter("uniqueItems", f))

      result
    }
  }

  case class ItemsShapeEmitter(array: ArrayShape, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        "items",
        //todo garrote review ordering
        _.map(b => RamlTypeEmitter(array.items, ordering).emitters().foreach(_.emit(b)))
      )
    }

    override def position(): Position = {
      pos(array.fields.getValue(ArrayShapeModel.Items).annotations)
    }
  }

  case class TupleItemsShapeEmitter(tuple: TupleShape, ordering: SpecOrdering) extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      val result = mutable.ListBuffer[EntryEmitter]()

      tuple.items
        .foreach(item => {
          RamlTypeEmitter(item, ordering).emitters().foreach(result += _)
        })

      //todo garrote review type
      /*b.entry(
        "items",
        _.list { b =>
          traverse(ordering.sorted(result), b)
        }
      )*/
    }

    override def position(): Position = pos(tuple.fields.getValue(ArrayShapeModel.Items).annotations)
  }

  case class PropertiesShapeEmitter(f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        "properties",
        _.map { b =>
          val result = f.array.values.map(v => PropertyShapeEmitter(v.asInstanceOf[PropertyShape], ordering))
          traverse(ordering.sorted(result), b)
        }
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class PropertyShapeEmitter(property: PropertyShape, ordering: SpecOrdering) extends EntryEmitter {

    override def emit(b: EntryBuilder): Unit = {
      b.entry(
        property.name,
        _.map(b => traverse(ordering.sorted(RamlTypeEmitter(property.range, ordering).emitters()), b))
      )
    }

    override def position(): Position = pos(property.annotations) // TODO check this
  }

  case class SchemaEmitter(f: FieldEntry, ordering: SpecOrdering) extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      val shape = f.value.value.asInstanceOf[Shape]
      b.entry(
        "type",
        _.map(b => traverse(ordering.sorted(RamlTypeEmitter(shape, ordering).emitters()), b))
      )
    }

    override def position(): Position = pos(f.value.annotations)
  }

  case class AnnotationTypeEmitter(property: CustomDomainProperty, ordering: SpecOrdering) {
    def emitters(): Seq[EntryEmitter] = {
      val result = ListBuffer[EntryEmitter]()
      val fs     = property.fields

      fs.entry(CustomDomainPropertyModel.DisplayName).map(f => result += ValueEmitter("displayName", f))

      fs.entry(CustomDomainPropertyModel.Description).map(f => result += ValueEmitter("description", f))

      fs.entry(CustomDomainPropertyModel.Domain).map { f =>
        val scalars = f.array.scalars.map { s =>
          VocabularyMappings.uriToRaml.get(s.toString) match {
            case Some(identifier) => AmfScalar(identifier, s.annotations)
            case None             => s
          }
        }
        val finalArray      = AmfArray(scalars, f.array.annotations)
        val finalFieldEntry = FieldEntry(f.field, Value(finalArray, f.value.annotations))

        result += ArrayEmitter("allowedTargets", finalFieldEntry, ordering)
      }

      fs.entry(CustomDomainPropertyModel.Schema).map(f => result += SchemaEmitter(f, ordering))

      result ++= RamlAnnotationsEmitter(property, ordering).emitters

      result
    }
  }

  case class LocalReferenceEmitter(reference: Linkable) extends PartEmitter {
    override def emit(b: PartBuilder): Unit = reference.linkLabel match {
      case Some(label) => raw(b, label)
      case None        => throw new Exception("Missing link label")
    }

    override def position(): Position = pos(reference.annotations)
  }

  case class TypeExpressionEmitter(shape: Shape) extends PartEmitter {
    override def emit(b: PartBuilder): Unit = raw(b, shape.typeExpression)

    override def position(): Position = pos(shape.annotations)
  }

  protected def ref(b: PartBuilder, url: String): Unit = b.scalar(YNode(YScalar("!include " + url), YType("!include")))

  case class ParameterEmitter(parameter: Parameter, ordering: SpecOrdering, references: Seq[BaseUnit])
      extends EntryEmitter {
    override def emit(b: EntryBuilder): Unit = {
      sourceOr(
        parameter.annotations,
        if (parameter.isLink) emitLink(b) else emitParameter(b)
      )
    }

    private def emitLink(b: EntryBuilder) = {
      val fs = parameter.linkTarget.get.fields

      b.complexEntry(
        emitParameterKey(fs, _),
        b => {
          parameter.linkTarget.foreach(l =>
            TagToReferenceEmitter(l, parameter.linkLabel.getOrElse(l.id), references).emit(b))
        }
      )
    }

    private def emitParameter(b: EntryBuilder) = {
      val fs = parameter.fields

      b.complexEntry(
        emitParameterKey(fs, _),
        _.map { b =>
          val result = mutable.ListBuffer[EntryEmitter]()

          fs.entry(ParameterModel.Description).map(f => result += ValueEmitter("description", f))

          fs.entry(ParameterModel.Required)
            .filter(_.value.annotations.contains(classOf[ExplicitField]))
            .map(f => result += ValueEmitter("required", f))

          result ++= RamlTypeEmitter(parameter.schema, ordering, Seq(ShapeModel.Description)).emitters()

          result ++= RamlAnnotationsEmitter(parameter, ordering).emitters

          Option(parameter.fields.getValue(ParameterModel.Binding)) match {
            case Some(v) =>
              v.annotations.find(classOf[ExplicitField]) match {
                case Some(_) =>
                  fs.entry(ParameterModel.Binding).map { f =>
                    result += ValueEmitter("(binding)", f)
                  }
                case None => // ignore
              }
            case _ => // ignore
          }

          traverse(ordering.sorted(result), b)
        }
      )

    }

    private def emitParameterKey(fs: Fields, b: PartBuilder) = {
      val explicit = fs
        .entry(ParameterModel.Required)
        .exists(_.value.annotations.contains(classOf[ExplicitField]))

      if (!explicit && !parameter.required) {
        ScalarEmitter(AmfScalar(parameter.name + "?")).emit(b)
      } else {
        ScalarEmitter(fs.entry(ParameterModel.Name).get.scalar).emit(b)
      }
    }

    override def position(): Position = pos(parameter.annotations)
  }
}
