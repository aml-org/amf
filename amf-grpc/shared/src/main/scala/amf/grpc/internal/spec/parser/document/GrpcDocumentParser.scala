package amf.grpc.internal.spec.parser.document

import amf.antlr.client.scala.parse.document.AntlrParsedDocument
import amf.apicontract.client.scala.model.domain.EndPoint
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.internal.metamodel.domain.api.WebApiModel
import amf.apicontract.internal.validation.definitions.ParserSideValidations.AntlrError
import amf.core.client.scala.model.document.{DeclaresModel, Document}
import amf.core.client.scala.parse.document._
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.metamodel.document.FragmentModel
import amf.core.internal.parser.Root
import amf.core.internal.remote.Spec
import amf.grpc.internal.spec.common.WellKnownTypes
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.domain._
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.annotations.{GrpcRawProto, WellKnownType}
import org.mulesoft.antlrast.ast.{AST, ASTNode, Node}

case class GrpcDocumentParser(root: Root)(implicit val ctx: GrpcWebApiContext) extends GrpcASTParserHelper {

  val doc: Document = Document()

  private def loadReferences(references: Seq[ParsedReference]): Unit = {
    references.foreach { reference =>
      reference.unit match {
        case dec: DeclaresModel =>
          dec.declares.foreach { case shape: AnyShape =>
            ctx.globalSpace += (shape.name.value() -> shape)
          }
        case _ => // ignore
      }
    }
    doc.withReferences(references.map(_.unit))
  }

  def parseDocument(): Document = {
    val ast = root.parsed.asInstanceOf[AntlrParsedDocument].ast
    loadSyntaxErrors(ast)
    loadReferences(root.references)
    ast.rootOption().collect({ case n: Node => parseRootNode(n) })

    ctx.declarations.futureDeclarations.resolve()
    generateImportAnnotations(doc)
    setDeclarations(doc)
    setProcessingData(doc, Spec.GRPC)

    doc.annotations += GrpcRawProto(root.raw)
    doc
  }

  private def generateImportAnnotations(doc: Document): Unit = {
    val importFromTypes = ctx.globalSpace.collect {
      case (key, _) if key.startsWith(".google") =>
        WellKnownTypes.getImportUrlForKey(key).getOrElse(key.replace(".", "/"))
    }.toSet

    importFromTypes.foreach(importFromType => doc.annotations += WellKnownType(importFromType))
  }

  private def parseRootNode(node: Node): Unit = {
    parseWebAPI(node)
    parseMessages(node)
    parseEnums(node)
    parseServices(node)
    parseExtensions(node)
  }

  private def parseWebAPI(node: Node): Unit = {
    val webApi = GrpcPackageParser(node, doc).parse()
    doc.withLocation(root.location)
    doc set webApi as FragmentModel.Encodes
  }

  private def parseMessages(node: Node): Unit = {
    collect(node, Seq(TOP_LEVEL_DEF, MESSAGE_DEF)).zipWithIndex.foreach { case (element: ASTNode, idx: Int) =>
      withNode(element) { node =>
        val shape = GrpcMessageParser(node).parse(shape => {
          shape.name.option() match {
            case None => shape.withName(s"Message$idx", toAnnotations(element))
            case _    =>
          }
        })
        ctx.declarations += shape.add(DeclaredElement())
      }
    }
  }

  private def parseEnums(node: Node): Unit = {
    collect(node, Seq(TOP_LEVEL_DEF, ENUM_DEF)).zipWithIndex.foreach { case (element: ASTNode, idx: Int) =>
      withNode(element) { node =>
        val shape = GrpcEnumParser(node).parse(shape => {
          shape.name.option() match {
            case None => shape.withName(s"Enum$idx", toAnnotations(element))
            case _    =>
          }
        })
        ctx.declarations += shape.add(DeclaredElement())
      }
    }
  }

  private def parseExtensions(node: Node): Unit = {
    collect(node, Seq(EXTENDS_STATEMENT)).foreach { element =>
      withNode(element) { node =>
        GrpcExtendOptionParser(node).parse(customDomainProperty =>
          ctx.declarations += customDomainProperty.add(DeclaredElement())
        )
      }
    }
  }

  private def parseServices(node: Node): Unit = {
    val webApi = doc.encodes.asInstanceOf[WebApi]
    val endPoints: Seq[EndPoint] = collect(node, Seq(TOP_LEVEL_DEF, SERVICE_DEF)).map { element =>
      withNode(element) { node =>
        GrpcServiceParser(node).parse()
      }
    }
    webApi set (endPoints, toAnnotations(node)) as WebApiModel.EndPoints
  }

  private def loadSyntaxErrors(ast: AST): Unit = {
    ast.getErrors.foreach(err => ctx.eh.violation(AntlrError, "", err.message, err.location))
  }

}
