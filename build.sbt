
import com.typesafe.sbt.uglify.Import._
import net.ground5hark.sbt.concat.Import._
import play.core.PlayVersion
import scoverage.ScoverageKeys
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
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

val compileDeps = Seq(
  filters,
  "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "7.3.0",
  "uk.gov.hmrc" %% "play-frontend-hmrc" % "3.24.0-play-28",
  "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "0.71.0",
  "uk.gov.hmrc" %% "http-caching-client" % "9.6.0-play-28",
  "uk.gov.hmrc" %% "play-partials" % "8.3.0-play-28",
  "com.typesafe.play" %% "play-json-joda" % "2.9.3",
  "com.typesafe.play" %% "play-joda-forms" % PlayVersion.current,
  "org.xhtmlrenderer" % "flying-saucer-pdf-itext5" % "9.1.22",
  "nu.validator" % "htmlparser" % "1.4.16",
  "org.webjars" % "jquery" % "3.6.1",
  "org.webjars.bower" % "compass-mixins" % "1.0.2"
)

val scalatestPlusPlayVersion = "5.1.0"
val scalatestVersion = "3.2.13"
val mockitoScalaVersion = "1.17.12"
val flexMarkVersion = "0.64.0"

def testDeps(scope: String) = Seq(
  "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
  "org.scalatest" %% "scalatest" % scalatestVersion % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % scope,
  "org.mockito" %% "mockito-scala-scalatest" % mockitoScalaVersion % scope,
  "com.vladsch.flexmark" % "flexmark-all" % flexMarkVersion % scope // for scalatest 3.2.x
)

lazy val root = (project in file("."))
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    name := "for-frontend",
    scalaVersion := "2.13.8",
    DefaultBuildSettings.targetJvm := "jvm-11",
    PlayKeys.playDefaultPort := 9521,
    javaOptions += "-Xmx1G",
    resolvers += Resolver.bintrayRepo("hmrc", "releases"),
    resolvers += Resolver.jcenterRepo,
    libraryDependencies ++= compileDeps ++ testDeps("test,it"),
    publishingSettings,
    scoverageSettings,
    routesGenerator := InjectedRoutesGenerator,
    majorVersion := 3
  )
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    DefaultBuildSettings.integrationTestSettings()
  )
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(plugins.JUnitXmlReportPlugin)
  .settings(
    Concat.groups := Seq(
      "javascripts/app.js" -> group(Seq(
        "javascripts/application.js", "javascripts/common.js", "javascripts/feedback.js", "javascripts/intelAlerts.js",
        "javascripts/messages.js", "javascripts/radioToggle.js", "javascripts/voaFor.js"
      ))
    ),
    // Force asset pipeline to operate in dev rather than only prod
    Assets / pipelineStages := Seq(concat, uglify, digest),
    // Prevent removal of unused code which generates warning errors due to use of third-party libs
    uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
    // Take a source file and produces minified file only (no source map)
    uglifyOps := UglifyOps.singleFile,
    // Only compress javascript files generated by concat and files from polyfills directory
    uglify / includeFilter := GlobFilter("app.js") || GlobFilter("*polyfill.js"),
    // Include only final files for assets fingerprinting
    digest / includeFilter := GlobFilter("app.js") || GlobFilter("*.min.js") || GlobFilter("*.min.css")
  )
