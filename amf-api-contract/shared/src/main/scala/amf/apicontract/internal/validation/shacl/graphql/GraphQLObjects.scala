package amf.apicontract.internal.validation.shacl.graphql

import amf.apicontract.client.scala.model.domain.{EndPoint, Response}
import amf.apicontract.internal.validation.shacl.graphql.GraphQLUtils.isValidOutputType
import amf.core.client.scala.model.domain.extensions.{CustomDomainProperty, DomainExtension, PropertyShape}
import amf.core.client.scala.model.domain.{DataNode, ObjectNode, ScalarNode, Shape}
import amf.core.internal.parser.domain.Annotations
import amf.shapes.client.scala.model.domain._
import amf.shapes.client.scala.model.domain.operations._

case class GraphQLObject(node: NodeShape) {
  def name: String                      = node.name.value()
  def annotations: Annotations          = node.annotations
  def isInterface: Boolean              = node.isAbstract.value()
  def isInput: Boolean                  = node.isInputOnly.value()
  def isSchema: Boolean                 = node.name.isNullOrEmpty
  def properties: Seq[GraphQLProperty]  = node.properties.map(GraphQLProperty)
  def operations: Seq[GraphQLOperation] = node.operations.map(GraphQLOperation)

  def fields(): GraphQLFields = GraphQLFields(properties, operations)

  def inherits: Seq[GraphQLObject] = node.inherits.map(_.asInstanceOf[NodeShape]).map(GraphQLObject)
}

case class GraphQLFields(properties: Seq[GraphQLProperty], operations: Seq[GraphQLOperation]) {
  def names: Set[String] = {
    (properties.map(_.name) ++ operations.map(_.name)).toSet
  }
}

case class GraphQLEndpoint(endpoint: EndPoint) {
  def name: String                      = endpoint.name.value()
  def path: String                      = endpoint.path.value()
  def operations: Seq[GraphQLOperation] = endpoint.operations.map(GraphQLOperation)
  def parameters: Seq[GraphQLParameter] = operations.flatMap(_.parameters)
  def isValidInputType: Boolean         = parameters.forall(_.isValidInputType)
  def isValidOutputType: Boolean        = parameters.forall(_.isValidOutputType)
}

case class GraphQLProperty(property: PropertyShape) {
  def name: String               = property.name.value()
  def annotations: Annotations   = property.annotations
  def datatype: Option[String]   = GraphQLUtils.datatype(property.range)
  def default: Option[DataNode]  = Option(property.default)
  def range: Shape               = property.range
  def isValidInputType: Boolean  = GraphQLUtils.isValidInputType(range)
  def isValidOutputType: Boolean = GraphQLUtils.isValidOutputType(range)
}

case class GraphQLOperation(operation: AbstractOperation) {
  def name: String                       = operation.name.value()
  def annotations: Annotations           = operation.annotations
  def parameters: Seq[GraphQLParameter]  = operation.request.queryParameters.map(GraphQLParameter)
  def response: Option[AbstractResponse] = operation.responses.headOption

  def payload: Option[AbstractPayload] = response.flatMap {
    case r: Response       => r.payloads.headOption
    case sr: ShapeResponse => Some(sr.payload)
    case _                 => None
  }

  def isValidOutputType: Boolean = payload match {
    case Some(payload) => GraphQLUtils.isValidOutputType(payload.schema)
    case None          => false
  }
}

case class GraphQLParameter(parameter: AbstractParameter) {
  def name: String               = parameter.name.value()
  def annotations: Annotations   = parameter.annotations
  def datatype: Option[String]   = GraphQLUtils.datatype(parameter.schema)
  def default: DataNode          = parameter.schema.default
  def schema: Shape              = parameter.schema
  def isValidInputType: Boolean  = GraphQLUtils.isValidInputType(schema)
  def isValidOutputType: Boolean = GraphQLUtils.isValidOutputType(schema)
}

case class GraphQLDirective(directive: CustomDomainProperty) {
  def name: String             = directive.name.value()
  def annotations: Annotations = directive.annotations
  def fields: GraphQLFields    = GraphQLObject(directive.schema.asInstanceOf[NodeShape]).fields()
}

case class GraphQLAppliedDirective(directive: DomainExtension) {
  def name: String             = directive.name.value()
  def annotations: Annotations = directive.annotations
  def definedProps(): Seq[GraphQLProperty] = directive.definedBy match {
    case c: CustomDomainProperty =>
      c.schema match {
        case n: NodeShape => n.properties.map(GraphQLProperty)
        case _            => Seq()
      }
    case _ => Seq()
  }

  def parsedProps(): Seq[ScalarNode] = directive.extension match {
    case o: ObjectNode => o.allProperties().map(_.asInstanceOf[ScalarNode]).toList
    case _             => Seq()
  }
}

case class GraphQLNullable(union: UnionShape) {
  def name: String = {
    val name = union.name
    if (name.isNullOrEmpty) {
      union.anyOf match {
        case List(_: NilShape, n: NodeShape)   => n.name.value()
        case List(_: NilShape, u: UnionShape)  => GraphQLNullable(u).name
        case List(_: NilShape, s: ScalarShape) => s.name.value()
        case List(_: NilShape, a: ArrayShape) =>
          val items = a.items match {
            case u: UnionShape => GraphQLNullable(u).name
            case s: Shape      => s.name.value()
          }
          s"a list of $items"
        case _ => "an Union"
      }
    } else name.value()
  }

  def isNullable: Boolean = union.anyOf.head.isInstanceOf[NilShape]

  def isValidInput: Boolean = {
    union.anyOf.forall {
      case _: NilShape    => true
      case _: ScalarShape => true
      case n: NodeShape   => GraphQLObject(n).isInput
      case _              => false
    }
  }

  def isValidOutput: Boolean = {
    union.anyOf.forall {
      case _: NilShape     => true
      case _: ScalarShape  => true
      case n: NodeShape    => !GraphQLObject(n).isInput
      case arr: ArrayShape => isValidOutputType(arr.items)
      case _               => true
    }
  }
}

object GraphQLUtils {
  def datatype(shape: Shape): Option[String] = {
    shape match {
      case u: UnionShape => // nullable type
        u.anyOf.collectFirst { case s: ScalarShape => s.dataType.value() }
      case s: ScalarShape => Some(s.dataType.value())
      case _              => None
    }
  }

  def isValidInputType(schema: Shape): Boolean = {
    schema match {
      case u: UnionShape   => GraphQLNullable(u).isValidInput
      case n: NodeShape    => GraphQLObject(n).isInput
      case arr: ArrayShape => isValidInputType(arr.items)
      case _               => true
    }
  }

  def isValidOutputType(schema: Shape): Boolean = {
    schema match {
      case u: UnionShape   => GraphQLNullable(u).isValidOutput
      case n: NodeShape    => !GraphQLObject(n).isInput
      case arr: ArrayShape => isValidOutputType(arr.items)
      case _               => true
    }
  }
}
