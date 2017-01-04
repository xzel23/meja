/*
 * Copyright 2016 axel@dua3.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dua3.meja.model;

/**
 * Options for generating CellRef.
 */
public enum RefOption {
  /**
   * make the row fix by adding a '$' before the row number
   */
  FIX_ROW,
  /**
   * make the column fix by adding a '$' before the column index
   */
  FIX_COLUMN,
  /**
   * include the sheet name
   */
  WITH_SHEET
};

