package amf.spec

import amf.builder._
import amf.common.Strings.strings
import amf.metadata.Field
import amf.metadata.domain.EndPointModel.Path
import amf.metadata.domain.APIDocumentationModel._
import amf.metadata.domain._
import amf.domain.Annotation.{LexicalInformation, ParentEndPoint}
import amf.domain.{Annotation, EndPoint}
import amf.parser.ASTNode
import amf.remote.{Amf, Oas, Raml, Vendor}
import amf.spec.Matcher.{KeyMatcher, Matcher, RegExpMatcher}
import amf.spec.SpecFieldEmitter.{
  ObjectEmitter,
  SpecEmitter,
  SpecFieldEmitter,
  StringListValueEmitter,
  StringValueEmitter
}
import amf.spec.SpecFieldParser._

import scala.collection.mutable.ListBuffer
import scala.util.matching.Regex

/**
  * Vendor specs.
  */
object Spec {

  def apply(vendor: Vendor): Spec = vendor match {
    case Raml => RamlSpec
    case Oas  => OasSpec
    case Amf  => JsonLdSpec
    case _    => Spec()
  }

  implicit def fieldAsList(field: Field): List[Field] = List(field)

  case class Spec(fields: SpecField*) {
    def emitter: SpecEmitter = SpecEmitter(fields.toList)
  }

  val RamlSpec = Spec(
    SpecField(Name, KeyMatcher("title"), StringValueParser, StringValueEmitter),
    SpecField(Host, KeyMatcher("baseUri"), StringValueParser, StringValueEmitter),
    SpecField(Description, KeyMatcher("description"), StringValueParser, StringValueEmitter),
    SpecField(List(ContentType, Accepts), KeyMatcher("mediaType"), StringValueParser, StringValueEmitter),
    SpecField(Version, KeyMatcher("version"), StringValueParser, StringValueEmitter),
    SpecField(TermsOfService, KeyMatcher("termsOfService"), StringValueParser, StringValueEmitter),
    SpecField(Schemes, KeyMatcher("protocols"), StringListParser(Schemes), StringListValueEmitter),
    SpecField(
      EndPoints,
      RegExpMatcher("/.*"),
      new EndPointParser(),
      ObjectEmitter,
      List(
        SpecField(EndPointModel.Name, KeyMatcher("displayName"), StringValueParser, StringValueEmitter),
        SpecField(EndPointModel.Description, KeyMatcher("description"), StringValueParser, StringValueEmitter)
      )
    ),
    SpecField(
      Provider,
      KeyMatcher("contact"),
      new BuilderParser(OrganizationBuilder.apply, Provider),
      ObjectEmitter,
      List(
        SpecField(OrganizationModel.Url, KeyMatcher("url"), StringValueParser, StringValueEmitter),
        SpecField(OrganizationModel.Name, KeyMatcher("name"), StringValueParser, StringValueEmitter),
        SpecField(OrganizationModel.Email, KeyMatcher("email"), StringValueParser, StringValueEmitter)
      )
    ),
    SpecField(
      Documentation,
      KeyMatcher("externalDocs"),
      new BuilderParser(CreativeWorkBuilder.apply, Documentation),
      ObjectEmitter,
      List(
        SpecField(CreativeWorkModel.Url, KeyMatcher("url"), StringValueParser, StringValueEmitter),
        SpecField(CreativeWorkModel.Description, KeyMatcher("description"), StringValueParser, StringValueEmitter)
      )
    ),
    SpecField(
      License,
      KeyMatcher("license"),
      new BuilderParser(LicenseBuilder.apply, License),
      ObjectEmitter,
      List(
        SpecField(LicenseModel.Url, KeyMatcher("url"), StringValueParser, StringValueEmitter),
        SpecField(LicenseModel.Name, KeyMatcher("name"), StringValueParser, StringValueEmitter)
      )
    )
  )

  val OasSpec = Spec(
    SpecField(
      Nil,
      KeyMatcher("info"),
      ChildrenParser(),
      null,
      List(
        SpecField(Name, KeyMatcher("title"), StringValueParser, StringValueEmitter),
        SpecField(Description, KeyMatcher("description"), StringValueParser, StringValueEmitter),
        SpecField(TermsOfService, KeyMatcher("termsOfService"), StringValueParser, StringValueEmitter),
        SpecField(Version, KeyMatcher("version"), StringValueParser, StringValueEmitter),
        SpecField(
          License,
          KeyMatcher("license"),
          new BuilderParser(LicenseBuilder.apply, License),
          ObjectEmitter,
          List(
            SpecField(LicenseModel.Url, KeyMatcher("url"), StringValueParser, StringValueEmitter),
            SpecField(LicenseModel.Name, KeyMatcher("name"), StringValueParser, StringValueEmitter)
          )
        )
      )
    ),
    SpecField(Host, KeyMatcher("host"), StringValueParser, StringValueEmitter),
    SpecField(BasePath, KeyMatcher("basePath"), StringValueParser, StringValueEmitter),
    SpecField(Accepts, KeyMatcher("consumes"), StringValueParser, StringValueEmitter),
    SpecField(ContentType, KeyMatcher("produces"), StringValueParser, StringValueEmitter),
    SpecField(Schemes, KeyMatcher("schemes"), StringListParser(Schemes), StringListValueEmitter),
    SpecField(
      Nil,
      KeyMatcher("paths"),
      ChildrenParser(),
      null,
      List(
        SpecField(
          EndPoints,
          RegExpMatcher("/.*"),
          new EndPointParser(),
          null,
          List(
            SpecField(EndPointModel.Name, KeyMatcher("displayName"), StringValueParser, StringValueEmitter),
            SpecField(EndPointModel.Description, KeyMatcher("description"), StringValueParser, StringValueEmitter)
          )
        )
      )
    ),
    SpecField(
      Provider,
      KeyMatcher("contact"),
      new BuilderParser(OrganizationBuilder.apply, Provider),
      ObjectEmitter,
      List(
        SpecField(OrganizationModel.Url, KeyMatcher("url"), StringValueParser, StringValueEmitter),
        SpecField(OrganizationModel.Name, KeyMatcher("name"), StringValueParser, StringValueEmitter),
        SpecField(OrganizationModel.Email, KeyMatcher("email"), StringValueParser, StringValueEmitter)
      )
    ),
    SpecField(
      Documentation,
      KeyMatcher("externalDocs"),
      new BuilderParser(CreativeWorkBuilder.apply, Documentation),
      ObjectEmitter,
      List(
        SpecField(CreativeWorkModel.Url, KeyMatcher("url"), StringValueParser, StringValueEmitter),
        SpecField(CreativeWorkModel.Description, KeyMatcher("description"), StringValueParser, StringValueEmitter)
      )
    )
  )

