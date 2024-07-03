package amf.shapes.client.scala.model.domain

import amf.core.client.scala.model.domain.Shape
import amf.core.client.scala.model.{IntField, StrField}
import amf.shapes.internal.domain.metamodel.AnyShapeModel._

protected[amf] trait AvroShapeFields { self: Shape =>

  def namespace: StrField    = fields.field(AvroNamespace)
  def aliases: Seq[StrField] = fields.field(Aliases)
  def size: IntField         = fields.field(Size)

  def withNamespace(namespace: String): this.type  = set(AvroNamespace, namespace)
  def withAliases(aliases: Seq[String]): this.type = set(Aliases, aliases)
  def withSize(size: Int): this.type               = set(Size, size)

}
