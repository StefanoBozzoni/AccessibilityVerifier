// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.example.accessibilityverifier.axemodels;

import android.util.Log;

import com.deque.axe.android.AxeResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultsV2ContainerSerializer {
  private final Gson gson;
  private final Gson gson2;
  private final TypeAdapter<ResultsV2Container> resultsContainerTypeAdapter =
      new TypeAdapter<ResultsV2Container>() {
        @Override
        public void write(JsonWriter out, ResultsV2Container value) throws IOException {
          out.beginObject();
          String jsonString  = value.AxeResult.toJson();
          Pattern pattern = Pattern.compile("\\s*(\\r|\\n)\\s*");
          Matcher matcher = pattern.matcher(jsonString);
          jsonString = ((Matcher) matcher).replaceAll(" ");
          out.name("AxeResults").jsonValue(jsonString);
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
            //.registerTypeAdapter(AxeResult.class, new AxeResultSerializer())
            .registerTypeAdapter(ResultsV2Container.class, this.resultsContainerTypeAdapter)
            .setPrettyPrinting()
            .create();

      gson2 = new GsonBuilder()
              //.registerTypeAdapter(AxeResult.class, new AxeResultSerializer())
              .setPrettyPrinting()
              .create();
  }

  public String createResultsJson(AxeResult axeResult) {
    ResultsV2Container container = new ResultsV2Container();
    container.AxeResult = axeResult;
    return gson.toJson(container);
  }
}
