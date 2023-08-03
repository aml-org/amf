package amf.apicontract.internal.spec.oas.parser.document

import amf.apicontract.internal.spec.oas.parser.context.OasWebApiContext
import amf.core.internal.parser.Root
import amf.core.internal.remote.{AwsOas30, Spec}

class AwsOas3DocumentParser(override val root: Root)(implicit override val ctx: OasWebApiContext)
    extends Oas3DocumentParser(root) {

  override val spec: Spec = AwsOas30

}
