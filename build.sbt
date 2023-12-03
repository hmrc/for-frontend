
import net.ground5hark.sbt.concat.Import.*
import play.core.PlayVersion
import scoverage.ScoverageKeys
import uk.gov.hmrc.{DefaultBuildSettings, SbtAutoBuildPlugin}
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}

val scoverageSettings = {
  Seq(
    // Semicolon-separated list of regex matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := """<empty>;uk\.gov\.hmrc\.BuildInfo;""" +
     """.*\.Routes;.*\.RoutesPrefix;.*Filters?;MicroserviceAuditConnector;Module;GraphiteStartUp;.*\.Reverse[^.]*;""" +
    """views\..*;.*\.template\.scala""",

    ScoverageKeys.coverageMinimumStmtTotal := 80.00,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true,
    Test / parallelExecution := false
  )
}

val bootstrapVersion = "8.1.0"
val playFrontendVersion = "8.1.0"
val mongoVersion = "1.6.0"

val compileDeps = Seq(
  filters,
  "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
  "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playFrontendVersion,
  "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % mongoVersion,
  "org.xhtmlrenderer" % "flying-saucer-pdf-itext5" % "9.1.22",
  "nu.validator" % "htmlparser" % "1.4.16",
  "org.webjars" % "jquery" % "3.7.1",
  "com.github.java-json-tools" % "json-schema-validator" % "2.2.14", // must be the same version as in "for-hod-adapter"
  "org.webjars.bower" % "compass-mixins" % "1.0.2"
)

val scalatestPlusPlayVersion = "7.0.0"
val scalatestVersion = "3.2.17"
val mockitoScalaVersion = "1.17.30"
val flexMarkVersion = "0.64.8"

def testDeps(scope: String) = Seq(
  "org.playframework" %% "play-test" % PlayVersion.current % scope,
  "org.scalatest" %% "scalatest" % scalatestVersion % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % scope,
  "org.mockito" %% "mockito-scala-scalatest" % mockitoScalaVersion % scope,
  "com.vladsch.flexmark" % "flexmark-all" % flexMarkVersion % scope // for scalatest 3.2.x
)

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always // Resolves versions conflict

lazy val root = (project in file("."))
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    name := "for-frontend",
    scalaVersion := "2.13.12",
    DefaultBuildSettings.targetJvm := "jvm-11",
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    PlayKeys.playDefaultPort := 9521,
    javaOptions += "-Xmx1G",
    libraryDependencies ++= compileDeps ++ testDeps("test,it"),
    scoverageSettings,
    routesGenerator := InjectedRoutesGenerator,
    majorVersion := 3
  )
  .configs(IntegrationTest)
  .settings(
    DefaultBuildSettings.integrationTestSettings()
  )
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    Concat.groups := Seq(
      "javascripts/app.js" -> group(Seq(
        "javascripts/application.js", "javascripts/common.js", "javascripts/feedback.js", "javascripts/intelAlerts.js",
        "javascripts/messages.js", "javascripts/radioToggle.js", "javascripts/voaFor.js"
      ))
    ),
    Assets / pipelineStages := Seq(concat, digest),
    // Include only final files for assets fingerprinting
    digest / includeFilter := GlobFilter("app.js") || GlobFilter("*.min.js") || GlobFilter("app.min.css")
  )
