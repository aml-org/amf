import metabuild.Common

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "0.6.33")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"            % "1.9.3")
addSbtPlugin("com.sonar-scala"    % "sbt-sonar"                % "2.3.0")
addSbtPlugin("net.virtual-void"   % "sbt-dependency-graph"     % "0.10.0-RC1")
addSbtPlugin("com.eed3si9n"       % "sbt-sriracha"             % "0.1.0")
addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo"            % "0.10.0")

resolvers ++= List(Common.releases, Common.snapshots, Resolver.mavenLocal, Resolver.mavenCentral)
credentials ++= Common.credentials()

addSbtPlugin("com.github.amlorg" % "scala-js-typings" % "0.0.14")
