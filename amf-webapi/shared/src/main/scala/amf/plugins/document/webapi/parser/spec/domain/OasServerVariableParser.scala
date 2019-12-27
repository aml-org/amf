package amf.plugins.document.webapi.parser.spec.domain
import amf.plugins.document.webapi.contexts.parser.oas.OasWebApiContext
import org.yaml.model.{YMapEntry, YMap}
import amf.plugins.domain.webapi.models.{Server, Parameter}
import amf.validations.ParserSideValidations.ServerVariableMissingDefault
import amf.core.parser.YMapOps

case class OasServerVariableParser(entry: YMapEntry, server: Server)(implicit override val ctx: OasWebApiContext)
    extends OasLikeServerVariableParser(entry, server) {

  override protected def parseMap(variable: Parameter, map: YMap): Unit = {
    requiredDefaultField(variable, map)
    super.parseMap(variable, map)
  }
  private def requiredDefaultField(variable: Parameter, map: YMap): Unit =
    if (map.key("default").isEmpty)
      ctx.eh.violation(ServerVariableMissingDefault, variable.id, "Server variable must define a 'default' field", map)
}
