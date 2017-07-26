package amf.spec

import amf.builder._
import amf.common.Strings.strings
import amf.domain.Annotation.{LexicalInformation, ParentEndPoint}
import amf.domain.{Annotation, EndPoint}
import amf.metadata.Type
import amf.metadata.domain.EndPointModel.Path
import amf.metadata.domain.ParameterModel.Required
import amf.metadata.domain.WebApiModel.EndPoints
import amf.metadata.domain._
import amf.parser.ASTNode
import amf.remote.{Oas, Raml, Vendor}
import amf.spec.Matcher.RegExpMatcher
import amf.spec.Spec.RequestSpec

import scala.collection.mutable.ListBuffer

/**
  * Spec parsers.
  */
object FieldParser {

  trait SpecFieldParser {
    def parse(spec: SpecField, node: ASTNode[_], builder: Builder): Unit

    def apply(spec: SpecField, node: ASTNode[_], builder: Builder): Unit = parse(spec, node, builder)
  }

  trait ValueParser[T] extends SpecFieldParser {
    def value(content: String): T

    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder): Unit = {
      spec.fields.foreach(builder.set(_, value(entry.last.content.unquote), annotations(entry)))
    }
  }

  object StringValueParser extends ValueParser[String] {
    override def value(content: String): String = content
  }

  object BoolValueParser extends ValueParser[Boolean] {
    override def value(content: String): Boolean = content.toBoolean
  }

  object StringListParser extends SpecFieldParser {

    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder): Unit =
      spec.fields.foreach(builder.set(_, entry.last.children.map(c => c.content.unquote), annotations(entry)))
  }

  case class ChildrenParser() extends SpecFieldParser {
    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder): Unit =
      parseMap(spec, entry.last, builder)

    protected def parseMap(spec: SpecField, mapNode: ASTNode[_], builder: Builder): Unit = {
      mapNode.children.foreach(entry => {
        spec.children
          .map(_.copy(vendor = spec.vendor)) //TODO copying parent vendor to children here...
          .find(_.matcher.matches(entry)) match {
          case Some(field) => field.parser(field, entry, builder)
          case _           => // Unknown node...
        }
      })
    }
  }

  object ParametersParser extends ChildrenParser {
    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder): Unit = {
      def parseParametersFromEntries() = {
        entry.last.children.foreach(paramEntry => {
          val param = ParameterBuilder()
          val name  = paramEntry.head.content.unquote
          param set (Required, !name.endsWith("?"))
          param set (ParameterModel.Name, name)

          super.parse(spec, paramEntry, param)

          builder add (spec.fields.head, List(param.build), annotations(paramEntry))
        })
      }

      spec.vendor match {
        case Raml                                                  => parseParametersFromEntries()
        case Oas if spec.fields.head.equals(ResponseModel.Headers) => parseParametersFromEntries()
        case Oas =>
          entry.last.children.foreach(paramMap => {
            val b = ParameterBuilder()

            super.parseMap(spec, paramMap, b)

            val param = b.build

            val field =
              if (spec.fields.size == 1) spec.fields.head
              else if (param.binding == "header") RequestModel.Headers
              else RequestModel.QueryParameters

            builder add (field, List(param), annotations(paramMap))
          })
      }
    }
  }

  object ResponseParser extends ChildrenParser {
    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder): Unit = {
      val statusCode = entry.head.content.unquote

      val response = ResponseBuilder()
        .set(ResponseModel.Name, statusCode, annotations(entry.head))
        .set(
          ResponseModel.StatusCode,
          if (statusCode == "default") "200" else statusCode,
          annotations(entry.head)
        )

      super.parse(spec, entry, response)

      builder add (spec.fields.head, List(response.build), annotations(entry))
    }
  }

  object PayloadsParser extends ChildrenParser {
    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder): Unit = {}
  }

  object EndPointParser extends ChildrenParser {
    override def parse(spec: SpecField, node: ASTNode[_], builder: Builder): Unit = {
      val result: ListBuffer[EndPoint] = ListBuffer()
      parse(spec, node, None, result)
      builder.add(EndPoints, result.toList)
    }

    private def parse(spec: SpecField,
                      node: ASTNode[_],
                      parent: Option[EndPoint],
                      collector: ListBuffer[EndPoint]): Unit = {

      val annotation = annotations(node.head)
      val endpoint = EndPointBuilder().set(
        Path,
        parent.map(_.path).getOrElse("") + node.head.content.unquote,
        if (parent.isDefined) annotation :+ ParentEndPoint(parent.get) else annotation)
      super.parse(spec, node, endpoint)

      val actual = endpoint.build
      collector += actual

      node.last.children
        .filter(RegExpMatcher("/.*").matches)
        .foreach(parse(spec, _, Some(actual), collector))
    }
  }

  object OperationParser extends ChildrenParser {

    def setRequest(op: OperationBuilder, entry: ASTNode[_], vendor: Vendor): Unit = {
      val req: RequestBuilder = RequestBuilder()
      var add                 = false
      entry.last.children.foreach(e => {
        RequestSpec(vendor).fields.find(_.matcher.matches(e)) match {
          case Some(field) =>
            add = true
            field.parser(field, e, req)
          case _ => // Unknown node...
        }
      })

      if (add) op set (OperationModel.Request, req.build, annotations(entry))
    }

    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder): Unit = {
      val op: OperationBuilder = operationBuilder(spec, entry)
      setRequest(op, entry, spec.vendor)

      builder add (EndPointModel.Operations, List(op.build))
    }

    private def operationBuilder(spec: SpecField, entry: ASTNode[_]) = {
      val op = OperationBuilder().set(OperationModel.Method, entry.head.content.unquote, annotations(entry))
      super.parse(spec, entry, op)
      op
    }
  }

  object ObjectParser extends ChildrenParser {

    def builderForType(t: Type): Builder = t match {
      case OrganizationModel => OrganizationBuilder()
      case LicenseModel      => LicenseBuilder()
      case CreativeWorkModel => CreativeWorkBuilder()
    }

    override def parse(spec: SpecField, node: ASTNode[_], builder: Builder): Unit = {
      val field                 = spec.fields.head
      val innerBuilder: Builder = builderForType(field.`type`)
      super.parse(spec, node, innerBuilder)
      builder.set(field, innerBuilder.build)
    }
  }

  def annotations(node: ASTNode[_]): List[Annotation] = {
    List(LexicalInformation(node.range))
  }
}
