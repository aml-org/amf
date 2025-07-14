package amf.grpc.internal.spec.parser.domain

import amf.core.client.scala.model.domain.{DataNode, ScalarNode}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}
import org.mulesoft.antlrast.ast.Node

import scala.collection.mutable

case class GrpcEnumParser(ast: Node)(implicit val ctx: GrpcWebApiContext) extends GrpcASTParserHelper {
  val enum: ScalarShape = ScalarShape(toAnnotations(ast))

  def parse(setterFn: ScalarShape => Unit = _ => ()): ScalarShape = {
    parseName()
    setterFn(enum)
    parseElements()
    enum
  }

  def parseName(): Unit = withDeclaredShape(ast, ENUM_NAME, enum)

  private def parseElements(): Unit = {
    val values: mutable.Buffer[DataNode] = mutable.Buffer()
    val enumSchema                       = NodeShape(toAnnotations(ast))
    collect(ast, Seq(ENUM_BODY, ENUM_ELEMENT, ENUM_FIELD)).foreach { enumField =>
      val propertySchema = PropertyShape(toAnnotations(enumField))
      path(enumField, Seq(IDENTIFIER)) match {
        case Some(e) =>
          withOptTerminal(e) {
            case Some(t) =>
              propertySchema.withName(t.value)
              val data = ScalarNode(toAnnotations(e)).withValue(t.value)
              values.append(data)
            case _ =>
              astError("Missing protobuf 3 enumeration field name", toAnnotations(e))
          }
        case _ =>
          astError("Missing protobuf 3 enumeration field name", toAnnotations(enumField))
      }
      path(enumField, Seq(INT_LITERAL)) match {
        case Some(e) =>
          withOptTerminal(e) {
            case Some(t) =>
              propertySchema.withSerializationOrder(Integer.parseInt(t.value))
            case _ =>
              astError("Missing protobuf 3 enumeration field order", toAnnotations(e))
          }
        case _ =>
          astError("Missing protobuf 3 enumeration field order", toAnnotations(enumField))
      }
      enumSchema.withProperties(enumSchema.properties :+ propertySchema)
    }
    enum.withValues(values).withSerializationSchema(enumSchema)
  }
}
