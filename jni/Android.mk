###########################################################################
##                                                                       ##
##                  Language Technologies Institute                      ##
##                     Carnegie Mellon University                        ##
##                         Copyright (c) 2012                            ##
##                        All Rights Reserved.                           ##
##                                                                       ##
##  Permission is hereby granted, free of charge, to use and distribute  ##
##  this software and its documentation without restriction, including   ##
##  without limitation the rights to use, copy, modify, merge, publish,  ##
##  distribute, sublicense, and/or sell copies of this work, and to      ##
##  permit persons to whom this work is furnished to do so, subject to   ##
##  the following conditions:                                            ##
##   1. The code must retain the above copyright notice, this list of    ##
##      conditions and the following disclaimer.                         ##
##   2. Any modifications must be clearly marked as such.                ##
##   3. Original authors' names are not deleted.                         ##
##   4. The authors' names are not used to endorse or promote products   ##
##      derived from this software without specific prior written        ##
##      permission.                                                      ##
##                                                                       ##
##  CARNEGIE MELLON UNIVERSITY AND THE CONTRIBUTORS TO THIS WORK         ##
##  DISCLAIM ALL WARRANTIES WITH REGARD TO THIS SOFTWARE, INCLUDING      ##
##  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO EVENT   ##
##  SHALL CARNEGIE MELLON UNIVERSITY NOR THE CONTRIBUTORS BE LIABLE      ##
##  FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES    ##
##  WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN   ##
##  AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,          ##
##  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF       ##
##  THIS SOFTWARE.                                                       ##
##                                                                       ##
###########################################################################
##                                                                       ##
##  Author: Alok Parlikar (aup@cs.cmu.edu)                               ##
##  Date  : July 2012                                                    ##
###########################################################################
##                                                                       ##
## Makefile for Android NDK                                              ##
##                                                                       ##
###########################################################################

LOCAL_PATH:= $(call my-dir)

###########################################################################
# Setup Flite related paths
FLITEDIR := flite/flite-2.0.0-release

# We require that FLITEDIR be defined
ifndef FLITEDIR
  $(error "FLITEDIR variable should be set to path where flite is compiled")
endif

FLITE_BUILD_SUBDIR:=$(TARGET_ARCH_ABI)

ifeq "$(TARGET_ARCH_ABI)" "armeabi-v7a"
  FLITE_BUILD_SUBDIR:="armeabiv7a"
endif
# I added this part
ifeq "$(TARGET_ARCH_ABI)" "arm64-v8a"
    FLITE_BUILD_SUBDIR:="aarch64"
endif

# I changed it from android to none
FLITE_LIB_DIR:= $(FLITEDIR)/build/$(FLITE_BUILD_SUBDIR)-none/lib
###########################################################################

include $(CLEAR_VARS)

#LOCAL_MODULE    := ttsflite
LOCAL_MODULE := flitetts

LOCAL_CPP_EXTENSION := .cc

LOCAL_SRC_FILES := edu_cmu_cs_speech_tts_flite_service.cc \
	edu_cmu_cs_speech_tts_flite_engine.cc \
	edu_cmu_cs_speech_tts_flite_voices.cc \
	edu_cmu_cs_speech_tts_string.cc

LOCAL_C_INCLUDES := $(FLITEDIR)/include

# I removed the .a extension, and it seems to like that better
# Also I just changed this to local static libraries, and I don't know if that's proper
LOCAL_STATIC_LIBRARIES:= $(FLITE_LIB_DIR)/libflite_cmu_indic_lex \
	$(FLITE_LIB_DIR)/libflite_cmu_indic_lang \
	$(FLITE_LIB_DIR)/libflite_cmulex \
	$(FLITE_LIB_DIR)/libflite_usenglish \
	$(FLITE_LIB_DIR)/libflite

ifeq ("$(APP_OPTIM)", "debug")
  LOCAL_CFLAGS += -DFLITE_DEBUG_ENABLED=1
else
  LOCAL_CFLAGS += -DFLITE_DEBUG_ENABLED=0
endif

include $(PREBUILT_STATIC_LIBRARIES)
include $(BUILD_SHARED_LIBRARY)
