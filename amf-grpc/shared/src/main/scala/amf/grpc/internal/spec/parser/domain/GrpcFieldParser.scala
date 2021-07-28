package amf.grpc.internal.spec.parser.domain

import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.AntlrASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes.{FIELD_NAME, FIELD_OPTION, FIELD_OPTIONS_ELEMENTS}
import org.mulesoft.antlrast.ast.Node

case class GrpcFieldParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends AntlrASTParserHelper {
  val propertyShape: PropertyShape = PropertyShape(toAnnotations(ast))

  def parse(adopt:PropertyShape => Unit): PropertyShape = {
    parseFieldName()
    adopt(propertyShape)
    parseFieldNumber()
    parseFieldRange()
    parseOptions()
    propertyShape
  }

  def parseOptions() = {
    collect(ast, Seq(FIELD_OPTIONS_ELEMENTS, FIELD_OPTION)).foreach { case n: Node =>
      GrpcOptionParser(n).parse({ extension =>
        extension.adopted(propertyShape.id)
        propertyShape.withCustomDomainProperty(extension)
      })
    }
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
