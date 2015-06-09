LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
#LOCAL_MODULE_TAGS := user eng

include $(call all-makefiles-under, $(LOCAL_PATH))


