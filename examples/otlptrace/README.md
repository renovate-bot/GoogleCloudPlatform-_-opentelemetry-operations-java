# OTLP Trace with Google Auth Example

Run this sample to connect to an endpoint that is protected by GCP authentication.

First, get GCP credentials on your machine:

```shell
gcloud auth application-default login
```
Executing this command will save your application credentials to default path which will depend on the type of machine -
 - Linux, macOS: `$HOME/.config/gcloud/application_default_credentials.json`
 - Windows: `%APPDATA%\gcloud\application_default_credentials.json`

Next, export the `GOOGLE_CLOUD_PROJECT` environment variable:
```shell
# Use your GCP project ID
export GOOGLE_CLOUD_PROJECT="your-gcp-project-id"
```
This environment variable also configures the [OpenTelemetry GCP Auth Extension](https://github.com/open-telemetry/opentelemetry-java-contrib/tree/main/gcp-auth-extension) that facilitates authentication to GCP.

Finally, to run the sample from the project root:

```
./gradlew :examples-otlptrace:run
```

Running this sample will generate and export Traces to Google Cloud.
