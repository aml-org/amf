package amf.document

/**
  * RAML Fragments
  */
object Fragment {

  /** Units encoding domain fragments */
  sealed trait Fragment extends Unit

  case object DocumentationItem extends Fragment

  case object DataType extends Fragment

  case object NamedExample extends Fragment

  case object ResourceType extends Fragment

  case object Trait extends Fragment

  case object AnnotationTypeDeclaration extends Fragment

  case object Library extends Fragment

  case object Overlay extends Fragment

  case object Extension extends Fragment

  case object SecurityScheme extends Fragment

  case object Default extends Fragment

  val fragments = Seq(DocumentationItem,
                      DataType,
                      NamedExample,
                      ResourceType,
                      Trait,
                      AnnotationTypeDeclaration,
                      Library,
                      Overlay,
                      Extension,
                      SecurityScheme,
                      Default)
}
