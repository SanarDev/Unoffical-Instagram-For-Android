package com.idirect.app.datasource.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class User {

    @SerializedName("pk")
    @Expose
    private long pk;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("full_name")
    @Expose
    private String fullName;
    @SerializedName("is_private")
    @Expose
    private Boolean isPrivate;
    @SerializedName("profile_pic_url")
    @Expose
    private String profilePicUrl;
    @SerializedName("profile_pic_id")
    @Expose
    private String profilePicId;
    @SerializedName("is_verified")
    @Expose
    private Boolean isVerified;
    @SerializedName("has_anonymous_profile_picture")
    @Expose
    private Boolean hasAnonymousProfilePicture;
    @SerializedName("media_count")
    @Expose
    private Integer mediaCount;
    @SerializedName("geo_media_count")
    @Expose
    private Integer geoMediaCount;
    @SerializedName("follower_count")
    @Expose
    private Integer followerCount;
    @SerializedName("following_count")
    @Expose
    private Integer followingCount;
    @SerializedName("following_tag_count")
    @Expose
    private Integer followingTagCount;
    @SerializedName("biography")
    @Expose
    private String biography;
    @SerializedName("can_link_entities_in_bio")
    @Expose
    private Boolean canLinkEntitiesInBio;
    @SerializedName("biography_with_entities")
    @Expose
    private BiographyWithEntities biographyWithEntities;
    @SerializedName("external_url")
    @Expose
    private String externalUrl;
    @SerializedName("can_boost_post")
    @Expose
    private Boolean canBoostPost;
    @SerializedName("can_see_organic_insights")
    @Expose
    private Boolean canSeeOrganicInsights;
    @SerializedName("show_insights_terms")
    @Expose
    private Boolean showInsightsTerms;
    @SerializedName("can_convert_to_business")
    @Expose
    private Boolean canConvertToBusiness;
    @SerializedName("can_create_sponsor_tags")
    @Expose
    private Boolean canCreateSponsorTags;
    @SerializedName("is_allowed_to_create_standalone_personal_fundraisers")
    @Expose
    private Boolean isAllowedToCreateStandalonePersonalFundraisers;
    @SerializedName("can_create_new_standalone_personal_fundraiser")
    @Expose
    private Boolean canCreateNewStandalonePersonalFundraiser;
    @SerializedName("can_be_tagged_as_sponsor")
    @Expose
    private Boolean canBeTaggedAsSponsor;
    @SerializedName("can_see_support_inbox")
    @Expose
    private Boolean canSeeSupportInbox;
    @SerializedName("can_see_support_inbox_v1")
    @Expose
    private Boolean canSeeSupportInboxV1;
    @SerializedName("total_igtv_videos")
    @Expose
    private Integer totalIgtvVideos;
    @SerializedName("total_clips_count")
    @Expose
    private Integer totalClipsCount;
    @SerializedName("total_ar_effects")
    @Expose
    private Integer totalArEffects;
    @SerializedName("reel_auto_archive")
    @Expose
    private String reelAutoArchive;
    @SerializedName("is_profile_action_needed")
    @Expose
    private Boolean isProfileActionNeeded;
    @SerializedName("usertags_count")
    @Expose
    private Integer usertagsCount;
    @SerializedName("usertag_review_enabled")
    @Expose
    private Boolean usertagReviewEnabled;
    @SerializedName("is_needy")
    @Expose
    private Boolean isNeedy;
    @SerializedName("is_interest_account")
    @Expose
    private Boolean isInterestAccount;
    @SerializedName("has_chaining")
    @Expose
    private Boolean hasChaining;
    @SerializedName("hd_profile_pic_versions")
    @Expose
    private List<HdProfilePicVersion> hdProfilePicVersions = null;
    @SerializedName("hd_profile_pic_url_info")
    @Expose
    private HdProfilePicUrlInfo hdProfilePicUrlInfo;
    @SerializedName("has_placed_orders")
    @Expose
    private Boolean hasPlacedOrders;
    @SerializedName("can_tag_products_from_merchants")
    @Expose
    private Boolean canTagProductsFromMerchants;
    @SerializedName("fbpay_experience_enabled")
    @Expose
    private Boolean fbpayExperienceEnabled;
    @SerializedName("show_conversion_edit_entry")
    @Expose
    private Boolean showConversionEditEntry;
    @SerializedName("aggregate_promote_engagement")
    @Expose
    private Boolean aggregatePromoteEngagement;
    @SerializedName("allowed_commenter_type")
    @Expose
    private String allowedCommenterType;
    @SerializedName("is_video_creator")
    @Expose
    private Boolean isVideoCreator;
    @SerializedName("has_profile_video_feed")
    @Expose
    private Boolean hasProfileVideoFeed;
    @SerializedName("has_highlight_reels")
    @Expose
    private Boolean hasHighlightReels;
    @SerializedName("is_eligible_to_show_fb_cross_sharing_nux")
    @Expose
    private Boolean isEligibleToShowFbCrossSharingNux;
    @SerializedName("page_id_for_new_suma_biz_account")
    @Expose
    private Object pageIdForNewSumaBizAccount;
    @SerializedName("eligible_shopping_signup_entrypoints")
    @Expose
    private List<Object> eligibleShoppingSignupEntrypoints = null;
    @SerializedName("can_be_reported_as_fraud")
    @Expose
    private Boolean canBeReportedAsFraud;
    @SerializedName("is_business")
    @Expose
    private Boolean isBusiness;
    @SerializedName("account_type")
    @Expose
    private Integer accountType;
    @SerializedName("professional_conversion_suggested_account_type")
    @Expose
    private Integer professionalConversionSuggestedAccountType;
    @SerializedName("is_call_to_action_enabled")
    @Expose
    private Object isCallToActionEnabled;
    @SerializedName("linked_fb_info")
    @Expose
    private LinkedFbInfo linkedFbInfo;
    @SerializedName("can_see_primary_country_in_settings")
    @Expose
    private Boolean canSeePrimaryCountryInSettings;
    @SerializedName("personal_account_ads_page_name")
    @Expose
    private Object personalAccountAdsPageName;
    @SerializedName("personal_account_ads_page_id")
    @Expose
    private Object personalAccountAdsPageId;
    @SerializedName("account_badges")
    @Expose
    private List<Object> accountBadges = null;
    @SerializedName("include_direct_blacklist_status")
    @Expose
    private Boolean includeDirectBlacklistStatus;
    @SerializedName("can_follow_hashtag")
    @Expose
    private Boolean canFollowHashtag;
    @SerializedName("is_potential_business")
    @Expose
    private Boolean isPotentialBusiness;
    @SerializedName("show_post_insights_entry_point")
    @Expose
    private Boolean showPostInsightsEntryPoint;
    @SerializedName("feed_post_reshare_disabled")
    @Expose
    private Boolean feedPostReshareDisabled;
    @SerializedName("besties_count")
    @Expose
    private Integer bestiesCount;
    @SerializedName("show_besties_badge")
    @Expose
    private Boolean showBestiesBadge;
    @SerializedName("recently_bestied_by_count")
    @Expose
    private Integer recentlyBestiedByCount;
    @SerializedName("nametag")
    @Expose
    private Nametag nametag;
    @SerializedName("existing_user_age_collection_enabled")
    @Expose
    private Boolean existingUserAgeCollectionEnabled;
    @SerializedName("about_your_account_bloks_entrypoint_enabled")
    @Expose
    private Boolean aboutYourAccountBloksEntrypointEnabled;
    @SerializedName("auto_expand_chaining")
    @Expose
    private Boolean autoExpandChaining;
    @SerializedName("highlight_reshare_disabled")
    @Expose
    private Boolean highlightReshareDisabled;
    @SerializedName("is_memorialized")
    @Expose
    private Boolean isMemorialized;
    @SerializedName("open_external_url_with_in_app_browser")
    @Expose
    private Boolean openExternalUrlWithInAppBrowser;

    public long getPk() {
        return pk;
    }

    public void setPk(long pk) {
        this.pk = pk;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getProfilePicId() {
        return profilePicId;
    }

    public void setProfilePicId(String profilePicId) {
        this.profilePicId = profilePicId;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Boolean getHasAnonymousProfilePicture() {
        return hasAnonymousProfilePicture;
    }

    public void setHasAnonymousProfilePicture(Boolean hasAnonymousProfilePicture) {
        this.hasAnonymousProfilePicture = hasAnonymousProfilePicture;
    }

    public Integer getMediaCount() {
        return mediaCount;
    }

    public void setMediaCount(Integer mediaCount) {
        this.mediaCount = mediaCount;
    }

    public Integer getGeoMediaCount() {
        return geoMediaCount;
    }

    public void setGeoMediaCount(Integer geoMediaCount) {
        this.geoMediaCount = geoMediaCount;
    }

    public Integer getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(Integer followerCount) {
        this.followerCount = followerCount;
    }

    public Integer getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Integer followingCount) {
        this.followingCount = followingCount;
    }

    public Integer getFollowingTagCount() {
        return followingTagCount;
    }

    public void setFollowingTagCount(Integer followingTagCount) {
        this.followingTagCount = followingTagCount;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public Boolean getCanLinkEntitiesInBio() {
        return canLinkEntitiesInBio;
    }

    public void setCanLinkEntitiesInBio(Boolean canLinkEntitiesInBio) {
        this.canLinkEntitiesInBio = canLinkEntitiesInBio;
    }

    public BiographyWithEntities getBiographyWithEntities() {
        return biographyWithEntities;
    }

    public void setBiographyWithEntities(BiographyWithEntities biographyWithEntities) {
        this.biographyWithEntities = biographyWithEntities;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public Boolean getCanBoostPost() {
        return canBoostPost;
    }

    public void setCanBoostPost(Boolean canBoostPost) {
        this.canBoostPost = canBoostPost;
    }

    public Boolean getCanSeeOrganicInsights() {
        return canSeeOrganicInsights;
    }

    public void setCanSeeOrganicInsights(Boolean canSeeOrganicInsights) {
        this.canSeeOrganicInsights = canSeeOrganicInsights;
    }

    public Boolean getShowInsightsTerms() {
        return showInsightsTerms;
    }

    public void setShowInsightsTerms(Boolean showInsightsTerms) {
        this.showInsightsTerms = showInsightsTerms;
    }

    public Boolean getCanConvertToBusiness() {
        return canConvertToBusiness;
    }

    public void setCanConvertToBusiness(Boolean canConvertToBusiness) {
        this.canConvertToBusiness = canConvertToBusiness;
    }

    public Boolean getCanCreateSponsorTags() {
        return canCreateSponsorTags;
    }

    public void setCanCreateSponsorTags(Boolean canCreateSponsorTags) {
        this.canCreateSponsorTags = canCreateSponsorTags;
    }

    public Boolean getIsAllowedToCreateStandalonePersonalFundraisers() {
        return isAllowedToCreateStandalonePersonalFundraisers;
    }

    public void setIsAllowedToCreateStandalonePersonalFundraisers(Boolean isAllowedToCreateStandalonePersonalFundraisers) {
        this.isAllowedToCreateStandalonePersonalFundraisers = isAllowedToCreateStandalonePersonalFundraisers;
    }

    public Boolean getCanCreateNewStandalonePersonalFundraiser() {
        return canCreateNewStandalonePersonalFundraiser;
    }

    public void setCanCreateNewStandalonePersonalFundraiser(Boolean canCreateNewStandalonePersonalFundraiser) {
        this.canCreateNewStandalonePersonalFundraiser = canCreateNewStandalonePersonalFundraiser;
    }

    public Boolean getCanBeTaggedAsSponsor() {
        return canBeTaggedAsSponsor;
    }

    public void setCanBeTaggedAsSponsor(Boolean canBeTaggedAsSponsor) {
        this.canBeTaggedAsSponsor = canBeTaggedAsSponsor;
    }

    public Boolean getCanSeeSupportInbox() {
        return canSeeSupportInbox;
    }

    public void setCanSeeSupportInbox(Boolean canSeeSupportInbox) {
        this.canSeeSupportInbox = canSeeSupportInbox;
    }

    public Boolean getCanSeeSupportInboxV1() {
        return canSeeSupportInboxV1;
    }

    public void setCanSeeSupportInboxV1(Boolean canSeeSupportInboxV1) {
        this.canSeeSupportInboxV1 = canSeeSupportInboxV1;
    }

    public Integer getTotalIgtvVideos() {
        return totalIgtvVideos;
    }

    public void setTotalIgtvVideos(Integer totalIgtvVideos) {
        this.totalIgtvVideos = totalIgtvVideos;
    }

    public Integer getTotalClipsCount() {
        return totalClipsCount;
    }

    public void setTotalClipsCount(Integer totalClipsCount) {
        this.totalClipsCount = totalClipsCount;
    }

    public Integer getTotalArEffects() {
        return totalArEffects;
    }

    public void setTotalArEffects(Integer totalArEffects) {
        this.totalArEffects = totalArEffects;
    }

    public String getReelAutoArchive() {
        return reelAutoArchive;
    }

    public void setReelAutoArchive(String reelAutoArchive) {
        this.reelAutoArchive = reelAutoArchive;
    }

    public Boolean getIsProfileActionNeeded() {
        return isProfileActionNeeded;
    }

    public void setIsProfileActionNeeded(Boolean isProfileActionNeeded) {
        this.isProfileActionNeeded = isProfileActionNeeded;
    }

    public Integer getUsertagsCount() {
        return usertagsCount;
    }

    public void setUsertagsCount(Integer usertagsCount) {
        this.usertagsCount = usertagsCount;
    }

    public Boolean getUsertagReviewEnabled() {
        return usertagReviewEnabled;
    }

    public void setUsertagReviewEnabled(Boolean usertagReviewEnabled) {
        this.usertagReviewEnabled = usertagReviewEnabled;
    }

    public Boolean getIsNeedy() {
        return isNeedy;
    }

    public void setIsNeedy(Boolean isNeedy) {
        this.isNeedy = isNeedy;
    }

    public Boolean getIsInterestAccount() {
        return isInterestAccount;
    }

    public void setIsInterestAccount(Boolean isInterestAccount) {
        this.isInterestAccount = isInterestAccount;
    }

    public Boolean getHasChaining() {
        return hasChaining;
    }

    public void setHasChaining(Boolean hasChaining) {
        this.hasChaining = hasChaining;
    }

    public List<HdProfilePicVersion> getHdProfilePicVersions() {
        return hdProfilePicVersions;
    }

    public void setHdProfilePicVersions(List<HdProfilePicVersion> hdProfilePicVersions) {
        this.hdProfilePicVersions = hdProfilePicVersions;
    }

    public HdProfilePicUrlInfo getHdProfilePicUrlInfo() {
        return hdProfilePicUrlInfo;
    }

    public void setHdProfilePicUrlInfo(HdProfilePicUrlInfo hdProfilePicUrlInfo) {
        this.hdProfilePicUrlInfo = hdProfilePicUrlInfo;
    }

    public Boolean getHasPlacedOrders() {
        return hasPlacedOrders;
    }

    public void setHasPlacedOrders(Boolean hasPlacedOrders) {
        this.hasPlacedOrders = hasPlacedOrders;
    }

    public Boolean getCanTagProductsFromMerchants() {
        return canTagProductsFromMerchants;
    }

    public void setCanTagProductsFromMerchants(Boolean canTagProductsFromMerchants) {
        this.canTagProductsFromMerchants = canTagProductsFromMerchants;
    }

    public Boolean getFbpayExperienceEnabled() {
        return fbpayExperienceEnabled;
    }

    public void setFbpayExperienceEnabled(Boolean fbpayExperienceEnabled) {
        this.fbpayExperienceEnabled = fbpayExperienceEnabled;
    }

    public Boolean getShowConversionEditEntry() {
        return showConversionEditEntry;
    }

    public void setShowConversionEditEntry(Boolean showConversionEditEntry) {
        this.showConversionEditEntry = showConversionEditEntry;
    }

    public Boolean getAggregatePromoteEngagement() {
        return aggregatePromoteEngagement;
    }

    public void setAggregatePromoteEngagement(Boolean aggregatePromoteEngagement) {
        this.aggregatePromoteEngagement = aggregatePromoteEngagement;
    }

    public String getAllowedCommenterType() {
        return allowedCommenterType;
    }

    public void setAllowedCommenterType(String allowedCommenterType) {
        this.allowedCommenterType = allowedCommenterType;
    }

    public Boolean getIsVideoCreator() {
        return isVideoCreator;
    }

    public void setIsVideoCreator(Boolean isVideoCreator) {
        this.isVideoCreator = isVideoCreator;
    }

    public Boolean getHasProfileVideoFeed() {
        return hasProfileVideoFeed;
    }

    public void setHasProfileVideoFeed(Boolean hasProfileVideoFeed) {
        this.hasProfileVideoFeed = hasProfileVideoFeed;
    }

    public Boolean getHasHighlightReels() {
        return hasHighlightReels;
    }

    public void setHasHighlightReels(Boolean hasHighlightReels) {
        this.hasHighlightReels = hasHighlightReels;
    }

    public Boolean getIsEligibleToShowFbCrossSharingNux() {
        return isEligibleToShowFbCrossSharingNux;
    }

    public void setIsEligibleToShowFbCrossSharingNux(Boolean isEligibleToShowFbCrossSharingNux) {
        this.isEligibleToShowFbCrossSharingNux = isEligibleToShowFbCrossSharingNux;
    }

    public Object getPageIdForNewSumaBizAccount() {
        return pageIdForNewSumaBizAccount;
    }

    public void setPageIdForNewSumaBizAccount(Object pageIdForNewSumaBizAccount) {
        this.pageIdForNewSumaBizAccount = pageIdForNewSumaBizAccount;
    }

    public List<Object> getEligibleShoppingSignupEntrypoints() {
        return eligibleShoppingSignupEntrypoints;
    }

    public void setEligibleShoppingSignupEntrypoints(List<Object> eligibleShoppingSignupEntrypoints) {
        this.eligibleShoppingSignupEntrypoints = eligibleShoppingSignupEntrypoints;
    }

    public Boolean getCanBeReportedAsFraud() {
        return canBeReportedAsFraud;
    }

    public void setCanBeReportedAsFraud(Boolean canBeReportedAsFraud) {
        this.canBeReportedAsFraud = canBeReportedAsFraud;
    }

    public Boolean getIsBusiness() {
        return isBusiness;
    }

    public void setIsBusiness(Boolean isBusiness) {
        this.isBusiness = isBusiness;
    }

    public Integer getAccountType() {
        return accountType;
    }

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }

    public Integer getProfessionalConversionSuggestedAccountType() {
        return professionalConversionSuggestedAccountType;
    }

    public void setProfessionalConversionSuggestedAccountType(Integer professionalConversionSuggestedAccountType) {
        this.professionalConversionSuggestedAccountType = professionalConversionSuggestedAccountType;
    }

    public Object getIsCallToActionEnabled() {
        return isCallToActionEnabled;
    }

    public void setIsCallToActionEnabled(Object isCallToActionEnabled) {
        this.isCallToActionEnabled = isCallToActionEnabled;
    }

    public LinkedFbInfo getLinkedFbInfo() {
        return linkedFbInfo;
    }

    public void setLinkedFbInfo(LinkedFbInfo linkedFbInfo) {
        this.linkedFbInfo = linkedFbInfo;
    }

    public Boolean getCanSeePrimaryCountryInSettings() {
        return canSeePrimaryCountryInSettings;
    }

    public void setCanSeePrimaryCountryInSettings(Boolean canSeePrimaryCountryInSettings) {
        this.canSeePrimaryCountryInSettings = canSeePrimaryCountryInSettings;
    }

    public Object getPersonalAccountAdsPageName() {
        return personalAccountAdsPageName;
    }

    public void setPersonalAccountAdsPageName(Object personalAccountAdsPageName) {
        this.personalAccountAdsPageName = personalAccountAdsPageName;
    }

    public Object getPersonalAccountAdsPageId() {
        return personalAccountAdsPageId;
    }

    public void setPersonalAccountAdsPageId(Object personalAccountAdsPageId) {
        this.personalAccountAdsPageId = personalAccountAdsPageId;
    }

    public List<Object> getAccountBadges() {
        return accountBadges;
    }

    public void setAccountBadges(List<Object> accountBadges) {
        this.accountBadges = accountBadges;
    }

    public Boolean getIncludeDirectBlacklistStatus() {
        return includeDirectBlacklistStatus;
    }

    public void setIncludeDirectBlacklistStatus(Boolean includeDirectBlacklistStatus) {
        this.includeDirectBlacklistStatus = includeDirectBlacklistStatus;
    }

    public Boolean getCanFollowHashtag() {
        return canFollowHashtag;
    }

    public void setCanFollowHashtag(Boolean canFollowHashtag) {
        this.canFollowHashtag = canFollowHashtag;
    }

    public Boolean getIsPotentialBusiness() {
        return isPotentialBusiness;
    }

    public void setIsPotentialBusiness(Boolean isPotentialBusiness) {
        this.isPotentialBusiness = isPotentialBusiness;
    }

    public Boolean getShowPostInsightsEntryPoint() {
        return showPostInsightsEntryPoint;
    }

    public void setShowPostInsightsEntryPoint(Boolean showPostInsightsEntryPoint) {
        this.showPostInsightsEntryPoint = showPostInsightsEntryPoint;
    }

    public Boolean getFeedPostReshareDisabled() {
        return feedPostReshareDisabled;
    }

    public void setFeedPostReshareDisabled(Boolean feedPostReshareDisabled) {
        this.feedPostReshareDisabled = feedPostReshareDisabled;
    }

    public Integer getBestiesCount() {
        return bestiesCount;
    }

    public void setBestiesCount(Integer bestiesCount) {
        this.bestiesCount = bestiesCount;
    }

    public Boolean getShowBestiesBadge() {
        return showBestiesBadge;
    }

    public void setShowBestiesBadge(Boolean showBestiesBadge) {
        this.showBestiesBadge = showBestiesBadge;
    }

    public Integer getRecentlyBestiedByCount() {
        return recentlyBestiedByCount;
    }

    public void setRecentlyBestiedByCount(Integer recentlyBestiedByCount) {
        this.recentlyBestiedByCount = recentlyBestiedByCount;
    }

    public Nametag getNametag() {
        return nametag;
    }

    public void setNametag(Nametag nametag) {
        this.nametag = nametag;
    }

    public Boolean getExistingUserAgeCollectionEnabled() {
        return existingUserAgeCollectionEnabled;
    }

    public void setExistingUserAgeCollectionEnabled(Boolean existingUserAgeCollectionEnabled) {
        this.existingUserAgeCollectionEnabled = existingUserAgeCollectionEnabled;
    }

    public Boolean getAboutYourAccountBloksEntrypointEnabled() {
        return aboutYourAccountBloksEntrypointEnabled;
    }

    public void setAboutYourAccountBloksEntrypointEnabled(Boolean aboutYourAccountBloksEntrypointEnabled) {
        this.aboutYourAccountBloksEntrypointEnabled = aboutYourAccountBloksEntrypointEnabled;
    }

    public Boolean getAutoExpandChaining() {
        return autoExpandChaining;
    }

    public void setAutoExpandChaining(Boolean autoExpandChaining) {
        this.autoExpandChaining = autoExpandChaining;
    }

    public Boolean getHighlightReshareDisabled() {
        return highlightReshareDisabled;
    }

    public void setHighlightReshareDisabled(Boolean highlightReshareDisabled) {
        this.highlightReshareDisabled = highlightReshareDisabled;
    }

    public Boolean getIsMemorialized() {
        return isMemorialized;
    }

    public void setIsMemorialized(Boolean isMemorialized) {
        this.isMemorialized = isMemorialized;
    }

    public Boolean getOpenExternalUrlWithInAppBrowser() {
        return openExternalUrlWithInAppBrowser;
    }

    public void setOpenExternalUrlWithInAppBrowser(Boolean openExternalUrlWithInAppBrowser) {
        this.openExternalUrlWithInAppBrowser = openExternalUrlWithInAppBrowser;
    }
}