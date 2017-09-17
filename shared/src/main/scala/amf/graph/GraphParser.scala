package amf.graph

import amf.common.AMFAST
import amf.common.AMFToken.{MapToken, SequenceToken}
import amf.common.core.Strings
import amf.document.{BaseUnit, Document}
import amf.domain._
import amf.domain.extensions._
import amf.metadata.Type.{Array, Bool, Iri, RegExp, SortedArray, Str}
import amf.metadata.document.BaseUnitModel.Location
import amf.metadata.document.DocumentModel
import amf.metadata.domain._
import amf.metadata.domain.extensions.{CustomDomainPropertyModel, DomainExtensionModel}
import amf.metadata.shape._
import amf.metadata.{Field, Obj, Type}
import amf.model.{AmfElement, AmfObject, AmfScalar}
import amf.shape._
import amf.vocabulary.Namespace

/**
  * AMF Graph parser
  */
object GraphParser extends GraphParserHelpers {
  def parse(ast: AMFAST, location: String): BaseUnit = {
    val parser = Parser(Map())
    parser.parse(ast, location)
  }

  case class Parser(var nodes: Map[String, AmfElement]) {

    val dynamicGraphParser = new DynamicGraphParser(nodes)

    def parse(ast: AMFAST, location: String): BaseUnit = {
      val root = ast > SequenceToken > MapToken
      val ctx  = context(root)
      parse(root, ctx).set(Location, location).asInstanceOf[BaseUnit]
    }

    private def retrieveType(ast: AMFAST, ctx: GraphContext): Obj =
      ts(ast).find( t => {
        types.get(ctx.expand(t)).isDefined
      }) match {
        case Some(t) => types(ctx.expand(t))
        case None    =>
          throw new Exception(s"Error parsing JSON-LD node, unknown @types ${ts(ast)}")
      }

    private def parseList(listElement: Type, node: AMFAST, ctx: GraphContext) = {
      retrieveElements(node).map ({ (n) =>
        listElement match {
          case _: Obj  => parse(n, ctx)
          case _       => str(value(listElement, n))
        }
      })
    }

    def retrieveElements(ast: AMFAST): Seq[AMFAST] = {
      ast.children.find(key("@list")) match {
        case Some(entry) => (entry > SequenceToken).children
        case _           => throw new Exception(s"No @list declaration on list node $ast")
      }
    }

    private def parse(node: AMFAST, ctx: GraphContext): AmfObject = {
      val id      = retrieveId(node)
      val sources = retrieveSources(id, node)
      val model   = retrieveType(node, ctx)

      val instance = builders(model)(annotations(nodes, sources, id))
      instance.withId(id)
      val children = node.children

      model.fields.foreach(f => {
        val k = ctx.reduce(f.value)
        children.find(key(k)) match {
          case Some(entry) => traverse(ctx, instance, f, value(f.`type`, entry.last), sources, k)
          case _           =>
        }
      })

      // parsing custom extensions
      instance match {
        case elm:DomainElement => parseCustomProperties(node, elm, ctx)
        case _                 => // ignore
      }


      nodes = nodes + (id -> instance)
      instance
    }

    private def parseCustomProperties(node: AMFAST, instance: DomainElement, ctx: GraphContext) = {
      val customProperties = node.children.find(key(DomainElementModel.CustomDomainProperties.value.iri())) match {
        case Some(entry) => {
          entry.children.last.`type` match {
            case SequenceToken =>
              val elements = entry.children.last.children
              elements.filter { propertyUriNode =>
                propertyUriNode.children.exists(key("@id"))
              }.map { propertyUriNode =>
                propertyUriNode.children.find(key("@id")).get.last.content.unquote
              }
            case _        => Seq()
          }
        }
        case _           => Seq()
      }

      val domainExtensions: Seq[DomainExtension] = customProperties.map { propertyUri =>
        node.children.find(key(propertyUri)) match {
          case Some(entry) =>
            val parsedNode = dynamicGraphParser.parseDynamicType(entry.children.last, ctx)
            val domainExtension = DomainExtension()
            val domainProperty = CustomDomainProperty()
            domainProperty.id = propertyUri
            domainExtension.withId(parsedNode.id)
            domainExtension.withDefinedBy(domainProperty)
            domainExtension.withExtension(parsedNode)
            Some(domainExtension)
          case _          => None
        }
      }.filter(_.isDefined).map(_.get)
      instance.withCustomDomainProperties(domainExtensions)
    }



    private def traverse(ctx: GraphContext,
                         instance: AmfObject,
                         f: Field,
                         node: AMFAST,
                         sources: SourceMap,
                         key: String) = {
      f.`type` match {
        case _: Obj             => instance.set(f, parse(node, ctx), annotations(nodes, sources, key))
        case Str | RegExp | Iri => instance.set(f, str(node), annotations(nodes, sources, key))
        case Bool               => instance.set(f, bool(node), annotations(nodes, sources, key))
        case Type.Int           => instance.set(f, int(node), annotations(nodes, sources, key))
        case l: SortedArray     => instance.setArray(f, parseList(l.element, node, ctx), annotations(nodes, sources, key))
        case a: Array =>
          val values: Seq[AmfElement] = a.element match {
            case _: Obj    => node.children.map(n => parse(n, ctx))
            case Str | Iri => node.children.map(n => str(value(a.element, n)))
          }
          instance.setArray(f, values, annotations(nodes, sources, key))
      }
    }

    private def context(ast: AMFAST): GraphContext = {
      ast.children.find(key("@context")) match {
        case Some(t) =>
          MapGraphContext(
            (t > MapToken).children
              .map((entry) => {
                entry.head.content.unquote -> Namespace(entry.last.content.unquote)
              })
              .toMap)
        case _ => EmptyGraphContext
      }
    }

  }

