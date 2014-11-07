LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := ftd2xx
LOCAL_SRC_FILES := libftd2xx.a

include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE    := ftd2xx-jni
LOCAL_SRC_FILES := ftd2xxjni.cpp
LOCAL_STATIC_LIBRARIES := libftd2xx
LOCAL_LDLIBS    := -llog -lstdc++

include $(BUILD_SHARED_LIBRARY)


