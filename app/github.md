# GitHub Integration Steps

## 1. Code Push
- Push all code and infra files (including Dockerfile, YAMLs) to GitHub.

## 2. Release Creation
- Create a new tag and release for every major change.
- This triggers GitHub Actions workflow for build/test.

## 3. Docker Image
- Build Docker image locally or via CI.
- Push to Docker Hub: `docker push phogatavi88/gradlew:<tag>`

## 4. Kubernetes Deploy
- Use YAMLs in `app/` folder to deploy to cluster.
- Example:
  ```sh
  kubectl apply -f app/gapp-deployment-prod.yaml
  kubectl apply -f app/gapp-hpa-prod.yaml
  ```

## 5. Best Practices
- Use separate namespaces for dev/qa/prod.
- Always version your Docker images and YAMLs.
- Use GitHub Actions for CI/CD automation.
