import play.core.PlayVersion
import play.sbt.PlayImport.filters
import sbt.*

object AppDependencies {

  val bootstrapVersion    = "9.0.0"
  val playFrontendVersion = "10.5.0"
  val mongoVersion        = "2.1.0"

  // Test dependencies
  val scalatestPlusPlayVersion    = "7.0.1"
  val scalatestVersion            = "3.2.19"
  val scalaTestPlusMockitoVersion = "3.2.19.0"
  val flexMarkVersion             = "0.64.8"

  private val compile = Seq(
    filters,
    "uk.gov.hmrc"               %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"               %% "play-frontend-hmrc-play-30" % playFrontendVersion,
    "uk.gov.hmrc.mongo"         %% "hmrc-mongo-play-30"         % mongoVersion,
    "org.xhtmlrenderer"          % "flying-saucer-pdf-itext5"   % "9.7.1",
    "nu.validator"               % "htmlparser"                 % "1.4.16",
    "org.webjars"                % "jquery"                     % "3.7.1",
    "com.github.java-json-tools" % "json-schema-validator"      % "2.2.14", // must be the same version as in "for-hod-adapter"
    "org.webjars.bower"          % "compass-mixins"             % "1.0.2"
  )

  private val test = Seq(
    "org.playframework"      %% "play-test"          % PlayVersion.current         % Test,
    "org.scalatest"          %% "scalatest"          % scalatestVersion            % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion    % Test,
    "org.scalatestplus"      %% "mockito-5-12"       % scalaTestPlusMockitoVersion % Test,
    "com.vladsch.flexmark"    % "flexmark-all"       % flexMarkVersion             % Test // for scalatest 3.2.x
  )

  val appDependencies: Seq[ModuleID] = compile ++ test

}
