package amf.resolution

import amf.ProfileNames
import amf.client.GenerationOptions
import amf.common.Tests.checkDiff
import amf.compiler.AMFCompiler
import amf.dumper.AMFDumper
import amf.remote.Syntax.Yaml
import amf.remote.{Raml, RamlYamlHint}
import amf.shape._
import amf.spec.Declarations
import amf.spec.raml.RamlTypeExpressionParser
import amf.unsafe.PlatformSecrets
import amf.vocabulary.Namespace
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class ResolutionTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("TypeExpressions") {
    
    val adopt = (shape: Shape) => { shape.adopted("/test") }

    var res = RamlTypeExpressionParser(adopt, Declarations()).parse("integer")
    assert(res.get.isInstanceOf[ScalarShape])
    assert(res.get.asInstanceOf[ScalarShape].dataType == (Namespace.Xsd + "integer").iri())


    res = RamlTypeExpressionParser(adopt, Declarations()).parse("(integer)")
    assert(res.get.isInstanceOf[ScalarShape])
    assert(res.get.asInstanceOf[ScalarShape].dataType == (Namespace.Xsd + "integer").iri())


    res = RamlTypeExpressionParser(adopt, Declarations()).parse("((integer))")
    assert(res.get.isInstanceOf[ScalarShape])
    assert(res.get.asInstanceOf[ScalarShape].dataType == (Namespace.Xsd + "integer").iri())


    res = RamlTypeExpressionParser(adopt, Declarations()).parse("integer[]")
    assert(res.get.isInstanceOf[ArrayShape])
    assert(res.get.asInstanceOf[ArrayShape].items.asInstanceOf[ScalarShape].dataType == (Namespace.Xsd + "integer").iri())
    assert(res != null)


    res = RamlTypeExpressionParser(adopt, Declarations()).parse("(integer)[]")
    assert(res.get.isInstanceOf[ArrayShape])
    assert(res.get.asInstanceOf[ArrayShape].items.asInstanceOf[ScalarShape].dataType == (Namespace.Xsd + "integer").iri())
    assert(res != null)

    var error = false
    try {
      RamlTypeExpressionParser(adopt, Declarations()).parse("[]")
    } catch {
      case e: Exception => error = true
    }
    assert(error)


    res = RamlTypeExpressionParser(adopt, Declarations()).parse("integer | string")
    assert(res.get.isInstanceOf[UnionShape])
    var union = res.get.asInstanceOf[UnionShape]
    assert(union.anyOf.length == 2)
    assert(union.anyOf.map { e =>
      e.asInstanceOf[ScalarShape].dataType
    } == Seq((Namespace.Xsd + "integer").iri(), (Namespace.Xsd + "string").iri()))
    assert(res != null)


    res = RamlTypeExpressionParser(adopt, Declarations()).parse("(integer )| (string)")
    assert(res.get.isInstanceOf[UnionShape])
    union = res.get.asInstanceOf[UnionShape]
    assert(union.anyOf.length == 2)
    assert(union.anyOf.map { e =>
      e.asInstanceOf[ScalarShape].dataType
    } == Seq((Namespace.Xsd + "integer").iri(), (Namespace.Xsd + "string").iri()))
    assert(res != null)

    res = RamlTypeExpressionParser(adopt, Declarations()).parse("(integer | string) | number")
    assert(res.get.isInstanceOf[UnionShape])
    union = res.get.asInstanceOf[UnionShape]
    assert(union.anyOf.length == 3)
    assert(union.anyOf.map { e =>
      e.asInstanceOf[ScalarShape].dataType
    } == Seq((Namespace.Xsd + "integer").iri(), (Namespace.Xsd + "string").iri(), (Namespace.Xsd + "float").iri()))
    assert(res != null)

    res = RamlTypeExpressionParser(adopt, Declarations()).parse("(integer | string)[]")
    assert(res.get.isInstanceOf[ArrayShape])
    var array = res.get.asInstanceOf[ArrayShape]
    assert(array.items.isInstanceOf[UnionShape])
    union = array.items.asInstanceOf[UnionShape]
    assert(union.anyOf.map { e =>
      e.asInstanceOf[ScalarShape].dataType
    } == Seq((Namespace.Xsd + "integer").iri(), (Namespace.Xsd + "string").iri()))
    assert(res != null)


    res = RamlTypeExpressionParser(adopt, Declarations()).parse("(integer | string[])")
    assert(res != null)
    assert(res.get.isInstanceOf[UnionShape])
    union = res.get.asInstanceOf[UnionShape]
    assert(union.anyOf.length == 2)
    assert(union.anyOf.head.isInstanceOf[ScalarShape])
    assert(union.anyOf.head.asInstanceOf[ScalarShape].dataType == (Namespace.Xsd + "integer").iri())
    assert(union.anyOf.last.isInstanceOf[ArrayShape])
    assert(union.anyOf.last.asInstanceOf[ArrayShape].items.asInstanceOf[ScalarShape].dataType == (Namespace.Xsd + "string").iri())


    res = RamlTypeExpressionParser(adopt, Declarations()).parse("integer | string[]")
    assert(res != null)
    assert(res.get.isInstanceOf[UnionShape])
    union = res.get.asInstanceOf[UnionShape]
    assert(union.anyOf.length == 2)
    assert(union.anyOf.head.isInstanceOf[ScalarShape])
    assert(union.anyOf.head.asInstanceOf[ScalarShape].dataType == (Namespace.Xsd + "integer").iri())
    assert(union.anyOf.last.isInstanceOf[ArrayShape])
    assert(union.anyOf.last.asInstanceOf[ArrayShape].items.asInstanceOf[ScalarShape].dataType == (Namespace.Xsd + "string").iri())


    res = RamlTypeExpressionParser(adopt, Declarations()).parse("integer[][]")
    assert(res != null)
    assert(res.get.isInstanceOf[MatrixShape])
    var matrix = res.get.asInstanceOf[MatrixShape]
    assert(matrix.items.isInstanceOf[ArrayShape])
    array = matrix.items.asInstanceOf[ArrayShape]
    assert(array.items.isInstanceOf[ScalarShape])
    assert(array.items.asInstanceOf[ScalarShape].dataType == (Namespace.Xsd + "integer").iri())
  }

  val basePath = "file://shared/src/test/resources/resolution/"

  val examples = Seq(
    "union1",
    "union2"
    // "inheritance1"
  )

  examples.foreach { example =>
    test(s"Resolve data types: $example") {
      val expected   = platform.resolve(basePath + s"${example}_canonical.raml", None).map(_.stream.toString)
      AMFCompiler(basePath + s"$example.raml",
        platform,
        RamlYamlHint,
        None,
        None,
        platform.dialectsRegistry)
        .build().map { model =>
        new ShapeNormalizationStage(ProfileNames.RAML).resolve(model, null)
      }.flatMap({ unit =>
        AMFDumper(unit, Raml, Yaml, GenerationOptions()).dumpToString
      })
        .zip(expected)
        .map(checkDiff)
    }
  }

}
