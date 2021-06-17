package amf.plugins.document.apicontract.parser.spec.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.plugins.document.apicontract.contexts.parser.grpc.GrpcWebApiContext
import amf.plugins.document.apicontract.parser.spec.grpc.AntlrASTParserHelper
import amf.plugins.document.apicontract.parser.spec.grpc.TokenTypes.FIELD_NAME
import org.mulesoft.antlrast.ast.Node

case class GrpcFieldParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends AntlrASTParserHelper {
  val propertyShape: PropertyShape = PropertyShape(toAnnotations(ast))

  def parse(adopt:PropertyShape => Unit): PropertyShape = {
    parseFieldName()
    adopt(propertyShape)
    parseFieldNumber()
    parseFieldRange()
    propertyShape
  }

  def parseFieldName(): Unit = {
    withName(ast, FIELD_NAME, propertyShape)
  }

  def parseFieldNumber(): Unit = {
    parseFieldNumber(ast) match {
      case Some(order) => propertyShape.withSerializationOrder(order)
      case None        => astError(propertyShape.id, "missing Protobuf3 field number", propertyShape.annotations)
    }
  }

  def parseFieldRange(): Unit = {
    parseFieldRange(ast) match {
      case Some(range) => propertyShape.withRange(range)
      case _           => astError(propertyShape.id, "missing Protobuf3 field type", propertyShape.annotations)
    }
  }

}
