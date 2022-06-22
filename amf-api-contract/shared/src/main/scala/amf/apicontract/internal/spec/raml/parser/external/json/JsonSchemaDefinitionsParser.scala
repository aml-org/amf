package amf.apicontract.internal.spec.raml.parser.external.json

import amf.apicontract.internal.spec.raml.parser.context.RamlWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.JsonSchemaFragmentNotFound
import amf.core.internal.annotations.ExternalFragmentRef
import amf.shapes.client.scala.model.domain.AnyShape
import amf.shapes.internal.spec.raml.parser.external.ValueAndOrigin
import org.yaml.model.YNode

object JsonSchemaDefinitionsParser {

  def parse(
      key: YNode,
      origin: ValueAndOrigin,
      basePath: String,
      localPath: Option[String],
      normalizedLocalPath: Option[String]
  )(implicit ctx: RamlWebApiContext): AnyShape = {
    RamlExternalOasLibParser(ctx, origin.text, origin.valueAST, basePath).parse()
    val shape = ctx.declarations.findInExternalsLibs(basePath, normalizedLocalPath.get) match {
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
    ctx.declarations.fragments
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
