package com.sanardev.instagrammqtt.datasource.model.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.sanardev.instagrammqtt.datasource.model.Cookie


class InstagramLoggedUser {

    @SerializedName("pk")
    @Expose
    var pk: Long? = null
    @SerializedName("username")
    @Expose
    var username: String? = null
    @SerializedName("full_name")
    @Expose
    var fullName: String? = null
    @SerializedName("is_private")
    @Expose
    var isPrivate: Boolean? = null
    @SerializedName("profile_pic_url")
    @Expose
    var profilePicUrl: String? = null
    @SerializedName("profile_pic_id")
    @Expose
    var profilePicId: String? = null
    @SerializedName("is_verified")
    @Expose
    var isVerified: Boolean? = null
    @SerializedName("has_anonymous_profile_picture")
    @Expose
    var hasAnonymousProfilePicture: Boolean? = null
    @SerializedName("can_boost_post")
    @Expose
    var canBoostPost: Boolean? = null
    @SerializedName("is_business")
    @Expose
    var isBusiness: Boolean? = null
    @SerializedName("account_type")
    @Expose
    var accountType: Int? = null
    @SerializedName("professional_conversion_suggested_account_type")
    @Expose
    var professionalConversionSuggestedAccountType: Int? = null
    @SerializedName("is_call_to_action_enabled")
    @Expose
    var isCallToActionEnabled: Any? = null
    @SerializedName("can_see_organic_insights")
    @Expose
    var canSeeOrganicInsights: Boolean? = null
    @SerializedName("show_insights_terms")
    @Expose
    var showInsightsTerms: Boolean? = null
    @SerializedName("total_igtv_videos")
    @Expose
    var totalIgtvVideos: Int? = null
    @SerializedName("reel_auto_archive")
    @Expose
    var reelAutoArchive: String? = null
    @SerializedName("has_placed_orders")
    @Expose
    var hasPlacedOrders: Boolean? = null
    @SerializedName("allowed_commenter_type")
    @Expose
    var allowedCommenterType: String? = null
    @SerializedName("nametag")
    @Expose
    var nametag: Nametag? = null
    @SerializedName("is_using_unified_inbox_for_direct")
    @Expose
    var isUsingUnifiedInboxForDirect: Boolean? = null
    @SerializedName("interop_messaging_user_fbid")
    @Expose
    var interopMessagingUserFbid: Long? = null
    @SerializedName("can_see_primary_country_in_settings")
    @Expose
    var canSeePrimaryCountryInSettings: Boolean? = null
    @SerializedName("allow_contacts_sync")
    @Expose
    var allowContactsSync: Boolean? = null
    @SerializedName("phone_number")
    @Expose
    var phoneNumber: String? = null
    @SerializedName("password")
    @Expose
    var password: String? = null
    @SerializedName("cookie")
    var cookie:Cookie?=null
}