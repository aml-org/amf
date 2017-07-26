package amf.spec

import amf.metadata.Field
import amf.metadata.Type.{Array, Bool, Str}
import amf.metadata.domain._
import amf.remote.{Amf, Vendor}
import amf.spec.FieldEmitter._
import amf.spec.FieldParser._
import amf.spec.Matcher.{KeyMatcher, Matcher, RegExpMatcher}

/**
  * Spec field
  */
protected case class SpecField(fields: List[Field],
                               matcher: Matcher,
                               parser: SpecFieldParser,
                               emitter: SpecFieldEmitter,
                               children: List[SpecField] = Nil,
                               vendor: Vendor = Amf) {

  def ->(specs: SpecField*): SpecField      = copy(children = specs.toList)
  def ->(specs: List[SpecField]): SpecField = copy(children = specs)
}

protected trait SpecNode {

  def ~(field: Field): SpecField = createSpecField(List(field))

  def ~(fields: List[Field]): SpecField = createSpecField(fields)

  private def createSpecField(fields: List[Field]) = {
    fields.head.`type` match {
      case Str        => SpecField(fields, matcher(), StringValueParser, StringValueEmitter)
      case Bool       => SpecField(fields, matcher(), BoolValueParser, BooleanValueEmitter)
      case Array(Str) => SpecField(fields, matcher(), StringListParser, StringListEmitter)
      case OrganizationModel | CreativeWorkModel | LicenseModel =>
        SpecField(fields, matcher(), ObjectParser, ObjectEmitter)
      case Array(EndPointModel) =>
        SpecField(fields, matcher(), EndPointParser, EndPointEmitter)
      case Array(OperationModel) =>
        SpecField(fields, matcher(), OperationParser, OperationEmitter)
      case Array(ParameterModel) =>
        SpecField(fields, matcher(), ParametersParser, null)
      case Array(PayloadModel) =>
        SpecField(fields, matcher(), PayloadsParser, null)
      case Array(ResponseModel) =>
        SpecField(fields, matcher(), ResponseParser, null)
    }
  }

  def matcher(): Matcher
}

protected case class SpecRegexNode(regex: String) extends SpecNode {
  override def matcher(): Matcher = RegExpMatcher(regex)
}

protected case class SpecKeyNode(symbol: Symbol) extends SpecNode {
  override def matcher(): Matcher = KeyMatcher(symbol.name)

  /** Virtual node (no mapping to field). */
  def ->(specs: SpecField*): SpecField = SpecField(
    Nil,
    matcher(),
    ChildrenParser(),
    null,
    specs.toList
  )
}

protected case class FieldLike(field: Field) {
  def |(other: Field): List[Field] = List(field, other)
}
