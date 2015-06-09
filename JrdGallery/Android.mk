LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
#LOCAL_MODULE_TAGS := user eng

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13
LOCAL_STATIC_JAVA_LIBRARIES += com.jrdcom.android.gallery3d.common2 
LOCAL_STATIC_JAVA_LIBRARIES += com.jrdcom.mediatek.gallery3d.ext
LOCAL_STATIC_JAVA_LIBRARIES += com.jrdcom.mediatek.camera.ext
LOCAL_STATIC_JAVA_LIBRARIES += com.mediatek.transcode
LOCAL_STATIC_JAVA_LIBRARIES += xmp_toolkit
LOCAL_STATIC_JAVA_LIBRARIES += mp4parser
LOCAL_STATIC_JAVA_LIBRARIES += iflytek_camera
LOCAL_JAVA_LIBRARIES := telephony-common
LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_STATIC_JAVA_LIBRARIES += QR_camera

LOCAL_CERTIFICATE:=platform

LOCAL_SRC_FILES := $(call all-java-files-under, src) $(call all-renderscript-files-under, src)
LOCAL_SRC_FILES += $(call all-java-files-under, src_pd)
LOCAL_SRC_FILES += $(call all-java-files-under, ../JrdCamera/src)
LOCAL_SRC_FILES += $(call all-renderscript-files-under, ../JrdCamera/src/com/android/jrdcamera/effects/rs)

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/res packages/apps/JrdCamera/res
LOCAL_AAPT_FLAGS := --auto-add-overlay --extra-packages com.android.jrdcamera

LOCAL_PACKAGE_NAME := JrdGallery

LOCAL_OVERRIDES_PACKAGES := Gallery Gallery3D GalleryNew3D

ifeq ($(TARGET_PRODUCT),soul4)
LOCAL_FULL_MANIFEST_FILE := $(LOCAL_PATH)/manifest/AndroidManifest.xml
endif

#LOCAL_SDK_VERSION := current

# If this is an unbundled build (to install seprately) then include
# the libraries in the APK, otherwise just put them in /system/lib and
# leave them out of the APK
#FR 576703 add by xiangchen
#yaogang.hao for Rename the below libraries to avoid build conflicts with GMS libraries
# PR839108 modify Gallery force close when enter video 2014.11.13 start
ifneq (,$(TARGET_BUILD_APPS))
  LOCAL_JNI_SHARED_LIBRARIES := libjni_mosaic_jrd libjni_jrdcom_eglfence_jrd libjni_filtershow_filters_jrd libjtranscode_jrd lib_mt_image_jni lib_mt_image lib_mt_image_puzzle lib_mt_gif
else
  LOCAL_REQUIRED_MODULES := libjni_mosaic_jrd libjni_jrdcom_eglfence_jrd libjni_filtershow_filters_jrd libjtranscode_jrd lib_mt_image_jni lib_mt_image lib_mt_image_puzzle lib_mt_gif
endif
# PR839108 modify Gallery force close when enter video 2014.11.13 end

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := iflytek_camera:./libs/SpeechServiceLibUtil.jar \
                                        QR_camera:./libs/QRcore.jar


include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under, jni)

ifeq ($(strip $(LOCAL_PACKAGE_OVERRIDES)),)
# Use the following include to make gallery test apk.
include $(call all-makefiles-under, $(LOCAL_PATH))

# Use the following include to make camera test apk.
include $(call all-makefiles-under, ../JrdCamera)

endif
