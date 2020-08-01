package com.idirect.app.utils

import com.idirect.app.datasource.model.APIErrors
import retrofit2.Response

class ErrorUtils<T> {

    companion object {
        fun <T> parseError(response: Response<T>): APIErrors<T> {
            return APIErrors(response.code(), response.message(), data = response.errorBody())
        }
    }
}