import org.irundaia.sass.Minified
import uk.gov.hmrc.DefaultBuildSettings.{itSettings, targetJvm}

ThisBuild / scalaVersion := "3.8.1"
ThisBuild / majorVersion := 3

val commonSettings = Seq(
  targetJvm := "jvm-21",
  scalacOptions += "-Wconf:msg=Flag .* set repeatedly:s"
)

lazy val microservice = Project("for-frontend", file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(commonSettings)
  .settings(
    PlayKeys.playDefaultPort := 9521,
    maintainer := "voa.service.optimisation@digital.hmrc.gov.uk",
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:msg=Implicit parameters should be provided with a \\`using\\` clause&src=views/.*:s",
    scalacOptions += "-feature",
    javaOptions += "-XX:+EnableDynamicAgentLoading",
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

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(commonSettings)
  .settings(itSettings())
