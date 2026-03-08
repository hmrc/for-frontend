import play.sbt.PlayImport.filters
import sbt.*

object AppDependencies {

  val bootstrapVersion    = "10.7.0"
  val playFrontendVersion = "12.32.0"
  val mongoVersion        = "2.12.0"

  // Test dependencies
  val scalaTestPlusMockitoVersion = "3.2.19.0"

  private val compile = Seq(
    filters,
    "uk.gov.hmrc"               %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"               %% "play-frontend-hmrc-play-30" % playFrontendVersion,
    "uk.gov.hmrc.mongo"         %% "hmrc-mongo-play-30"         % mongoVersion,
    "org.webjars"                % "jquery"                     % "4.0.0",
    "org.mozilla"                % "rhino"                      % "1.9.1",
    "com.github.java-json-tools" % "json-schema-validator"      % "2.2.14" // must be the same version as in "for-hod-adapter"
  )

  private val test = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30" % bootstrapVersion            % Test,
    "org.scalatestplus" %% "mockito-5-12"           % scalaTestPlusMockitoVersion % Test
  )

  val appDependencies: Seq[ModuleID] = compile ++ test

}
