package com.idirect.app.datasource.model.payload;

import com.google.gson.annotations.SerializedName;

public class StatusResult2 {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("error_type")
    private String errorType;

    @SerializedName("error_body")
    private String errorMessage;

    @SerializedName("error_title")
    private String errorTitle;
    private String checkpoint_url;

    private final boolean spam = false;
    private final boolean lock = false;

    public static void setValues(StatusResult2 to, StatusResult2 from) {
        to.setStatus(from.getStatus());
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public void setErrorTitle(String errorTitle) {
        this.errorTitle = errorTitle;
    }

    public String getCheckpoint_url() {
        return checkpoint_url;
    }

    public void setCheckpoint_url(String checkpoint_url) {
        this.checkpoint_url = checkpoint_url;
    }
}
/*
package com.idirect.app.datasource.model.payload

import androidx.annotation.NonNull
import com.google.gson.annotations.SerializedName

open class StatusResult {
    @NonNull
    var status: String? = null
    var message: String? = null

    var spam: Boolean = false
    var lock: Boolean = false


    var checkpoint_url: String? = null

    companion object {


    }
}
 */
