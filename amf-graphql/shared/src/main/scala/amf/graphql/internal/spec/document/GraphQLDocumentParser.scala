package amf.graphql.internal.spec.document

import amf.antlr.client.scala.parse.document.AntlrParsedDocument
import amf.apicontract.client.scala.model.document.APIContractProcessingData
import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.core.client.scala.model.document.Document
import amf.core.internal.parser.Root
import amf.core.internal.remote.Spec
import amf.graphql.internal.spec.context.GraphQLWebApiContext
import amf.graphql.internal.spec.context.GraphQLWebApiContext.RootTypes
import amf.graphql.internal.spec.domain.{
  GraphQLInputTypeParser,
  GraphQLNestedEnumParser,
  GraphQLNestedTypeParser,
  GraphQLNestedUnionParser,
  GraphQLRootTypeParser
}
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain.{ScalarShape, UnionShape}
import org.mulesoft.antlrast.ast.{ASTElement, Node, Terminal}

case class GraphQLDocumentParser(root: Root)(implicit val ctx: GraphQLWebApiContext) extends GraphQLASTParserHelper {

  // default values, can be changed through a schema declaration
  var QUERY_TYPE        = "Query"
  var SUBSCRIPTION_TYPE = "Subscription"
  var MUTATION_TYPE     = "Mutation"

  val doc: Document          = Document()
  private def webapi: WebApi = doc.encodes.asInstanceOf[WebApi]

  def parseDocument(): Document = {
    val ast = root.parsed.asInstanceOf[AntlrParsedDocument].ast
    parseWebAPI()
    ast.current() match {
      case node: Node =>
        processTypes(node)
      case _ => // nil

    }
    ctx.declarations.futureDeclarations.resolve()
    doc
      .withDeclares(
        ctx.declarations.shapes.values.toList ++
          ctx.declarations.annotations.values.toList
      )
      .withProcessingData(APIContractProcessingData().withSourceSpec(Spec.GRAPHQL))
  }

  private def parseWebAPI(): Unit = {
    val webApi = WebApi()
    webApi.withName(root.location.split("/").last)
    doc.adopted(root.location).withLocation(root.location).withEncodes(webApi)
    webApi.withId(webApi.id.replace(webApi.componentId, s"#${webApi.componentId}"))
  }

  private def parseNestedType(objTypeDef: Node): Unit = {
    val shape = new GraphQLNestedTypeParser(objTypeDef, isInterface = false).parse(doc.id)
    ctx.declarations += shape
  }

  private def parseInputType(objTypeDef: Node): Unit = {
    val shape = GraphQLInputTypeParser(objTypeDef).parse(doc.id)
    ctx.declarations += shape
  }

  private def parseInterfaceType(objTypeDef: Node): Unit = {
    val shape = new GraphQLNestedTypeParser(objTypeDef, isInterface = true).parse(doc.id)
    ctx.declarations += shape
  }

  private def parseUnionType(unionTypeDef: Node): Unit = {
    val shape: UnionShape = new GraphQLNestedUnionParser(unionTypeDef).parse(doc.id)
    ctx.declarations += shape
  }

  private def parseEnumType(enumTypeDef: Node): Unit = {
    val enum: ScalarShape = new GraphQLNestedEnumParser(enumTypeDef).parse(doc.id)
    ctx.declarations += enum
  }

  private def processTypes(node: Node): Unit = {
    this.collect(node, Seq(DOCUMENT, DEFINITION, TYPE_SYSTEM_DEFINITION, SCHEMA_DEFINITION)).toList match {
      case head :: Nil => parseSchemaNode(head)
      case _           => // ignore TODO violation
    }

    // no schema node, let's look for the default top-level types (query, subscription, mutation)
    this
      .collect(node, Seq(DOCUMENT, DEFINITION, TYPE_SYSTEM_DEFINITION, TYPE_DEFINITION, OBJECT_TYPE_DEFINITION)) foreach {
      case objTypeDef: Node =>
        searchName(objTypeDef) match {
          case Some(query) if query == QUERY_TYPE => parseTopLevelType(objTypeDef, RootTypes.Query)
          case Some(subscription) if subscription == SUBSCRIPTION_TYPE =>
            parseTopLevelType(objTypeDef, RootTypes.Subscription)
          case Some(mutation) if mutation == MUTATION_TYPE => parseTopLevelType(objTypeDef, RootTypes.Mutation)
          case _                                           => parseNestedType(objTypeDef)
        }
    }

    // let's parse input types
    this
      .collect(node, Seq(DOCUMENT, DEFINITION, TYPE_SYSTEM_DEFINITION, TYPE_DEFINITION, INPUT_OBJECT_TYPE_DEFINITION)) foreach {
      case objTypeDef: Node =>
        parseInputType(objTypeDef)
    }
    // let's parse interfaces
    this
      .collect(node, Seq(DOCUMENT, DEFINITION, TYPE_SYSTEM_DEFINITION, TYPE_DEFINITION, INTERFACE_TYPE_DEFINITION)) foreach {
      case objTypeDef: Node =>
        parseInterfaceType(objTypeDef)
    }

    // let's parse unions
    this
      .collect(node, Seq(DOCUMENT, DEFINITION, TYPE_SYSTEM_DEFINITION, TYPE_DEFINITION, UNION_TYPE_DEFINITION)) foreach {
      case unionTypeDef: Node =>
        parseUnionType(unionTypeDef)
    }

    // let's parse enums
    this
      .collect(node, Seq(DOCUMENT, DEFINITION, TYPE_SYSTEM_DEFINITION, TYPE_DEFINITION, ENUM_TYPE_DEFINITION)) foreach {
      case enumTypeDef: Node =>
        parseEnumType(enumTypeDef)
    }
  }

  private def parseSchemaNode(schemaNode: ASTElement): Unit = {
    findDescription(schemaNode) match {
      case Some(terminal: Terminal) => // the description of the schema is set at the API level
        webapi.set(WebApiModel.Description, cleanDocumentation(terminal.value), toAnnotations(terminal))
      case _ => // ignore
    }

    // let's setup the names of the top level types
    collect(schemaNode, Seq(ROOT_OPERATION_TYPE_DEFINITION)).foreach {
      case typeDef: Node =>
        val targetType: String = path(typeDef, Seq(NAMED_TYPE, NAME, NAME_TERMINAL)) match {
          case Some(t: Terminal) => t.value
          case _ =>
            astError(webapi.id,
                     "Cannot find operation type for top-level schema root operation named type",
                     toAnnotations(typeDef))
            ""
        }
        find(typeDef, OPERATION_TYPE).headOption match {
          case Some(n: Node) =>
            n.children.headOption match {
              case Some(t: Terminal) =>
                t.value match {
                  case "query"        => QUERY_TYPE = targetType
                  case "mutation"     => MUTATION_TYPE = targetType
                  case "subscription" => SUBSCRIPTION_TYPE = targetType
                  case v              => astError(webapi.id, s"Unknown root-level operation type ${v}", toAnnotations(t))
                }
              case _ =>
                astError(webapi.id,
                         "Cannot find operation type for top-level schema root operation type definition",
                         toAnnotations(n))
            }
          case _ =>
            astError(webapi.id,
                     "Cannot find operation type for top-level schema root operation type definition",
                     toAnnotations(typeDef))
        }
    }

    if (Set(QUERY_TYPE, MUTATION_TYPE, SUBSCRIPTION_TYPE).size != 3) {
      astError(webapi.id, "Root types cannot have duplicated names", toAnnotations(schemaNode))
    }
  }

  private def parseTopLevelType(objTypeDef: Node, queryType: RootTypes.Value): Seq[EndPoint] = {
    GraphQLRootTypeParser(objTypeDef, queryType).parse { ep: EndPoint =>
      ep.adopted(webapi.id)
      val oldEndpoints = webapi.endPoints
      webapi.withEndPoints(oldEndpoints ++ Seq(ep))
    }
  }

}
