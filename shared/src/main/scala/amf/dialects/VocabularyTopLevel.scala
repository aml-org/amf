package amf.dialects
import amf.dialects._
import amf.model.AmfScalar
import amf.domain.dialects.DomainEntity
object RAML_1_0_VocabularyTopLevel {
  case class VocabularyObject(entity: DomainEntity = DomainEntity(Vocabulary),
                              override val parent: Option[TopLevelObject] = None)
      extends TopLevelObject(entity, parent) {
    def base(): Option[String]                    = entity.string(Vocabulary.base)
    def withBase(value: String): VocabularyObject = { entity.set(Vocabulary.base.field(), AmfScalar(value)); this }
    def vocabulary(): Option[String]              = entity.string(Vocabulary.vocabularyProperty)
    def withVocabulary(value: String): VocabularyObject = {
      entity.set(Vocabulary.vocabularyProperty.field(), AmfScalar(value)); this
    }
    def version(): Option[String] = entity.string(Vocabulary.version)
    def withVersion(value: String): VocabularyObject = {
      entity.set(Vocabulary.version.field(), AmfScalar(value)); this
    }
    def usage(): Option[String]                    = entity.string(Vocabulary.usage)
    def withUsage(value: String): VocabularyObject = { entity.set(Vocabulary.usage.field(), AmfScalar(value)); this }
    def external(): Seq[ExternalObject]            = entity.entities(Vocabulary.externals).map(ExternalObject(_, Some(this)))
    def withExternal(value: ExternalObject): VocabularyObject = {
      entity.add(Vocabulary.externals.field(), value.entity); this
    }
    def classTerms(): Seq[ClassObject] = entity.entities(Vocabulary.classTerms).map(ClassObject(_, Some(this)))
    def withClassTerms(value: ClassObject): VocabularyObject = {
      entity.add(Vocabulary.classTerms.field(), value.entity); this
    }
    def propertyTerms(): Seq[PropertyObject] =
      entity.entities(Vocabulary.propertyTerms).map(PropertyObject(_, Some(this)))
    def withPropertyTerms(value: PropertyObject): VocabularyObject = {
      entity.add(Vocabulary.propertyTerms.field(), value.entity); this
    }
    def externalTerms(): Seq[PropertyObject] =
      entity.entities(Vocabulary.externalTerms).map(PropertyObject(_, Some(this)))
    def withExternalTerms(value: PropertyObject): VocabularyObject = {
      entity.add(Vocabulary.externalTerms.field(), value.entity); this
    }
  }

  case class ExternalObject(entity: DomainEntity = DomainEntity(External),
                            override val parent: Option[TopLevelObject] = None)
      extends TopLevelObject(entity, parent) {
    def name(): Option[String]                  = entity.string(External.name)
    def withName(value: String): ExternalObject = { entity.set(External.name.field(), AmfScalar(value)); this }
    def uri(): Option[String]                   = entity.string(External.uri)
    def withUri(value: String): ExternalObject  = { entity.set(External.uri.field(), AmfScalar(value)); this }
  }

  case class ClassObject(entity: DomainEntity = DomainEntity(ClassTerm),
                         override val parent: Option[TopLevelObject] = None)
      extends TopLevelObject(entity, parent) {
    def id(): Option[String]               = entity.string(ClassTerm.idProperty)
    def withId(value: String): ClassObject = { entity.set(ClassTerm.idProperty.field(), AmfScalar(value)); this }
    def displayName(): Option[String]      = entity.string(ClassTerm.displayName)
    def withDisplayName(value: String): ClassObject = {
      entity.set(ClassTerm.displayName.field(), AmfScalar(value)); this
    }
    def description(): Option[String] = entity.string(ClassTerm.description)
    def withDescription(value: String): ClassObject = {
      entity.set(ClassTerm.description.field(), AmfScalar(value)); this
    }
    def example(): Seq[String]                  = entity.strings(ClassTerm.example)
    def withExample(value: String): ClassObject = { entity.add(ClassTerm.example.field(), AmfScalar(value)); this }
    def `extends`(): Seq[String]                = entity.strings(ClassTerm.`extends`)
    def withExtends(value: String): ClassObject = { entity.add(ClassTerm.`extends`.field(), AmfScalar(value)); this }
    def resolvedExtends(): List[Option[ClassObject]] =
      resolveReferences2Options(ClassTerm.`extends`, (r, s) => {
        r.asInstanceOf[VocabularyObject].classTerms.find(_.entity.id == s)
      }, e => ClassObject(e, Some(this)))
    def properties(): Seq[String] = entity.strings(ClassTerm.properties)
    def withProperties(value: String): ClassObject = {
      entity.add(ClassTerm.properties.field(), AmfScalar(value)); this
    }
  }

  case class PropertyObject(entity: DomainEntity = DomainEntity(PropertyTerm),
                            override val parent: Option[TopLevelObject] = None)
      extends TopLevelObject(entity, parent) {
    def id(): Option[String]                  = entity.string(PropertyTerm.idProperty)
    def withId(value: String): PropertyObject = { entity.set(PropertyTerm.idProperty.field(), AmfScalar(value)); this }
    def displayName(): Option[String]         = entity.string(PropertyTerm.displayName)
    def withDisplayName(value: String): PropertyObject = {
      entity.set(PropertyTerm.displayName.field(), AmfScalar(value)); this
    }
    def description(): Option[String] = entity.string(PropertyTerm.description)
    def withDescription(value: String): PropertyObject = {
      entity.set(PropertyTerm.description.field(), AmfScalar(value)); this
    }
    def example(): Seq[String] = entity.strings(PropertyTerm.example)
    def withExample(value: String): PropertyObject = {
      entity.add(PropertyTerm.example.field(), AmfScalar(value)); this
    }
    def domain(): Seq[String]                     = entity.strings(PropertyTerm.domain)
    def withDomain(value: String): PropertyObject = { entity.add(PropertyTerm.domain.field(), AmfScalar(value)); this }
    def resolvedDomain(): List[Option[ClassObject]] =
      resolveReferences2Options(PropertyTerm.domain, (r, s) => {
        r.asInstanceOf[VocabularyObject].classTerms.find(_.entity.id == s)
      }, e => ClassObject(e, Some(this)))
    def range(): Seq[String]                     = entity.strings(PropertyTerm.range)
    def withRange(value: String): PropertyObject = { entity.add(PropertyTerm.range.field(), AmfScalar(value)); this }
    def resolvedRange(): List[Option[ClassObject]] =
      resolveReferences2Options(PropertyTerm.range, (r, s) => {
        r.asInstanceOf[VocabularyObject].classTerms.find(_.entity.id == s)
      }, e => ClassObject(e, Some(this)))
    def `extends`(): Seq[String] = entity.strings(PropertyTerm.`extends`)
    def withExtends(value: String): PropertyObject = {
      entity.add(PropertyTerm.`extends`.field(), AmfScalar(value)); this
    }
  }

}
