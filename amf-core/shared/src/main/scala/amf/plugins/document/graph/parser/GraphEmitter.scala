package amf.plugins.document.graph.parser

import amf.core.annotations.{DomainExtensionAnnotation, ScalarType}
import amf.core.emitter.RenderOptions
import amf.core.metamodel.Type.{Any, Array, Bool, Iri, SortedArray, Str}
import amf.core.metamodel.document.SourceMapModel
import amf.core.metamodel.domain.extensions.DomainExtensionModel
import amf.core.metamodel.domain.{DomainElementModel, ShapeModel}
import amf.core.metamodel.{Field, MetaModelTypeMapping, Obj, Type}
import amf.core.model.document.{BaseUnit, SourceMap}
import amf.core.model.domain.DataNodeOps.adoptTree
import amf.core.model.domain._
import amf.core.model.domain.extensions.DomainExtension
import amf.core.parser.{FieldEntry, Value}
import amf.core.vocabulary.{Namespace, ValueType}
import org.mulesoft.common.time.SimpleDateTime
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * AMF Graph emitter
  */
trait ScalarEmitter {

  def scalar(b: PartBuilder, content: String, tag: YType = YType.Str, inArray: Boolean = false): Unit = {
    def emit(b: PartBuilder): Unit = {

      val tg: YType = fixTagIfNeeded(tag, content)

      b.obj(_.entry("@value", raw(_, content, tg)))
    }

    if (inArray) emit(b) else b.list(emit)
  }

  protected def fixTagIfNeeded(tag: YType, content: String): YType = {
    val tg: YType = tag match {
      case YType.Bool =>
        if (content != "true" && content != "false") {
          YType.Str
        } else {
          tag
        }
      case YType.Int =>
        try {
          content.toInt
          tag
        } catch {
          case _: NumberFormatException => YType.Str
        }
      case YType.Float =>
        try {
          content.toDouble
          tag
        } catch {
          case _: NumberFormatException => YType.Str
        }
      case _ => tag

    }
    tg
  }

  protected def raw(b: PartBuilder, content: String, tag: YType = YType.Str): Unit =
    b.+=(YNode(YScalar(content), tag))
}

object DefaultScalarEmitter extends ScalarEmitter

class EmissionContext(val prefixes: mutable.Map[String, String], var base: String, val options: RenderOptions) {
  var counter: Int = 1

  def shouldCompact                                    = options.isCompactUris
  protected def compactAndCollect(uri: String): String = Namespace.compactAndCollect(uri, prefixes)
  def emitIri(uri: String): String                     = if (shouldCompact) compactAndCollect(uri) else uri
  def emitId(uri: String): String                      = if (shouldCompact && uri.contains(base)) uri.replace(base, "") else uri
  def setupContextBase(location: String) = {
    if (Option(location).isDefined) {
      base = if (location.replace("://", "").contains("/")) {
        var basePre = if (location.contains("#")) {
          location.split("#").head
        } else {
          location
        }
        val parts = basePre.split("/").dropRight(1)
        parts.mkString("/")
      } else {
        location.split("#").head
      }
    } else {
      base = ""
    }
  }

  def emitContext(b: EntryBuilder) = {
    b.entry("@context", _.obj { b =>
      b.entry("@base", base)
      prefixes.foreach {
        case (p, v) =>
          b.entry(p, v)
      }
    })
  }
}

object EmissionContext {
  def apply(unit: BaseUnit, options: RenderOptions) =
    new EmissionContext(mutable.Map(), Option(unit.location).getOrElse(unit.id), options)
}

object GraphEmitter extends MetaModelTypeMapping {

  def emit(unit: BaseUnit, options: RenderOptions): YDocument = Emitter(options).root(unit)

