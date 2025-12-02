package amf.grpc.internal.spec.parser.domain

import amf.core.client.scala.model.domain.{AmfArray, AmfScalar}
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.internal.metamodel.domain.extensions.CustomDomainPropertyModel
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.internal.vocabulary.VocabularyMappings
import org.mulesoft.antlrast.ast.{Node, Terminal}

case class GrpcExtendOptionParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends GrpcASTParserHelper {

  def parse(setterFn: CustomDomainProperty => Unit): Unit = {
    val domain: String = parseDomain()
    parseExtensionFields(domain, setterFn)
  }

  private def parseExtensionFields(domain: String, setterFn: CustomDomainProperty => Unit): Unit = {
    collect(ast, Seq(FIELD)).foreach { case fieldElement: Node =>
      val ann                  = toAnnotations(fieldElement)
      val customDomainProperty = CustomDomainProperty(ann)
      val propertyShape        = GrpcFieldParser(fieldElement)(ctx).parse { _ => () }
      customDomainProperty set AmfScalar(
        propertyShape.serializationOrder.value(),
        ann
      ) as CustomDomainPropertyModel.SerializationOrder
      customDomainProperty set AmfArray(Seq(AmfScalar(domain, ann)), ann) as CustomDomainPropertyModel.Domain
      customDomainProperty set propertyShape.range as CustomDomainPropertyModel.Schema
      customDomainProperty.withName(propertyShape.name.value(), ann)
      setterFn(customDomainProperty)
    }
  }

  private def parseDomain(): String = {
    path(ast, Seq(EXTEND_IDENTIFIER)) match {
      case Some(n: Node) =>
        withOptTerminal(n) {
          case Some(t: Terminal) =>
            t.value match {
              case FIELD_OPTIONS           => "field"
              case ENUM_OPTIONS            => "enum"
              case ENUM_VALUE_OPTIONS      => "enum_value"
              case EXTENSION_RANGE_OPTIONS => "extension_range"
              case MESSAGE_OPTIONS         => VocabularyMappings.shape
              case METHOD_OPTIONS          => VocabularyMappings.operation
              case SERVICE_OPTIONS         => VocabularyMappings.endpoint
              case FILE_OPTIONS            => VocabularyMappings.webapi
              case ONEOF_OPTIONS           => "oneof"
            }
          case _ => VocabularyMappings.webapi
        }
      case _ => VocabularyMappings.webapi
    }
  }
}
