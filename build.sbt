name := "twitter-classifier-scala"

version := "0.1"
scalaVersion := "2.12.7"

val sparkVersion = "2.4.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion
).map(_ % "compile")


libraryDependencies += "com.rometools" % "rome" % "1.11.1"


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
//  mainClass in (Compile, run) := Some(“RSSDemo”),
mainClass in (Compile, run) := Some("RSSDemo")
assemblyJarName in assembly := "app.jar"
assemblyMergeStrategy in assembly := { x => (assemblyMergeStrategy in assembly).value(x) }
