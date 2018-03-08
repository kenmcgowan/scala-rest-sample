# This is the script that was used to install Java and Docker.

sudo apt update
sudo apt install -y apt-transport-https ca-certificates curl software-properties-common

# Add repository for Java
sudo add-apt-repository -y ppa:webupd8team/java

# Add repository for Docker
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository -y "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"

# Set up source for sbt
echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823

sudo apt update

# Install Java, Scala, sbt, and Docker
sudo apt install -y oracle-java8-installer
sudo apt install -y oracle-java8-set-default
sudo apt install -y scala
sudo apt-get install sbt
sudo apt install -y docker-ce

# Get the standard docker image for postgres
sudo docker pull postgres

# Build a Postgres Docker container with the analytics schema preinstalled.
# The integration tests will use this image to dynaimcally create their own
# containerized instance of Postgres.
sudo docker build -t postgres_analytics -f ./postgres-analytics.dockerfile .

# To create your own instance of a containerized analytics DB:
# sudo docker run --name <YOUR-CONTAINER-NAME> -e POSTGRES_PASSWORD=postgres -d postgres_analytics

# To run psql against your Postgres container:
# sudo docker run -it --rm --link <YOUR-CONTAINER-NAME> postgres_analytics psql -h <YOUR-CONTAINER-NAME> -U postgres -d analytics

# You can find more sample container management scripts in the build.sbt task definitions.
