name := "twitter-classifier"

version := "0.1"

scalaVersion := "2.11.8"
val sparkVersion = "2.3.2"
//lazy val `classifier` = project.dependsOn(project)
//lazy val `preprocessor` = project.dependsOn(project)
//lazy val `streamer` = project.dependsOn(project)

//lazy val `twiter_classifier` = project


resolvers ++= Seq(
  "bintray-spark-packages" at "https://dl.bintray.com/spark-packages/maven/",
  "apache-snapshots" at "http://repository.apache.org/snapshots/",
  "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)
//addSbtPlugin("org.spark-packages" % "sbt-spark-package" % "0.2.6")


libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "com.github.fommil.netlib" % "all" % "1.1.2" pomOnly()
)

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  artifact.name + "." + artifact.extension
//  artifact.name + "_" + sv.binary + "-" + sparkVersion + "_" + module.revision + "." + artifact.extension
}