package amf.graphql.internal.spec.document

import amf.antlr.client.scala.parse.document.AntlrParsedDocument
import amf.antlr.client.scala.parse.syntax.SourceASTElement
import amf.apicontract.client.scala.model.document.APIContractProcessingData
import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.{Api, WebApi}
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.validation.definitions.ParserSideValidations.DuplicatedDeclaration
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.NamedDomainElement
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.internal.parser.Root
import amf.core.internal.parser.domain.Annotations
import amf.core.internal.remote.Spec
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext
import amf.graphql.internal.spec.context.GraphQLBaseWebApiContext.RootTypes
import amf.graphql.internal.spec.domain._
import amf.graphql.internal.spec.parser.syntax.GraphQLASTParserHelper
import amf.graphql.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain.{ScalarShape, UnionShape}
import org.mulesoft.antlrast.ast.{AST, ASTNode, Node, Terminal}

case class GraphQLBaseDocumentParser(root: Root)(implicit val ctx: GraphQLBaseWebApiContext)
    extends GraphQLASTParserHelper {

  // default values, can be changed through a schema declaration
  var QUERY_TYPE        = "Query"
  var SUBSCRIPTION_TYPE = "Subscription"
  var MUTATION_TYPE     = "Mutation"

  val typeSystemDefinitionPath: Seq[String] = Seq(DOCUMENT, DEFINITION, TYPE_SYSTEM_DEFINITION)
  val typeDefinitionPath: Seq[String]       = typeSystemDefinitionPath :+ TYPE_DEFINITION

  val doc: Document          = Document()
  private def webapi: WebApi = doc.encodes.asInstanceOf[WebApi]

  def parseDocument(): Document = {
    val ast = root.parsed.asInstanceOf[AntlrParsedDocument].ast
    parseWebAPI(ast)
    ast.current() match {
      case node: Node =>
        processTypes(node)
      case _ => // nil
    }
    ctx.declarations.futureDeclarations.resolve()
    inFederation { implicit fCtx =>
      fCtx.linkingActions.executeAll()
    }
    val declarations = ctx.declarations.shapes.values.toList ++
      ctx.declarations.shapeExtensions.values.toList.flatten ++
      ctx.declarations.annotations.values.toList
    doc.withDeclares(declarations).withProcessingData(APIContractProcessingData().withSourceSpec(Spec.GRAPHQL))
  }

  private def parseWebAPI(ast: AST): Unit = {
    val webApi = WebApi(Annotations(SourceASTElement(ast.current())))
    webApi.withName(root.location.split("/").last)
    doc.withLocation(root.location).withEncodes(webApi)
  }

  private def parseNestedType(objTypeDef: Node): Unit = {
    val shape = new GraphQLNestedTypeParser(objTypeDef, isInterface = false).parse()
    addToDeclarations(shape)
  }

  private def parseInputType(objTypeDef: Node): Unit = {
    val shape = GraphQLInputTypeParser(objTypeDef).parse()
    addToDeclarations(shape)
  }

  private def parseInterfaceType(objTypeDef: Node): Unit = {
    val shape = new GraphQLNestedTypeParser(objTypeDef, isInterface = true).parse()
    addToDeclarations(shape)
  }

  private def parseUnionType(unionTypeDef: Node): Unit = {
    val shape: UnionShape = new GraphQLNestedUnionParser(unionTypeDef).parse()
    addToDeclarations(shape)
  }

  private def parseEnumType(enumTypeDef: Node): Unit = {
    val enum: ScalarShape = new GraphQLNestedEnumParser(enumTypeDef).parse()
    addToDeclarations(enum)
  }

  private def parseCustomScalarTypeDef(customScalarTypeDef: Node): Unit = {
    val scalar: ScalarShape = new GraphQLCustomScalarParser(customScalarTypeDef).parse()
    addToDeclarations(scalar)
  }

  private def parseDirectiveDeclaration(directiveDef: Node): Unit = {
    val directive: CustomDomainProperty = GraphQLDirectiveDeclarationParser(directiveDef).parse()
    addToDeclarations(directive)
  }

  private def parseDirectiveApplicationInDirectiveDeclaration(directiveDef: Node): Unit = {
    GraphQLDirectiveApplicationInDeclarationParser(directiveDef).parse()
  }


  def parseTypeExtension(typeExtensionDef: Node): Unit = {
    val shapeExtension = GraphQLTypeExtensionParser(typeExtensionDef).parse()
    ctx.declarations += shapeExtension
  }

  private def processTypes(node: Node): Unit = {
    // let's parse directive declarations first of all, because anything can have a directive applied to it
    this
      .collect(node, typeSystemDefinitionPath :+ DIRECTIVE_DEFINITION) foreach { case directiveDef: Node =>
      parseDirectiveDeclaration(directiveDef)
    }

    // let's parse directive applications within directive declarations
    this
      .collect(node, typeSystemDefinitionPath :+ DIRECTIVE_DEFINITION) foreach { case directiveDef: Node =>
      parseDirectiveApplicationInDirectiveDeclaration(directiveDef)
    }

    // let's parse schema
    this.collect(node, typeSystemDefinitionPath :+ SCHEMA_DEFINITION).toList match {
      case head :: Nil => parseSchemaNode(head)
      case _           => // ignore TODO violation
    }

    // let's parse types
    this.collect(node, typeDefinitionPath :+ OBJECT_TYPE_DEFINITION) foreach { case objTypeDef: Node =>
      searchName(objTypeDef) match {
        case Some(typeName) =>
          getRootType(typeName) match {
            case Some(rootType) => parseTopLevelType(objTypeDef, rootType)
            case None           => parseNestedType(objTypeDef)
          }
        case _ => parseNestedType(objTypeDef)
      }
    }

    // let's parse input types
    this
      .collect(node, typeDefinitionPath :+ INPUT_OBJECT_TYPE_DEFINITION) foreach { case objTypeDef: Node =>
      parseInputType(objTypeDef)
    }
    // let's parse interfaces
    this
      .collect(node, typeDefinitionPath :+ INTERFACE_TYPE_DEFINITION) foreach { case objTypeDef: Node =>
      parseInterfaceType(objTypeDef)
    }

    // let's parse enums
    this
      .collect(node, typeDefinitionPath :+ ENUM_TYPE_DEFINITION) foreach { case enumTypeDef: Node =>
      parseEnumType(enumTypeDef)
    }

    // let's parse custom scalars
    this
      .collect(node, typeDefinitionPath :+ SCALAR_TYPE_DEFINITION) foreach { case customScalarTypeDef: Node =>
      parseCustomScalarTypeDef(customScalarTypeDef)
    }

    // let's parse type extensions
    this
      .collect(node, Seq(DOCUMENT, DEFINITION, TYPE_SYSTEM_EXTENSION, TYPE_EXTENSION)) foreach {
      case typeExtensionDef: Node =>
        parseTypeExtension(typeExtensionDef)
    }

    // let's parse unions
    this
      .collect(node, typeDefinitionPath :+ UNION_TYPE_DEFINITION) foreach { case unionTypeDef: Node =>
      parseUnionType(unionTypeDef)
    }
  }

  private def parseSchemaNode(schemaNode: ASTNode): Unit = {
    GraphQLDirectiveApplicationParser(schemaNode.asInstanceOf[Node], webapi).parse()
    parseDescription(schemaNode, webapi, webapi.meta)
    // let's setup the names of the top level types
    collect(schemaNode, Seq(ROOT_OPERATION_TYPE_DEFINITION)).foreach { case typeDef: Node =>
      val targetType: String = path(typeDef, Seq(NAMED_TYPE, NAME, NAME_TERMINAL)) match {
        case Some(t: Terminal) => t.value
        case _ =>
          astError(
            "Cannot find operation type for top-level schema root operation named type",
            toAnnotations(typeDef)
          )
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
                case v              => astError(s"Unknown root-level operation type $v", toAnnotations(t))
              }
            case _ =>
              astError(
                "Cannot find operation type for top-level schema root operation type definition",
                toAnnotations(n)
              )
          }
        case _ =>
          astError(
            "Cannot find operation type for top-level schema root operation type definition",
            toAnnotations(typeDef)
          )
      }
    }

    if (Set(QUERY_TYPE, MUTATION_TYPE, SUBSCRIPTION_TYPE).size != 3) {
      astError("Root types cannot have duplicated names", toAnnotations(schemaNode))
    }
  }

  private def parseTopLevelType(objTypeDef: Node, queryType: RootTypes.Value): Api = {
    val endpoints    = GraphQLRootTypeParser(objTypeDef, queryType).parse()
    val oldEndpoints = webapi.endPoints
    webapi.withEndPoints(oldEndpoints ++ endpoints)
  }

  private def getRootType(typeName: String): Option[RootTypes.Value] = {
    typeName match {
      case q if q == QUERY_TYPE        => Some(RootTypes.Query)
      case s if s == SUBSCRIPTION_TYPE => Some(RootTypes.Subscription)
      case m if m == MUTATION_TYPE     => Some(RootTypes.Mutation)
      case _                           => None
    }
  }

  private def addToDeclarations(declaration: NamedDomainElement): Unit = {
    declaration match {
      case _: CustomDomainProperty => checkDeclarationIsUnique(declaration, ctx.declarations.annotations, "directives")
      case _                       => checkDeclarationIsUnique(declaration, ctx.declarations.shapes, "types")
    }
    ctx.declarations += declaration
  }

  private def checkDeclarationIsUnique(
      declaration: NamedDomainElement,
      declarations: Map[String, NamedDomainElement],
      kind: String
  ): Unit = {
    val declarationName = declaration.name.value()

    if (declarationIsDuplicated(declarations, declarationName)) {
      ctx.eh.violation(
        DuplicatedDeclaration,
        declaration,
        s"Cannot exist two or more $kind with name '$declarationName'",
        declaration.annotations
      )
    }
  }
  private def declarationIsDuplicated(declarations: Map[String, NamedDomainElement], declarationName: String) =
    declarations.exists(_._1 == declarationName)
}
