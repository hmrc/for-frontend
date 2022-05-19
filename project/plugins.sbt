resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.15")

addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "2.1.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "3.6.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "2.4.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.3")
