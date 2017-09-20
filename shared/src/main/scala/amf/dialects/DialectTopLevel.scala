package amf.dialects
import amf.dialects._;
import amf.model.AmfScalar;
object RAML_1_0_DialectTopLevel {
case class dialectObject(val entity: DomainEntity=DomainEntity(DialectDefinition)){
  def dialect():Option[String]= entity.string(DialectDefinition.dialectProperty)
  def withDialect(value:String):dialectObject= {entity.set(DialectDefinition.dialectProperty.field , AmfScalar(value)); this}
  def version():Option[String]= entity.string(DialectDefinition.version)
  def withVersion(value:String):dialectObject= {entity.set(DialectDefinition.version.field , AmfScalar(value)); this}
  def usage():Option[String]= entity.string(DialectDefinition.usage)
  def withUsage(value:String):dialectObject= {entity.set(DialectDefinition.usage.field , AmfScalar(value)); this}
  def vocabularies():Seq[ExternalObject]= entity.entities(DialectDefinition.vocabularies).map(ExternalObject(_))
  def withVocabularies(value:ExternalObject):dialectObject= {entity.add(DialectDefinition.vocabularies.field , value.entity); this}
  def external():Seq[ExternalObject]= entity.entities(DialectDefinition.externals).map(ExternalObject(_))
  def withExternal(value:ExternalObject):dialectObject= {entity.add(DialectDefinition.externals.field , value.entity); this}
  def nodeMappings():Seq[NodeDefinitionObject]= entity.entities(DialectDefinition.nodeMappings).map(NodeDefinitionObject(_))
  def withNodeMappings(value:NodeDefinitionObject):dialectObject= {entity.add(DialectDefinition.nodeMappings.field , value.entity); this}
  def raml():Option[DocumentObject]= entity.entity(DialectDefinition.raml).map(DocumentObject(_))
  def withRaml(value:DocumentObject):dialectObject= {entity.set(DialectDefinition.raml.field , value.entity); this}
}

case class ExternalObject(val entity: DomainEntity=DomainEntity(External)){
  def name():Option[String]= entity.string(External.name)
  def withName(value:String):ExternalObject= {entity.set(External.name.field , AmfScalar(value)); this}
  def uri():Option[String]= entity.string(External.uri)
  def withUri(value:String):ExternalObject= {entity.set(External.uri.field , AmfScalar(value)); this}
}

case class NodeDefinitionObject(val entity: DomainEntity=DomainEntity(NodeDefinition)){
  def name():Option[String]= entity.string(NodeDefinition.name)
  def withName(value:String):NodeDefinitionObject= {entity.set(NodeDefinition.name.field , AmfScalar(value)); this}
  def classTerm():Option[String]= entity.string(NodeDefinition.classTerm)
  def withClassTerm(value:String):NodeDefinitionObject= {entity.set(NodeDefinition.classTerm.field , AmfScalar(value)); this}
  def mapping():Seq[PropertyMappingObject]= entity.entities(NodeDefinition.mapping).map(PropertyMappingObject(_))
  def withMapping(value:PropertyMappingObject):NodeDefinitionObject= {entity.add(NodeDefinition.mapping.field , value.entity); this}
  def classTermMap():Seq[ClassObject]= entity.entities(NodeDefinition.classTermMap).map(ClassObject(_))
  def withClassTermMap(value:ClassObject):NodeDefinitionObject= {entity.add(NodeDefinition.classTermMap.field , value.entity); this}
  def is():Option[String]= entity.string(NodeDefinition.traitProperty)
  def withIs(value:String):NodeDefinitionObject= {entity.set(NodeDefinition.traitProperty.field , AmfScalar(value)); this}
}

case class PropertyMappingObject(val entity: DomainEntity=DomainEntity(PropertyMapping)){
  def name():Option[String]= entity.string(PropertyMapping.name)
  def withName(value:String):PropertyMappingObject= {entity.set(PropertyMapping.name.field , AmfScalar(value)); this}
  def propertyTerm():Option[String]= entity.string(PropertyMapping.propertyTerm)
  def withPropertyTerm(value:String):PropertyMappingObject= {entity.set(PropertyMapping.propertyTerm.field , AmfScalar(value)); this}
  def mandatory():Option[Boolean]= entity.boolean(PropertyMapping.mandatory)
  def withMandatory(value:Boolean):PropertyMappingObject= {entity.set(PropertyMapping.mandatory.field , AmfScalar(value)); this}
  def `enum`():Seq[String]= entity.strings(PropertyMapping.`enum`)
  def withEnum(value:String):PropertyMappingObject= {entity.add(PropertyMapping.`enum`.field , AmfScalar(value)); this}
  def pattern():Option[String]= entity.string(PropertyMapping.pattern)
  def withPattern(value:String):PropertyMappingObject= {entity.set(PropertyMapping.pattern.field , AmfScalar(value)); this}
  def minimum():Option[String]= entity.string(PropertyMapping.minimum)
  def withMinimum(value:String):PropertyMappingObject= {entity.set(PropertyMapping.minimum.field , AmfScalar(value)); this}
  def maximum():Option[String]= entity.string(PropertyMapping.maximum)
  def withMaximum(value:String):PropertyMappingObject= {entity.set(PropertyMapping.maximum.field , AmfScalar(value)); this}
  def range():Option[String]= entity.string(PropertyMapping.range)
  def withRange(value:String):PropertyMappingObject= {entity.set(PropertyMapping.range.field , AmfScalar(value)); this}
  def allowMultiple():Option[Boolean]= entity.boolean(PropertyMapping.allowMultiple)
  def withAllowMultiple(value:Boolean):PropertyMappingObject= {entity.set(PropertyMapping.allowMultiple.field , AmfScalar(value)); this}
  def asMap():Option[Boolean]= entity.boolean(PropertyMapping.asMap)
  def withAsMap(value:Boolean):PropertyMappingObject= {entity.set(PropertyMapping.asMap.field , AmfScalar(value)); this}
  def hash():Option[String]= entity.string(PropertyMapping.hash)
  def withHash(value:String):PropertyMappingObject= {entity.set(PropertyMapping.hash.field , AmfScalar(value)); this}
}

case class ClassObject(val entity: DomainEntity=DomainEntity(ClassTerm)){
  def id():Option[String]= entity.string(ClassTerm.idProperty)
  def withId(value:String):ClassObject= {entity.set(ClassTerm.idProperty.field , AmfScalar(value)); this}
  def displayName():Option[String]= entity.string(ClassTerm.displayName)
  def withDisplayName(value:String):ClassObject= {entity.set(ClassTerm.displayName.field , AmfScalar(value)); this}
  def description():Option[String]= entity.string(ClassTerm.description)
  def withDescription(value:String):ClassObject= {entity.set(ClassTerm.description.field , AmfScalar(value)); this}
  def `extends`():Seq[String]= entity.strings(ClassTerm.`extends`)
  def withExtends(value:String):ClassObject= {entity.add(ClassTerm.`extends`.field , AmfScalar(value)); this}
  def properties():Seq[String]= entity.strings(ClassTerm.properties)
  def withProperties(value:String):ClassObject= {entity.add(ClassTerm.properties.field , AmfScalar(value)); this}
}

case class DocumentObject(val entity: DomainEntity=DomainEntity(MainNode)){
  def document():Option[DocumentContentDeclarationObject]= entity.entity(MainNode.document).map(DocumentContentDeclarationObject(_))
  def withDocument(value:DocumentContentDeclarationObject):DocumentObject= {entity.set(MainNode.document.field , value.entity); this}
  def module():Option[ModuleDeclarationObject]= entity.entity(MainNode.module).map(ModuleDeclarationObject(_))
  def withModule(value:ModuleDeclarationObject):DocumentObject= {entity.set(MainNode.module.field , value.entity); this}
  def fragments():Option[FragmentsDeclarationObject]= entity.entity(MainNode.fragment).map(FragmentsDeclarationObject(_))
  def withFragments(value:FragmentsDeclarationObject):DocumentObject= {entity.set(MainNode.fragment.field , value.entity); this}
}

case class DocumentContentDeclarationObject(val entity: DomainEntity=DomainEntity(DocumentEncode)){
  def declares():Seq[DeclarationObject]= entity.entities(DocumentEncode.declares).map(DeclarationObject(_))
  def withDeclares(value:DeclarationObject):DocumentContentDeclarationObject= {entity.add(DocumentEncode.declares.field , value.entity); this}
  def encodes():Option[String]= entity.string(DocumentEncode.encodes)
  def withEncodes(value:String):DocumentContentDeclarationObject= {entity.set(DocumentEncode.encodes.field , AmfScalar(value)); this}
}

case class DeclarationObject(val entity: DomainEntity=DomainEntity(NodeReference)){
  def name():Option[String]= entity.string(NodeReference.name)
  def withName(value:String):DeclarationObject= {entity.set(NodeReference.name.field , AmfScalar(value)); this}
  def declaredNode():Option[String]= entity.string(NodeReference.uri)
  def withDeclaredNode(value:String):DeclarationObject= {entity.set(NodeReference.uri.field , AmfScalar(value)); this}
}

case class ModuleDeclarationObject(val entity: DomainEntity=DomainEntity(ModuleDeclaration)){
  def declares():Seq[DeclarationObject]= entity.entities(ModuleDeclaration.declares).map(DeclarationObject(_))
  def withDeclares(value:DeclarationObject):ModuleDeclarationObject= {entity.add(ModuleDeclaration.declares.field , value.entity); this}
}

case class FragmentsDeclarationObject(val entity: DomainEntity=DomainEntity(FragmentDeclaration)){
  def encodes():Seq[DeclarationObject]= entity.entities(FragmentDeclaration.encodes).map(DeclarationObject(_))
  def withEncodes(value:DeclarationObject):FragmentsDeclarationObject= {entity.add(FragmentDeclaration.encodes.field , value.entity); this}
}

}