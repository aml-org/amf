package amf.grpc.internal.spec.parser.domain

import amf.core.client.scala.model.domain.{DataNode, ScalarNode}
import amf.core.client.scala.model.domain.extensions.PropertyShape
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}
import org.mulesoft.antlrast.ast.{ASTNode, Node}


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
    parseReservedValues()
    val (values, properties) = parseEnumFields()
    val enumSchema = NodeShape(toAnnotations(ast)).withProperties(properties)
    enum.withValues(values).withSerializationSchema(enumSchema)
  }

  private def parseReservedValues(): Unit = {
    collect(ast, Seq(ENUM_BODY, ENUM_ELEMENT, RESERVED)).foreach { case reserved: Node =>
      GrpcReservedValuesParser(reserved).parse { reservedValues =>
        enum.withReservedValues(reservedValues)
      }
    }
  }

  private def parseEnumFields(): (Seq[DataNode], Seq[PropertyShape]) = {
    val enumFields = collect(ast, Seq(ENUM_BODY, ENUM_ELEMENT, ENUM_FIELD))
    val parsedFields = enumFields.map(parseEnumField)
    val (values, properties) = parsedFields.unzip
    (values.flatten, properties.flatten)
  }

  private def parseEnumField(enumField: ASTNode): (Option[DataNode], Option[PropertyShape]) = {
    val propertySchema = PropertyShape(toAnnotations(enumField))
    
    val maybeName = parseFieldName(enumField, propertySchema)
    val maybeOrder = parseFieldOrder(enumField, propertySchema)
    
    (maybeName, if (maybeName.isDefined && maybeOrder) Some(propertySchema) else None)
  }

  private def parseFieldName(enumField: ASTNode, propertySchema: PropertyShape): Option[DataNode] = {
    path(enumField, Seq(IDENTIFIER)).flatMap { identifier =>
      extractTerminalValue(identifier) match {
        case Some(name) =>
          propertySchema.withName(name)
          Some(ScalarNode(toAnnotations(identifier)).withValue(name))
        case None =>
          astError("Missing protobuf 3 enumeration field name", toAnnotations(identifier))
          None
      }
    } orElse {
      astError("Missing protobuf 3 enumeration field identifier", toAnnotations(enumField))
      None
    }
  }

  private def parseFieldOrder(enumField: ASTNode, propertySchema: PropertyShape): Boolean = {
    path(enumField, Seq(INT_LITERAL)).flatMap { intLiteral =>
      extractTerminalValue(intLiteral) match {
        case Some(orderStr) =>
          try {
            val order = Integer.parseInt(orderStr)
            propertySchema.withSerializationOrder(order)
            Some(true)
          } catch {
            case _: NumberFormatException =>
              astError(s"Invalid enumeration field order: '$orderStr'", toAnnotations(intLiteral))
              Some(false)
          }
        case None =>
          astError("Missing protobuf 3 enumeration field order value", toAnnotations(intLiteral))
          Some(false)
      }
    } getOrElse {
      astError("Missing protobuf 3 enumeration field order", toAnnotations(enumField))
      false
    }
  }
}
