# OTLP Trace with Spring Boot and Google Auth

A sample Spring Boot service that exports OTLP traces, protected by Google authentication. The sample uses auto-refreshing credentials.

### Prerequisites

##### Get Google Cloud Credentials on your machine

```shell
gcloud auth application-default login
```

##### Export the Google Cloud Project ID to `GOOGLE_CLOUD_PROJECT` environment variable:

This comes in handy when creating resources for deployment on Google Cloud.

```shell
export GOOGLE_CLOUD_PROJECT="my-awesome-gcp-project-id"
```

##### Update build.gradle to set required arguments

Update [`build.gradle`](build.grade) to set the following:

```
	'-Dgoogle.cloud.project=your-gcp-project-id',
```

## Running Locally on your machine

To run the spring boot application from project root:

```shell
./gradlew :examples-otlp-spring:run
```

This will start the web application on localhost:8080. The application provides 2 routes:
 - http://localhost:8080/ : The index root; does not generate a trace.
 - http://localhost:8080/work : This route generates a trace.

Visit these routes to interact with the application.

## Running on Google Kubernetes Engine

> [!NOTE]
> You need to have a GKE cluster already setup in your GCP project before continuing with these steps.

Create artifact registry repository to host your containerized image of the application:
```shell
gcloud artifacts repositories create otlp-samples --repository-format=docker --location=us-central1

gcloud auth configure-docker us-central1-docker.pkg.dev
```

Build and push your image to the Artifact Registry.
```shell
./gradlew :examples-otlp-spring:jib --image="us-central1-docker.pkg.dev/$GOOGLE_CLOUD_PROJECT/otlp-samples/spring-otlp-trace-example:v1"
```

Deploy the image on your Kubernetes cluster and setup port forwarding to interact with your cluster:
```shell
sed s/%GOOGLE_CLOUD_PROJECT%/$GOOGLE_CLOUD_PROJECT/g \
 examples/otlp-spring/k8s/deployment.yaml | kubectl apply -f -

# This connects port 8080 on your machine to port 60000 on the spring-otlp-service
kubectl port-forward service/spring-otlp-service 8080:60000
```

After successfully setting up port-forwarding, you can send requests to your cluster via `curl` or some similar tool: 
```shell
curl http://localhost:8080/work?desc=test
```

> [!IMPORTANT]
> If your cluster has GKE Workload Identity enabled, you will need to grant the `telemetry.traces.write` permission to the service account used by
> the application deployment.

This running this command will bind the `telemetry-export` service account used by the application deployment to the `telemetry.tracesWriter` role that grants the `telemetry.traces.write`
permission required to export traces to Google Cloud.

```shell
# Replace the <GCP-PROJECT-NUMBER> with the project number of your GCP Project ID.
gcloud projects add-iam-policy-binding projects/$GOOGLE_CLOUD_PROJECT \
    --role=roles/telemetry.tracesWriter \
    --member=principal://iam.googleapis.com/projects/<GCP-PROJECT-NUMBER>/locations/global/workloadIdentityPools/$GOOGLE_CLOUD_PROJECT.svc.id.goog/subject/ns/default/sa/telemetry-export \
    --condition=None
```

### Sending continuous requests

The sample comes with a [client program](src/test/java/com/google/cloud/opentelemetry/examples/otlpspring/MainClient.java) which sends requests to deployed application on your cluster at a fixed rate.
Once you have port forwarding setup for your cluster, run this client program to send continuous requests to the Spring service to generate multiple traces.

```shell
./gradlew :examples-otlp-spring:runClient
```

### GKE Cleanup

After running the sample, delete the deployment and the service if you are done with it:
```shell
kubectl delete services spring-otlp-service
kubectl delete deployment spring-otlp-trace-example
```
