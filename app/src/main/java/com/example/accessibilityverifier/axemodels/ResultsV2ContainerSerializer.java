// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.example.accessibilityverifier.axemodels;

import com.deque.axe.android.AxeResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ResultsV2ContainerSerializer {
  private final Gson gson;
  private final TypeAdapter<ResultsV2Container> resultsContainerTypeAdapter =
      new TypeAdapter<ResultsV2Container>() {
        @Override
        public void write(JsonWriter out, ResultsV2Container value) throws IOException {
          out.beginObject();
          out.name("AxeResults").jsonValue(value.AxeResult.toJson());
          out.endObject();
        }

        @Override
        public ResultsV2Container read(JsonReader in) {
          return null;
        }
      };

  public ResultsV2ContainerSerializer(GsonBuilder gsonBuilder) {
    this.gson =
        gsonBuilder
            .registerTypeAdapter(ResultsV2Container.class, this.resultsContainerTypeAdapter)
            .create();
  }

  public String createResultsJson(AxeResult axeResult) {
    ResultsV2Container container = new ResultsV2Container();
    container.AxeResult = axeResult;
    return gson.toJson(container);
  }
}
