name := "Test_DE"

version := "0.1"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  "marlin" at "https://github.com/PasaLab/marlin/",
  "Maven repository" at "https://repo1.maven.org/maven2/",
  "cakesolutions" at "http://dl.bintray.com/cakesolutions/maven/",
  "bintray-backline-open-source-releases" at "https://dl.bintray.com/backline/open-source",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "conjars.org" at "http://conjars.org/repo",
  "lightshed-maven" at "http://dl.bintray.com/content/lightshed/maven",
  "mvnrepository" at "https://mvnrepository.com/artifact/com.typesafe/config"
)

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.0.0",
  "org.apache.spark" %% "spark-sql" % "2.0.0",
  "org.scalanlp" %% "breeze" % "0.13.2",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "org.apache.spark" % "spark-mllib_2.11" % "2.1.0",
  "com.github.kxbmap" %% "configs" % "0.4.4"
)
// https://mvnrepository.com/artifact/com.typesafe/config
libraryDependencies += "com.typesafe" % "config" % "1.2.1"


