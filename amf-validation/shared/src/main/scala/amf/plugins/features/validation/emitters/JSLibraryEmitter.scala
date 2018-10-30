package amf.plugins.features.validation.emitters

import amf.core.validation.core.{ValidationProfile, ValidationSpecification}
import amf.core.vocabulary.Namespace

class JSLibraryEmitter(profile: Option[ValidationProfile] = None) {

  /**
    * Emit the JS library that will wrap custom JS validations to access the model elements.
    *
    * @param validations
    * @return JSON-LD graph with the validations
    */
  def emitJS(validations: Seq[ValidationSpecification]): Option[String] = {
    val constraints = for {
      validation         <- validations
      functionConstraint <- validation.functionConstraint
      _                  <- functionConstraint.code
    } yield {
      validation
    }

    if (constraints.isEmpty) {
      None
    } else {
      Some(composeText(constraints))
    }
  }

  def composeText(validations: Seq[ValidationSpecification]): String = {
    var text = preamble

    validations.foreach { (validation) =>
      val constraint = validation.functionConstraint.get
      val additionalParams = if (constraint.parameters.nonEmpty) {
        "," + constraint.parameters
          .map { param =>
            "$" + param.path.split("#").last
          }
          .mkString(",")
      } else {
        ""
      }

      text +=
        s"""
        |
        |function ${constraint.computeFunctionName(validation.id)}($$this, $$value $additionalParams) {
        |  var innerFn = ${constraint.code.get};
        |  var input = amfFindNode($$this, {});
        |  // print(JSON.stringify(input))
        |  try {
        |    return innerFn(input, $$value $additionalParams);
        |  } catch(e) {
        |    return false;
        |  }
        |}
        |
      """.stripMargin
    }

    text
  }

  val preamble: String =
    s"""
       |function amfExtractLiteral(v){
       |    if(v.datatype == null || v.datatype.value === "http://www.w3.org/2001/XMLSchema#string") {
       |        return v.value;
       |    } else if(v.datatype.value === "http://www.w3.org/2001/XMLSchema#integer") {
       |        return parseInt(v.value);
       |    } else if(v.datatype.value === "http://www.w3.org/2001/XMLSchema#float") {
       |        return parseFloat(v.value);
       |    } else if(v.datatype.value === "http://www.w3.org/2001/XMLSchema#double") {
       |        return v.value;
       |    } else if(v.datatype.value === "http://www.w3.org/2001/XMLSchema#boolean") {
       |        return v.value === "true";
       |    } else {
       |        return v.value;
       |    }
       |}
       |
       |function amfCompactProperty(prop) {
       |    var prefixes = $prefixes;
       |    for (var p in prefixes) {
       |        if (prop.indexOf(prefixes[p]) === 0) {
       |            return p + ":" + prop.replace(prefixes[p], "")
       |        }
       |    }
       |
       |    return prop;
       |}
       |
       |function amfFindNode(node, cache) {
       |    var acc = {"@id": amfCompactProperty(node.value)};
       |    var pairs = $$data.query().match(node, "?p", "?o");
       |    cache[node.value] = acc;
       |    for(var pair = pairs.nextSolution(); pair; pair = pairs.nextSolution()) {
       |        var prop = amfCompactProperty(pair.p.value);
       |        if (pair.p.value === "http://www.w3.org/1999/02/22-rdf-syntax-ns#type") {
       |            prop = "@type"
       |        }
       |
       |        var value = acc[prop] || [];
       |        acc[prop] = value;
       |        if(prop === "@type") {
       |            value.push(amfCompactProperty(pair.o.value));
       |        } else if(pair.o.termType === "BlankNode" || pair.o.termType === "NamedNode") {
       |            value.push(cache[pair.o.value] || amfFindNode(pair.o, cache));
       |        } else if (pair.o.termType === "Literal"){
       |          value.push(amfExtractLiteral(pair.o));
       |        }
       |    }
       |
       |    return acc;
       |}
       |
       |function path(node, path) {
       |  var acc = [node]
       |  if (node.constructor === Array) {
       |    acc = node;
       |  }
       |  var paths = path.replace(new RegExp(" ","g"), "").split("/") || [];
       |  for (var i=0; i<paths.length; i++) {
       |    var nextPath = paths[i];
       |    var newAcc = [];
       |    for (var j=0; j<acc.length; j++) {
       |      var nextNode = acc[j];
       |      newAcc = newAcc.concat(nextNode[nextPath] || [])
       |    }
       |    acc = newAcc;
       |  }
       |
       |  return acc;
       |}
       |
       |function value(node, propertyPath, value) {
       |  var values = path(node, propertyPath);
       |  for (var i=0; i<values.length; i++) {
       |    if (values[i] === value)
       |      return true;
       |  }
       |  return false;
       |}
       |
       |function filterByPropertyValue(nodes, propertyPath, val) {
       |  var acc = [];
       |  for (var i=0; i<nodes.length; i++) {
       |    if (value(nodes[i], propertyPath, val)) {
       |      acc.push(nodes[i]);
       |    }
       |  }
       |  return acc;
       |}
       |
       |
       |
       |function isModelType(node, type) {
       |  if (node == null) {
       |    return false;
       |  }
       |  if (node.constructor === Array && node.length == 1) {
       |    node = node[0]
       |  }
       |  var types = node['@type'] || [];
       |  for(var i=0; i<types.length; i++) {
       |    if (types[i] === type) {
       |      return true;
       |    }
       |  }
       |
       |  return false;
       |}
       |
       |if (typeof(console) === "undefined") {
       |  console = {
       |    log: function(x) { print(x) }
       |  };
       |}
       |
       |if (typeof(accumulators) == "undefined") {
       |  accumulators = {};
       |}
       |for (var p in accumulators) {
       |  delete accumulators[p];
       |}
    """.stripMargin

  def prefixes: String = {
    val namespaces = Namespace.ns.filter {
      case (prefix, _) =>
        prefix != "schema-org" && prefix != "raml-http"
    }
    val customNamespaces = profile match {
      case Some(prof) => prof.prefixes
      case _          => Map()
    }

    val allNamespaces = namespaces.map { case (prefix, ns) => (prefix, ns.base) } ++ customNamespaces.map {
      case (prefix, ns) => (prefix, ns)
    }
    "{\n" + allNamespaces.map { case (prefix, ns) => "  \"" + prefix + "\": \"" + ns + "\"" }.mkString(",\n") + "}"
  }

}
