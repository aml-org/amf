package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.SynthesizedField
import amf.core.model.domain.{AmfScalar, DataNode}
import amf.core.parser.{Annotations, ScalarNode, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.RamlTypeDefMatcher.{JSONSchema, XMLSchema}
import amf.plugins.document.webapi.parser.spec.common.{AnnotationParser, DataNodeParser, SpecParserOps}
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.models.Example
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
      .withStrict(false)
    RamlExampleValueAsString(yMapEntry.value, example, strict = false).populate()
  }
}

case class RamlExamplesParser(map: YMap,
                              singleExampleKey: String,
                              multipleExamplesKey: String,
                              producer: Option[String] => Example,
                              strictDefault: Boolean)(implicit ctx: WebApiContext) {
  def parse(): Seq[Example] =
    RamlMultipleExampleParser(multipleExamplesKey, map, producer, strictDefault).parse() ++
      RamlSingleExampleParser(singleExampleKey, map, producer, strictDefault).parse()

}

case class RamlMultipleExampleParser(key: String, map: YMap, producer: Option[String] => Example, strictDefault: Boolean)(
    implicit ctx: WebApiContext) {
  def parse(): Seq[Example] = {
    val examples = ListBuffer[Example]()

    map.key(key).foreach { entry =>
      ctx.link(entry.value) match {
        case Left(s) =>
          examples ++= ctx.declarations.findNamedExample(s).map(e => e.link(s).asInstanceOf[Example])

        case Right(node) =>
          node.tagType match {
            case YType.Map =>
              examples ++= node.as[YMap].entries.map(RamlNamedExampleParser(_, producer, strictDefault).parse())
            case YType.Seq => // example sequence must have a name ??
              RamlExampleValueAsString(node, Example(node), strict = strictDefault).populate()
            case _ => RamlExampleValueAsString(node, Example(node.as[YScalar]), strict = strictDefault).populate()
          }
      }
    }
    examples
  }
}

case class RamlNamedExampleParser(entry: YMapEntry, producer: Option[String] => Example, strictDefault: Boolean)(implicit ctx: WebApiContext) {
  def parse(): Example = {
    val name           = ScalarNode(entry.key)
    val simpleProducer = () => producer(Some(name.text().toString))
    val example: Example = ctx.link(entry.value) match {
      case Left(s) =>
        ctx.declarations
          .findNamedExample(s)
          .map(e => e.link(s).asInstanceOf[Example])
          .getOrElse(RamlSingleExampleValueParser(entry.value, simpleProducer, strictDefault).parse())
      case Right(_) => RamlSingleExampleValueParser(entry.value, simpleProducer, strictDefault).parse()
    }
    example.set(ExampleModel.Name, name.string(), Annotations(entry))
  }
}

case class RamlSingleExampleParser(key: String, map: YMap, producer: Option[String] => Example, strictDefault: Boolean)(
    implicit ctx: WebApiContext) {
  def parse(): Option[Example] = {
    val newProducer = () => producer(None)
    map.key(key).flatMap { entry =>
      entry.value.tagType match {
        case YType.Map =>
          Option(RamlSingleExampleValueParser(entry.value.as[YMap], newProducer, strictDefault).parse())
        case _ => // example can be any type or scalar value, like string int datetime etc. We will handle all like strings in this stage
          Option(
            RamlExampleValueAsString(entry.value, newProducer().add(Annotations(entry.value)), strict = strictDefault)
              .populate())
      }
    }
  }
}

case class RamlSingleExampleValueParser(node: YNode, producer: () => Example, strictDefault: Boolean)(implicit ctx: WebApiContext)
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
            RamlExampleValueAsString(entry.value, example, example.strict.option().getOrElse(strictDefault)).populate()
          }

        AnnotationParser(example, map).parse()

      case _ =>
        RamlExampleValueAsString(node, example, strict = strictDefault).populate()
    }

    example
  }
}

case class RamlExampleValueAsString(node: YNode, example: Example, strict: Boolean)(implicit ctx: WebApiContext) {
  def populate(): Example = {
    if (example.fields.entry(ExampleModel.Strict).isEmpty) {
      example.set(ExampleModel.Strict, AmfScalar(strict), Annotations() += SynthesizedField())
    }

    val (exampleNode, dataNode) = NodeDataNodeParser(node, example.id).parse()

    example.set(ExampleModel.StructuredValue, dataNode, Annotations(exampleNode))

    example.set(ExampleModel.Value,
                AmfScalar(YamlRender.render(exampleNode), Annotations(node.value)),
                Annotations(node.value))

    example
  }
}

case class NodeDataNodeParser(node: YNode, parentId: String)(implicit ctx: WebApiContext) {
  def parse(): (YNode, DataNode) = {

    val exampleNode = node.toOption[YScalar] match {
      case Some(scalar) if JSONSchema.unapply(scalar.text).isDefined || XMLSchema.unapply(scalar.text).isDefined =>
        node
          .toOption[YScalar]
          .flatMap { scalar =>
            YamlParser(scalar.text).parse(true).collectFirst({ case doc: YDocument => doc.node })
          }
          .getOrElse(node)
      case _ => node
    }

    val dataNode = DataNodeParser(exampleNode, parent = Some(parentId)).parse()
    dataNode.annotations ++= Annotations(exampleNode)
    (exampleNode, dataNode)
  }
}
