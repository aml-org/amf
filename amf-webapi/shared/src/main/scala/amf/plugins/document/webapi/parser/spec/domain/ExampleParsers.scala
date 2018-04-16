package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{LexicalInformation, SynthesizedField}
import amf.core.model.domain.{AmfScalar, DataNode}
import amf.core.parser.{Annotations, ScalarNode, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.RamlTypeDefMatcher.{JSONSchema, XMLSchema}
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, DataNodeParser, SpecParserOps}
import amf.plugins.document.webapi.parser.spec.declaration.ExternalSourceRef
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.models.Example
import amf.plugins.features.validation.ParserSideValidations
import org.yaml.model.YNode.MutRef
import org.yaml.model._
import org.yaml.parser.YamlParser
import org.yaml.render.YamlRender

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class OasResponseExamplesParser(key: String, map: YMap)(implicit ctx: WebApiContext) {
  def parse(): Seq[Example] = {
    val results = ListBuffer[Example]()
    map
      .key(key)
      .foreach(entry => {
        entry.value
          .as[YMap]
          .regex(".*/.*")
          .map(e => results += OasResponseExampleParser(e).parse())
      })

    results
  }
}

case class OasResponseExampleParser(yMapEntry: YMapEntry)(implicit ctx: WebApiContext) {
  def parse(): Example = {
    val example = Example(yMapEntry)
      .set(ExampleModel.MediaType, yMapEntry.key.as[YScalar].text)
    RamlExampleValueAsString(yMapEntry.value, example, ExampleOptions(strictDefault = false, quiet = true)).populate()
  }
}

case class RamlExamplesParser(map: YMap,
                              singleExampleKey: String,
                              multipleExamplesKey: String,
                              parentId: Option[String],
                              producer: Option[String] => Example,
                              options: ExampleOptions)(implicit ctx: WebApiContext) {
  def parse(): Seq[Example] = {
    if (map.key(singleExampleKey).isDefined && map.key(multipleExamplesKey).isDefined && parentId.isDefined) {
      ctx.violation(
        ParserSideValidations.ExclusivePropertiesSpecification.id(),
        parentId.get,
        s"Properties '$singleExampleKey' and '$multipleExamplesKey' are exclusive and cannot be declared together",
        map
      )
    }
    RamlMultipleExampleParser(multipleExamplesKey, map, producer, options).parse() ++
      RamlSingleExampleParser(singleExampleKey, map, producer, options).parse()
  }

}

case class RamlMultipleExampleParser(key: String,
                                     map: YMap,
                                     producer: Option[String] => Example,
                                     options: ExampleOptions)(implicit ctx: WebApiContext) {
  def parse(): Seq[Example] = {
    val examples = ListBuffer[Example]()

    map.key(key).foreach { entry =>
      ctx.link(entry.value) match {
        case Left(s) =>
          examples ++= ctx.declarations.findNamedExample(s).map(e => e.link(s).asInstanceOf[Example])

        case Right(node) =>
          node.tagType match {
            case YType.Map =>
              examples ++= node.as[YMap].entries.map(RamlNamedExampleParser(_, producer, options).parse())
            case YType.Seq => // example sequence must have a name ??
              RamlExampleValueAsString(node, Example(node), options).populate()
            case _ => RamlExampleValueAsString(node, Example(node.as[YScalar]), options).populate()
          }
      }
    }
    examples
  }
}

case class RamlNamedExampleParser(entry: YMapEntry, producer: Option[String] => Example, options: ExampleOptions)(
    implicit ctx: WebApiContext) {
  def parse(): Example = {
    val name           = ScalarNode(entry.key)
    val simpleProducer = () => producer(Some(name.text().toString))
    val example: Example = ctx.link(entry.value) match {
      case Left(s) =>
        ctx.declarations
          .findNamedExample(s)
          .map(e => e.link(s).asInstanceOf[Example])
          .getOrElse(RamlSingleExampleValueParser(entry.value, simpleProducer, options).parse())
      case Right(_) => RamlSingleExampleValueParser(entry.value, simpleProducer, options).parse()
    }
    example.set(ExampleModel.Name, name.string(), Annotations(entry))
  }
}

