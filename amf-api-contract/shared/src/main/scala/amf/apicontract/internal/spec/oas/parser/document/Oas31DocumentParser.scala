package amf.apicontract.internal.spec.oas.parser.document

import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.core.internal.parser.Root
import amf.core.internal.remote.Spec

class Oas31DocumentParser(root: Root, spec: Spec = Spec.OAS31)(implicit override val ctx: OasWebApiContext)
    extends Oas3DocumentParser(root, spec) {}

object Oas31DocumentParser {
  def apply(root: Root, spec: Spec = Spec.OAS31)(implicit ctx: OasWebApiContext): Oas31DocumentParser =
    new Oas31DocumentParser(root, spec)
}
