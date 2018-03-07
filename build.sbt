import scala.sys.process._

lazy val commonSettings = Seq(
  organization := "com.kenmcgowan",
  version := "0.1",
  scalaVersion := "2.12.4"
)

lazy val akkaVersion = "2.5.9"
lazy val akkaHttpVersion = "10.1.0-RC2"
lazy val scalikeVersion = "3.2.1"
lazy val scalatest = "org.scalatest" %% "scalatest" % "3.0.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "org.scalikejdbc" %% "scalikejdbc" % scalikeVersion,
  "org.scalikejdbc" %% "scalikejdbc-test" % scalikeVersion,
  "org.scalikejdbc" %% "scalikejdbc-config" % scalikeVersion,
  "org.postgresql" % "postgresql" % "42.1.4",
  "org.slf4j" % "slf4j-simple" % "1.7.25",
  "org.scalaz" %% "scalaz-core" % "7.2.20"
)

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      scalatest % "it,test",
      "org.scalamock" %% "scalamock" % "4.1.0" % Test
    )
  )

lazy val imageName = "postgres_analytics"
lazy val containerName = "postgres_analytics_it"

lazy val dbcreate = taskKey[Unit]("Attempts to create and start a new instance of the analytics postgres DB container for integration tests")
lazy val dbdestroy = taskKey[Unit]("Attempts to destroy the analytics db container for integration tests (the container itself, not the image)")
lazy val dbstart = taskKey[Unit]("Attempts to start a stopped, preexisting instance of the analytics DB container for integration tests")
lazy val dbstop = taskKey[Unit]("Attempts to stop a preexisting instance of the analytics db container for integration tests")
lazy val dbteardown = taskKey[Unit]("Attempts to stop and destroy a preexisting, running instance of the analytics db container for integration tests")
lazy val dbip = taskKey[String]("Gets the IP address for any running instance of the analytics db container for integration tests")
lazy val dbconfigure = taskKey[Unit]("Generates a configuration file that references dynamic properties of containers for testing")
lazy val dbsetup = taskKey[Unit]("Set up the databse for integration testing, including configuration")

def createTestContainer: Unit = { s"sudo docker run --name $containerName -e POSTGRES_PASSWORD=postgres -d $imageName" #&& "sleep 2" ! }
def destroyTestContainer: Unit = { s"sudo docker rm --force $containerName" ! }
def startTestContainer: Unit = { s"sudo docker start $containerName" ! }
def stopTestContainer: Unit = { s"sudo docker stop $containerName" ! }
def getTestContainerIP: String = { {
  s"sudo docker inspect --format '{{.NetworkSettings.IPAddress}}' $containerName" #|
  """grep -oE [0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}""" !! } replace("\n", "") }
def generateTestConf: Unit = { s"""echo db.analytics.url="jdbc:postgresql://$getTestContainerIP:5432/analytics"""" #> file("./src/it/resources/generated.conf") ! }

dbcreate in IntegrationTest := { createTestContainer }
dbdestroy in IntegrationTest := { destroyTestContainer }
dbstart in IntegrationTest := { startTestContainer }
dbstop in IntegrationTest := { stopTestContainer }
dbip in IntegrationTest := { getTestContainerIP }
dbconfigure in IntegrationTest := { generateTestConf }
dbsetup in IntegrationTest := { Def.sequential(
  dbcreate in IntegrationTest,
  dbconfigure in IntegrationTest).value
  }

(test in IntegrationTest) := {
  (test in IntegrationTest)
    .dependsOn(dbsetup in IntegrationTest)
    .andFinally { destroyTestContainer }
  }.value
