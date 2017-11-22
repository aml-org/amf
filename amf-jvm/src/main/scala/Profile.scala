import java.io.File
import java.util.Calendar

import amf.ProfileNames
import amf.client.{AmfGenerator, RamlParser}

object Profile {

  def main(args: Array[String]): Unit = {
    var before = Calendar.getInstance().getTimeInMillis
    var p = new RamlParser()
    var future = p.parseFileAsync("file://shared/src/test/resources/production/fhir-apis-1.0.0-raml/healthcare-system-api.raml")
    var bu = future.get()
    var res = p.reportValidation(ProfileNames.RAML).get()
    var after = Calendar.getInstance().getTimeInMillis
    println(res)
    println(s"ELLAPSED: ${after - before} millis")

    /*
    before = Calendar.getInstance().getTimeInMillis
    p = new RamlParser()
    future = p.parseFileAsync("file://shared/src/test/resources/production/fhir-apis-1.0.0-raml/healthcare-system-api.raml")
    bu = future.get()
    val validation = Calendar.getInstance().getTimeInMillis
    res = p.reportValidation(ProfileNames.RAML).get()
    after = Calendar.getInstance().getTimeInMillis
    println(res)
    println(s"PARSING: ${validation - before} millis")
    println(s"VALIDATION: ${after - validation} millis")
    println(s"ELLAPSED: ${after - before} millis")
    */
    new AmfGenerator().generateFile(bu, new File("dump.json")).get()
  }
}
