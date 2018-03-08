# scala-rest-sample
This is a simple example of a REST service implemented in scala. It uses akka-http for implementing the REST endpoints, scalikejdbc for database access, and Postgres for the database. It includes unit & integration tests based on scalatest, and the integration tets make use of a Docker container and custom sbt tasks to run tests against a containerized instance of a Postgres database.

# Prerequisites
The code was developed and tested on Ubuntu Linux 16.04 using the following:
 * Java 1.8 (JDK8)
 * Scala 2.12
 * sbt 1.1.0
 * Docker 17.12.0-ce
 
All prerequisites were installed on a fresh installation of Ubuntu using the script found in setup.sh.

# Running PostgreSQL
The database for this project is run in a container. The "setup.sh" script installs docker, pulls the correct postgres image, and uses that image to build a database container with the analytics database preinstalled. Or, if Docker is already installed, the following commands can be run in the root directory for this project to create the required image:

`sudo docker build -t postgres_analytics -f ./postgres-analytics.dockerfile .`

## Running a local development database container
To create your own container to run the development server, use the following template, replacing `<YOUR-CONTAINER-NAME>` with whatever name you choose:

`sudo docker run --name <YOUR-CONTAINER-NAME> -e POSTGRES_PASSWORD=postgres -d postgres_analytics`

In order for the application to locate the database, you'll need to update the application.conf with your new container's IP address. You can use the following command to get the IP address:

`sudo docker inspect --format '{{.NetworkSettings.IPAddress}}' <YOUR-CONTAINER-NAME>`

## Integration test database
The integration tests use custom sbt tasks to manage their own database, creating it before running any tests and destroying it when the test run is complete. So long as the analytics database container image has been created (see above), there are no additional steps required to set up integration tests.

# sbt
To build and run the application and tests, first launch the sbt console:

`sbt`

You can then use all the standard commands—`compile`, `test`, `run`, etc.

To run integration tests, use `it:test`.

# scalastyle
The project also has the scalastyle plugin installed. From the sbt console, you use use the `scalastyle` command to analyze the code using a preconfigured set of style rules.
