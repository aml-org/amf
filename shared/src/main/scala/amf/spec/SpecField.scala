package amf.spec

import amf.metadata.Type.{Array, Str}
import amf.metadata.domain.{CreativeWorkModel, LicenseModel, OrganizationModel}
import amf.metadata.{Field, Type}
import amf.spec.FieldEmitter.{ObjectEmitter, SpecFieldEmitter, StringListValueEmitter, StringValueEmitter}
import amf.spec.FieldParser.{ObjectParser, SpecFieldParser, StringListParser, StringValueParser}
import amf.spec.Matcher.{KeyMatcher, Matcher}

/**
  * Spec field
  */
protected case class SpecField(fields: List[Field],
                               matcher: Matcher,
                               parser: SpecFieldParser,
                               emitter: SpecFieldEmitter,
                               children: List[SpecField] = Nil) {

  def ->(specs: SpecField*): SpecField = copy(children = specs.toList)
}

protected case class SpecNode(symbol: Symbol) {

  def ~(field: Field): SpecField = createSpecField(List(field))

  def ~(fields: List[Field]): SpecField = createSpecField(fields)

  private def createSpecField(fields: List[Field]) = {
    fields.head.`type` match {
      case Str        => SpecField(fields, matcher(), StringValueParser, StringValueEmitter)
      case Array(Str) => SpecField(fields, matcher(), StringListParser, StringListValueEmitter)
      case OrganizationModel | CreativeWorkModel | LicenseModel =>
        SpecField(fields, matcher(), ObjectParser, ObjectEmitter)
    }
  }

  private def matcher() = KeyMatcher(symbol.name)
}

protected case class FieldLike(field: Field) {

  def |(other: Field): List[Field] = List(field, other)

}
