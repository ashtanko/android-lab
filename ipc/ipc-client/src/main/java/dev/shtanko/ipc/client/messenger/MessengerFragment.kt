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

package dev.shtanko.ipc.client.messenger

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process
import android.os.RemoteException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import dev.shtanko.ipc.client.R
import dev.shtanko.ipc.client.databinding.FragmentMessengerBinding
import dev.shtanko.ipc.common.CONNECTION_COUNT
import dev.shtanko.ipc.common.DATA
import dev.shtanko.ipc.common.IPCMethod
import dev.shtanko.ipc.common.PACKAGE_NAME
import dev.shtanko.ipc.common.PID
import dev.shtanko.ipc.common.applyFormattedString
import dev.shtanko.ipc.common.goneUnless
import timber.log.Timber

class MessengerFragment : Fragment(), ServiceConnection {

    private lateinit var binding: FragmentMessengerBinding
    private var isBound: Boolean = false
    private var serverMessenger: Messenger? = null
    private var clientMessenger: Messenger? = null

    private var handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val bundle = msg.data

            binding.apply {
                textViewProcessId.text = applyFormattedString(
                    R.string.process_id_title,
                    bundle.getInt(PID).toString()
                )

                textViewConnectionsCount.text = applyFormattedString(
                    R.string.connection_count_title,
                    bundle.getInt(CONNECTION_COUNT).toString()
                )

                card.goneUnless(isBound)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessengerBinding.inflate(inflater, container, false)
        binding.apply {
            btnSend.setOnClickListener {
                if (isBound) {
                    doUnbindService()
                } else {
                    doBindService()
                }
            }
        }
        return binding.root
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        serverMessenger = Messenger(service)
        sendMessageToServer()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        clearUI()
        serverMessenger = null
    }

    override fun onDestroy() {
        doUnbindService()
        super.onDestroy()
    }

    private fun doBindService() {
        clientMessenger = Messenger(handler)
        Intent(IPCMethod.MESSENGER_METHOD.desc).also { intent ->
            intent.`package` = "dev.shtanko.ipc.server"
            isBound = requireActivity().applicationContext.bindService(
                intent,
                this,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    private fun clearUI() {
        // todo
    }

    private fun doUnbindService() {
        if (isBound) {
            activity?.applicationContext?.unbindService(this)
            isBound = false
        }
    }

    private fun sendMessageToServer() {
        if (!isBound) return
        val message = Message.obtain(handler)
        val bundle = bundleOf(
            DATA to binding.textInputData.text.toString(),
            PACKAGE_NAME to context?.packageName,
            PID to Process.myPid()
        )
        message.data = bundle
        message.replyTo =
            clientMessenger
        try {
            serverMessenger?.send(message)
        } catch (e: RemoteException) {
            Timber.e(e)
        } finally {
            message.recycle()
        }
    }
}
