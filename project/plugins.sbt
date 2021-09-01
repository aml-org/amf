import metabuild.Common

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.32")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.1")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("com.github.mwz" % "sbt-sonar" % "2.1.0")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")
addSbtPlugin("com.eed3si9n" % "sbt-sriracha" % "0.1.0")

resolvers ++= List(Common.releases, Common.snapshots, Resolver.mavenLocal)
resolvers += "jitpack" at "https://jitpack.io"
credentials ++= Common.credentials()

addSbtPlugin("com.github.amlorg" % "scala-js-typings" % "0.0.14")
