package amf.document

/**
  * RAML Fragments
  */
object Fragment {

  sealed trait TypedFragment

  case object DocumentationItem extends TypedFragment

  case object DataType extends TypedFragment

  case object NamedExample extends TypedFragment

  case object ResourceType extends TypedFragment

  case object Trait extends TypedFragment

  case object AnnotationTypeDeclaration extends TypedFragment

  case object Library extends TypedFragment

  case object Overlay extends TypedFragment

  case object Extension extends TypedFragment

  case object SecurityScheme extends TypedFragment

  val fragments = Seq(DocumentationItem,
                      DataType,
                      NamedExample,
                      ResourceType,
                      Trait,
                      AnnotationTypeDeclaration,
                      Library,
                      Overlay,
                      Extension,
                      SecurityScheme)
}
