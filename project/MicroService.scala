import play.routes.compiler.InjectedRoutesGenerator
import play.sbt.PlayImport.PlayKeys._
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import play.sbt.routes.RoutesKeys.routesGenerator
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc.SbtArtifactory

trait MicroService {

  import uk.gov.hmrc._
  import DefaultBuildSettings._
  import uk.gov.hmrc.SbtAutoBuildPlugin
  import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
  import uk.gov.hmrc.versioning.SbtGitVersioning


  import TestPhases._

  val appName: String
  val defaultPort: Int

  lazy val appDependencies : Seq[ModuleID] = ???
  lazy val dependencyOverride: Set[ModuleID] = Set()

  lazy val plugins : Seq[Plugins] = Seq.empty
  lazy val playSettings : Seq[Setting[_]] = Seq.empty

  //lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(Seq(play.sbt.PlayScala,SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory) ++ plugins : _*)
    .settings(playSettings : _*)
    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(defaultSettings(): _*)
    .settings(organization := "uk.gov.hmrc")
    .settings(playDefaultPort := defaultPort)
    .settings(javaOptions += "-Xmx1G")
    .settings(majorVersion := 3)
    .settings(
        targetJvm := "jvm-1.8",
      libraryDependencies ++= appDependencies,
      dependencyOverrides ++= dependencyOverride,
      parallelExecution in Test := false,
      fork in Test := true,
      retrieveManaged := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
      routesGenerator := InjectedRoutesGenerator
    )
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(inConfig(TemplateTest)(Defaults.testSettings): _*)
    .settings(inConfig(TemplateItTest)(Defaults.itSettings): _*)
    .settings(
      Keys.fork in IntegrationTest := false,
      unmanagedSourceDirectories in IntegrationTest := (Seq((baseDirectory in IntegrationTest).value / "it")),
      addTestReportOption(IntegrationTest, "int-test-reports"),
      testGrouping in IntegrationTest := TestPhases.oneForkedJvmPerTest((definedTests in IntegrationTest).value),
      parallelExecution in IntegrationTest := false)
    .settings(resolvers ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo))
}

private object TestPhases {

  val allPhases = "tt->test;test->test;test->compile;compile->compile"
  val allItPhases = "tit->it;it->it;it->compile;compile->compile"

  lazy val TemplateTest = config("tt") extend Test
  lazy val TemplateItTest = config("tit") extend IntegrationTest

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map {
      test => new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }
}
