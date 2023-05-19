
import com.typesafe.sbt.uglify.Import._
import net.ground5hark.sbt.concat.Import._
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

val compileDeps = Seq(
  filters,
  "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "7.15.0",
  "uk.gov.hmrc" %% "play-frontend-hmrc" % "7.7.0-play-28",
  "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "1.2.0",
  "uk.gov.hmrc" %% "http-caching-client" % "10.0.0-play-28",
  "uk.gov.hmrc" %% "play-partials" % "8.4.0-play-28",
  "com.typesafe.play" %% "play-json-joda" % "2.9.4",
  "com.typesafe.play" %% "play-joda-forms" % PlayVersion.current,
  "org.xhtmlrenderer" % "flying-saucer-pdf-itext5" % "9.1.22",
  "nu.validator" % "htmlparser" % "1.4.16",
  "org.webjars" % "jquery" % "3.6.4",
  "com.github.java-json-tools" % "json-schema-validator" % "2.2.14", // must be the same version as in "for-hod-adapter"
  "org.webjars.bower" % "compass-mixins" % "1.0.2"
)

val scalatestPlusPlayVersion = "5.1.0"
val scalatestVersion = "3.2.16"
val mockitoScalaVersion = "1.17.14"
val flexMarkVersion = "0.64.6"

def testDeps(scope: String) = Seq(
  "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
  "org.scalatest" %% "scalatest" % scalatestVersion % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % scope,
  "org.mockito" %% "mockito-scala-scalatest" % mockitoScalaVersion % scope,
  "com.vladsch.flexmark" % "flexmark-all" % flexMarkVersion % scope // for scalatest 3.2.x
)

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always // Resolves versions conflict

lazy val root = (project in file("."))
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    name := "for-frontend",
    scalaVersion := "2.13.10",
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
    Defaults.itSettings,
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
