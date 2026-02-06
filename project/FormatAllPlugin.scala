import org.scalafmt.sbt.ScalafmtPlugin
import org.scalafmt.sbt.ScalafmtPlugin.autoImport.{scalafmtAll, scalafmtFailOnErrors, scalafmtSbt}
import sbt.*
import sbt.Keys.*
import scalafix.sbt.ScalafixPlugin
import scalafix.sbt.ScalafixPlugin.autoImport.scalafixAll
import scoverage.ScoverageSbtPlugin

/**
  * Provides sbt task `formatAll` and settings for `scalafmt`, `scalafix` plugins.
  *
  * @author Yuriy Tumakha
  */
object FormatAllPlugin extends AutoPlugin {

  override def requires: Plugins = ScalafmtPlugin && ScalafixPlugin && ScoverageSbtPlugin

  override def trigger = allRequirements

  object autoImport {
    val formatAll: TaskKey[Unit] = taskKey[Unit]("Run scalafmt scalafix for all files")
  }

  import autoImport.*

  override lazy val globalSettings: Seq[Setting[?]] = scalafmtSettings ++ scalafixSettings

  override lazy val projectSettings: Seq[Setting[?]] =
    Seq(
      commands += Command.command("coverage")(state => "formatAll" :: "it/formatAll" :: "set ThisBuild / coverageEnabled := true" :: state),
      formatAll :=
        Def
          .sequential(
            scalafmtAll,
            Compile / scalafmtSbt,
            scalafixAll.toTask("")
          )
          .value
    )

  private def scalafmtSettings: Seq[Setting[?]] = Seq(
    scalafmtFailOnErrors := true
  )

  private def scalafixSettings: Seq[Setting[?]] = Seq(
    semanticdbEnabled := true
  )

}
