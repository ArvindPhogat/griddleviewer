# Docker Image & Usage

## Build Image
```sh
docker build -t gapp:latest .
```

## Tag for Docker Hub
```sh
docker tag gapp:latest phogatavi88/gradlew:v1.0.3
```

## Push to Docker Hub
```sh
docker push phogatavi88/gradlew:v1.0.3
```

## Pull from Docker Hub
```sh
docker pull phogatavi88/gradlew:v1.0.3
```

## Run Locally
```sh
docker run -p 8080:8080 gapp:latest
```

## Use in Kubernetes
- Reference image in your deployment YAMLs (see grelew.md)
