/*
 * Copyright 2022 Oleksii Shtanko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.shtanko.ipc.server

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process
import android.text.TextUtils
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import dev.shtanko.ipc.common.IPCMethod

class IPCService : LifecycleService() {

    companion object {
        var connectionCount: Int = 0
        const val NOT_SENT = "Not sent!"
        private const val IPC_METHOD_MESSENGER = "Messenger"
        private const val IPC_METHOD_AIDL = "AIDL"
        private const val IPC_METHOD_UNKNOWN = "unknown"
        private val mClientData = MutableLiveData<Data>()
        val clientData = mClientData
    }

    private val messenger = Messenger(IncomingHandler())

    private val messengerBinder by lazy {
        messenger.binder
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        connectionCount++

        return when (IPCMethod.valueOf(intent.action?.uppercase() ?: IPC_METHOD_UNKNOWN)) {
            IPCMethod.AIDL_METHOD -> aidlBinder
            IPCMethod.MESSENGER_METHOD -> messengerBinder
            else -> null
        }
    }

    internal class IncomingHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val receivedBundle = msg.data


            mClientData.postValue(
                Data(
                    receivedBundle.getString(PACKAGE_NAME) ?: NOT_SENT,
                    receivedBundle.getInt(PID).toString(),
                    receivedBundle.getString(DATA) ?: NOT_SENT,
                    IPC_METHOD_MESSENGER
                )
            )

            val message = Message.obtain(this@IncomingHandler, 0)
            val bundle = Bundle()

            bundle.putInt(CONNECTION_COUNT, connectionCount)
            bundle.putInt(PID, Process.myPid())
            message.data = bundle
            msg.replyTo.send(message)
        }
    }

    private val aidlBinder = object : IIPCExample.Stub() {
        override fun getPid(): Int = Process.myPid()

        override fun getConnectionCount() = IPCService.connectionCount

        override fun setDisplayedValue(packageName: String?, pid: Int, data: String?) {
            val clientData =
                if (data == null || TextUtils.isEmpty(data)) NOT_SENT
                else data
            mClientData.postValue(
                Data(
                    packageName ?: NOT_SENT,
                    pid.toString(),
                    clientData,
                    IPC_METHOD_AIDL
                )
            )
        }
    }
}
