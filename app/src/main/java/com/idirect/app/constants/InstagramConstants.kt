package com.idirect.app.constants

import com.idirect.app.datasource.model.Device

class InstagramConstants {

    companion object {

        /**
         * Base API URL
         *
         */
        const val ALERT_NOTIFICATION_CHANNEL_ID = "iDirect"
        const val DEBUG_TAG = "debug_application"

        const val X_DEVICE_ID = "X-DEVICE-ID"
        const val X_IG_APP_LOCALE = "X-IG-App-Locale"
        const val X_IG_DEVICE_LOCALE = "X-IG-Device-Locale"
        const val X_IG_MAPPED_LOCALE = "X-IG-Mapped-Locale"
        const val X_PIGEON_SESSION_ID = "X-Pigeon-Session-Id"
        const val X_PIGEON_RAWCLIENT_TIEM = "X-Pigeon-Rawclienttime"
        const val X_IG_CONNECTION_SPEED = "X-IG-Connection-Speed"
        const val X_IG_BANDWIDTH_SPEED_KBPS = "X-IG-Bandwidth-Speed-KBPS"
        const val X_IG_BANDWIDTH_TOTALBYTES_B = "X-IG-Bandwidth-TotalBytes-B"
        const val X_IG_BAND_WIDTH_TOTALTIME_MS = "X-IG-Bandwidth-TotalTime-MS"
        const val X_IG_APP_STARTUP_COUNTRY = "X-IG-App-Startup-Country"
        const val X_BLOKS_VERSION_ID = "X-Bloks-Version-Id"
        const val X_IG_WWW_CLAIM = "X-IG-WWW-Claim"
        const val X_BLOKS_IS_LAYOUT_RTL = "X-Bloks-Is-Layout-RTL"
        const val X_BLOKS_ENABLE_RENDER_CORE = "X-Bloks-Enable-RenderCore"
        const val X_IG_DEVICE_ID = "X-IG-Device-ID"
        const val X_IG_ANDROID_ID = "X-IG-Android-ID"
        const val X_IG_CONNECTION_TYPE = "X-IG-Connection-Type"
        const val X_IG_CAPABILITIES = "X-IG-Capabilities"
        const val X_IG_APP_ID = "X-IG-App-ID"
        const val X_USER_AGENT = "User-Agent"
        const val ACCEPT_LANGUAGE = "Accept-Language"
        const val COOKIE = "Cookie"
        const val ACCEPT = "Accept"
        const val CONTENT_TYPE = "Content-Type"
        const val HOST = "Host"
        const val X_FB_HTTP_ENGINE = "X-FB-HTTP-Engine"
        const val CONNECTION = "Connection"
        const val X_MID = "x-mid"

        const val X_INSTAGRAM_RUPLOAD_PARAMS = "X-Instagram-Rupload-Params"
        const val X_FB_VIDEO_WATERFALL_ID = "X_FB_VIDEO_WATERFALL_ID"
        const val X_ENTITY_LENGTH = "X-Entity-Length"
        const val X_ENTITY_NAME = "X-Entity-Name"
        const val X_ENTITY_TYPE = "X-Entity-Type"
        const val OFFSET = "Offset"
        const val ACCEPT_ENCODING = "Accept-Encoding"

        const val DEVICE_TYPE = "device_type"
        const val IS_MAIN_PUSH_CHANNEL = "is_main_push_channel"
        const val PHONE_ID = "phone_id"
        const val DEVICE_SUB_TYPE = "device_sub_type"
        const val DEVICE_TOKEN = "device_token"
        const val CSRFTOKEN = "_csrftoken"
        const val GUID = "guid"
        const val UUID = "_uuid"
        const val USERS = "users"

        const val BASE_API_URL = "https://i.instagram.com/"
        /**
         * API v1 URL
         */
        const val API_VERSION = "api/v1/"
        const val API_URL = BASE_API_URL + API_VERSION
        /**
         * API Key (extracted from the apk)
         */
        const val API_KEY = "f0bdfd5332d66a64d5e04965e6a7ade67c4e2cfc57ea38f0083c0400640a5e20"
        const val BLOKS_VERSION_ID = "0e9b6d9c0fb2a2df4862cd7f46e3f719c55e9f90c20db0e5d95791b66f43b367"
        /**
         * API Key Version
         */
        const val API_KEY_VERSION = "4"
        /**
         * Device
         */

        const val INSTAGRAM_PACKAGE_NAME = "com.instagram.android"

        var device = Device.GOOD_DEVICES[0]
            set(value) {
                field = value
                DEVICE_MANUFACTURER = device.DEVICE_MANUFACTURER
                DEVICE_MODEL = device.DEVICE_MODEL
                DEVICE_ANDROID_VERSION = device.DEVICE_ANDROID_VERSION
                DEVICE_ANDROID_RELEASE = device.DEVICE_ANDROID_RELEASE
                USER_AGENT = String.format(
                    "Instagram %s Android (%s/%s; %s; %s; %s; %s; %s; %s; %s)",
                    APP_VERSION,
                    device.DEVICE_ANDROID_VERSION,
                    device.DEVICE_ANDROID_RELEASE,
                    device.DPI,
                    device.DISPLAY_RESOLUTION,
                    device.DEVICE_MANUFACTURER,
                    device.DEVICE_MODEL,
                    device.DEVICE,
                    device.CPU,
                    LOCALE
                )
            }
        /**
         * Device to mimic
         */
        @Deprecated("")
        var DEVICE_MANUFACTURER = device.DEVICE_MANUFACTURER
        /**
         * Model to mimic
         */
        @Deprecated("")
        var DEVICE_MODEL = device.DEVICE_MODEL
        /**
         * Android version to mimic
         */
        @Deprecated("")
        var DEVICE_ANDROID_VERSION = device.DEVICE_ANDROID_VERSION
        /**
         * Android Release
         */
        @Deprecated("")
        var DEVICE_ANDROID_RELEASE = device.DEVICE_ANDROID_RELEASE
        /**
         * Experiments Activated
         */
        val DEVICE_EXPERIMENTS =
            "ig_android_push_notifications_settings_redesign_universe,ig_hashtag_display_universe,ig_android_video_ssim_fix_pts_universe,coupon_price_test_ad4ad_instagram_resurrection_universe,ig_android_live_rendering_looper_universe,ig_shopping_checkout_improvements_universe,ig_android_mqtt_cookie_auth_memcache_universe,ig_android_optional_contact_and_preset_universe,ig_android_video_player_memory_leaks,ig_android_stories_seen_state_serialization,ig_stories_photo_time_duration_universe,ig_android_bitmap_cache_executor_size,ig_android_stories_music_search_typeahead,android_ard_ig_use_brotli_effect_universe,ig_android_remove_fb_nux_universe,ig_android_delayed_comments,ig_android_direct_mutation_manager_media_3,ig_smb_ads_holdout_2019_h1_universe,ig_fb_graph_differentiation,ig_android_stories_share_extension_video_segmentation,ig_android_igtv_crop_top,ig_android_stories_create_flow_favorites_tooltip,ig_android_direct_reshare_chaining,ig_android_stories_no_inflation_on_app_start,ig_android_stories_viewer_viewpoint_universe,ig_android_separate_empty_feed_su_universe,ig_android_zero_rating_carrier_signal,ig_direct_holdout_h1_2019,ig_explore_2019_h1_destination_cover,ig_android_direct_stories_in_direct_inbox,ig_android_explore_recyclerview_universe,ig_android_show_muted_accounts_page,ig_android_vc_service_crash_fix_universe,ig_camera_android_subtle_filter_universe,ig_android_lazy_init_live_composer_controller,ig_fb_graph_differentiation_no_fb_data,ig_android_viewpoint_stories_public_testing,ig_camera_android_api_rewrite_universe,ig_android_growth_fci_team_holdout_universe,android_camera_core_cpu_frames_sync,ig_android_video_source_sponsor_fix,ig_android_save_all,ig_android_ttcp_improvements,ig_android_camera_ar_platform_profile_universe,ig_android_separate_sms_n_email_invites_setting_universe,ig_shopping_bag_universe,ig_ar_shopping_camera_android_universe,ig_android_recyclerview_binder_group_enabled_universe,ig_android_stories_viewer_tall_android_cap_media_universe,ig_android_video_exoplayer_2,native_contact_invites_universe,ig_android_stories_seen_state_processing_universe,ig_android_dash_script,ig_android_insights_media_hashtag_insight_universe,ig_android_search_qpl_switch,ig_camera_fast_tti_universe,ig_android_igtv_improved_search,ig_android_stories_music_filters,ig_android_render_thread_memory_leak_holdout,ig_android_automated_logging,ig_android_viewmaster_post_capture_universe,ig_android_2018_h1_hashtag_report_universe,ig_android_camera_effect_gallery_prefetch,ig_share_to_story_toggle_include_shopping_product,ig_android_interactions_verified_badge_on_comment_details,ig_android_fix_ppr_thumbnail_url,ig_android_camera_reduce_file_exif_reads,ig_interactions_project_daisy_creators_universe,ig_payments_billing_address,ig_android_fs_new_gallery_hashtag_prompts,ig_camera_android_gyro_senser_sampling_period_universe,ig_android_xposting_feed_to_stories_reshares_universe,ig_android_combined_consumption,ig_camera_remove_display_rotation_cb_universe,ig_android_interactions_migrate_inline_composer_to_viewpoint_universe,ig_android_ufiv3_holdout,ig_android_neue_igtv_profile_tab_rollout,ig_android_enable_zero_rating,ig_android_story_ads_carousel_performance_universe_1,ig_android_direct_leave_from_group_message_requests,ig_android_import_page_post_after_biz_conversion,ig_camera_ar_effect_attribution_position,ig_promote_add_payment_navigation_universe,ig_android_story_ads_carousel_performance_universe_2,ig_android_main_feed_refresh_style_universe,ig_stories_engagement_holdout_2019_h1_universe,ig_android_story_ads_performance_universe_1,ig_android_stories_viewer_modal_activity,ig_android_story_ads_performance_universe_2,ig_android_publisher_stories_migration,ig_android_story_ads_performance_universe_3,ig_android_quick_conversion_universe,ig_android_story_import_intent,ig_android_story_ads_performance_universe_4,instagram_android_profile_follow_cta_context_feed,ig_biz_graph_connection_universe,ig_android_stories_boomerang_v2_universe,ig_android_ads_profile_cta_feed_universe,ig_android_vc_cowatch_universe,ig_android_nametag,ig_hashtag_creation_universe,ig_android_igtv_chaining,ig_android_live_qa_viewer_v1_universe,ig_shopping_insights_wc_copy_update_android,ig_android_stories_music_lyrics_pre_capture,android_cameracore_fbaudio_integration_ig_universe,ig_android_camera_stopmotion,ig_android_igtv_reshare,ig_android_wellbeing_timeinapp_v1_universe,ig_android_profile_cta_v3,ig_end_of_feed_universe,ig_android_mainfeed_generate_prefetch_background,ig_android_vc_shareable_moments_universe,ig_camera_text_overlay_controller_opt_universe,ig_android_shopping_product_metadata_on_product_tiles_universe,ig_android_video_qp_logger_universe,ig_android_shopping_pdp_cache,ig_android_follow_request_button_improvements_universe,ig_android_vc_start_from_direct_inbox_universe,ig_android_separate_network_executor,ig_perf_android_holdout,ig_fb_graph_differentiation_only_fb_candidates,ig_android_media_streaming_sdk_universe,ig_android_direct_reshares_from_thread,ig_android_stories_video_prefetch_kb,ig_android_wellbeing_timeinapp_v1_migration,ig_android_camera_post_smile_face_first_universe,ig_android_maintabfragment,ig_android_cookie_injection_retry_universe,ig_inventory_connections,ig_stories_injection_tool_enabled_universe,ig_android_canvas_cookie_universe,ig_android_stories_disable_highlights_media_preloading,ig_android_effect_gallery_post_capture_universe,ig_android_shopping_variant_selector_redesign,ig_android_branded_content_ads_universe,ig_promote_lotus_universe,ig_android_video_streaming_upload_universe,ig_camera_android_attribution_bottomsheet_universe,ig_android_product_tag_hint_dots,ig_interactions_h1_2019_team_holdout_universe,ig_camera_android_release_drawing_view_universe,ig_android_music_story_fb_crosspost_universe,ig_android_disable_scroll_listeners,ig_carousel_bumped_organic_impression_client_universe,ig_android_ad_async_ads_universe,ig_biz_post_approval_nux_universe,ig_android_vc_participants_grid_refactor_universe,android_ard_ig_modelmanager_cache_hit,ig_android_persistent_nux,ig_android_crash_fix_detach_from_gl_context,ig_android_branded_content_upsell_keywords_extension,ig_android_vc_ringscreen_timeout_universe,ig_android_edit_location_page_info,ig_android_stories_project_eclipse,ig_camera_android_segmentation_v106_igdjango_universe,ig_android_camera_recents_gallery_modal,ig_promote_are_you_sure_universe,ig_android_li_session_chaining,ig_android_camera_platform_effect_share_universe,ig_android_rate_limit_mediafeedviewablehelper,ig_android_search_empty_state,ig_android_camera_ar_platform_logging,ig_stories_engagement_holdout_2019_h2_universe,ig_android_search_remove_null_state_sections,ig_direct_android_mentions_receiver,ig_camera_android_device_capabilities_experiment,ig_android_stories_viewer_drawable_cache_universe,ig_camera_android_qcc_constructor_opt_universe,ig_android_stories_alignment_guides_universe,ig_android_rn_ads_manager_universe,ig_android_video_visual_quality_score_based_abr,ig_explore_2018_post_chaining_account_recs_dedupe_universe,ig_android_stories_video_seeking_audio_bug_fix,ig_android_insights_holdout,ig_android_do_not_show_social_context_for_likes_page_universe,ig_android_context_feed_recycler_view,ig_fb_notification_universe,ig_android_report_website_universe,ig_android_feed_post_sticker,ig_android_inline_editing_local_prefill,ig_android_commerce_platform_bloks_universe,ig_android_stories_unregister_decor_listener_universe,ig_android_search_condensed_search_icons,ig_android_video_abr_universe,ig_android_blended_inbox_split_button_v2_universe,ig_android_nelson_v0_universe,ig_android_scroll_audio_priority,ig_android_own_profile_sharing_universe,ig_android_vc_cowatch_media_share_universe,ig_biz_graph_unify_assoc_universe,ig_challenge_general_v2,ig_android_place_signature_universe,ig_android_direct_inbox_cache_universe,ig_android_ig_branding_in_fb_universe,ig_android_business_promote_tooltip,ig_android_tap_to_capture_universe,ig_android_follow_requests_ui_improvements,ig_android_video_ssim_fix_compare_frame_index,ig_android_direct_aggregated_media_and_reshares,ig_android_story_camera_share_to_feed_universe,ig_android_fb_follow_server_linkage_universe,ig_android_stories_viewer_reply_box_placeholder_copy,ig_android_biz_reorder_value_props,ig_android_direct_view_more_qe,ig_android_churned_find_friends_redirect_to_discover_people,ig_android_main_feed_new_posts_indicator_universe,ig_vp9_hd_blacklist,ig_camera_android_ar_effect_stories_deeplink,ig_android_client_side_delivery_universe,ig_ios_queue_time_qpl_universe,ig_android_fix_direct_badge_count_universe,ig_android_insights_audience_tab_native_universe,ig_android_stories_send_client_reels_on_tray_fetch_universe,ig_android_felix_prefetch_thumbnail_sprite_sheet,ig_android_live_use_rtc_upload_universe,ig_android_multi_dex_class_loader_v2,ig_android_live_ama_viewer_universe,ig_smb_ads_holdout_2018_h2_universe,ig_android_camera_post_smile_low_end_universe,ig_android_profile_follow_tab_hashtag_row_universe,ig_android_watch_and_more_redesign,igtv_feed_previews,ig_android_live_realtime_comments_universe,ig_android_story_insights_native_universe,ig_smb_ads_holdout_2019_h2_universe,ig_android_purx_native_checkout_universe,ig_camera_android_filter_optmizations,ig_android_integrity_sprint_universe,ig_android_apr_lazy_build_request_infra,ig_android_igds_edit_profile_fields,ig_android_business_transaction_in_stories_creator,ig_android_rounded_corner_framelayout_perf_fix,ig_android_branded_content_appeal_states,android_cameracore_ard_ig_integration,ig_video_experimental_encoding_consumption_universe,ig_android_iab_autofill,ig_android_creator_quick_reply_universe,ig_android_location_page_intent_survey,ig_camera_android_segmentation_async_universe,ig_android_biz_story_to_fb_page_improvement,ig_android_direct_thread_target_queue_universe,ig_android_branded_content_insights_disclosure,ig_camera_android_target_recognition_universe,ig_camera_android_skip_camera_initialization_open_to_post_capture,ig_android_combined_tagging_videos,ig_android_stories_samsung_sharing_integration,ig_android_create_page_on_top_universe,ig_iab_use_default_intent_loading,ig_android_camera_focus_v2,ig_android_biz_conversion_pull_suggest_biz_data,ig_discovery_holdout_2019_h1_universe,ig_android_wellbeing_support_frx_comment_reporting,ig_android_insights_post_dismiss_button,ig_android_user_url_deeplink_fbpage_endpoint,ig_android_ad_holdout_watchandmore_universe,ig_android_follow_request_button_new_ui,ig_iab_dns_prefetch_universe,ig_android_explore_use_shopping_endpoint,ig_android_image_upload_skip_queue_only_on_wifi,ig_android_igtv_pip,ig_android_ad_watchbrowse_carousel_universe,ig_android_camera_new_post_smile_universe,ig_android_shopping_signup_redesign_universe,ig_android_direct_hide_inbox_header,ig_shopping_pdp_more_related_product_section,ig_android_experimental_onetap_dialogs_universe,ig_android_fix_main_feed_su_cards_size_universe,ig_android_direct_multi_upload_universe,ig_camera_text_mode_composer_controller_opt_universe,ig_explore_2019_h1_video_autoplay_resume,ig_android_multi_capture_camera,ig_android_video_upload_quality_qe1,ig_android_follow_requests_copy_improvements,ig_android_save_collaborative_collections,coupon_price_test_boost_instagram_media_acquisition_universe,ig_android_video_outputsurface_handlerthread_universe,ig_android_country_code_fix_universe,ig_perf_android_holdout_2018_h1,ig_android_stories_music_overlay,ig_android_enable_lean_crash_reporting_universe,ig_android_resumable_downloads_logging_universe,ig_android_low_latency_consumption_universe,ig_android_render_output_surface_timeout_universe,ig_android_big_foot_foregroud_reporting,ig_android_unified_iab_logging_universe,ig_threads_app_close_friends_integration,ig_aggregated_quick_reactions,ig_android_shopping_pdp_post_purchase_sharing,ig_android_aggressive_cookie,ig_android_offline_mode_holdout,ig_android_realtime_mqtt_logging,ig_android_rainbow_hashtags,ig_android_no_bg_effect_tray_live_universe,ig_android_direct_block_from_group_message_requests,ig_android_react_native_universe_kill_switch,ig_android_viewpoint_occlusion,ig_android_logged_in_delta_migration,ig_android_push_reliability_universe,ig_android_stories_gallery_video_segmentation,ig_android_direct_business_holdout,ig_android_vc_direct_inbox_ongoing_pill_universe,ig_android_xposting_upsell_directly_after_sharing_to_story,ig_android_direct_sticker_search_upgrade_universe,ig_android_insights_native_post_universe,ig_android_dual_destination_quality_improvement,ig_android_camera_focus_low_end_universe,ig_android_camera_hair_segmentation_universe,ig_android_direct_combine_action_logs,ig_android_leak_detector_upload_universe,ig_android_ads_data_preferences_universe,ig_android_branded_content_access_upsell,ig_android_follow_button_in_story_viewers_list,ig_android_vc_background_call_toast_universe,ig_hashtag_following_holdout_universe,ig_promote_default_destination_universe,ig_android_delay_segmentation_low_end_universe,ig_android_direct_media_latency_optimizations,mi_viewpoint_viewability_universe,android_ard_ig_download_manager_v2,ig_direct_reshare_sharesheet_ranking,ig_music_dash,ig_android_fb_url_universe,ig_android_le_videoautoplay_disabled,ig_android_reel_raven_video_segmented_upload_universe,ig_android_promote_native_migration_universe,invite_friends_by_messenger_in_setting_universe,ig_android_fb_sync_options_universe,ig_android_thread_gesture_refactor,ig_android_stories_skip_seen_state_update_for_direct_stories,ig_android_recommend_accounts_destination_routing_fix,ig_android_fix_prepare_direct_push,ig_android_enable_automated_instruction_text_ar,ig_android_multi_author_story_reshare_universe,ig_android_building_aymf_universe,ig_android_internal_sticker_universe,ig_traffic_routing_universe,ig_android_payments_growth_business_payments_within_payments_universe,ig_camera_async_gallerycontroller_universe,ig_android_direct_state_observer,ig_android_page_claim_deeplink_qe,ig_android_camera_effects_order_universe,ig_android_video_controls_universe,ig_android_video_local_proxy_video_metadata,ig_android_logging_metric_universe_v2,ig_android_network_onbehavior_change_fix,ig_android_xposting_newly_fbc_people,ig_android_visualcomposer_inapp_notification_universe,ig_android_do_not_show_social_context_on_follow_list_universe,ig_android_contact_point_upload_rate_limit_killswitch,ig_android_webrtc_encoder_factory_universe,ig_android_qpl_class_marker,ig_android_fix_profile_pic_from_fb_universe,ig_android_sso_kototoro_app_universe,ig_android_camera_3p_in_post,ig_android_ar_effect_sticker_consumption_universe,ig_android_direct_unread_count_badge,ig_android_profile_thumbnail_impression,ig_android_igtv_autoplay_on_prepare,ig_android_list_adapter_prefetch_infra,ig_file_based_session_handler_2_universe,ig_branded_content_tagging_upsell,ig_android_clear_inflight_image_request,ig_android_main_feed_video_countdown_timer,ig_android_live_ama_universe,ig_android_external_gallery_import_affordance,ig_search_hashtag_content_advisory_remove_snooze,ig_payment_checkout_info,ig_android_optic_new_zoom_controller,ig_android_photos_qpl,ig_stories_ads_delivery_rules,ig_android_downloadable_spark_spot,ig_android_video_upload_iframe_interval,ig_business_new_value_prop_universe,ig_android_power_metrics,ig_android_vio_pipeline_universe,ig_android_show_profile_picture_upsell_in_reel_universe,ig_discovery_holdout_universe,ig_android_direct_import_google_photos2,ig_direct_feed_media_sticker_universe,ig_android_igtv_upload_error_messages,ig_android_stories_collapse_seen_segments,ig_android_self_profile_suggest_business_main,ig_android_suggested_users_background,ig_android_fetch_xpost_setting_in_camera_fully_open,ig_android_hashtag_discover_tab,ig_android_stories_separate_overlay_creation,ig_android_ads_bottom_sheet_report_flow,ig_android_login_onetap_upsell_universe,ig_android_iris_improvements,enable_creator_account_conversion_v0_universe,ig_android_test_not_signing_address_book_unlink_endpoint,ig_android_low_disk_recovery_universe,ig_ei_option_setting_universe,ig_android_account_insights_native_universe,ig_camera_android_ar_platform_universe,ig_android_browser_ads_page_content_width_universe,ig_android_stories_viewer_prefetch_improvements,ig_android_livewith_liveswap_optimization_universe,ig_android_camera_leak,ig_android_feed_core_unified_tags_universe,ig_android_jit,ig_android_optic_camera_warmup,ig_stories_rainbow_ring,ig_android_place_search_profile_image,ig_android_vp8_audio_encoder,android_cameracore_safe_makecurrent_ig,ig_android_analytics_diagnostics_universe,ig_android_ar_effect_sticker_universe,ig_direct_android_mentions_sender,ig_android_whats_app_contact_invite_universe,ig_android_stories_reaction_setup_delay_universe,ig_shopping_visual_product_sticker,ig_android_profile_unified_follow_view,ig_android_video_upload_hevc_encoding_universe,ig_android_mentions_suggestions,ig_android_vc_face_effects_universe,ig_android_fbpage_on_profile_side_tray,ig_android_direct_empty_state,ig_android_shimmering_loading_state,ig_android_igtv_refresh_tv_guide_interval,ig_android_gallery_minimum_video_length,ig_android_notif_improvement_universe,ig_android_hashtag_remove_share_hashtag,ig_android_fb_profile_integration_fbnc_universe,ig_shopping_checkout_2x2_platformization_universe,ig_android_direct_bump_active_threads,ig_fb_graph_differentiation_control,ig_android_show_create_content_pages_universe,ig_android_igsystrace_universe,ig_android_search_register_recent_store,ig_feed_content_universe,ig_android_disk_usage_logging_universe,ig_android_search_without_typed_hashtag_autocomplete,ig_android_video_product_specific_abr,ig_android_vc_interop_latency,ig_android_stories_layout_universe,ig_android_dont_animate_shutter_button_on_open,ig_android_vc_cpu_overuse_universe,ig_android_invite_list_button_redesign_universe,ig_android_react_native_email_sms_settings_universe,ig_hero_player,ag_family_bridges_2018_h2_holdout,ig_promote_net_promoter_score_universe,ig_android_save_auto_sharing_to_fb_option_on_server,aymt_instagram_promote_flow_abandonment_ig_universe,ig_android_whitehat_options_universe,ig_android_keyword_media_serp_page,ig_android_delete_ssim_compare_img_soon,ig_android_felix_video_upload_length,android_cameracore_preview_frame_listener2_ig_universe,ig_android_direct_message_follow_button,ig_android_biz_conversion_suggest_biz_nux,ig_stories_ads_media_based_insertion,ig_android_analytics_background_uploader_schedule,ig_camera_android_boomerang_attribution_universe,ig_android_igtv_browse_long_press,ig_android_profile_neue_infra_rollout_universe,ig_android_profile_ppr_fixes,ig_discovery_2019_h2_holdout_universe,ig_android_stories_weblink_creation,ig_android_blur_image_renderer,ig_profile_company_holdout_h2_2018,ig_android_ads_manager_pause_resume_ads_universe,ig_android_vc_capture_universe,ig_nametag_local_ocr_universe,ig_android_stories_media_seen_batching_universe,ig_android_interactions_nav_to_permalink_followup_universe,ig_camera_discovery_surface_universe,ig_android_save_to_collections_flow,ig_android_direct_segmented_video,instagram_stories_time_fixes,ig_android_le_cold_start_improvements,ig_android_direct_mark_as_read_notif_action,ig_android_stories_async_view_inflation_universe,ig_android_stories_recently_captured_universe,ig_android_direct_inbox_presence_refactor_universe,ig_business_integrity_ipc_universe,ig_android_direct_selfie_stickers,ig_android_vc_missed_call_call_back_action_universe,ig_cameracore_android_new_optic_camera2,ig_fb_graph_differentiation_top_k_fb_coefficients,ig_android_fbc_upsell_on_dp_first_load,ig_android_rename_share_option_in_dialog_menu_universe,ig_android_direct_refactor_inbox_observable_universe,ig_android_business_attribute_sync,ig_camera_android_bg_processor,ig_android_view_and_likes_cta_universe,ig_android_optic_new_focus_controller,ig_android_dropframe_manager,ig_android_direct_default_group_name,ig_android_optic_new_features_implementation,ig_android_search_hashtag_badges,ig_android_stories_reel_interactive_tap_target_size,ig_android_video_live_trace_universe,ig_android_tango_cpu_overuse_universe,ig_android_igtv_browse_with_pip_v2,ig_android_direct_fix_realtime_status,ig_android_unfollow_from_main_feed_v2,ig_android_self_story_setting_option_in_menu,ig_android_story_ads_tap_and_hold_fixes,ig_android_camera_ar_platform_details_view_universe,android_ard_ig_cache_size,ig_android_story_real_time_ad,ig_android_hybrid_bitmap_v4,ig_android_iab_downloadable_strings_universe,ig_android_branded_content_ads_enable_partner_boost,ufi_share,ig_android_direct_remix_visual_messages,ig_quick_story_placement_validation_universe,ig_android_custom_story_import_intent,ig_android_live_qa_broadcaster_v1_universe,ig_android_search_impression_logging_viewpoint,ig_android_downloadable_fonts_universe,ig_android_view_info_universe,ig_android_camera_upsell_dialog,ig_android_business_transaction_in_stories_consumer,ig_android_dead_code_detection,ig_android_promotion_insights_bloks,ig_android_direct_autoplay_videos_automatically,ig_android_ad_watchbrowse_universe,ig_android_pbia_proxy_profile_universe,ig_android_qp_kill_switch,ig_android_new_follower_removal_universe,instagram_android_stories_sticker_tray_redesign,ig_android_branded_content_access_tag,ig_android_gap_rule_enforcer_universe,ig_android_business_cross_post_with_biz_id_infra,ig_android_direct_delete_or_block_from_message_requests,ig_android_photo_invites,ig_interactions_h2_2019_team_holdout_universe,ig_android_reel_tray_item_impression_logging_viewpoint,ig_account_identity_2018_h2_lockdown_phone_global_holdout,ig_android_direct_left_aligned_navigation_bar,ig_android_high_res_gif_stickers,ig_android_feed_load_more_viewpoint_universe,ig_android_stories_reshare_reply_msg,ig_close_friends_v4,ig_android_ads_history_universe,ig_android_pigeon_sampling_runnable_check,ig_promote_media_picker_universe,ig_direct_holdout_h2_2018,ig_android_sidecar_report_ssim,ig_android_pending_media_file_registry,ig_android_wab_adjust_resize_universe,ig_camera_android_facetracker_v12_universe,ig_android_camera_ar_effects_low_storage_universe,ig_android_profile_add_profile_pic_universe,ig_android_ig_to_fb_sync_universe,ig_android_ar_background_effect_universe,ig_android_audience_control,ig_android_fix_recommended_user_impression,ig_android_stories_cross_sharing_to_fb_holdout_universe,shop_home_hscroll_see_all_button_universe,ig_android_refresh_empty_feed_su_universe,ig_android_shopping_parallel_pdp_fetch,ig_android_enable_main_feed_reel_tray_preloading,ig_android_ad_view_ads_native_universe,ig_android_branded_content_tag_redesign_organic,ig_android_profile_neue_universe,ig_android_igtv_whitelisted_for_web,ig_android_viewmaster_dial_ordering_universe,ig_company_profile_holdout,ig_rti_inapp_notifications_universe,ig_android_vc_join_timeout_universe,ig_shop_directory_entrypoint,ig_android_direct_rx_thread_update,ig_android_add_ci_upsell_in_normal_account_chaining_universe,ig_android_feed_core_ads_2019_h1_holdout_universe,ig_close_friends_v4_global,ig_android_share_publish_page_universe,ig_android_new_camera_design_universe,ig_direct_max_participants,ig_promote_hide_local_awareness_universe,ar_engine_audio_service_fba_decoder_ig,ar_engine_audio_fba_integration_instagram,ig_android_igtv_save,ig_android_explore_lru_cache,ig_android_graphql_survey_new_proxy_universe,ig_android_music_browser_redesign,ig_camera_android_try_on_camera_universe,ig_android_follower_following_whatsapp_invite_universe,ig_android_fs_creation_flow_tweaks,ig_direct_blocking_redesign_universe,ig_android_viewmaster_ar_memory_improvements,ig_android_downloadable_vp8_module,ig_android_claim_location_page,ig_android_direct_inbox_recently_active_presence_dot_universe,ig_android_stories_gutter_width_universe,ig_android_story_ads_2019_h1_holdout_universe,ig_android_3pspp,ig_android_cache_timespan_objects,ig_timestamp_public_test,ig_android_fb_profile_integration_universe,ig_android_feed_auto_share_to_facebook_dialog,ig_android_skip_button_content_on_connect_fb_universe,ig_android_network_perf_qpl_ppr,ig_android_post_live,ig_camera_android_focus_attribution_universe,ig_camera_async_space_validation_for_ar,ig_android_core_search_2019_h2,ig_android_prefetch_notification_data,ig_android_stories_music_line_by_line_cube_reveal_lyrics_sticker,ig_android_iab_clickid_universe,ig_android_interactions_hide_keyboard_onscroll,ig_early_friending_holdout_universe,ig_story_camera_reverse_video_experiment,ig_android_profile_lazy_load_carousel_media,ig_android_stories_question_sticker_music_format,ig_android_vpvd_impressions_universe,ig_android_payload_based_scheduling,ig_pacing_overriding_universe,ig_android_ard_ptl_universe,ig_android_q3lc_transparency_control_settings,ig_stories_selfie_sticker,ig_android_sso_use_trustedapp_universe,ig_android_stories_music_lyrics,ig_android_spark_studio_promo,ig_android_stories_music_awareness_universe,ard_ig_broti_effect,ig_android_camera_class_preloading,ig_android_new_fb_page_selection,ig_video_holdout_h2_2017,ig_background_prefetch,ig_camera_android_focus_in_post_universe,ig_android_time_spent_dashboard,ig_android_story_sharing_universe,ig_promote_political_ads_universe,ig_android_camera_effects_initialization_universe,ig_promote_post_insights_entry_universe,ig_android_ad_iab_qpl_kill_switch_universe,ig_android_live_subscribe_user_level_universe,ig_android_igtv_creation_flow,ig_android_vc_sounds_universe,ig_android_video_call_finish_universe,ig_camera_android_cache_format_picker_children,direct_unread_reminder_qe,ig_android_direct_mqtt_send,ig_android_self_story_button_non_fbc_accounts,ig_android_self_profile_suggest_business_gating,ig_feed_video_autoplay_stop_threshold,ig_android_explore_discover_people_entry_point_universe,ig_android_live_webrtc_livewith_params,ig_feed_experience,ig_android_direct_activator_cards,ig_android_vc_codec_settings,ig_promote_prefill_destination_universe,ig_android_appstate_logger,ig_android_profile_leaks_holdouts,ig_android_video_cached_bandwidth_estimate,ig_promote_insights_video_views_universe,ig_android_global_scheduler_offscreen_prefetch,ig_android_discover_interests_universe,ig_android_camera_gallery_upload_we_universe,ig_android_business_category_sticky_header_qe,ig_android_dismiss_recent_searches,ig_android_feed_camera_size_setter,ig_payment_checkout_cvv,ig_android_fb_link_ui_polish_universe,ig_android_tags_unification_universe,ig_android_shopping_lightbox,ig_android_bandwidth_timed_estimator,ig_android_stories_mixed_attribution_universe,ig_iab_tti_holdout_universe,ig_android_ar_button_visibility,ig_android_igtv_crop_top_consumption,ig_android_camera_gyro_universe,ig_android_nametag_effect_deeplink_universe,ig_android_blurred_product_image_previews,ig_android_igtv_ssim_report,ig_android_optic_surface_texture_cleanup,ig_android_business_remove_unowned_fb_pages,ig_android_stories_combined_asset_search,ig_promote_enter_error_screens_universe,ig_stories_allow_camera_actions_while_recording,ig_android_analytics_mark_events_as_offscreen,ig_shopping_checkout_mvp_experiment,ig_android_video_fit_scale_type_igtv,ig_android_direct_pending_media,ig_android_scroll_main_feed,instagram_pcp_activity_feed_following_tab_universe,ig_android_optic_feature_testing,ig_android_igtv_player_follow_button,ig_android_intialization_chunk_410,ig_android_vc_start_call_minimized_universe,ig_android_recognition_tracking_thread_prority_universe,ig_android_stories_music_sticker_position,ig_android_optic_photo_cropping_fixes,ig_camera_regiontracking_use_similarity_tracker_for_scaling,ig_android_interactions_media_breadcrumb,ig_android_vc_cowatch_config_universe,ig_android_nametag_save_experiment_universe,ig_android_refreshable_list_view_check_spring,ig_android_biz_endpoint_switch,ig_android_direct_continuous_capture,ig_android_comments_direct_reply_to_author,ig_android_profile_visits_in_bio,ig_android_fs_new_gallery,ig_android_remove_follow_all_fb_list,ig_android_vc_webrtc_params,ig_android_specific_story_sharing,ig_android_claim_or_connect_page_on_xpost,ig_android_anr,ig_android_story_viewpoint_impression_event_universe,ig_android_image_exif_metadata_ar_effect_id_universe,ig_android_optic_new_architecture,ig_android_stories_viewer_as_modal_high_end_launch,ig_android_local_info_page,ig_new_eof_demarcator_universe"

        /**
         * Device Capabilities
         */
        const val DEVICE_CAPABILITIES = "3brTvwM="

        /**
         * Instagram App Version
         */
        const val APP_VERSION = "130.0.0.31.121"
        /**
         * Instagram App Id
         */
        const val APP_ID = "567310203415052"
        /**
         * User Locale
         */
        var LOCALE = "en_US"
        /**
         * Android Release
         */
        var USER_AGENT = String.format(
            "Instagram %s Android (%s/%s; %s; %s; %s; %s; %s; %s; %s)",
            APP_VERSION,
            device.DEVICE_ANDROID_VERSION,
            device.DEVICE_ANDROID_RELEASE,
            device.DPI,
            device.DISPLAY_RESOLUTION,
            device.DEVICE_MANUFACTURER,
            device.DEVICE_MODEL,
            device.DEVICE,
            device.CPU,
            LOCALE
        )
    }

