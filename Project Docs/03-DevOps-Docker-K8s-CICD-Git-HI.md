# DevOps Roadmap: Docker -> Kubernetes -> CI/CD -> Git Version Control

Yeh document specifically aapke goal ke liye banaya gaya hai: repository me image rakhna, fir Kubernetes cluster par le jaana, aur Git based CI/CD set karna.

## 1. Current State (as-is)

- Gradle project hai.
- Existing GitHub Action release event par build + publish run karti hai.
- BootBuildImage task configured hai (Paketo buildpacks).
- Java version alignment verify karna zaroori hai (source 1.8 vs CI JDK 17).

## 2. Target State (to-be)

1. Har merge to main par:
   - Test pass
   - Build artifact
   - Docker image build
   - Image registry push
2. Tag/release par:
   - Versioned immutable image
   - Kubernetes deploy (staging -> prod promotion)
3. Rollback ready strategy.

## 3. Dockerization Plan

### 3.1 Recommended image approach

Option A: Spring Boot buildpacks (fast start)
- Pros: Dockerfile ki zaroorat nahi, standardized output.

Option B: Custom Dockerfile (fine control)
- Pros: predictable layers, tighter image hardening.

### 3.2 Tagging strategy

- latest (sirf non-prod convenience)
- git-sha tag (immutable traceability)
- semver tag (vX.Y.Z release)

### 3.3 Registry recommendations

- GitHub Container Registry ya GitLab Registry ya Docker Hub (team policy ke hisaab se).

## 4. Kubernetes Rollout Plan

### 4.1 Minimum manifests

1. Namespace
2. Deployment
3. Service
4. ConfigMap
5. Secret
6. HorizontalPodAutoscaler (phase 2)
7. Ingress (if external exposure needed)

### 4.2 App config mapping

- application.yaml based values ko ConfigMap/Secret me externalize karo.
- H2 in-memory demo hai; production me external DB migrate karo.

### 4.3 Readiness/Liveness

- Spring Actuator health endpoints use karo.
- Startup delay ko dekhkar readiness probe tune karo.

## 5. CI/CD Pipeline Stages

1. Lint/Static checks
2. Unit tests
3. Build jar
4. Build container image
5. Security scan (SCA + image)
6. Push image
7. Deploy staging
8. Smoke test
9. Manual approval gate
10. Deploy production

## 6. Git Workflow for Version Control (practical)

### 6.1 Branch model

1. main
   - Production-ready stable branch
2. develop (optional)
   - Integration branch for multiple features
3. feature/<ticket-id>-<short-name>
   - New feature work
4. hotfix/<ticket-id>-<short-name>
   - Urgent production fix
5. release/vX.Y.Z
   - Final stabilization before release (optional)

### 6.2 Commit convention

Conventional Commits adopt karo:
- feat: new feature
- fix: bug fix
- refactor: structure improvement
- test: tests add/update
- docs: documentation changes
- chore: tooling/config work

Example:
- feat: add portfolio update event serialization
- fix: load position csv via classpath resource

### 6.3 Pull Request rules

1. PR template mandatory
2. Minimum 1 reviewer approval
3. Required status checks pass
4. No direct push to main
5. Squash merge or rebase merge policy set

### 6.4 Versioning model

Semantic Versioning use karo:
- MAJOR: breaking change
- MINOR: backward-compatible feature
- PATCH: backward-compatible bug fix

### 6.5 Release control

1. Changelog maintain karo.
2. Signed tags (optional but recommended).
3. Release notes me image tag + commit SHA mention karo.

## 7. Security and Governance Checklist

1. Secret scanning enable karo.
2. Dependency updates automate karo (Dependabot/Renovate).
3. Branch protection rules enforce karo.
4. Artifact retention policy define karo.
5. SBOM generate and store karo.

## 8. Recommended Execution Order (next steps)

1. Java/build compatibility clean-up.
2. Docker image build standard finalize.
3. Registry authentication and push flow setup.
4. K8s base manifests add.
5. Staging deploy pipeline create.
6. Production promotion with manual approvals.
7. Monitoring + alerting integrate.

## 9. Aapko Ab Practical Me Kya Karna Hoga

1. Repository me Docker image build/push workflow finalize karna.
2. Cluster ke liye namespace + deployment manifests banana.
3. Environment specific config externalize karna.
4. Branch protection and PR rules enable karna.
5. Release tagging discipline start karna.

Is roadmap ke baad project demo se production-style delivery pipeline tak move ho jayega.