  val JsonLdSpec = Spec(
    SpecField(
      Nil,
      RegExpMatcher(".*#encodes"),
      ChildrenParser(),
      null,
      List(
        SpecField(Nil,
                  RegExpMatcher(".*name"),
                  ChildrenJsonLdParser(),
                  null,
                  List(
                    SpecField(Name, KeyMatcher("@value"), StringValueParser, StringValueEmitter)
                  )),
        SpecField(Nil,
                  RegExpMatcher(".*host"),
                  ChildrenJsonLdParser(),
                  null,
                  List(
                    SpecField(Host, KeyMatcher("@value"), StringValueParser, StringValueEmitter)
                  )),
        SpecField(Schemes, RegExpMatcher(".*scheme"), StringJsonListParser(), null)
      )
    )
  )
}

case class SpecField(fields: List[Field],
                     matcher: Matcher,
                     parse: SpecFieldParser,
                     emitter: SpecFieldEmitter,
                     children: List[SpecField] = Nil)

object SpecFieldParser {

  trait SpecFieldParser {
    def parse(spec: SpecField, node: ASTNode[_], builder: Builder[_]): Unit

    def apply(spec: SpecField, node: ASTNode[_], builder: Builder[_]): Unit = parse(spec, node, builder)
  }

  object StringValueParser extends SpecFieldParser {

    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder[_]): Unit =
      spec.fields.foreach(builder.set(_, entry.last.content.unquote, annotations(entry)))
  }

  case class StringListParser(field: Field*) extends SpecFieldParser {

    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder[_]): Unit =
      field.foreach(builder.set(_, entry.last.children.map(c => c.content.unquote), annotations(entry)))
  }

  case class StringJsonListParser(field: Field*) extends SpecFieldParser {

    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder[_]): Unit =
      field.foreach(builder.set(_, entry.last.children.map(c => c.head.last.content.unquote), annotations(entry)))
  }

  case class ChildrenParser() extends SpecFieldParser {
    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder[_]): Unit = {
      entry.last.children.foreach(child => {
        spec.children.find(_.matcher.matches(child)) match {
          case Some(field) => field.parse(field, child, builder)
          case _           => // Unknown node...
        }
      })
    }
  }

  case class ChildrenJsonLdParser() extends SpecFieldParser {
    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder[_]): Unit = {
      entry.last.head.children.foreach(child => {
        spec.children.find(_.matcher.matches(child)) match {
          case Some(field) => field.parse(field, child, builder)
          case _           => // Unknown node...
        }
      })
    }
  }

  class EndPointParser() extends ChildrenParser {
    override def parse(spec: SpecField, node: ASTNode[_], builder: Builder[_]): Unit = {
      val result: ListBuffer[EndPoint] = ListBuffer()
      parse(spec, node, None, result)
      builder.add(EndPoints, result.toList)
    }

    private def parse(spec: SpecField,
                      node: ASTNode[_],
                      parent: Option[EndPoint],
                      collector: ListBuffer[EndPoint]): Unit = {

      val endpoint = EndPointBuilder().set(Path,
                                           parent.map(_.path).getOrElse("") + node.head.content.unquote,
                                           annotations(node.head) :+ ParentEndPoint(parent))
      super.parse(spec, node, endpoint)

      val actual = endpoint.build
      collector += actual

      node.last.children
        .filter(RegExpMatcher("/.*").matches)
        .foreach(parse(spec, _, Some(actual), collector))
    }
  }

  class BuilderParser(b: () => Builder[_], field: Field) extends ChildrenParser {
    override def parse(spec: SpecField, node: ASTNode[_], builder: Builder[_]): Unit = {
      val innerBuilder: Builder[_] = b()
      super.parse(spec, node, innerBuilder)
      builder.set(field, innerBuilder.build)
    }
  }

  def annotations(node: ASTNode[_]): List[Annotation] = {
    List(LexicalInformation(node.range))
  }
}

object Matcher {

  trait Matcher {
    def matches(node: ASTNode[_]): Boolean
  }

  case class KeyMatcher(key: String) extends Matcher {
    override def matches(entry: ASTNode[_]): Boolean = key == entry.head.content.unquote
  }

  case class RegExpMatcher(expr: String) extends Matcher {
    val path: Regex = expr.r

    override def matches(entry: ASTNode[_]): Boolean = entry.head.content.unquote match {
      case path() => true
      case _      => false
    }
  }
}
