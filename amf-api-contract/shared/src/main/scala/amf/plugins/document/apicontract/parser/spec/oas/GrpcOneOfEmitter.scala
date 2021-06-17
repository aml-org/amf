package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.internal.plugins.syntax.StringDocBuilder
import amf.shapes.client.scala.model.domain.UnionShape

case class GrpcOneOfEmitter(union: UnionShape, builder: StringDocBuilder, ctx: GrpcEmitterContext) {

}
