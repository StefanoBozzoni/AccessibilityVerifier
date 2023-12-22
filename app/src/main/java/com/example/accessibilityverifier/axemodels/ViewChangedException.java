// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.example.accessibilityverifier.axemodels;

class ViewChangedException extends Exception {
  public ViewChangedException() {
    this("");
  }

  public ViewChangedException(String additionalMessage) {
    super("The view hierarchy changed while building the AxeView tree. " + additionalMessage);
  }
}