  case class Emitter(options: RenderOptions) {

    def root(unit: BaseUnit): YDocument = {
      val ctx = EmissionContext(unit, options)
      if (ctx.shouldCompact) {
        YDocument {
          _.list { l =>
            l.obj { o =>
              traverse(unit, o, ctx)
              ctx.emitContext(o)
            }
          }
        }
      } else {
        YDocument {
          _.list {
            _.obj {
              traverse(unit, _, ctx)
            }
          }
        }
      }
    }

    def traverse(element: AmfObject, b: EntryBuilder, ctx: EmissionContext): Unit = {
      val id = element.id

      createIdNode(b, id, ctx)

      val sources = SourceMap(id, element)

      val obj = metaModel(element)

      if (obj.dynamic) traverseDynamicMetaModel(id, element, sources, obj, b, ctx)
      else traverseStaticMetamodel(id, element, sources, obj, b, ctx)

      createCustomExtensions(element, b, ctx)

      val sourceMapId = if (id.endsWith("/")) {
        id + "source-map"
      } else {
        id + "/source-map"
      }
      createSourcesNode(sourceMapId, sources, b, ctx)
    }

    def traverseDynamicMetaModel(id: String,
                                 element: AmfObject,
                                 sources: SourceMap,
                                 obj: Obj,
                                 b: EntryBuilder,
                                 ctx: EmissionContext): Unit = {
      val schema: DynamicDomainElement = element.asInstanceOf[DynamicDomainElement]

      createDynamicTypeNode(schema, b, ctx)
      // workaround for lazy values in shape
      val modelFields = schema.dynamicFields ++ (obj match {
        case _: ShapeModel =>
          Seq(
            ShapeModel.CustomShapePropertyDefinitions,
            ShapeModel.CustomShapeProperties
          )
        case _ => Nil
      })
      modelFields.foreach { f: Field =>
        schema.valueForField(f).foreach { amfValue =>
          val url = ctx.emitIri(f.value.iri())
          schema match {
            case schema: DynamicDomainElement if !schema.isInstanceOf[ExternalSourceElement] =>
              b.entry(
                url,
                value(f.`type`, Value(amfValue.value, amfValue.value.annotations), id, _ => {}, _, ctx)
              )
            case _ =>
              b.entry(
                url,
                value(f.`type`, amfValue, id, sources.property(url), _, ctx)
              )
          }
        }
      }
    }

    def traverseStaticMetamodel(id: String,
                                element: AmfObject,
                                sources: SourceMap,
                                obj: Obj,
                                b: EntryBuilder,
                                ctx: EmissionContext): Unit = {
      createTypeNode(b, obj, Some(element), ctx)

      // workaround for lazy values in shape

      obj.fields.map(element.fields.entryJsonld) foreach {
        case Some(FieldEntry(f, v)) =>
          val url = ctx.emitIri(f.value.iri())
          b.entry(
            url,
            value(f.`type`, v, id, sources.property(url), _, ctx)
          )
        case None => // Missing field
      }
    }

    private def createCustomExtensions(element: AmfObject, b: EntryBuilder, ctx: EmissionContext): Unit = {
      val customProperties: ListBuffer[String] = ListBuffer()

      // Collect element custom annotations
      element.fields.entry(DomainElementModel.CustomDomainProperties) foreach {
        case FieldEntry(_, v) =>
          v.value match {
            case AmfArray(values, _) =>
              values.foreach {
                case extension: DomainExtension =>
                  val uri = extension.definedBy.id
                  customProperties += uri
                  createCustomExtension(b, uri, extension, None, ctx)
              }
            case _ => // ignore
          }
      }

      // Collect element scalar fields custom annotations
      var count = 1
      element.fields.foreach {
        case (f, v) =>
          v.value.annotations
            .collect({ case e: DomainExtensionAnnotation => e })
            .foreach(e => {
              val extension = e.extension
              val uri       = s"${element.id}/scalar-valued/$count/${extension.name.value()}"
              customProperties += uri
              adoptTree(uri, extension.extension) // Fix ids
              createCustomExtension(b, uri, extension, Some(f), ctx)
              count += 1
            })
      }

      if (customProperties.nonEmpty)
        b.entry(
          ctx.emitIri(DomainElementModel.CustomDomainProperties.value.iri()),
          _.list { b =>
            customProperties.foreach(iri(b, _, inArray = true))
          }
        )
    }

    private def createCustomExtension(b: EntryBuilder,
                                      uri: String,
                                      extension: DomainExtension,
                                      field: Option[Field] = None,
                                      ctx: EmissionContext): Unit = {
      val encoded = URLEncoder.encode(uri)
      b.entry(
        encoded,
        _.obj { b =>
          b.entry(
            ctx.emitIri(DomainExtensionModel.Name.value.iri()),
            DefaultScalarEmitter.scalar(_, extension.name.value())
          )
          field.foreach(
            f =>
              b.entry(
                ctx.emitIri(DomainExtensionModel.Element.value.iri()),
                DefaultScalarEmitter.scalar(_, f.value.iri())
            ))
          traverse(extension.extension, b, ctx)
        }
      )
    }

    def createSortedArray(b: PartBuilder,
                          seq: Seq[AmfElement],
                          parent: String,
                          element: Type,
                          sources: (Value) => Unit,
                          v: Option[Value] = None,
                          ctx: EmissionContext): Unit = {
      b.list {
        _.obj { b =>
          if (seq.nonEmpty) {
            val id = s"$parent/list"
            createIdNode(b, id, ctx)

            b.entry(
              ctx.emitIri((Namespace.Rdf + "first").iri()),
              _.list { b =>
                element match {
                  case _: Obj =>
                    seq.asInstanceOf[Seq[AmfObject]].headOption.foreach {
                      case elementInArray: DomainElement with Linkable if elementInArray.isLink =>
                        link(b, elementInArray, inArray = true, ctx)
                      case elementInArray =>
                        obj(b, elementInArray, inArray = true, ctx)
                    }
                  case Str =>
                    seq
                      .asInstanceOf[Seq[AmfScalar]]
                      .headOption
                      .foreach(e => DefaultScalarEmitter.scalar(b, e.toString, inArray = true))

                  case Iri =>
                    seq.asInstanceOf[Seq[AmfScalar]].headOption.foreach(e => iri(b, e.toString, inArray = true))

                  case Any =>
                    seq.asInstanceOf[Seq[AmfScalar]].headOption.foreach {
                      scalarElement =>
                        scalarElement.value match {
                          case bool: Boolean =>
                            typedScalar(b, bool.toString, (Namespace.Xsd + "boolean").iri(), inArray = true, ctx)
                          case str: String =>
                            typedScalar(b, str.toString, (Namespace.Xsd + "string").iri(), inArray = true, ctx)
                          case i: Int =>
                            typedScalar(b, i.toString, (Namespace.Xsd + "integer").iri(), inArray = true, ctx)
                          case f: Float =>
                            typedScalar(b, f.toString, (Namespace.Xsd + "float").iri(), inArray = true, ctx)
                          case d: Double =>
                            typedScalar(b, d.toString, (Namespace.Xsd + "double").iri(), inArray = true, ctx)
                          case other => DefaultScalarEmitter.scalar(b, other.toString, inArray = true)
                        }
                    }
                }
              }
            )

            b.entry(ctx.emitIri((Namespace.Rdf + "rest").iri()),
                    createSortedArray(_, seq.tail, id, element, sources, v, ctx))

            v.foreach(sources)

          } else {
            createIdNode(b, ctx.emitIri((Namespace.Rdf + "nil").iri()), ctx)
          }
        }
      }
    }

    private def value(t: Type,
                      v: Value,
                      parent: String,
                      sources: (Value) => Unit,
                      b: PartBuilder,
                      ctx: EmissionContext): Unit = {
      val scalarEmitter = options.getCustomEmitter.getOrElse(DefaultScalarEmitter)
      t match {
        case t: DomainElement with Linkable if t.isLink =>
          link(b, t, false, ctx)
          sources(v)
        case _: Obj =>
          obj(b, v.value.asInstanceOf[AmfObject], false, ctx)
          sources(v)
        case Iri =>
          iri(b, v.value.asInstanceOf[AmfScalar].toString)
          sources(v)
        case Str =>
          v.annotations.find(classOf[ScalarType]) match {
            case Some(annotation) =>
              typedScalar(b, v.value.asInstanceOf[AmfScalar].toString, annotation.datatype, false, ctx)
            case None =>
              scalarEmitter.scalar(b, v.value.asInstanceOf[AmfScalar].toString)
          }
          sources(v)
        case Bool =>
          scalarEmitter.scalar(b, v.value.asInstanceOf[AmfScalar].toString, YType.Bool)
          sources(v)
        case Type.Int =>
          scalarEmitter.scalar(b, v.value.asInstanceOf[AmfScalar].toString, YType.Int)
          sources(v)
        case Type.Double =>
          // this will transform the value to double and will not emit @type TODO: ADD YType.Double
          scalarEmitter.scalar(b, v.value.asInstanceOf[AmfScalar].toString, YType.Float)
          sources(v)
        case Type.Float =>
          scalarEmitter.scalar(b, v.value.asInstanceOf[AmfScalar].toString, YType.Float)
          sources(v)
        case Type.Date =>
          val dateTime = v.value.asInstanceOf[AmfScalar].value.asInstanceOf[SimpleDateTime]
          if (dateTime.timeOfDay.isDefined || dateTime.zoneOffset.isDefined) {
            // TODO: add support for RFC3339 here
            //typedScalar(b, dateTime.toRFC3339, (Namespace.Xsd + "dateTime").iri())
            throw new Exception("Serialisation of timestamps not supported yet")
          } else {
            typedScalar(b,
                        f"${dateTime.year}%04d-${dateTime.month}%02d-${dateTime.day}%02d",
                        (Namespace.Xsd + "date").iri(),
                        false,
                        ctx)
            sources(v)
          }
        case a: SortedArray =>
          createSortedArray(b, v.value.asInstanceOf[AmfArray].values, parent, a.element, sources, Some(v), ctx)
        case a: Array =>
          b.list { b =>
            val seq = v.value.asInstanceOf[AmfArray]
            sources(v)
            a.element match {
              case _: Obj =>
                seq.values.asInstanceOf[Seq[AmfObject]].foreach {
                  case elementInArray: DomainElement with Linkable if elementInArray.isLink =>
                    link(b, elementInArray, inArray = true, ctx)
                  case elementInArray =>
                    obj(b, elementInArray, inArray = true, ctx)
                }
              case Str =>
                seq.values.asInstanceOf[Seq[AmfScalar]].foreach { e =>
                  e.annotations.find(classOf[ScalarType]) match {
                    case Some(annotation) =>
                      typedScalar(b,
                                  e.value.asInstanceOf[AmfScalar].toString,
                                  annotation.datatype,
                                  inArray = true,
                                  ctx)
                    case None => scalarEmitter.scalar(b, e.toString, inArray = true)
                  }
                }
              case Iri => seq.values.asInstanceOf[Seq[AmfScalar]].foreach(e => iri(b, e.toString, inArray = true))
              case Type.Int =>
                seq.values
                  .asInstanceOf[Seq[AmfScalar]]
                  .foreach(e => scalarEmitter.scalar(b, e.value.toString, YType.Int, inArray = true))
              case Type.Float =>
                seq.values
                  .asInstanceOf[Seq[AmfScalar]]
                  .foreach(e => scalarEmitter.scalar(b, e.value.toString, YType.Float, inArray = true))
              case Bool =>
                seq.values
                  .asInstanceOf[Seq[AmfScalar]]
                  .foreach(e => scalarEmitter.scalar(b, e.value.toString, YType.Bool, inArray = true))
              case Any =>
                seq.values.asInstanceOf[Seq[AmfScalar]].foreach { scalarElement =>
                  scalarElement.value match {
                    case bool: Boolean =>
                      typedScalar(b, bool.toString, (Namespace.Xsd + "boolean").iri(), inArray = true, ctx)
                    case str: String =>
                      typedScalar(b, str.toString, (Namespace.Xsd + "string").iri(), inArray = true, ctx)
                    case i: Int    => typedScalar(b, i.toString, (Namespace.Xsd + "integer").iri(), inArray = true, ctx)
                    case f: Float  => typedScalar(b, f.toString, (Namespace.Xsd + "float").iri(), inArray = true, ctx)
                    case d: Double => typedScalar(b, d.toString, (Namespace.Xsd + "double").iri(), inArray = true, ctx)
                    case other     => scalarEmitter.scalar(b, other.toString, inArray = true)
                  }
                }
              case _ => seq.values.asInstanceOf[Seq[AmfScalar]].foreach(e => iri(b, e.toString, inArray = true))
            }
          }
      }
    }

    private def obj(b: PartBuilder, element: AmfObject, inArray: Boolean = false, ctx: EmissionContext): Unit = {
      def emit(b: PartBuilder): Unit = b.obj(traverse(element, _, ctx))

      if (inArray) emit(b) else b.list(emit)
    }

    private def link(b: PartBuilder,
                     elementWithLink: DomainElement with Linkable,
                     inArray: Boolean = false,
                     ctx: EmissionContext): Unit = {
      def emit(b: PartBuilder): Unit = {
        b.obj { o =>
          traverse(elementWithLink, o, ctx)
        }
      }

      if (inArray) emit(b) else b.list(emit)
    }

    object URLEncoder {
      def encode(input: String): String = {
        val resultStr = new StringBuilder
        input.foreach(ch => {

          if (isUnsafe(ch)) {
            resultStr.append('%')
            resultStr.append(toHex(ch / 16))
            resultStr.append(toHex(ch % 16))
          } else resultStr.append(ch)
        })
        resultStr.toString
      }

      private def toHex(ch: Int) =
        (if (ch < 10) '0' + ch
         else 'A' + ch - 10).toChar

      private def isUnsafe(ch: Char) = {
        if (ch > 128) true
        " %$&+,;=@<>".indexOf(ch) >= 0 //should we encode %?
      }
    }

    private def iri(b: PartBuilder, content: String, inArray: Boolean = false) = {
      //we can not use java.net.URLEncoder and can not use anything more correct because we does not have actual constraints for it yet.
      //TODO please review it and propose something better
      def emit(b: PartBuilder) = b.obj(_.entry("@id", raw(_, URLEncoder.encode(content))))

      if (inArray) emit(b) else b.list(emit)
    }

    private def typedScalar(b: PartBuilder,
                            content: String,
                            dataType: String,
                            inArray: Boolean = false,
                            ctx: EmissionContext): Unit = {
      def emit(b: PartBuilder): Unit = b.obj { m =>
        m.entry("@value", raw(_, content, YType.Str))
        m.entry("@type", raw(_, ctx.emitIri(dataType), YType.Str))
      }

      if (inArray) emit(b) else b.list(emit)
    }

    private def createIdNode(b: EntryBuilder, id: String, ctx: EmissionContext): Unit = b.entry(
      "@id",
      raw(_, ctx.emitId(id))
    )

    private def createTypeNode(b: EntryBuilder,
                               obj: Obj,
                               maybeElement: Option[AmfObject] = None,
                               ctx: EmissionContext): Unit = {
      b.entry(
        "@type",
        _.list { b =>
          val allTypes = obj.`type`.map(_.iri()) ++ (maybeElement match {
            case Some(element) => element.dynamicTypes()
            case _             => List()
          })
          allTypes.distinct.foreach(t => raw(b, ctx.emitIri(t)))
        }
      )
    }

    private def createDynamicTypeNode(obj: DynamicDomainElement, b: EntryBuilder, ctx: EmissionContext): Unit = {
      b.entry(
        "@type",
        _.list { b =>
          obj.dynamicType.foreach(t => raw(b, ctx.emitIri(t.iri())))
        }
      )
    }

    private def raw(b: PartBuilder, content: String, tag: YType = YType.Str): Unit =
      b.+=(YNode(YScalar(content), tag))

    private def createSourcesNode(id: String, sources: SourceMap, b: EntryBuilder, ctx: EmissionContext): Unit = {
      if (options.isWithSourceMaps && sources.nonEmpty) {
        if (options.isWithRawSoureMaps) {
          b.entry(
            "smaps",
            _.obj { b =>
              createAnnotationNodes(b, sources, ctx)
            }
          )
        } else {
          b.entry(
            ctx.emitIri(DomainElementModel.Sources.value.iri()),
            _.list {
              _.obj { b =>
                createIdNode(b, id, ctx)
                createTypeNode(b, SourceMapModel, None, ctx)
                createAnnotationNodes(b, sources, ctx)
              }
            }
          )
        }
      }
    }

    private def createAnnotationNodes(b: EntryBuilder, sources: SourceMap, ctx: EmissionContext): Unit = {
      sources.annotations.foreach({
        case (a, values) =>
          if (ctx.options.isWithRawSoureMaps) {
            b.entry(
              a,
              _.obj { o =>
                values.foreach {
                  case (iri, v) =>
                    o.entry(
                      ctx.emitId(ctx.emitIri(iri)),
                      raw(_, v)
                    )
                }
              }
            )
          } else {
            b.entry(
              ctx.emitIri(ValueType(Namespace.SourceMaps, a).iri()),
              _.list(b => values.foreach(createAnnotationValueNode(b, _, ctx)))
            )
          }
      })
    }

    private def createAnnotationValueNode(b: PartBuilder, tuple: (String, String), ctx: EmissionContext): Unit =
      tuple match {
        case (iri, v) =>
          b.obj { b =>
            b.entry(ctx.emitIri(SourceMapModel.Element.value.iri()), DefaultScalarEmitter.scalar(_, iri))
            b.entry(ctx.emitIri(SourceMapModel.Value.value.iri()), DefaultScalarEmitter.scalar(_, v))
          }
      }
  }
}
