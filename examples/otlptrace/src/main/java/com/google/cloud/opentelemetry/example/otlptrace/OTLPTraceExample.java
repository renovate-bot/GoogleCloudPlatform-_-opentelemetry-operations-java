/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.opentelemetry.example.otlptrace;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class OTLPTraceExample {
  private static final String INSTRUMENTATION_SCOPE_NAME = OTLPTraceExample.class.getName();
  private static final Random random = new Random();

  private static OpenTelemetrySdk openTelemetrySdk;

  private static void myUseCase(String description) {
    // Generate a span
    Span span =
        openTelemetrySdk.getTracer(INSTRUMENTATION_SCOPE_NAME).spanBuilder(description).startSpan();
    try (Scope scope = span.makeCurrent()) {
      span.addEvent("Event A");
      // Do some work for the use case
      for (int i = 0; i < 3; i++) {
        String work = String.format("%s - Work #%d", description, (i + 1));
        doWork(work);
      }
      span.addEvent("Event B");
    } finally {
      span.end();
    }
  }

  private static void doWork(String description) {
    // Child span
    Span span =
        openTelemetrySdk.getTracer(INSTRUMENTATION_SCOPE_NAME).spanBuilder(description).startSpan();
    try (Scope scope = span.makeCurrent()) {
      // Simulate work: this could be simulating a network request or an expensive disk operation
      int randomSleep = random.nextInt(5) * 100;
      span.setAttribute("RandomSleep", randomSleep);
      Thread.sleep(100 + randomSleep);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      span.end();
    }
  }

  public static void main(String[] args) {
    // Configure the OpenTelemetry pipeline with Auto configuration
    openTelemetrySdk = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();

    // Application-specific logic
    myUseCase("One");
    myUseCase("Two");

    // Flush all buffered traces
    CompletableResultCode completableResultCode =
        openTelemetrySdk.getSdkTracerProvider().shutdown();
    // wait till export finishes
    completableResultCode.join(10000, TimeUnit.MILLISECONDS);
  }
}
