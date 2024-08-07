import org.irundaia.sass.Minified
import uk.gov.hmrc.DefaultBuildSettings.itSettings

ThisBuild / scalaVersion := "3.4.2"
ThisBuild / majorVersion := 3

lazy val microservice = Project("for-frontend", file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    PlayKeys.playDefaultPort := 9521,
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    libraryDependencies ++= AppDependencies.appDependencies
  )
  .settings(
    SassKeys.generateSourceMaps := false,
    SassKeys.cssStyle := Minified
  )
  .settings(
    Concat.groups := Seq(
      "javascripts/app.js" -> group(Seq(
        "javascripts/application.js",
        "javascripts/common.js",
        "javascripts/feedback.js",
        "javascripts/intelAlerts.js",
        "javascripts/messages.js",
        "javascripts/radioToggle.js",
        "javascripts/voaFor.js"
      ))
    ),
    Assets / pipelineStages := Seq(concat, digest),
    // Include only final files for assets fingerprinting
    digest / includeFilter := GlobFilter("app.js") || GlobFilter("*.min.js") || GlobFilter("app.min.css")
  )

lazy val it =
  (project in file("it"))
    .enablePlugins(PlayScala)
    .dependsOn(microservice % "test->test")
    .settings(itSettings())
