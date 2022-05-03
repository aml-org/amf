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

  def parse(adopt: ScalarShape => Unit): ScalarShape = {
    parseName(adopt)
    parseElements()
    enum
  }

  def parseName(adopt: ScalarShape => Unit): Unit = withDeclaredShape(ast, ENUM_NAME, enum, { _ => adopt(enum) })

  def parseElements(): Unit = {
    val values: mutable.Buffer[DataNode] = mutable.Buffer()
    val enumSchema                       = NodeShape(toAnnotations(ast)).adopted(enum.id)
    collect(ast, Seq(ENUM_BODY, ENUM_ELEMENT, ENUM_FIELD)).foreach { enumField =>
      val propertySchema = PropertyShape(toAnnotations(enumField)).adopted(enumSchema.id)
      path(enumField, Seq(IDENTIFIER)) match {
        case Some(e) =>
          withOptTerminal(e) {
            case Some(t) =>
              propertySchema.withName(t.value)
              val data = ScalarNode(toAnnotations(e)).withValue(t.value)
              data.adopted(enumSchema.id)
              values.append(data)
            case _ =>
              astError(propertySchema.id, "Missing protobuf 3 enumeration field name", toAnnotations(e))
          }
        case _ =>
          astError(propertySchema.id, "Missing protobuf 3 enumeration field name", toAnnotations(enumField))
      }
      path(enumField, Seq(INT_LITERAL)) match {
        case Some(e) =>
          withOptTerminal(e) {
            case Some(t) =>
              propertySchema.withSerializationOrder(Integer.parseInt(t.value))
            case _ =>
              astError(propertySchema.id, "Missing protobuf 3 enumeration field order", toAnnotations(e))
          }
        case _ =>
          astError(propertySchema.id, "Missing protobuf 3 enumeration field order", toAnnotations(enumField))
      }
      enumSchema.withProperties(enumSchema.properties ++ Seq(propertySchema))
    }
    enum.withValues(values).withSerializationSchema(enumSchema)
  }
}
