// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.example.accessibilityverifier;

import java.io.ByteArrayOutputStream;

public class ByteArrayOutputStreamProvider {

  public ByteArrayOutputStream get() {
    return new ByteArrayOutputStream();
  }
}
