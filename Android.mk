LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME  := Budget
LOCAL_SRC_FILES     := $(call all-subdir-java-files)

ifneq ($(BUDGET_DEBUG),)
  LOCAL_CERTIFICATE := testkey
  LOCAL_AAPT_FLAGS  := --rename-manifest-package com.notriddle.budget.dev
else
  LOCAL_CERTIFICATE := platform
endif

include $(BUILD_PACKAGE)
