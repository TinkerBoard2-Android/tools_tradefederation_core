# Copyright (C) 2010 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Makefile to build device-based native tests.

# GTest does not build on the simulator because it depends on STLport.
ifneq ($(TARGET_SIMULATOR),true)

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

# All source files will be bundled into one test module
LOCAL_SRC_FILES := TradeFedNativeTestApp_test.cpp

LOCAL_CFLAGS := -Wall -Werror

# All gtests in all files should be compiled into one binary
# The standard naming should conform to: <module_being_tested>tests
# For example, for libjingle, use libjingletests
LOCAL_MODULE := tfnativetests

LOCAL_MODULE_TAGS := optional

include $(BUILD_NATIVE_TEST)

endif
