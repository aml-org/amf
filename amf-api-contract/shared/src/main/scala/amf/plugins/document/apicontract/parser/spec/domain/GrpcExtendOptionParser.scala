package amf.plugins.document.apicontract.parser.spec.domain

import amf.core.model.domain.extensions.CustomDomainProperty
import amf.plugins.document.apicontract.contexts.parser.grpc.GrpcWebApiContext
import amf.plugins.document.apicontract.parser.spec.grpc.AntlrASTParserHelper
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes.{ENUM_OPTIONS, ENUM_VALUE_OPTIONS, EXTEND_IDENTIFIER, EXTENSION_RANGE_OPTIONS, FIELD, FIELD_OPTIONS, FILE_OPTIONS, MESSAGE_OPTIONS, METHOD_OPTIONS, ONEOF_OPTIONS, SERVICE_OPTIONS}
import amf.plugins.document.apicontract.vocabulary.VocabularyMappings
import org.mulesoft.antlrast.ast.{Node, Terminal}

case class GrpcExtendOptionParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends AntlrASTParserHelper {

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
