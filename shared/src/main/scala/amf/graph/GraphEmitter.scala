package amf.graph

import amf.client.GenerationOptions
import amf.document.{BaseUnit, Document, Module}
import amf.domain._
import amf.domain.`abstract`._
import amf.domain.dialects.DomainEntity
import amf.domain.extensions._
import amf.metadata.Type.{Array, Bool, Iri, RegExp, SortedArray, Str}
import amf.metadata.document.{DocumentModel, ModuleModel}
import amf.metadata.domain.DomainElementModel.Sources
import amf.metadata.domain._
import amf.metadata.domain.`abstract`._
import amf.metadata.domain.dialects.DialectEntityModel
import amf.metadata.domain.extensions.{CustomDomainPropertyModel, DataNodeModel, DomainExtensionModel}
import amf.metadata.shape._
import amf.metadata.{Field, Obj, SourceMapModel, Type}
import amf.model.{AmfArray, AmfObject, AmfScalar}
import amf.parser.ASTEmitter
import amf.shape._
import amf.vocabulary.Namespace.SourceMaps
import amf.vocabulary.{Namespace, ValueType}
import org.yaml.model.{YDocument, YType}

import scala.collection.mutable.ListBuffer

/**
  * AMF Graph emitter
  */
object GraphEmitter {

  def emit(unit: BaseUnit, options: GenerationOptions): YDocument = {
    val emitter = Emitter(ASTEmitter(), options)
    emitter.root(unit)
  }

  case class Emitter(emitter: ASTEmitter, options: GenerationOptions) {

    def root(unit: BaseUnit): YDocument = {
      emitter.document { () =>
        array { () =>
          map { () =>
            traverse(unit, unit.location)
          }
        }
      }
    }

    def traverse(element: AmfObject, parent: String): Unit = {
      val id = element.id
      createIdNode(id)

      val sources = SourceMap(id, element)

      val obj = metaModel(element)
      if (obj.dynamic) {
        traverseDynamicMetaModel(id, element, sources, obj, parent)
      } else {
        traverseStaticMetamodel(id, element, sources, obj, parent)
      }

      createCustomExtensions(element, parent)

      createSourcesNode(id + "/source-map", sources)
    }

    def traverseDynamicMetaModel(id: String, element: AmfObject, sources: SourceMap, obj: Obj, parent: String): Unit = {
      val schema: DynamicDomainElement = element.asInstanceOf[DynamicDomainElement]

      createDynamicTypeNode(schema)

      schema.dynamicFields.foreach { f: Field =>
        schema.valueForField(f).foreach { amfElement =>
          entry { () =>
            val propertyUri = f.value.iri()
            raw(propertyUri)
            value(f.`type`, Value(amfElement, amfElement.annotations), id, { (_) =>
              })
          }
        }
      }
    }

    def traverseStaticMetamodel(id: String, element: AmfObject, sources: SourceMap, obj: Obj, parent: String): Unit = {
      createTypeNode(obj, Some(element))

      obj.fields.map(element.fields.entryJsonld).foreach {
        case Some(FieldEntry(f, v)) =>
          entry { () =>
            val url = f.value.iri()
            raw(url)
            value(f.`type`, v, id, sources.property(url))
          }
        case None => // Missing field
      }
    }

    private def createCustomExtensions(element: AmfObject, parent: String): Unit = {
      val customProperties: ListBuffer[String] = ListBuffer()

      element.fields.entry(DomainElementModel.CustomDomainProperties) match {
        case Some(FieldEntry(_, v)) =>
          v.value match {
            case AmfArray(values, _) =>
              values.foreach {
                case customExtension: DomainExtension =>
                  val propertyUri = customExtension.definedBy.id
                  customProperties += propertyUri
                  entry { () =>
                    raw(propertyUri)
                    map { () =>
                      traverse(customExtension.extension, parent)
                    }
                  }
              }

            case _ => // ignore
          }
        case None => // ignore
      }

      if (customProperties.nonEmpty) {
        entry { () =>
          raw((Namespace.Document + "customDomainProperties").iri())
          array { () =>
            customProperties.foreach(iri(_, inArray = true))
          }
        }
      }
    }

    private def value(t: Type, v: Value, parent: String, sources: (Value) => Unit): Unit = {
      t match {
        case _: Obj =>
          obj(v.value.asInstanceOf[AmfObject], parent)
          sources(v)
        case Iri =>
          iri(v.value.asInstanceOf[AmfScalar].toString)
          sources(v)
        case Str | RegExp =>
          scalar(v.value.asInstanceOf[AmfScalar].toString)
          sources(v)
        case Bool =>
          scalar(v.value.asInstanceOf[AmfScalar].toString, YType.Bool)
          sources(v)
        case Type.Int =>
          scalar(v.value.asInstanceOf[AmfScalar].toString, YType.Int)
          sources(v)
        case a: SortedArray =>
          map { () =>
            entry { () =>
              raw("@list")
              array { () =>
                val seq = v.value.asInstanceOf[AmfArray]
                sources(v)
                a.element match {
                  case _: Obj => seq.values.asInstanceOf[Seq[AmfObject]].foreach(e => obj(e, parent, inArray = true))
                  case Str    => seq.values.asInstanceOf[Seq[AmfScalar]].foreach(e => scalar(e.toString, inArray = true))
                }
              }
            }
          }
        case a: Array =>
          array { () =>
            val seq = v.value.asInstanceOf[AmfArray]
            sources(v)
            a.element match {
              case _: Obj => seq.values.asInstanceOf[Seq[AmfObject]].foreach(e => obj(e, parent, inArray = true))
              case Str    => seq.values.asInstanceOf[Seq[AmfScalar]].foreach(e => scalar(e.toString, inArray = true))
              case Iri    => seq.values.asInstanceOf[Seq[AmfScalar]].foreach(e => iri(e.toString, inArray = true))
              case Type.Int =>
                seq.values
                  .asInstanceOf[Seq[AmfScalar]]
                  .foreach(e => scalar(e.value.asInstanceOf[AmfScalar].toString, YType.Int, inArray = true))
              case Bool =>
                seq.values
                  .asInstanceOf[Seq[AmfScalar]]
                  .foreach(e => scalar(e.value.asInstanceOf[AmfScalar].toString, YType.Bool, inArray = true))
              case _ => seq.values.asInstanceOf[Seq[AmfScalar]].foreach(e => iri(e.toString, inArray = true))
            }
          }
      }
    }

    private def obj(element: AmfObject, parent: String, inArray: Boolean = false): Unit = {
      val obj = () =>
        map { () =>
          traverse(element, parent)
      }
      if (inArray) {
        obj()
      } else {
        array { () =>
          obj()
        }
      }
    }

    private def iriValue(content: String): Unit = {
      map { () =>
        entry { () =>
          raw("@id")
          raw(content)
        }
      }
    }

    private def iri(content: String, inArray: Boolean = false): Unit = {
      if (inArray) {
        iriValue(content)
      } else {
        array { () =>
          iriValue(content)
        }
      }
    }

    private def scalar(content: String, tag: YType = YType.Str, inArray: Boolean = false): Unit = {
      if (inArray) {
        value(content, tag)
      } else {
        array { () =>
          value(content, tag)
        }
      }
    }

    private def value(content: String, tag: YType): Unit = {
      map { () =>
        entry { () =>
          raw("@value")
          raw(content, tag)
        }
      }
    }

    private def createIdNode(id: String): Unit = entry("@id", id)

    private def createTypeNode(obj: Obj, maybeElement: Option[AmfObject] = None): Unit = {
      entry { () =>
        raw("@type")
        array { () =>
          obj.`type`.foreach(t => raw(t.iri()))
          if (obj.dynamicType) {
            maybeElement match {
              case Some(element) => element.dynamicTypes().foreach(t => raw(t))
              case _             => // ignore
            }
          }
        }
      }
    }

    private def createDynamicTypeNode(obj: DynamicDomainElement): Unit = {
      entry { () =>
        raw("@type")
        array { () =>
          obj.dynamicType.foreach(t => raw(t.iri()))
        }
      }
    }

    private def entry(k: String, v: String): Unit = entry { () =>
      raw(k)
      raw(v)
    }

    private def raw(content: String, tag: YType = YType.Str): Unit = emitter.scalar(content, tag)

    private def entry(inner: () => Unit): Unit = emitter.entry(inner)

    private def array(inner: () => Unit): Unit = emitter.sequence(inner)

    private def map(inner: () => Unit): Unit = emitter.mapping(inner)

    private def createSourcesNode(id: String, sources: SourceMap): Unit = {
      if (options.isWithSourceMaps && sources.nonEmpty) {
        entry { () =>
          raw(Sources.value.iri())
          array { () =>
            map { () =>
              createIdNode(id)
              createTypeNode(SourceMapModel)
              createAnnotationNodes(sources)
            }
          }
        }
      }
    }

    private def createAnnotationNodes(sources: SourceMap): Unit = {
      sources.annotations.foreach({
        case (a, values) =>
          entry { () =>
            raw(ValueType(SourceMaps, a).iri())
            array { () =>
              values.foreach(createAnnotationValueNode)
            }
          }
      })
    }

    private def createAnnotationValueNode(tuple: (String, String)): Unit = tuple match {
      case (iri, v) =>
        map { () =>
          entry { () =>
            raw(SourceMapModel.Element.value.iri())
            scalar(iri)
          }
          entry { () =>
            raw(SourceMapModel.Value.value.iri())
            scalar(v)
          }
        }
    }
  }

