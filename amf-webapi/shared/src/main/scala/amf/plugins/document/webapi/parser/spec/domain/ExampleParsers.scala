package amf.plugins.document.webapi.parser.spec.domain

import amf.core.annotations.{SingleValueArray, SynthesizedField}
import amf.core.model.domain.AmfScalar
import amf.core.parser.{Annotations, _}
import amf.plugins.document.webapi.contexts.WebApiContext
import amf.plugins.document.webapi.parser.spec.common.AnnotationParser
import amf.plugins.domain.shapes.metamodel.ExampleModel
import amf.plugins.domain.shapes.models.Example
import org.yaml.model._
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

case class RamlExamplesParser(map: YMap, singleExampleKey: String, multipleExamplesKey: String)(
    implicit ctx: WebApiContext) {
  def parse(): Seq[Example] =
    RamlMultipleExampleParser(multipleExamplesKey, map).parse() ++
      RamlSingleExampleParser(singleExampleKey, map).parse()

}

case class RamlMultipleExampleParser(key: String, map: YMap)(implicit ctx: WebApiContext) {
  def parse(): Seq[Example] = {
    val examples = ListBuffer[Example]()

    map.key(key).foreach { entry =>
      ctx.link(entry.value) match {
        case Left(s) => examples ++= ctx.declarations.findNamedExample(s).map(e => e.link(s).asInstanceOf[Example])
        case Right(node) =>
          node.tagType match {
            case YType.Map =>
              examples ++= node.as[YMap].entries.map(RamlNamedExampleParser(_).parse())
            case YType.Seq => //example sequence must have a name ??
              RamlExampleValueAsString(node, Example(node), strict = true).populate()
            case _ => RamlExampleValueAsString(node, Example(node.as[YScalar]), strict = true).populate()
          }
      }
    }
    examples
  }
}

case class RamlNamedExampleParser(entry: YMapEntry)(implicit ctx: WebApiContext) {
  def parse(): Example = {
    val name             = ValueNode(entry.key)
    val example: Example = RamlSingleExampleValueParser(entry.value).parse()
    example.set(ExampleModel.Name, name.string(), Annotations(entry))
  }
}

case class RamlSingleExampleParser(key: String, map: YMap)(implicit ctx: WebApiContext) {
  def parse(): Option[Example] = {
    map.key(key).flatMap { entry =>
      entry.value.tagType match {
        case YType.Map => Option(RamlSingleExampleValueParser(entry.value.as[YMap]).parse().add(SingleValueArray()))
        case _ => // example can be any type or scalar value, like string int datetime etc. We will handle all like strings in this stage
          Option(
            RamlExampleValueAsString(entry.value, Example(entry.value), strict = true)
              .populate()
              .add(SingleValueArray()))
      }
    }
  }
}

case class RamlSingleExampleValueParser(node: YNode)(implicit ctx: WebApiContext) {
  def parse(): Example = {
    val example = Example(node)

    node.to[YMap] match {
      case Right(map) if map.regex("""displayName|description|strict|value|\(.+\)""").nonEmpty =>
        map
          .key("displayName")
          .foreach { entry =>
            val value = ValueNode(entry.value)
            example.set(ExampleModel.DisplayName, value.string(), Annotations(entry))
          }

        map
          .key("description")
          .foreach { entry =>
            val value = ValueNode(entry.value)
            example.set(ExampleModel.Description, value.string(), Annotations(entry))
          }

        map
          .key("strict")
          .foreach { entry =>
            val value = ValueNode(entry.value)
            example.set(ExampleModel.Strict, value.boolean(), Annotations(entry))
          }

        map
          .key("value")
          .foreach { entry =>
            RamlExampleValueAsString(entry.value, example, Option(example.strict).getOrElse(true)).populate()
          }

        AnnotationParser(() => example, map).parse()

      case _ =>
        RamlExampleValueAsString(node, example, strict = true).populate()
    }

    example
  }
}

case class RamlExampleValueAsString(node: YNode, example: Example, strict: Boolean)(implicit ctx: WebApiContext) {
  def populate(): Example = {
    if (example.fields.entry(ExampleModel.Strict).isEmpty) {
      example.set(ExampleModel.Strict, AmfScalar(strict), Annotations() += SynthesizedField())
    }
    node.tagType match {
      case YType.Map | YType.Seq =>
        example.set(ExampleModel.Value,
                    AmfScalar(YamlRender.render(node.value), Annotations(node.value)),
                    Annotations(node.value))
      case _ =>
        val scalar = node.as[YScalar]
        example.set(ExampleModel.Value, AmfScalar(scalar.text, Annotations(node.value)), Annotations(node.value))
    }

    example
  }
}
