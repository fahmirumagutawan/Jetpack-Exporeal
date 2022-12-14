package com.bcc.exporeal.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bcc.exporeal.model.PermintaanModel
import com.bcc.exporeal.model.ProductModel
import com.bcc.exporeal.model.UserModel
import com.bcc.exporeal.repository.AppRepository
import com.bcc.exporeal.util.Resource
import com.google.firebase.database.DataSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {
    val chatRoomSnapshot = mutableStateOf<DataSnapshot?>(null)
    val isChatRoomCheckLoaded = mutableStateOf(false)
    val chatChannelId = mutableStateOf("")
    val shouldCreateNew = mutableStateOf(false)
    val chatInputState = mutableStateOf("")
    val withItem = mutableStateOf("")
    val shouldLoadId = mutableStateOf(true)
    val isProcessSending = mutableStateOf(false)

    private val _target_user = MutableStateFlow<Resource<UserModel>?>(Resource.Loading())
    val target_user get() = _target_user

    private val _user = MutableStateFlow<Resource<UserModel>?>(Resource.Loading())
    val user get() = _user

    fun getTargetUserInfo(uid: String) = viewModelScope.launch {
        repository.getUserInfoByUid(uid = uid, delay = 0L).collect {
            _target_user.value = it
        }
    }

    fun getProductByProductId(product_id: String, onSuccess: (ProductModel) -> Unit) {
        viewModelScope.launch {
            repository.getProductByProductId(product_id, 0L).collect {
                if(it is Resource.Success){
                    it.data?.let(onSuccess)
                }
            }
        }
    }

    fun getPermintaanByPermintaanId(permintaan_id: String, onSuccess: (PermintaanModel) -> Unit) {
        viewModelScope.launch {
            repository.getPermintaanByPermintaanId(permintaan_id, 0L).collect {
                if(it is Resource.Success){
                    it.data?.let(onSuccess)
                }
            }
        }
    }

    fun getCurrentUid() = repository.getCurrentUid()

    fun getAvailableChatRoom(
        possibleChannel1: String,
        possibleChannel2: String,
        onSuccess: (shouldCreateNew: Boolean, channel: String) -> Unit,
        onFailed: () -> Unit
    ) {
        repository.getAvailableChatChannel(possibleChannel1, possibleChannel2, onSuccess, onFailed)
    }

    fun createChannelId(
        user_1: String,
        user_2: String,
        onSuccess: (String) -> Unit,
        onFailed: () -> Unit
    ) {
        repository.createNewChatChannel(
            channel_id = chatChannelId.value,
            user_1 = user_1,
            user_2 = user_2,
            onSuccess = onSuccess,
            onFailed = onFailed
        )
    }

    fun sendMessage(
        channel_id: String,
        chat: String,
        sender: String,
        receiver: String,
        product_id: String,
        permintaan_id: String,
        onSuccess: () -> Unit,
        onFailed: () -> Unit
    ) = repository.sendMessage(
        channel_id,
        chat,
        sender,
        receiver,
        product_id,
        permintaan_id,
        onSuccess = {
            onSuccess()
            chatInputState.value = ""
        },
        onFailed
    )

    fun listenToChatByChannelId(
        channel_id: String,
        onDataChange: (DataSnapshot) -> Unit,
        onCancelled: () -> Unit
    ) = repository.listenChatByChannelId(channel_id, onDataChange, onCancelled)

    fun updateLastChatOnFirestore(
        channel_id: String,
        last_chat: String,
        onFailed: () -> Unit,
        onSuccess: () -> Unit
    ) = repository.updateLastChatOnFirestore(channel_id, last_chat, onFailed, onSuccess)

    fun getTargetFcmToken(target_uid: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            repository.getFcmTokenByUid(target_uid).collect {
                if (it is Resource.Success) {
                    it.data?.let {
                        onSuccess(
                            it.token!!
                        )
                    }
                }
            }
        }
    }

    fun sendNotification(
        my_name: String,
        my_message: String,
        target_token: String
    ) {
        viewModelScope.launch {
            repository.sendCloudNotification(my_name, my_message, target_token)
        }
    }

    init {
        viewModelScope.launch {
            repository.getOwnUserInfo(0L).collect {
                _user.value = it
            }
        }
    }
}