  private def str(node: AMFAST) = AmfScalar(node.content.unquote)

  private def bool(node: AMFAST) = AmfScalar(node.content.unquote.toBoolean)

  private def int(node: AMFAST) = AmfScalar(node.content.unquote.toInt)

  /** Object Type builders. */
  private val builders: Map[Obj, (Annotations) => AmfObject] = Map(
    DocumentModel      -> {(a: Annotations) => Document(a)},
    WebApiModel        -> {(a: Annotations) => WebApi(a) },
    OrganizationModel  -> {(a: Annotations) => Organization(a)},
    LicenseModel       -> {(a: Annotations) => License(a)},
    CreativeWorkModel  -> {(a: Annotations) => CreativeWork(a)},
    EndPointModel      -> {(a: Annotations) => EndPoint(a)},
    OperationModel     -> {(a: Annotations) => Operation(a)},
    ParameterModel     -> {(a: Annotations) => Parameter(a)},
    PayloadModel       -> {(a: Annotations) => Payload(a)},
    RequestModel       -> {(a: Annotations) => Request(a)},
    ResponseModel      -> {(a: Annotations) => Response(a)},
    NodeShapeModel     -> {(a: Annotations) => NodeShape(a)},
    ArrayShapeModel    -> {(a: Annotations) => ArrayShape(a)},
    ScalarShapeModel   -> {(a: Annotations) => ScalarShape(a)},
    PropertyShapeModel -> {(a: Annotations) => PropertyShape(a)},
    XMLSerializerModel -> {(a: Annotations) => XMLSerializer(a)},
    PropertyDependenciesModel -> {(a: Annotations) => PropertyDependencies(a)},
    DomainExtensionModel      -> {(a: Annotations) => DomainExtension(a)},
    CustomDomainPropertyModel -> {(a: Annotations) => CustomDomainProperty(a)}
  )

  private val types: Map[String, Obj] = builders.keys.map(t => t.`type`.head.iri() -> t).toMap
}