case class RamlSingleExampleParser(key: String,
                                   map: YMap,
                                   producer: Option[String] => Example,
                                   options: ExampleOptions)(implicit ctx: WebApiContext) {
  def parse(): Option[Example] = {
    val newProducer = () => producer(None)
    map.key(key).flatMap { entry =>
      entry.value.tagType match {
        case YType.Map =>
          Option(RamlSingleExampleValueParser(entry.value.as[YMap], newProducer, options).parse())
        case _ => // example can be any type or scalar value, like string int datetime etc. We will handle all like strings in this stage
          Option(
            RamlExampleValueAsString(entry.value, newProducer().add(Annotations(entry.value)), options)
              .populate())
      }
    }
  }
}

case class RamlSingleExampleValueParser(node: YNode, producer: () => Example, options: ExampleOptions)(
    implicit ctx: WebApiContext)
    extends SpecParserOps {
  def parse(): Example = {
    val example = producer().add(Annotations(node))

    node.to[YMap] match {
      case Right(map) if map.regex("""displayName|description|strict|value|\(.+\)""").nonEmpty =>
        map.key("displayName", (ExampleModel.DisplayName in example).allowingAnnotations)
        map.key("description", (ExampleModel.Description in example).allowingAnnotations)
        map.key("strict", (ExampleModel.Strict in example).allowingAnnotations)

        map
          .key("value")
          .foreach { entry =>
            RamlExampleValueAsString(entry.value, example, options).populate()
          }

        AnnotationParser(example, map).parse()

      case _ =>
        RamlExampleValueAsString(node, example, options).populate()
    }

    example
  }
}

case class RamlExampleValueAsString(node: YNode, example: Example, options: ExampleOptions)(
    implicit ctx: WebApiContext)
    extends ExternalSourceRef {
  def populate(): Example = {
    if (example.fields.entry(ExampleModel.Strict).isEmpty) {
      example.set(ExampleModel.Strict, AmfScalar(options.strictDefault), Annotations() += SynthesizedField())
    }

    val targetNode = node match {
      case mut: MutRef =>
        sourceRefAnnotation(node, example, ctx)
        mut.target.getOrElse(node)
      case _ =>
        node.toOption[YScalar] match {
          case Some(scalar) =>
            example.set(ExampleModel.Value, AmfScalar(scalar.text, Annotations(node.value)), Annotations(node.value))
          case _ =>
            example.set(ExampleModel.Value,
                        AmfScalar(YamlRender.render(node), Annotations(node.value)),
                        Annotations(node.value))

        }
        node
    }

    val result = NodeDataNodeParser(targetNode, example.id, options.quiet).parse()

    result.dataNode.foreach { dataNode =>
      example.set(ExampleModel.StructuredValue, dataNode, Annotations(node))
    }

    example
  }
}

case class NodeDataNodeParser(node: YNode, parentId: String, quiet: Boolean)(implicit ctx: WebApiContext) {
  def parse(): DataNodeParserResult = {
    val errorHandler = if (quiet) WarningOnlyHandler(ctx.rootContextDocument) else ctx

    val exampleNode: Option[YNode] = node.toOption[YScalar] match {
      case Some(scalar) if JSONSchema.unapply(scalar.text).isDefined =>
        node
          .toOption[YScalar]
          .flatMap { scalar =>
            YamlParser(scalar.text)(errorHandler).parse(true).collectFirst({ case doc: YDocument => doc.node })
          }
      case Some(scalar) if XMLSchema.unapply(scalar.text).isDefined => None
      case _                                                        => Some(node) // return default node for xml too.
    }

    errorHandler match {
      case wh: WarningOnlyHandler if wh.hasRegister => DataNodeParserResult(exampleNode, None)
      case _ =>
        val dataNode = exampleNode.map { ex =>
          val dataNode = DataNodeParser(ex, parent = Some(parentId)).parse()
          dataNode.annotations.reject(_.isInstanceOf[LexicalInformation])
          dataNode.annotations += LexicalInformation(Range(node.value.range))
          dataNode
        }
        DataNodeParserResult(exampleNode, dataNode)
    }
  }
}

case class DataNodeParserResult(exampleNode: Option[YNode], dataNode: Option[DataNode]) {}

case class ExampleOptions(strictDefault: Boolean, quiet: Boolean)

object DefaultExampleOptions extends ExampleOptions(true, false)
