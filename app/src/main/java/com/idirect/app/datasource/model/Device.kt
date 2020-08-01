package com.idirect.app.datasource.model

import com.idirect.app.constants.InstagramConstants

class Device(formatted: String) {

    /**
     * Android version to mimic
     */
     val DEVICE_ANDROID_VERSION: String

    /**
     * Android Release
     */
     val DEVICE_ANDROID_RELEASE: String

    /**
     * Device DPI
     */
     val DPI: String

    /**
     * Device Display Resolution
     */
     val DISPLAY_RESOLUTION: String

    /**
     * Device to mimic
     */
     val DEVICE_MANUFACTURER: String

    /**
     * Model to mimic
     */
     val DEVICE_MODEL: String

    /**
     * Device name
     */
     val DEVICE: String

    /**
     * Device CPU
     */
     val CPU: String

    /**
     * Device Capabilities
     */
     val CAPABILITIES = InstagramConstants.DEVICE_CAPABILITIES

    init {
        val format = formatted.split("; ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        this.DEVICE_ANDROID_VERSION =
            format[0].split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        this.DEVICE_ANDROID_RELEASE =
            format[0].split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
        this.DPI = format[1]
        this.DISPLAY_RESOLUTION = format[2]
        this.DEVICE_MANUFACTURER = format[3]
        this.DEVICE_MODEL = format[4]
        this.DEVICE = format[5]
        this.CPU = format[6]
    }

    companion object {

        val GOOD_DEVICES = arrayOf(
            /*
             * OnePlus 3T. Released: November 2016.
             * https://www.amazon.com/OnePlus-A3010-64GB-Gunmetal-International/dp/
             * B01N4H00V8 https://www.handsetdetection.com/properties/devices/OnePlus/A3010
             */
            Device("24/7.0; 380dpi; 1080x1920; OnePlus; ONEPLUS A3010; OnePlus3T; qcom"),

            /*
             * LG G5. Released: April 2016.
             * https://www.amazon.com/LG-Unlocked-Phone-Titan-Warranty/dp/B01DJE22C2
             * https://www.handsetdetection.com/properties/devices/LG/RS988
             */
            Device("23/6.0.1; 640dpi; 1440x2392; LGE/lge; RS988; h1; h1"),

            /*
             * Huawei Mate 9 Pro. Released: January 2017.
             * https://www.amazon.com/Huawei-Dual-Sim-Titanium-Unlocked-International/dp/
             * B01N9O1L6N https://www.handsetdetection.com/properties/devices/Huawei/LON-L29
             */
            Device("24/7.0; 640dpi; 1440x2560; HUAWEI; LON-L29; HWLON; hi3660"),

            /*
             * ZTE Axon 7. Released: June 2016.
             * https://www.frequencycheck.com/models/OMYDK/zte-axon-7-a2017u-dual-sim-lte-a-
             * 64gb https://www.handsetdetection.com/properties/devices/ZTE/A2017U
             */
            Device("23/6.0.1; 640dpi; 1440x2560; ZTE; ZTE A2017U; ailsa_ii; qcom"),

            /*
             * Samsung Galaxy S7 SM-G930F. Released: March 2016.
             * https://www.amazon.com/Samsung-SM-G930F-Factory-Unlocked-Smartphone/dp/
             * B01J6MS6BC
             * https://www.handsetdetection.com/properties/devices/Samsung/SM-G930F
             */
            Device("23/6.0.1; 640dpi; 1440x2560; samsung; SM-G930F; herolte; samsungexynos8890")
        )
    }

}
