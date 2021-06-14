package amf.plugins.document.apicontract.parser.spec.domain
import amf.core.internal.parser.YMapOps
import amf.plugins.document.apicontract.contexts.parser.oas.OasWebApiContext
import amf.plugins.domain.apicontract.models.Parameter
import amf.validations.ParserSideValidations.ServerVariableMissingDefault
import org.yaml.model.{YMap, YMapEntry}

case class OasServerVariableParser(entry: YMapEntry, parent: String)(implicit override val ctx: OasWebApiContext)
    extends OasLikeServerVariableParser(entry, parent)(ctx) {

  override protected def parseMap(variable: Parameter, map: YMap): Unit = {
    requiredDefaultField(variable, map)
    super.parseMap(variable, map)
  }
  private def requiredDefaultField(variable: Parameter, map: YMap): Unit =
    if (map.key("default").isEmpty)
      ctx.eh.violation(ServerVariableMissingDefault, variable.id, "Server variable must define a 'default' field", map)
}
