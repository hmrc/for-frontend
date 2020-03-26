
resolvers ++= Seq(
  Resolver.url(
    "hmrc-sbt-plugin-releases", url("https://dl.bintray.com/hmrc/sbt-plugin-releases")
  )(Resolver.ivyStylePatterns),
  Resolver.bintrayRepo("hmrc", "releases")
)

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.24")

addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "2.0.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "2.6.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "2.1.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "1.2.0")