    enum class Error(var msg:String){
        BAD_PASSWORD("bad_password"),
        INVALID_TWO_FACTOR_CODE("invalid_nonce"),
        INVALID_CODE_VALIDATION("sms_code_validation_code_invalid"),
        RATE_LIMIT("rate_limit_error"),
        LOGIN_REQUIRED("login_required"),
        NOT_AUTHORIZED_VIEW_USER("Not authorized to view user")
    }

    enum class ErrorCode(var code:Int){
        INTERNET_CONNECTION(301)
    }

    enum class MessageType(var type:String){
        LIKE("like"),
        TEXT("text"),
        MEDIA_SHARE("media_share"),
        LINK("link"),
        MEDIA("media"),
        STORY_SHARE("story_share"),
        RAVEN_MEDIA("raven_media"),
        VOICE_MEDIA("voice_media"),
        ACTION_LOG("action_log"),
        PROFILE("profile"),
        PLACE_HOLDER("placeholder"),
        LOCATION("location"),
        FELIX_SHARE("felix_share"),
        REEL_SHARE("reel_share"),
        ANIMATED_MEDIA("animated_media"),
        HASH_TAG("hash_tag"),
        LIVE_VIEWER_INVITE("live_viewer_invite"),
        VIDEO_CALL_EVENT("video_call_event")
    }


