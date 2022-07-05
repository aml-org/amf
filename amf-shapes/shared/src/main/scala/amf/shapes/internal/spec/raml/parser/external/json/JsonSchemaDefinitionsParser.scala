package amf.shapes.internal.spec.raml.parser.external.json

import amf.core.internal.annotations.ExternalFragmentRef
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.ShapeParserContext
import amf.shapes.internal.spec.raml.parser.external.ValueAndOrigin
import amf.shapes.internal.validation.definitions.ShapeParserSideValidations.JsonSchemaFragmentNotFound
import org.yaml.model.YNode

object JsonSchemaDefinitionsParser {

  def parse(
      key: YNode,
      origin: ValueAndOrigin,
      basePath: String,
      localPath: Option[String],
      normalizedLocalPath: Option[String]
  )(implicit ctx: ShapeParserContext): AnyShape = {
    RamlExternalOasLibParser(ctx, origin.text, origin.valueAST, basePath).parse()
    val shape = ctx.findInExternalsLibs(basePath, normalizedLocalPath.get) match {
      case Some(s) =>
        s.copyShape().withName(key.as[String])
      case _ =>
        val empty = AnyShape()
        ctx.eh.violation(
          JsonSchemaFragmentNotFound,
          empty,
          s"could not find json schema fragment ${localPath.get} in file $basePath",
          origin.valueAST.location
        )
        empty

    }
    ctx.fragments
      .get(basePath)
      .foreach(e =>
        shape.callAfterAdoption { () =>
          shape.withReference(e.encoded.id + localPath.get)
        }
      )

    shape.annotations += ExternalFragmentRef(localPath.get)
    shape
  }
}
