package amf.apicontract.internal.spec.oas.parser.domain

import amf.apicontract.client.scala.model.domain.Parameter
import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.apicontract.internal.validation.definitions.ParserSideValidations.ServerVariableMissingDefault
import amf.core.internal.parser.YMapOps
import org.yaml.model.{YMap, YMapEntry}

case class OasServerVariableParser(entry: YMapEntry, parent: String)(implicit override val ctx: OasWebApiContext)
    extends OasLikeServerVariableParser(entry, parent)(ctx) {

  override protected def parseMap(variable: Parameter, map: YMap): Unit = {
    requiredDefaultField(variable, map)
    super.parseMap(variable, map)
  }
  private def requiredDefaultField(variable: Parameter, map: YMap): Unit =
    if (map.key("default").isEmpty)
      ctx.eh.violation(ServerVariableMissingDefault, variable.id, "Server variable must define a 'default' field", map.location)
}
