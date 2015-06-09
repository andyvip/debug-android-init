LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
#LOCAL_MODULE_TAGS := user eng
# PR839108 modify Gallery force close when enter video 2014.11.13 addlibjtranscode_jrd.so
LOCAL_PREBUILT_LIBS := lib_mt_image_jni:lib_mt_image_jni.so \
                       lib_mt_image:lib_mt_image.so \
                       lib_mt_gif:lib_mt_gif.so \
                       lib_mt_image_puzzle:lib_mt_image_puzzle.so \
                       libjtranscode_jrd:libjtranscode_jrd.so
include $(BUILD_MULTI_PREBUILT)

