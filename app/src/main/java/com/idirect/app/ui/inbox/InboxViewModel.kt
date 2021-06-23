package com.idirect.app.ui.inbox

import android.app.Application
import com.idirect.app.core.BaseViewModel
import com.idirect.app.usecase.UseCase
import javax.inject.Inject

class InboxViewModel @Inject constructor(application: Application,var mUseCase: UseCase):BaseViewModel(application)