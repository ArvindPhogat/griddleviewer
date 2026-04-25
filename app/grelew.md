# Kubernetes Manifest for gapp

YAML files for dev, qa, prod environments, including deployment, service, and autoscaling (HPA).

## Files
- gapp-namespaces.yaml
- gapp-deployment-dev.yaml
- gapp-deployment-qa.yaml
- gapp-deployment-prod.yaml
- gapp-hpa-prod.yaml

## Usage
Apply each file with:
```sh
kubectl apply -f <filename>
```

---

## gapp-namespaces.yaml
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: dev
---
apiVersion: v1
kind: Namespace
metadata:
  name: qa
---
apiVersion: v1
kind: Namespace
metadata:
  name: prod
```

## gapp-deployment-dev.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gapp
  namespace: dev
spec:
  replicas: 1
  selector:
    matchLabels:
      app: gapp
  template:
    metadata:
      labels:
        app: gapp
    spec:
      containers:
      - name: gapp
        image: phogatavi88/gradlew:v1.0.3
        ports:
        - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: gapp-service
  namespace: dev
spec:
  type: NodePort
  selector:
    app: gapp
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
      nodePort: 30080
```

## gapp-deployment-qa.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gapp
  namespace: qa
spec:
  replicas: 1
  selector:
    matchLabels:
      app: gapp
  template:
    metadata:
      labels:
        app: gapp
    spec:
      containers:
      - name: gapp
        image: phogatavi88/gradlew:v1.0.3
        ports:
        - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: gapp-service
  namespace: qa
spec:
  type: NodePort
  selector:
    app: gapp
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
      nodePort: 30081
```

## gapp-deployment-prod.yaml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: gapp
  namespace: prod
spec:
  replicas: 3
  selector:
    matchLabels:
      app: gapp
  template:
    metadata:
      labels:
        app: gapp
    spec:
      containers:
      - name: gapp
        image: phogatavi88/gradlew:v1.0.3
        ports:
        - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: gapp-service
  namespace: prod
spec:
  type: NodePort
  selector:
    app: gapp
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
      nodePort: 30082
```

## gapp-hpa-prod.yaml
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: gapp-hpa
  namespace: prod
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: gapp
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 60
```
