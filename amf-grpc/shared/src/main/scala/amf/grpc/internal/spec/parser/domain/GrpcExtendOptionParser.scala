package amf.grpc.internal.spec.parser.domain

import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.internal.vocabulary.VocabularyMappings
import org.mulesoft.antlrast.ast.{Node, Terminal}

case class GrpcExtendOptionParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends GrpcASTParserHelper {

  def parse(adopt: CustomDomainProperty => Unit): Unit = {
    val domain: String = parseDomain()
    parseExtensionFields(domain, adopt)
  }

  def parseExtensionFields(domain: String, adopt: CustomDomainProperty => Unit): Unit = {
    collect(ast, Seq(FIELD)).foreach { case fieldElement: Node =>
      val customDomainProperty = CustomDomainProperty(toAnnotations(ast))
      adopt(customDomainProperty)
      val propertyShape = GrpcFieldParser(fieldElement)(ctx).parse { _ => }
      customDomainProperty
        .withSerializationOrder(propertyShape.serializationOrder.value())
        .withDomain(Seq(domain))
        .withSchema(propertyShape.range)
        .withName(propertyShape.name.value())
    }
  }

  def parseDomain(): String = {
    path(ast, Seq(EXTEND_IDENTIFIER)) match {
      case Some(n: Node) =>
        withOptTerminal(n) {
          case Some(t: Terminal) => t.value match {
            case FIELD_OPTIONS => "field"
            case ENUM_OPTIONS  => "enum"
            case ENUM_VALUE_OPTIONS => "enum_value"
            case EXTENSION_RANGE_OPTIONS => "extension_range"
            case MESSAGE_OPTIONS => VocabularyMappings.shape
            case METHOD_OPTIONS => VocabularyMappings.operation
            case SERVICE_OPTIONS => VocabularyMappings.endpoint
            case FILE_OPTIONS => VocabularyMappings.webapi
            case ONEOF_OPTIONS => "oneof"
          }
          case _                 => VocabularyMappings.webapi
        }
      case _             =>  VocabularyMappings.webapi
    }
  }
}
