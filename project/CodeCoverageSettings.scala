import sbt.{AutoPlugin, Setting}
import scoverage.ScoverageKeys

object CodeCoverageSettings extends AutoPlugin {

  override def trigger = allRequirements

  private val excludedPackages: Seq[String] = Seq(".*Reverse.*")

  private val excludedFiles: Seq[String] = Seq(".*Routes.*")

  override lazy val projectSettings: Seq[Setting[?]] = Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(","),
    ScoverageKeys.coverageExcludedFiles := excludedFiles.mkString(","),
    ScoverageKeys.coverageMinimumStmtTotal := 15.3,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )

}
