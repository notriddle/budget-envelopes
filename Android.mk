#
# This file is a part of Budget with Envelopes.
# Copyright 2013 Emilio López <emilio@elopez.com.ar>
#
# Budget is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Budget is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with Budget. If not, see <http://www.gnu.org/licenses/>.

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
