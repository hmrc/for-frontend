import org.irundaia.sass.Minified
import uk.gov.hmrc.DefaultBuildSettings.{itSettings, targetJvm}

ThisBuild / majorVersion := 3
ThisBuild / scalaVersion := "3.8.3"
ThisBuild / targetJvm := "jvm-21"
ThisBuild / scalacOptions ++= Seq("-feature", "-Wconf:msg=Flag .* set repeatedly:s")

lazy val microservice = Project("for-frontend", file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    PlayKeys.playDefaultPort := 9521,
    maintainer := "voa.service.optimisation@digital.hmrc.gov.uk",
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:msg=Implicit parameters should be provided with a \\`using\\` clause&src=views/.*:s",
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
        "javascripts/voRALD.js"
      ))
    ),
    Assets / pipelineStages := Seq(concat, digest),
    // Include only final files for assets fingerprinting
    digest / includeFilter := GlobFilter("app.js") || GlobFilter("*.min.js") || GlobFilter("app.min.css")
  )

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(itSettings())

addCommandAlias("precommit", "scalafmtSbt;scalafmtAll;it/Test/scalafmt;coverage;test;it/test;coverageReport")
