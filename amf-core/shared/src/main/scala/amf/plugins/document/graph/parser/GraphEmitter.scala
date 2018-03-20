package amf.plugins.document.graph.parser

import amf.client.render.RenderOptions
import amf.core.annotations.{DomainExtensionAnnotation, ScalarType}
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
import amf.core.vocabulary.Namespace.SourceMaps
import amf.core.vocabulary.{Namespace, ValueType}
import org.mulesoft.common.time.SimpleDateTime
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YDocument, YNode, YScalar, YType}

import scala.collection.mutable.ListBuffer

/**
  * AMF Graph emitter
  */
object GraphEmitter extends MetaModelTypeMapping {

  def emit(unit: BaseUnit, options: RenderOptions): YDocument = Emitter(options).root(unit)

  case class Emitter(options: RenderOptions) {

    def root(unit: BaseUnit): YDocument = {
      YDocument {
        _.list {
          _.obj {
            traverse(unit, _)
          }
        }
      }
    }

    def traverse(element: AmfObject, b: EntryBuilder): Unit = {
      val id = element.id
      createIdNode(b, id)

      val sources = SourceMap(id, element)

      val obj = metaModel(element)

      if (obj.dynamic) traverseDynamicMetaModel(id, element, sources, obj, b)
      else traverseStaticMetamodel(id, element, sources, obj, b)

      createCustomExtensions(element, b)

      val sourceMapId = if (id.endsWith("/")) {
        id + "source-map"
      } else {
        id + "/source-map"
      }
      createSourcesNode(sourceMapId, sources, b)
    }

    def traverseDynamicMetaModel(id: String, element: AmfObject, sources: SourceMap, obj: Obj, b: EntryBuilder): Unit = {
      val schema: DynamicDomainElement = element.asInstanceOf[DynamicDomainElement]

      createDynamicTypeNode(schema, b)

      schema.dynamicFields.foreach { f: Field =>
        schema.valueForField(f).foreach { amfElement =>
          b.entry(
            f.value.iri(),
            value(f.`type`, Value(amfElement, amfElement.annotations), id, _ => {}, _)
          )
        }
      }
    }

    def traverseStaticMetamodel(id: String, element: AmfObject, sources: SourceMap, obj: Obj, b: EntryBuilder): Unit = {
      createTypeNode(b, obj, Some(element))

      // workaround for lazy values in shape
      val modelFields = obj match {
        case shapeModel: ShapeModel =>
          shapeModel.fields ++ Seq(
            ShapeModel.CustomShapePropertyDefinitions,
            ShapeModel.CustomShapeProperties
          )
        case _ => obj.fields
      }
      modelFields.map(element.fields.entryJsonld) foreach {
        case Some(FieldEntry(f, v)) =>
          val url = f.value.iri()
          b.entry(
            url,
            value(f.`type`, v, id, sources.property(url), _)
          )
        case None => // Missing field
      }
    }

    private def createCustomExtensions(element: AmfObject, b: EntryBuilder): Unit = {
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
                  createCustomExtension(b, uri, extension)
              }
            case _ => // ignore
          }
      }

      // Collect element scalar fields custom annotations
      var count = 1
      element.fields.foreach {
        case (f, v) =>
          v.annotations
            .collect({ case e: DomainExtensionAnnotation => e })
            .foreach(e => {
              val extension = e.extension
              val uri       = s"${element.id}/scalar-valued/$count/${extension.name.value()}"
              customProperties += uri
              adoptTree(uri, extension.extension) // Fix ids
              createCustomExtension(b, uri, extension, Some(f))
              count += 1
            })
      }

      if (customProperties.nonEmpty)
        b.entry(
          DomainElementModel.CustomDomainProperties.value.iri(),
          _.list { b =>
            customProperties.foreach(iri(b, _, inArray = true))
          }
        )
    }

    private def createCustomExtension(b: EntryBuilder,
                                      uri: String,
                                      extension: DomainExtension,
                                      field: Option[Field] = None): Unit = {
      val encoded = URLEncoder.encode(uri)
      b.entry(
        encoded,
        _.obj { b =>
          b.entry(DomainExtensionModel.Name.value.iri(), scalar(_, extension.name.value()))
          field.foreach(f => b.entry(DomainExtensionModel.Element.value.iri(), scalar(_, f.value.iri())))
          //val sources = SourceMap(extension.id, extension)
          //createSourcesNode(extension.id + "/source-map", sources, b)
          traverse(extension.extension, b)
        }
      )
    }

    def createSortedArray(b: PartBuilder,
                          seq: Seq[AmfElement],
                          parent: String,
                          element: Type,
                          sources: (Value) => Unit,
                          v: Option[Value] = None): Unit = {
      b.list {
        _.obj { b =>
          if (seq.nonEmpty) {
            val id = s"$parent/list"
            createIdNode(b, id)

            b.entry(
              (Namespace.Rdf + "first").iri(),
              _.list { b =>
                element match {
                  case _: Obj =>
                    seq.asInstanceOf[Seq[AmfObject]].headOption.foreach {
                      case elementInArray: DomainElement with Linkable if elementInArray.isLink =>
                        link(b, elementInArray, inArray = true)
                      case elementInArray =>
                        obj(b, elementInArray, inArray = true)
                    }
                  case Str =>
                    seq.asInstanceOf[Seq[AmfScalar]].headOption.foreach(e => scalar(b, e.toString, inArray = true))

                  case Iri =>
                    seq.asInstanceOf[Seq[AmfScalar]].headOption.foreach(e => iri(b, e.toString, inArray = true))

                  case Any =>
                    seq.asInstanceOf[Seq[AmfScalar]].headOption.foreach {
                      scalarElement =>
                        scalarElement.value match {
                          case bool: Boolean =>
                            typedScalar(b, bool.toString, (Namespace.Xsd + "boolean").iri(), inArray = true)
                          case str: String =>
                            typedScalar(b, str.toString, (Namespace.Xsd + "string").iri(), inArray = true)
                          case i: Int   => typedScalar(b, i.toString, (Namespace.Xsd + "integer").iri(), inArray = true)
                          case f: Float => typedScalar(b, f.toString, (Namespace.Xsd + "float").iri(), inArray = true)
                          case d: Double =>
                            typedScalar(b, d.toString, (Namespace.Xsd + "double").iri(), inArray = true)
                          case other => scalar(b, other.toString, inArray = true)
                        }
                    }
                }
              }
            )

            b.entry((Namespace.Rdf + "rest").iri(), createSortedArray(_, seq.tail, id, element, sources))

            v.foreach(sources)

          } else {
            createIdNode(b, (Namespace.Rdf + "nil").iri())
          }
        }
      }
    }

    private def value(t: Type, v: Value, parent: String, sources: (Value) => Unit, b: PartBuilder): Unit = {
      t match {
        case t: DomainElement with Linkable if t.isLink =>
          link(b, t)
          sources(v)
        case _: Obj =>
          obj(b, v.value.asInstanceOf[AmfObject])
          sources(v)
        case Iri =>
          iri(b, v.value.asInstanceOf[AmfScalar].toString)
          sources(v)
        case Str =>
          v.annotations.find(classOf[ScalarType]) match {
            case Some(annotation) =>
              typedScalar(b, v.value.asInstanceOf[AmfScalar].toString, annotation.datatype)
            case None =>
              scalar(b, v.value.asInstanceOf[AmfScalar].toString)
          }
          sources(v)
        case Bool =>
          scalar(b, v.value.asInstanceOf[AmfScalar].toString, YType.Bool)
          sources(v)
        case Type.Int =>
          scalar(b, v.value.asInstanceOf[AmfScalar].toString, YType.Int)
          sources(v)
        case Type.Double =>
          // this will transform the value to double and will not emit @type TODO: ADD YType.Double
          scalar(b, v.value.asInstanceOf[AmfScalar].toString, YType.Float)
          sources(v)
        case Type.Float =>
          scalar(b, v.value.asInstanceOf[AmfScalar].toString, YType.Float)
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
                        (Namespace.Xsd + "date").iri())
            sources(v)
          }
        case a: SortedArray =>
          createSortedArray(b, v.value.asInstanceOf[AmfArray].values, parent, a.element, sources, Some(v))
        case a: Array =>
          b.list { b =>
            val seq = v.value.asInstanceOf[AmfArray]
            sources(v)
            a.element match {
              case _: Obj =>
                seq.values.asInstanceOf[Seq[AmfObject]].foreach {
                  case elementInArray: DomainElement with Linkable if elementInArray.isLink =>
                    link(b, elementInArray, inArray = true)
                  case elementInArray =>
                    obj(b, elementInArray, inArray = true)
                }
              case Str =>
                seq.values.asInstanceOf[Seq[AmfScalar]].foreach { e =>
                  e.annotations.find(classOf[ScalarType]) match {
                    case Some(annotation) =>
                      typedScalar(b, e.value.asInstanceOf[AmfScalar].toString, annotation.datatype, inArray = true)
                    case None => scalar(b, e.toString, inArray = true)
                  }
                }
              case Iri => seq.values.asInstanceOf[Seq[AmfScalar]].foreach(e => iri(b, e.toString, inArray = true))
              case Type.Int =>
                seq.values
                  .asInstanceOf[Seq[AmfScalar]]
                  .foreach(e => scalar(b, e.value.toString, YType.Int, inArray = true))
              case Type.Float =>
                seq.values
                  .asInstanceOf[Seq[AmfScalar]]
                  .foreach(e => scalar(b, e.value.toString, YType.Float, inArray = true))
              case Bool =>
                seq.values
                  .asInstanceOf[Seq[AmfScalar]]
                  .foreach(e => scalar(b, e.value.toString, YType.Bool, inArray = true))
              case Any =>
                seq.values.asInstanceOf[Seq[AmfScalar]].foreach { scalarElement =>
                  scalarElement.value match {
                    case bool: Boolean =>
                      typedScalar(b, bool.toString, (Namespace.Xsd + "boolean").iri(), inArray = true)
                    case str: String => typedScalar(b, str.toString, (Namespace.Xsd + "string").iri(), inArray = true)
                    case i: Int      => typedScalar(b, i.toString, (Namespace.Xsd + "integer").iri(), inArray = true)
                    case f: Float    => typedScalar(b, f.toString, (Namespace.Xsd + "float").iri(), inArray = true)
                    case d: Double   => typedScalar(b, d.toString, (Namespace.Xsd + "double").iri(), inArray = true)
                    case other       => scalar(b, other.toString, inArray = true)
                  }
                }
              case _ => seq.values.asInstanceOf[Seq[AmfScalar]].foreach(e => iri(b, e.toString, inArray = true))
            }
          }
      }
    }

    private def obj(b: PartBuilder, element: AmfObject, inArray: Boolean = false): Unit = {
      def emit(b: PartBuilder): Unit = b.obj(traverse(element, _))

      if (inArray) emit(b) else b.list(emit)
    }

    private def link(b: PartBuilder, elementWithLink: DomainElement with Linkable, inArray: Boolean = false): Unit = {
      def emit(b: PartBuilder): Unit = {
        b.obj { o =>
          traverse(elementWithLink, o)
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

    private def fixTagIfNeeded(tag: YType, content: String): YType = {
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

    private def scalar(b: PartBuilder, content: String, tag: YType = YType.Str, inArray: Boolean = false): Unit = {
      def emit(b: PartBuilder): Unit = {

        val tg: YType = fixTagIfNeeded(tag, content)

        b.obj(_.entry("@value", raw(_, content, tg)))
      }

      if (inArray) emit(b) else b.list(emit)
    }

    private def typedScalar(b: PartBuilder, content: String, dataType: String, inArray: Boolean = false): Unit = {
      def emit(b: PartBuilder): Unit = b.obj { m =>
        m.entry("@value", raw(_, content, YType.Str))
        m.entry("@type", raw(_, dataType, YType.Str))
      }

      if (inArray) emit(b) else b.list(emit)
    }

    private def createIdNode(b: EntryBuilder, id: String): Unit = b.entry(
      "@id",
      raw(_, id)
    )

    private def createTypeNode(b: EntryBuilder, obj: Obj, maybeElement: Option[AmfObject] = None): Unit = {
      b.entry(
        "@type",
        _.list { b =>
          val allTypes = obj.`type`.map(_.iri()) ++ (maybeElement match {
            case Some(element) => element.dynamicTypes()
            case _             => List()
          })
          allTypes.distinct.foreach(t => raw(b, t))
        }
      )
    }

    private def createDynamicTypeNode(obj: DynamicDomainElement, b: EntryBuilder): Unit = {
      b.entry(
        "@type",
        _.list { b =>
          obj.dynamicType.foreach(t => raw(b, t.iri()))
        }
      )
    }

    private def raw(b: PartBuilder, content: String, tag: YType = YType.Str): Unit =
      b.+=(YNode(YScalar(content), tag))

    private def createSourcesNode(id: String, sources: SourceMap, b: EntryBuilder): Unit = {
      if (options.isWithSourceMaps && sources.nonEmpty) {
        b.entry(
          DomainElementModel.Sources.value.iri(),
          _.list {
            _.obj { b =>
              createIdNode(b, id)
              createTypeNode(b, SourceMapModel)
              createAnnotationNodes(b, sources)
            }
          }
        )
      }
    }

    private def createAnnotationNodes(b: EntryBuilder, sources: SourceMap): Unit = {
      sources.annotations.foreach({
        case (a, values) =>
          b.entry(
            ValueType(SourceMaps, a).iri(),
            _.list(b => values.foreach(createAnnotationValueNode(b, _)))
          )
      })
    }

    private def createAnnotationValueNode(b: PartBuilder, tuple: (String, String)): Unit = tuple match {
      case (iri, v) =>
        b.obj { b =>
          b.entry(SourceMapModel.Element.value.iri(), scalar(_, iri))
          b.entry(SourceMapModel.Value.value.iri(), scalar(_, v))
        }
    }
  }
}
