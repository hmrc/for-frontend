
import play.core.PlayVersion
import scoverage.ScoverageKeys
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.{DefaultBuildSettings, SbtAutoBuildPlugin}
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

val scoverageSettings = {
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := """<empty>;uk\.gov\.hmrc\.BuildInfo;""" +
     """.*\.Routes;.*\.RoutesPrefix;.*Filters?;MicroserviceAuditConnector;Module;GraphiteStartUp;.*\.Reverse[^.]*;""" +
    """views\..*;.*\.template\.scala""",

    ScoverageKeys.coverageMinimumStmtTotal := 80.00,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}

val compileDeps = Seq(
  filters,
  ws,
  "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.24.0",
  "uk.gov.hmrc" %% "play-frontend-hmrc" % "3.18.0-play-28",
  "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "0.64.0",
  "uk.gov.hmrc" %% "http-caching-client" % "9.6.0-play-28",
  "uk.gov.hmrc" %% "play-conditional-form-mapping" % "1.11.0-play-28",
  "uk.gov.hmrc" %% "play-partials" % "8.3.0-play-28",
  "com.typesafe.play" %% "play-json-joda" % "2.9.2",
  "com.typesafe.play" %% "play-joda-forms" % PlayVersion.current,
  "org.xhtmlrenderer" % "flying-saucer-pdf-itext5" % "9.1.22",
  "nu.validator" % "htmlparser" % "1.4.16",
  "org.webjars.bower" % "compass-mixins" % "1.0.2"
)

val scalatestPlusPlayVersion = "5.1.0"
val scalatestVersion = "3.2.12"
val mockitoScalaVersion = "1.17.5"
val flexmarkVersion = "0.62.2"

def testDeps(scope: String) = Seq(
  "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
  "org.scalatest" %% "scalatest" % scalatestVersion % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % scope,
  "org.mockito" %% "mockito-scala-scalatest" % mockitoScalaVersion % scope,
  "com.vladsch.flexmark" % "flexmark-all" % flexmarkVersion % scope // for scalatest 3.1+
)

lazy val root = (project in file("."))
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    name := "for-frontend",
    scalaVersion := "2.12.15",
    PlayKeys.playDefaultPort := 9521,
    javaOptions += "-Xmx1G",
    resolvers += Resolver.bintrayRepo("hmrc", "releases"),
    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= compileDeps ++ testDeps("test,it"),
    publishingSettings,
    scoverageSettings,
    routesGenerator := InjectedRoutesGenerator,
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    majorVersion := 3,
  ).settings(JavaScriptBuild.javaScriptUiSettings: _*)
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    DefaultBuildSettings.integrationTestSettings()
  )
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(plugins.JUnitXmlReportPlugin)
