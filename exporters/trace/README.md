# Cloud Trace Exporter for OpenTelemetry

  OpenTelemetry Google Cloud Trace Exporter allows the user to send collected traces to Google Cloud. 

[![Maven Central][maven-image]][maven-url]

 [Google Cloud Trace](https://cloud.google.com/trace) is a distributed tracing backend system. It helps developers to gather timing data needed to troubleshoot latency problems in microservice & monolithic architectures. It manages both the collection and lookup of gathered trace data.

## Setup

### Prerequisites
  Google Cloud Trace is a managed service provided by Google Cloud Platform.
  To use this exporter, you must have an application that you'd like to trace. The app can be on Google Cloud Platform, on-premise, or another cloud platform.
  
  In order to be able to push your traces to Trace, you must:
  
1. [Create a Cloud project](https://support.google.com/cloud/answer/6251787?hl=en).
2. [Enable billing](https://support.google.com/cloud/answer/6288653#new-billing).
3. [Enable the Trace API](https://console.cloud.google.com/apis/api/cloudtrace.googleapis.com/overview).

### Installation

This artifact is currently published to [Maven Central](https://search.maven.org/search?q=com.google.cloud.opentelemetry).

You can pull this library in via the following maven
config:

```
<dependency>
  <groupId>com.google.cloud.opentelemetry</groupId>
  <artifactId>exporter-trace</artifactId>
  <version>0.31.0</version>
</dependency>
```

### Usage
If you are running in a GCP environment, the exporter will automatically authenticate using the environment's service account. If not, you will need to follow the instructions in Authentication.

See [the code example](../../examples/trace) for details.

#### Create the exporter

You can create exporter using the default configuration as follows:

```java
    SpanExporter traceExporter = TraceExporter.createWithDefaultConfiguration(); 
```

You can also customize the configuration using a TraceConfiguration object
```java
    SpanExporter traceExporter = TraceExporter.createWithConfiguration(
      TraceConfiguration.builder().setProjectId("myCoolGcpProject").build()
    );
```

You can register a `SpanExporter` with your OpenTelemetry instance as follows:

```java
  OpenTelemetrySdk.builder()
        .setTracerProvider(
            SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(traceExporter).build())
                .build())
        .build();
```

#### Specifying a project ID
This exporter uses [google-cloud-java](https://github.com/GoogleCloudPlatform/google-cloud-java),
for details about how to configure the project ID see [here](https://github.com/GoogleCloudPlatform/google-cloud-java#specifying-a-project-id).

If you prefer to manually set the project ID, change it in the TraceConfiguration:
```java
TraceConfiguration.builder().setProjectId("MyProjectId").build();
```
before passing it in to the constructor
#### Authentication
  This exporter uses [google-cloud-java](https://github.com/googleapis/google-cloud-java), for details about how to configure the authentication see [here](https://github.com/googleapis/google-cloud-java#authentication).  


If you prefer to manually set the credentials use:
```java
TraceConfiguration.builder()
    .setCredentials(new GoogleCredentials(new AccessToken(accessToken, expirationTime)))
    .setProjectId( "MyProjectId")
    .build();
```
before passing it into the TraceExporter constructor

    
  In the case that there are problems creating a service account key, make sure that the **constraints/iam.disableServiceAccountKeyCreation** boolean variable is set to false. This can be edited on Google Cloud by clicking on Navigation Menu -> IAM & Admin -> Organization Policies -> Disable Service Account Key Creation -> Edit  
    
  If you are unable to edit this variable due to lack of permission, you can authenticate by running `gcloud auth application-default login` in the command line.

#### Java Versions
Java 8 or above is required for using this exporter.


#### Special Attributes

The Trace Exporter will add the following additional attributes onto all exported spans:

- All `Resource` attributes that do not have a key name conflict with underlying `Span` attributes
- `g.co/r/{resourcee_type}/{label}` attributes that correlate with GCP monitored resource.
- A `g.co/agent` attribute that denotes the exporter used to write the Span.
- `otel.scope.name` and `otel.scope.version`, as specified [by OpenTelemetry exporter requirements](https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/trace/sdk_exporters/non-otlp.md#instrumentationscope)

#### Span Attribute relabelling

The Trace Exporter attempts to convert from OpenTelemetry semantic conventions into Cloud Trace conventions.

By default, the following span or event attributes will be converted as follows:

| OpenTelemetry Attribute        | Cloud Trace Attribute   |
|--------------------------------|-------------------------|
| `http.host`                    | `/http/host`            |
| `http.method`                  | `/http/method`          |
| `http.target`                  | `/http/path`            |
| `http.status_code`             | `/http/status_code`     |
| `http.url`                     | `/http/url`             |
| `http.request_content_length`  | `/http/request/size`    |
| `http_response_content_length` | `/http/response/size`   |
| `http.scheme`                  | `/http/client_protocol` |
| `http.route`                   | `/http/route`           |
| `http.user_agent`              | `/http/user_agent`      |
| `exception.type`               | `/error/name`           |
| `exception.message`            | `/error/message`        |
| `thread.id`                    | `/tid`                  |

This can be disabled by clearing out the mapping configuration:

```java
TraceConfiguration.builder()
        .setAttributeMapping(ImmutableMap.of())
        .build()
```


## Useful Links
  - For more information on OpenTelemetry, visit: https://opentelemetry.io/  
  - For more about OpenTelemetry Java, visit: https://github.com/open-telemetry/opentelemetry-java  
  - Learn more about Google Cloud Trace at https://cloud.google.com/trace


[maven-image]: https://img.shields.io/maven-central/v/com.google.cloud.opentelemetry/exporter-trace?color=dark-green
[maven-url]: https://maven-badges.herokuapp.com/maven-central/com.google.cloud.opentelemetry/exporter-trace