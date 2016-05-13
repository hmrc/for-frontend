import sbt._
import sbt.Keys._

object FrontendBuild extends Build with MicroService {

  import scala.util.Properties._

  val appName = "for-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()

  override lazy val plugins : Seq[Plugins] = Seq(play.PlayScala)

  override val defaultPort: Int = 9521

  override lazy val playSettings: Seq[Setting[_]] = JavaScriptBuild.javaScriptUiSettings
}

private object AppDependencies {
  
  import play.PlayImport._

  private val playHealthVersion = "1.1.0"
  private val playUiVersion = "4.4.0"

  val compile = Seq(
    filters,
    ws,
    "com.kenshoo" %% "metrics-play" % "2.3.0_0.1.8",
    "com.codahale.metrics" % "metrics-graphite" % "3.0.1",
    "com.kenshoo" %% "metrics-play" % "2.3.0_0.1.8",
    "com.google.guava" % "guava" % "18.0",
    "uk.gov.hmrc" %% "govuk-template" % "4.0.0",
    "uk.gov.hmrc" %% "json-encryption" % "2.0.0",
    "uk.gov.hmrc" %% "play-config" % "2.0.1",
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-partials" % "4.2.0",
    "uk.gov.hmrc" %% "url-builder" % "1.0.0",
    "uk.gov.hmrc" %% "frontend-bootstrap" % "6.4.0",
    "uk.gov.hmrc" %% "play-json-logger" % "2.1.0",
    "uk.gov.hmrc" %% "http-caching-client" % "5.3.0",
    "it.innove" %  "play2-pdf"        % "1.1.3",
    "joda-time" % "joda-time" % "2.8.2",
    "uk.gov.hmrc" %% "play-language" % "2.0.0",
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % "0.2.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test,it"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatestplus" %% "play" % "1.2.0" % "test",
        "org.scalatest" %% "scalatest" % "2.2.2" % scope,
        "org.pegdown" % "pegdown" % "1.4.2" % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {
      override lazy val scope = "it"

      override lazy val test = Seq(
        "org.scalatestplus" %% "play" % "1.2.0",
        "org.scalatest" %% "scalatest" % "2.2.2" % scope,
        "org.pegdown" % "pegdown" % "1.4.2" % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()

}




