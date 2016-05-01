FROM java:7
COPY . /usr/src/myapp
WORKDIR /usr/src/myapp
RUN apt-get update && apt-get install -y make
RUN make clean
RUN make
