package amf.spec.domain

import amf.domain.Annotation.{SingleValueArray, SynthesizedField}
import amf.domain.{Annotations, Example}
import amf.metadata.domain.ExampleModel
import amf.model.AmfScalar
import amf.parser.{YMapOps, YValueOps}
import amf.spec.common.{AnnotationParser, ValueNode}
import amf.spec.{Declarations, ParserContext}
import org.yaml.model._
import org.yaml.render.YamlRender

import scala.collection.mutable.ListBuffer

/**
  *
  */
case class OasResponseExamplesParser(key: String, map: YMap) {
  def parse(): Seq[Example] = {
    val results = ListBuffer[Example]()
    map
      .key(key)
      .foreach(entry => {
        entry.value.value.toMap
          .regex(".*/.*")
          .map(e => results += OasResponseExampleParser(e).parse())
      })

    results
  }
}

case class OasResponseExampleParser(yMapEntry: YMapEntry) {
  def parse(): Example = {
    val example = Example(yMapEntry)
      .set(ExampleModel.MediaType, yMapEntry.key.value.toScalar.text)
      .withStrict(false)
    RamlExampleValueAsString(yMapEntry.value.value, example, strict = false).populate()
  }
}

case class RamlExamplesParser(map: YMap,
                              singleExampleKey: String,
                              multipleExamplesKey: String,
                              declarations: Declarations)(implicit ctx: ParserContext) {
  def parse(): Seq[Example] = {
    val results = ListBuffer[Example]()

    RamlMultipleExampleParser(multipleExamplesKey, map, declarations).parse() ++
      RamlSingleExampleParser(singleExampleKey, map).parse()
  }
}

case class RamlMultipleExampleParser(key: String, map: YMap, declarations: Declarations)(implicit ctx: ParserContext) {
  def parse(): Seq[Example] = {
    val examples = ListBuffer[Example]()

    map.key(key).foreach { entry =>
      ctx.link(entry.value) match {
        case Left(s) => examples ++= declarations.findNamedExample(s).map(e => e.link(s).asInstanceOf[Example])
        case Right(node) =>
          node.value match {
            case map: YMap =>
              examples ++= map.entries.map(RamlNamedExampleParser(_).parse())
            case scalar: YScalar => RamlExampleValueAsString(scalar, Example(scalar), strict = true).populate()
          }
      }
    }
    examples
  }
}

case class RamlNamedExampleParser(entry: YMapEntry) {
  def parse(): Example = {
    val name             = ValueNode(entry.key)
    val example: Example = RamlSingleExampleValueParser(entry.value.value.toMap).parse()
    example.set(ExampleModel.Name, name.string(), Annotations(entry))
  }
}

case class RamlSingleExampleParser(key: String, map: YMap) {
  def parse(): Option[Example] = {
    map.key(key).map { entry =>
      entry.value.value match {
        case map: YMap => RamlSingleExampleValueParser(map).parse().add(SingleValueArray())
        case scalar: YScalar =>
          RamlExampleValueAsString(scalar, Example(scalar), strict = true).populate().add(SingleValueArray())
        case other => throw new IllegalArgumentException("Not supported part type for example")
      }

    }
  }
}

case class RamlSingleExampleValueParser(node: YMap) {
  def parse(): Example = {
    val isExpanded = node.regex("""displayName|description|strict|value|\(.+\)""").nonEmpty

    val example = Example(node)
    if (isExpanded) {
      node
        .key("displayName")
        .foreach(entry => {
          val value = ValueNode(entry.value)
          example.set(ExampleModel.DisplayName, value.string(), Annotations(entry))
        })
      node
        .key("description")
        .foreach(entry => {
          val value = ValueNode(entry.value)
          example.set(ExampleModel.Description, value.string(), Annotations(entry))
        })
      node
        .key("strict")
        .foreach(entry => {
          val value = ValueNode(entry.value)
          example.set(ExampleModel.Strict, value.boolean(), Annotations(entry))
        })

      node
        .key("value")
        .foreach(entry => {
          RamlExampleValueAsString(entry.value.value, example, Option(example.strict).getOrElse(true)).populate()
        })
      AnnotationParser(() => example, node).parse()
    } else {
      RamlExampleValueAsString(node, example, strict = true).populate()
    }

    example
  }
}

case class RamlExampleValueAsString(value: YValue, example: Example, strict: Boolean) {
  def populate(): Example = {
    if (example.fields.entry(ExampleModel.Strict).isEmpty) {
      example.set(ExampleModel.Strict, AmfScalar(strict), Annotations() += SynthesizedField())
    }
    value match {
      case map: YMap =>
        example.set(ExampleModel.Value, AmfScalar(YamlRender.render(value), Annotations(value)), Annotations(value))
      case scalar: YScalar =>
        example.set(ExampleModel.Value, AmfScalar(scalar.text, Annotations(value)), Annotations(value))
      case other => throw new IllegalArgumentException("Not supported part type for example")
    }
  }
}
