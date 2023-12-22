// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.example.accessibilityverifier;

import com.deque.axe.android.wrappers.AxeRect;

public class AxeRectProvider {

  public AxeRect createAxeRect(int left, int right, int top, int bottom) {
    return new AxeRect(left, right, top, bottom);
  }
}
