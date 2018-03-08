# scala-rest-sample
This is a simple example of a REST service implemented in scala. It uses akka-http for implementing the REST endpoints, scalikejdbc for database access, and Postgres for the database. It includes unit & integration tests based on scalatest, and the integration tets make use of a Docker container and custom sbt tasks to run tests against a real, containerized instance of a Postgres database.

# Prerequisites
The code was developed and tested on Ubuntu Linux 16.04 using the following:
