package com.sanardev.instagrammqtt.realtime.subcribers

import com.google.gson.Gson
import com.sanardev.instagrammqtt.datasource.model.subscribers.GraphQLSubBaseOptions

class GraphQLSubscriptions {

    companion object {
        class QueryIDs {
            companion object {
                const val appPresence = "17846944882223835"
                const val asyncAdSub = "17911191835112000"
                const val clientConfigUpdate = "17849856529644700"
                const val directStatus = "17854499065530643"
                const val directTyping = "17867973967082385"
                const val liveWave = "17882305414154951"
                const val interactivityActivateQuestion = "18005526940184517"
                const val interactivityRealtimeQuestionSubmissionsStatus = "18027779584026952"
                const val interactivitySub = "17907616480241689"
                const val liveRealtimeComments = "17855344750227125"
                const val liveTypingIndicator = "17926314067024917"
                const val mediaFeedback = "17877917527113814"
                const val reactNativeOTA = "17861494672288167"
                const val videoCallCoWatchControl = "17878679623388956"
                const val videoCallInAlert = "17878679623388956"
                const val videoCallPrototypePublish = "18031704190010162"
                const val zeroProvision = "17913953740109069"
            }
        }

        private fun formatSubscriptionString(
            queryId: String,
            inputParam: Map<String, Any>,
            clientLogged: Boolean
        ): String {
            return "1/graphqlsubscriptions/${queryId}/${Gson().toJson(HashMap<String, Any>().apply {
                put("input_data", inputParam)
            })}"
        }

        fun getAppPresenceSubscription(options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()) =
            formatSubscriptionString(
                QueryIDs.appPresence, HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                },
                options.isClientLogged
            )

        fun getAsyncAdSubscription(
            userId: String,
            options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()
        ) =
            formatSubscriptionString(
                QueryIDs.asyncAdSub,
                HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                    put("user_id", userId)
                },
                options.isClientLogged
            )

        fun getClientConfigUpdateSubscription(options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()) =
            formatSubscriptionString(
                QueryIDs.clientConfigUpdate,
                HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                },
                options.isClientLogged
            )

        fun getDirectStatusSubscription(options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()) =
            formatSubscriptionString(
                QueryIDs.directStatus,
                HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                },
                options.isClientLogged
            )

        fun getDirectTypingSubscription(userId: String, clientLogged: Boolean = false) =
            formatSubscriptionString(
                QueryIDs.directTyping,
                HashMap<String, Any>().apply {
                    put("user_id", userId)
                },
                clientLogged
            )

        fun getIgLiveWaveSubscription(
            broadcastId: String,
            receiverId: String,
            options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()
        ) =
            formatSubscriptionString(
                QueryIDs.liveWave,
                HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                    put("broadcast_id", broadcastId)
                    put("receiver_id", receiverId)
                },
                options.isClientLogged
            )

        fun getInteractivityActivateQuestionSubscription(
            broadcastId: String,
            options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()
        ) =
            formatSubscriptionString(
                QueryIDs.interactivityActivateQuestion,
                HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                    put("broadcast_id", broadcastId)
                },
                options.isClientLogged
            )

        fun getInteractivityRealtimeQuestionSubmissionsStatusSubscription(
            broadcastId: String,
            options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()
        ) =
            formatSubscriptionString(
                QueryIDs.interactivityRealtimeQuestionSubmissionsStatus,
                HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                    put("broadcast_id", broadcastId)
                },
                options.isClientLogged
            )

        fun getInteractivitySubscription(
            broadcastId: String,
            options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()
        ) =
            formatSubscriptionString(
                QueryIDs.interactivitySub,
                HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                    put("broadcast_id", broadcastId)
                },
                options.isClientLogged
            )

        fun getLiveRealtimeCommentsSubscription(
            broadcastId: String,
            options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()
        ) =
            formatSubscriptionString(
                QueryIDs.liveRealtimeComments,
                HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                    put("broadcast_id", broadcastId)
                },
                options.isClientLogged
            )

        fun getLiveTypingIndicatorSubscription(
            broadcastId: String,
            options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()
        ) =
            formatSubscriptionString(
                QueryIDs.liveTypingIndicator,
                HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                    put("broadcast_id", broadcastId)
                },
                options.isClientLogged
            )

        fun getMediaFeedbackSubscription(
            feedbackId: String,
            options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()
        ) =
            formatSubscriptionString(
                QueryIDs.mediaFeedback,
                HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                    put("feedback_id", feedbackId)
                },
                options.isClientLogged
            )

        fun getReactNativeOTAUpdateSubscription(
            buildNumber: String,
            options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()
        ) =
            formatSubscriptionString(
                QueryIDs.reactNativeOTA,
                HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                    put("build_number", buildNumber)
                },
                options.isClientLogged
            )

        fun getVideoCallCoWatchControlSubscription(
            videoCallId: String,
            options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()
        ) =
            formatSubscriptionString(
                QueryIDs.videoCallCoWatchControl,
                HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                    put("video_call_id", videoCallId)
                },
                options.isClientLogged
            )

        fun getVideoCallInCallAlertSubscription(
            videoCallId: String,
            options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()
        ) =
            formatSubscriptionString(
                QueryIDs.videoCallInAlert,
                HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                    put("video_call_id", videoCallId)
                },
                options.isClientLogged
            )

        fun getVideoCallPrototypePublishSubscription(
            videoCallId: String,
            options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()
        ) =
            formatSubscriptionString(
                QueryIDs.videoCallPrototypePublish,
                HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                    put("video_call_id", videoCallId)
                },
                options.isClientLogged
            )

        fun getZeroProvisionSubscription(
            deviceId: String,
            options: GraphQLSubBaseOptions = GraphQLSubBaseOptions()
        ) =
            formatSubscriptionString(
                QueryIDs.zeroProvision,
                HashMap<String, Any>().apply {
                    put("client_subscription_id", options.subscriptionId)
                    put("device_id", deviceId)
                },
                options.isClientLogged
            )
    }
}