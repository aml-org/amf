package amf.plugins.document.graph.parser

import amf.core.annotations.ScalarType
import amf.core.client.GenerationOptions
import amf.core.metamodel.Type.{Array, Bool, Iri, RegExp, SortedArray, Str}
import amf.core.metamodel.document.SourceMapModel
import amf.core.metamodel.domain.{DomainElementModel, LinkableElementModel, ShapeModel}
import amf.core.metamodel.{Field, MetaModelTypeMapping, Obj, Type}
import amf.core.model.document.{BaseUnit, SourceMap}
import amf.core.model.domain._
import amf.core.model.domain.extensions.DomainExtension
import amf.core.parser.{Annotations, FieldEntry, Value}
import amf.core.vocabulary.Namespace.SourceMaps
import amf.core.vocabulary.{Namespace, ValueType}
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import org.yaml.model.{YDocument, YNode, YScalar, YType}

import scala.collection.mutable.ListBuffer

/**
  * AMF Graph emitter
  */
object GraphEmitter extends MetaModelTypeMapping {

  def emit(unit: BaseUnit, options: GenerationOptions): YDocument = Emitter(options).root(unit)

  case class Emitter(options: GenerationOptions) {

    def root(unit: BaseUnit): YDocument = {
      YDocument {
        _.list {
          _.obj {
            traverse(unit, unit.location, _)
          }
        }
      }
    }

    def traverse(element: AmfObject, parent: String, b: EntryBuilder): Unit = {
      val id = element.id
      createIdNode(b, id)

      val sources = SourceMap(id, element)

      val obj = metaModel(element)

      if (obj.dynamic) traverseDynamicMetaModel(id, element, sources, obj, parent, b)
      else traverseStaticMetamodel(id, element, sources, obj, parent, b)

      createCustomExtensions(element, parent, b)

      createSourcesNode(id + "/source-map", sources, b)
    }

    def traverseDynamicMetaModel(id: String,
                                 element: AmfObject,
                                 sources: SourceMap,
                                 obj: Obj,
                                 parent: String,
                                 b: EntryBuilder): Unit = {
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

    def traverseStaticMetamodel(id: String,
                                element: AmfObject,
                                sources: SourceMap,
                                obj: Obj,
                                parent: String,
                                b: EntryBuilder): Unit = {
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

    private def createCustomExtensions(element: AmfObject, parent: String, b: EntryBuilder): Unit = {
      val customProperties: ListBuffer[String] = ListBuffer()

      element.fields.entry(DomainElementModel.CustomDomainProperties) foreach {
        case FieldEntry(_, v) =>
          v.value match {
            case AmfArray(values, _) =>
              values.foreach {
                case extension: DomainExtension =>
                  val propertyUri = extension.definedBy.id
                  customProperties += propertyUri
                  b.entry(
                    propertyUri,
                    _.obj { b =>
                      b.entry((Namespace.Document + "name").iri(), scalar(_, extension.name))
                      traverse(extension.extension, parent, b)
                    }
                  )
              }
            case _ => // ignore
          }
      }

      if (customProperties.nonEmpty)
        b.entry(
          (Namespace.Document + "customDomainProperties").iri(),
          _.list { b =>
            customProperties.foreach(iri(b, _, inArray = true))
          }
        )
    }

    private def value(t: Type, v: Value, parent: String, sources: (Value) => Unit, b: PartBuilder): Unit = {
      t match {
        case t: DomainElement with Linkable if t.isLink =>
          link(b, t, parent)
          sources(v)
        case _: Obj =>
          obj(b, v.value.asInstanceOf[AmfObject], parent)
          sources(v)
        case Iri =>
          iri(b, v.value.asInstanceOf[AmfScalar].toString)
          sources(v)
        case Str | RegExp =>
          v.annotations.find(classOf[ScalarType]) match {
            case Some(annotation) => {
              typedScalar(b, v.value.asInstanceOf[AmfScalar].toString, annotation.datatype)
            }
            case None => {
              scalar(b, v.value.asInstanceOf[AmfScalar].toString)
            }
          }
          sources(v)
        case Bool =>
          scalar(b, v.value.asInstanceOf[AmfScalar].toString, YType.Bool)
          sources(v)
        case Type.Int =>
          scalar(b, v.value.asInstanceOf[AmfScalar].toString, YType.Int)
          sources(v)
        case a: SortedArray =>
          b.obj {
            _.entry(
              "@list",
              _.list { b =>
                sources(v)
                val seq = v.value.asInstanceOf[AmfArray]
                a.element match {
                  case _: Obj => seq.values.asInstanceOf[Seq[AmfObject]].foreach {
                    case elementInArray: DomainElement with Linkable if elementInArray.isLink =>
                      link(b, elementInArray, parent, inArray = true)
                    case elementInArray =>
                      obj(b, elementInArray, parent, inArray = true)
                  }
                  case Str =>
                    seq.values.asInstanceOf[Seq[AmfScalar]].foreach(e => scalar(b, e.toString, inArray = true))
                }
              }
            )
          }
        case a: Array =>
          b.list { b =>
            val seq = v.value.asInstanceOf[AmfArray]
            sources(v)
            a.element match {
              case _: Obj => seq.values.asInstanceOf[Seq[AmfObject]].foreach {
                case elementInArray: DomainElement with Linkable if elementInArray.isLink =>
                  link(b, elementInArray, parent, inArray = true)
                case elementInArray =>
                  obj(b, elementInArray, parent, inArray = true)
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
                  .foreach(e => scalar(b, e.value.asInstanceOf[AmfScalar].toString, YType.Int, inArray = true))
              case Bool =>
                seq.values
                  .asInstanceOf[Seq[AmfScalar]]
                  .foreach(e => scalar(b, e.value.asInstanceOf[AmfScalar].toString, YType.Bool, inArray = true))
              case _ => seq.values.asInstanceOf[Seq[AmfScalar]].foreach(e => iri(b, e.toString, inArray = true))
            }
          }
      }
    }

    private def obj(b: PartBuilder, element: AmfObject, parent: String, inArray: Boolean = false): Unit = {
      def emit(b: PartBuilder) = b.obj(traverse(element, parent, _))

      if (inArray) emit(b) else b.list(emit)
    }

    private def link(b: PartBuilder, elementWithLink: DomainElement with Linkable, parent: String, inArray: Boolean = false): Unit = {
      def emit(b: PartBuilder): Unit = {
        b.obj { o =>
          traverse(elementWithLink, parent, o)
        }
      }

      if (inArray) emit(b) else b.list(emit)
    }

    private def iri(b: PartBuilder, content: String, inArray: Boolean = false): Unit = {
      def emit(b: PartBuilder): Unit = b.obj(_.entry("@id", raw(_, content)))

      if (inArray) emit(b) else b.list(emit)
    }

    private def fixTagIfNeeded(tag: YType, content: String): YType = {
      var tg: YType = tag match {
        case YType.Bool => {
          if (content != "true" && content != "false") {
            YType.Str
          } else {
            tag
          }
        }
        case YType.Int => {
          try {
            content.toInt
            tag
          } catch {
            case e: NumberFormatException => YType.Str
          }
        }
        case YType.Float => {
          try {
            content.toDouble
            tag
          } catch {
            case e: NumberFormatException => YType.Str
          }
        }
        case _ => tag

      }
      tg
    }

    private def scalar(b: PartBuilder, content: String, tag: YType = YType.Str, inArray: Boolean = false): Unit = {
      def emit(b: PartBuilder): Unit = {

        var tg: YType = fixTagIfNeeded(tag, content)

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