    enum class ReelType(var type:String){
        REPLY("reply"),
        USER_REEL("user_reel"),
        MENTION("mention"),
        REACTION("reaction")
    }


    enum class TopicIds(var id:Int,var path:String)
    {
        Message(76,"/fbns_msg"),   // "/fbns_msg"
        RegReq(79,"/fbns_reg_req"),    // "/fbns_reg_req"
        RegResp(80,"/fbns_reg_resp")    // "/fbns_reg_resp"
    }

    enum class RealTimeTopics(var id:Int,var path:String)
    {
        GRAPHQL(9,"/graphql"),
        PUBSUB(88,"/pubsub"),
        SEND_MESSAGE_RESPONSE(133,"/ig_send_message_response"),
        IRIS_SUB(134,"/ig_sub_iris"),
        MESSAGE_SYNC(146,"/ig_message_sync"),
        REALTIME_SUB(149,"/ig_realtime_sub"),
        IRIS_SUB_RESPONSE(135,"/ig_sub_iris_response"),
        REGION_HINT(150,"/t_region_hint"),
        FOREGROUND_STATE(102,"/t_fs"),
        SEND_MESSAGE(132,"/ig_send_message"),
    }

    enum class RealTimeEvent(var id:String) {
        MESSAGE("items"), // for new message
        ACTIVITY_INDICATOR_ID("activity_indicator_id"), // for type event
        PARTICIPANTS("participants"), // for update last seen
        ITEMS("items") // for update last seen
    }

    enum class MediaType(var type:Int,var strType:String){
        VIDEO(2,"video"),
        IMAGE(1,"photo"),
        CAROUSEL_MEDIA(8,"")
    }

    enum class SharedPref(name:String){
        USER("user"),
        NOTIFICATION_DATA("notification_data"),
        SETTING("setting"),
        FBNS_DATA("fbns_data"),
    }
    enum class CommentType(var type:Int){
        NORMAL(0),
        REPLY(1)
    }
}