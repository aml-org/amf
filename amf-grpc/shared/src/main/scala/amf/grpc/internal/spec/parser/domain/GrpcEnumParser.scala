package amf.grpc.internal.spec.parser.domain

import amf.core.client.scala.model.domain.extensions.{DomainExtension, PropertyShape}
import amf.core.client.scala.model.domain.{AmfArray, AmfScalar, DataNode, ScalarNode}
import amf.core.internal.metamodel.domain.extensions.PropertyShapeModel
import amf.core.internal.metamodel.domain.{ScalarNodeModel, ShapeModel}
import amf.grpc.internal.spec.parser.context.GrpcWebApiContext
import amf.grpc.internal.spec.parser.syntax.GrpcASTParserHelper
import amf.grpc.internal.spec.parser.syntax.TokenTypes._
import amf.shapes.client.scala.model.domain.{NodeShape, ScalarShape}
import amf.shapes.internal.domain.metamodel.{NodeShapeModel, ScalarShapeModel}
import amf.shapes.internal.domain.metamodel.grpc.GrpcFields
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
    // todo: ENUM_BODY, ENUM_ELEMENT repeats on every parse, abstract for less searches and better annotations
    parseReservedValues()
    parseOptions()
    val (values, properties) = parseEnumFields()
    val ann                  = toAnnotations(ast)
    val enumSchema           = NodeShape(ann)
    enumSchema set (properties, ann) as NodeShapeModel.Properties
    enum set (values, ann) as ShapeModel.Values
    enum set enumSchema as ShapeModel.SerializationSchema
  }

  private def parseReservedValues(): Unit = {
    collect(ast, Seq(ENUM_BODY, ENUM_ELEMENT, RESERVED)).foreach { case reserved: Node =>
      GrpcReservedValuesParser(reserved).parse { reservedValues =>
        enum.withReservedValues(reservedValues, toAnnotations(reserved))
      }
    }
  }

  private def parseOptions(): Unit = {
    val extensions: Seq[DomainExtension] = collect(ast, Seq(ENUM_BODY, ENUM_ELEMENT, OPTION_STATEMENT)).map {
      case option: Node => GrpcOptionParser(option).parse()
    }
    enum set (extensions, toAnnotations(ast)) as ScalarShapeModel.CustomDomainProperties
  }

  private def parseEnumFields(): (Seq[DataNode], Seq[PropertyShape]) = {
    val enumFields           = collect(ast, Seq(ENUM_BODY, ENUM_ELEMENT, ENUM_FIELD))
    val parsedFields         = enumFields.map(parseEnumField)
    val (values, properties) = parsedFields.unzip
    (values.flatten, properties.flatten)
  }

  private def parseEnumField(enumField: ASTNode): (Option[DataNode], Option[PropertyShape]) = {
    val propertySchema = PropertyShape(toAnnotations(enumField))

    val maybeName  = parseFieldName(enumField, propertySchema)
    val maybeOrder = parseFieldOrder(enumField, propertySchema)

    collect(enumField, Seq(OPTIONS_ENUM_VALUE, OPTION_ENUM_VALUE)).foreach { case option: Node =>
      GrpcOptionParser(option).parse { extension =>
        val extensions = propertySchema.customDomainProperties :+ extension
        val ann        = toAnnotations(option)
        propertySchema.setWithoutId(PropertyShapeModel.CustomDomainProperties, AmfArray(extensions, ann), ann)
      }
    }

    (maybeName, if (maybeName.isDefined && maybeOrder) Some(propertySchema) else None)
  }

  private def parseFieldName(enumField: ASTNode, propertySchema: PropertyShape): Option[DataNode] = {
    path(enumField, Seq(IDENTIFIER)).flatMap { identifier =>
      val ann = toAnnotations(identifier)
      extractTerminalValue(identifier) match {
        case Some(name) =>
          propertySchema.withName(name, ann)
          val scalar = ScalarNode(ann)
          scalar set AmfScalar(name, ann) as ScalarNodeModel.Value
          Some(scalar)
        case None =>
          astError("Missing protobuf 3 enumeration field name", ann)
          None
      }
    } orElse {
      astError("Missing protobuf 3 enumeration field identifier", toAnnotations(enumField))
      None
    }
  }

  private def parseFieldOrder(enumField: ASTNode, propertySchema: PropertyShape): Boolean = {
    path(enumField, Seq(INT_LITERAL)).flatMap { intLiteral =>
      val ann = toAnnotations(intLiteral)
      extractTerminalValue(intLiteral) match {
        case Some(orderStr) =>
          try {
            val order = Integer.parseInt(orderStr)
            propertySchema.setWithoutId(PropertyShapeModel.SerializationOrder, AmfScalar(order, ann), ann)
            Some(true)
          } catch {
            case _: NumberFormatException =>
              astError(s"Invalid enumeration field order: '$orderStr'", ann)
              Some(false)
          }
        case None =>
          astError("Missing protobuf 3 enumeration field order value", ann)
          Some(false)
      }
    } getOrElse {
      astError("Missing protobuf 3 enumeration field order", toAnnotations(enumField))
      false
    }
  }
}
