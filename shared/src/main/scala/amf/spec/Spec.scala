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

  case class Spec(fields: SpecField*)

  val RamlSpec = Spec(
    SpecField(KeyMatcher("title"), StringValueParser(Name), null),
    SpecField(KeyMatcher("baseUri"), StringValueParser(Host), null),
    SpecField(KeyMatcher("description"), StringValueParser(Description), null),
    SpecField(KeyMatcher("mediaType"), StringValueParser(ContentType, Accepts), null),
    SpecField(KeyMatcher("version"), StringValueParser(Version), null),
    SpecField(KeyMatcher("termsOfService"), StringValueParser(TermsOfService), null),
    SpecField(KeyMatcher("protocols"), StringListParser(Schemes), null),
    SpecField(
      RegExpMatcher("/.*"),
      new EndPointParser(),
      null,
      List(
        SpecField(KeyMatcher("displayName"), StringValueParser(EndPointModel.Name), null),
        SpecField(KeyMatcher("description"), StringValueParser(EndPointModel.Description), null)
      )
    ),
    SpecField(
      KeyMatcher("contact"),
      new BuilderParser(OrganizationBuilder.apply, Provider),
      null,
      List(
        SpecField(KeyMatcher("url"), StringValueParser(OrganizationModel.Url), null),
        SpecField(KeyMatcher("name"), StringValueParser(OrganizationModel.Name), null),
        SpecField(KeyMatcher("email"), StringValueParser(OrganizationModel.Email), null)
      )
    ),
    SpecField(
      KeyMatcher("externalDocs"),
      new BuilderParser(CreativeWorkBuilder.apply, Documentation),
      null,
      List(
        SpecField(KeyMatcher("url"), StringValueParser(CreativeWorkModel.Url), null),
        SpecField(KeyMatcher("description"), StringValueParser(CreativeWorkModel.Description), null)
      )
    ),
    SpecField(
      KeyMatcher("license"),
      new BuilderParser(LicenseBuilder.apply, License),
      null,
      List(
        SpecField(KeyMatcher("url"), StringValueParser(LicenseModel.Url), null),
        SpecField(KeyMatcher("name"), StringValueParser(LicenseModel.Name), null)
      )
    )
  )

  val OasSpec = Spec(
    SpecField(
      KeyMatcher("info"),
      ChildrenParser(),
      null,
      List(
        SpecField(KeyMatcher("title"), StringValueParser(Name), null),
        SpecField(KeyMatcher("description"), StringValueParser(Description), null),
        SpecField(KeyMatcher("termsOfService"), StringValueParser(TermsOfService), null),
        SpecField(KeyMatcher("version"), StringValueParser(Version), null),
        SpecField(
          KeyMatcher("license"),
          new BuilderParser(LicenseBuilder.apply, License),
          null,
          List(
            SpecField(KeyMatcher("url"), StringValueParser(LicenseModel.Url), null),
            SpecField(KeyMatcher("name"), StringValueParser(LicenseModel.Name), null)
          )
        )
      )
    ),
    SpecField(KeyMatcher("host"), StringValueParser(Host), null),
    SpecField(KeyMatcher("basePath"), StringValueParser(BasePath), null),
    SpecField(KeyMatcher("consumes"), StringValueParser(Accepts), null),
    SpecField(KeyMatcher("produces"), StringValueParser(ContentType), null),
    SpecField(KeyMatcher("schemes"), StringListParser(Schemes), null),
    SpecField(
      KeyMatcher("paths"),
      ChildrenParser(),
      null,
      List(
        SpecField(
          RegExpMatcher("/.*"),
          new EndPointParser(),
          null,
          List(
            SpecField(KeyMatcher("displayName"), StringValueParser(EndPointModel.Name), null),
            SpecField(KeyMatcher("description"), StringValueParser(EndPointModel.Description), null)
          )
        )
      )
    ),
    SpecField(
      KeyMatcher("contact"),
      new BuilderParser(OrganizationBuilder.apply, Provider),
      null,
      List(
        SpecField(KeyMatcher("url"), StringValueParser(OrganizationModel.Url), null),
        SpecField(KeyMatcher("name"), StringValueParser(OrganizationModel.Name), null),
        SpecField(KeyMatcher("email"), StringValueParser(OrganizationModel.Email), null)
      )
    ),
    SpecField(
      KeyMatcher("externalDocs"),
      new BuilderParser(CreativeWorkBuilder.apply, Documentation),
      null,
      List(
        SpecField(KeyMatcher("url"), StringValueParser(CreativeWorkModel.Url), null),
        SpecField(KeyMatcher("description"), StringValueParser(CreativeWorkModel.Description), null)
      )
    )
  )

  val JsonLdSpec = Spec(
    SpecField(
      RegExpMatcher(".*#encodes"),
      ChildrenParser(),
      null,
      List(
        SpecField(RegExpMatcher(".*name"),
                  ChildrenJsonLdParser(),
                  null,
                  List(
                    SpecField(KeyMatcher("@value"), StringValueParser(Name), null)
                  )),
        SpecField(RegExpMatcher(".*host"),
                  ChildrenJsonLdParser(),
                  null,
                  List(
                    SpecField(KeyMatcher("@value"), StringValueParser(Host), null)
                  )),
        SpecField(RegExpMatcher(".*scheme"), StringJsonListParser(), null)
      )
    )
  )
}

case class SpecField(matcher: Matcher, parse: SpecFieldParser, emit: () => Unit, children: List[SpecField] = Nil)

object SpecFieldParser {

  trait SpecFieldParser {
    def parse(spec: SpecField, node: ASTNode[_], builder: Builder[_]): Unit

    def apply(spec: SpecField, node: ASTNode[_], builder: Builder[_]): Unit = parse(spec, node, builder)
  }

  case class StringValueParser(field: Field*) extends SpecFieldParser {

    override def parse(spec: SpecField, entry: ASTNode[_], builder: Builder[_]): Unit =
      field.foreach(builder.set(_, entry.last.content.unquote, annotations(entry)))
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
