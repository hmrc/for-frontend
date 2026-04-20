import play.sbt.PlayImport.filters
import sbt.*

object AppDependencies {

  private val bootstrapVersion    = "10.7.0"
  private val playFrontendVersion = "13.3.0"
  private val voServiceVersion    = "0.9.0"
  private val mongoVersion        = "2.12.0"
  private val jQueryVersion       = "4.0.0"
  private val mailApiVersion      = "2.0.2"

  // Test dependencies
  private val voTestVersion = "0.5.0"

  private val compile = Seq(
    filters,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30" % playFrontendVersion,
    "uk.gov.hmrc"       %% "vo-frontend-service"        % voServiceVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % mongoVersion,
    "org.webjars"        % "jquery"                     % jQueryVersion,
    "com.sun.mail"       % "mailapi"                    % mailApiVersion
  )

  private val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "uk.gov.hmrc" %% "vo-unit-test"           % voTestVersion    % Test
  )

  val appDependencies: Seq[ModuleID] = compile ++ test

}
