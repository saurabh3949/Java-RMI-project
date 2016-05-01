sh cleanup.sh
docker network create -d bridge sg-network
echo "Created Network bridge"
docker build -f Dockerfile -t rmi-app .
echo "Docker image created! Now starting server and client..."
docker run -d --net=sg-network --name pingserver rmi-app java PingPong.PingServerFactory 8000
docker run -d --net=sg-network --name pingclient rmi-app java PingPong.PingPongClient pingserver 8000
echo "Server and client up!"
docker logs -f pingclient
