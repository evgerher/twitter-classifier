name := "twitter-classifier"

version := "0.1"

scalaVersion := "2.11.8"
//scalaVersion := "2.12.7"

val sparkVersion = "2.3.2"

resolvers ++= Seq(
  "bintray-spark-packages" at "https://dl.bintray.com/spark-packages/maven/",
  "apache-snapshots" at "http://repository.apache.org/snapshots/",
  "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion
).map(_ % "compile")

libraryDependencies += "com.rometools" % "rome" % "1.11.1"

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  artifact.name + "." + artifact.extension
//  artifact.name + "_" + sv.binary + "-" + sparkVersion + "_" + module.revision + "." + artifact.extension
}

// https://mvnrepository.com/artifact/com.rometools/rome
//
//lazy val commonSettings = Seq(
//  version := "0.1",
//  scalaVersion := "2.12.7",
//)
//
//lazy val app = (project in file("src")).
//  settings(commonSettings: _*).
//  settings(
//    mainClass in (Compile, run) := Some(“streamer.RSSDemo”),
//    assemblyJarName in assembly := "app.jar",
//    assemblyMergeStrategy in assembly := { x => (assemblyMergeStrategy in assembly).value(x)
//  )
//
//lazy val utils = (project in file("utils")).
//  settings(commonSettings: _*).
//  settings(
//    assemblyJarName in assembly := "utils.jar",
//    // more settings here ...
//  )
//


mainClass in (Compile, run) := Some("RSSDemo")
assemblyJarName in assembly := "app.jar"
assemblyMergeStrategy in assembly := { x => (assemblyMergeStrategy in assembly).value(x) }