  /** Metadata Type references. */
  private def metaModel(instance: Any): Obj = instance match {
    case _: Document                 => DocumentModel
    case _: WebApi                   => WebApiModel
    case _: Organization             => OrganizationModel
    case _: License                  => LicenseModel
    case _: CreativeWork             => CreativeWorkModel
    case _: EndPoint                 => EndPointModel
    case _: Operation                => OperationModel
    case _: Parameter                => ParameterModel
    case _: Request                  => RequestModel
    case _: Response                 => ResponseModel
    case _: Payload                  => PayloadModel
    case _: NodeShape                => NodeShapeModel
    case _: ArrayShape               => ArrayShapeModel
    case _: ScalarShape              => ScalarShapeModel
    case _: PropertyShape            => PropertyShapeModel
    case _: XMLSerializer            => XMLSerializerModel
    case _: PropertyDependencies     => PropertyDependenciesModel
    case _: DomainExtension          => DomainExtensionModel
    case _: CustomDomainProperty     => CustomDomainPropertyModel
    case _: DataNode                 => DataNodeModel
    case entity: DomainEntity        => new DialectEntityModel(entity)
    case _: Module                   => ModuleModel
    case _: ResourceType             => ResourceTypeModel
    case _: Trait                    => TraitModel
    case _: ParametrizedResourceType => ParametrizedResourceTypeModel
    case _: ParametrizedTrait        => ParametrizedTraitModel
    case _: Variable                 => VariableModel
    case _: VariableValue            => VariableValueModel
    case _                           => throw new Exception(s"Missing metadata mapping for $instance")
  }
}
