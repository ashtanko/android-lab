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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dev.shtanko.ipc.common.DATA
import dev.shtanko.ipc.common.METHOD
import dev.shtanko.ipc.common.PACKAGE_NAME
import dev.shtanko.ipc.common.PID
import dev.shtanko.ipc.common.THREAD_NAME
import dev.shtanko.ipc.common.applyFormattedString
import dev.shtanko.ipc.server.IPCBroadcastReceiver.Companion.PASS_TO_ACTIVITY_ACTION
import dev.shtanko.ipc.server.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        IPCService.clientData.observe(this) {
            applyData(it.clientData, it.ipcMethod, it.clientPackageName, it.clientProcessId, it.threadName)
            applyStatus(true)
        }

        val intentFilter = IntentFilter().apply {
            addAction(PASS_TO_ACTIVITY_ACTION)
        }
        registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Timber.d(
                        "${intent?.getStringExtra(PACKAGE_NAME)} " +
                            "${intent?.getStringExtra(PID)} " +
                            "${intent?.getStringExtra(DATA)}" +
                            "${intent?.getStringExtra(METHOD)}"
                    )
                    applyData(
                        intent?.getStringExtra(DATA),
                        intent?.getStringExtra(METHOD),
                        intent?.getStringExtra(PACKAGE_NAME),
                        intent?.getStringExtra(PID),
                        intent?.getStringExtra(THREAD_NAME)
                    )
                    applyStatus(true)
                }
            },
            intentFilter
        )
    }

    private fun applyData(
        data: String?,
        method: String?,
        packageName: String?,
        processId: String?,
        threadName: String?
    ) = binding.apply {
        card.visibility = View.VISIBLE
        textViewData.text = applyFormattedString(R.string.data_format, data)
        textViewIpcMethod.text =
            applyFormattedString(R.string.ipc_method_format, method)
        textViewPackage.text =
            applyFormattedString(R.string.package_format, packageName)
        textViewProcessId.text =
            applyFormattedString(R.string.process_id_format, processId)
        textViewThread.text =
            applyFormattedString(R.string.thread_format, threadName)
    }

    private fun applyStatus(isConnected: Boolean) = binding.apply {
        if (isConnected) {
            connectedTextView.text = getString(R.string.status_connected)
            connectedImageView.setImageResource(R.drawable.ic_cloud_on_dark)
        } else {
            connectedTextView.text = getString(R.string.status_disconnected)
            connectedImageView.setImageResource(R.drawable.ic_cloud_off_dark)
        }
    }
}
