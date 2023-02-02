package com.mobilicos.smotrofon.ui.channels.content

import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.remote.UserMediaListDataSource
import com.mobilicos.smotrofon.data.models.Media
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
class UserContentViewModel @Inject constructor(
    private val retrofit: Retrofit
) : ViewModel() {

    var userFullName: String = ""
    var userImageUrl: String = ""
    var userSubscribersCount: Int? = null
    var currentUser: Int = 0
    var currentType: Int = Config.TYPE_VIDEO
    var currentTab: Int = 0

    fun getUserMediaListData(user: Int, type: Int): Flow<PagingData<Media>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 3,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { UserMediaListDataSource(retrofit=retrofit, user=user, type=type) }
        ).flow
    }
}
