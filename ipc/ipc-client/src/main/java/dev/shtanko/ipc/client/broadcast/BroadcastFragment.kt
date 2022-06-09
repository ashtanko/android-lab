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

package dev.shtanko.ipc.client.broadcast

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.shtanko.ipc.client.R
import dev.shtanko.ipc.client.databinding.FragmentBroadcastBinding
import dev.shtanko.ipc.common.DATA
import dev.shtanko.ipc.common.PACKAGE_NAME
import dev.shtanko.ipc.common.PID
import dev.shtanko.ipc.common.applyFormattedString
import java.util.Calendar

class BroadcastFragment : Fragment() {
    private lateinit var binding: FragmentBroadcastBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBroadcastBinding.inflate(inflater, container, false)
        binding.apply {
            binding.btnSend.setOnClickListener {
                sendBroadcast()
                showBroadcastTime()
            }
        }
        return binding.root
    }

    private fun sendBroadcast() {
        val intent = Intent().apply {
            action = INTENT_ACTION
            putExtra(PACKAGE_NAME, requireContext().packageName)
            putExtra(PID, Process.myPid().toString())
            putExtra(DATA, binding.textInputData.text.toString())
            component =
                ComponentName(COMPONENT_PACKAGE_NAME, BROADCAST_PATH)
        }
        requireActivity().applicationContext.sendBroadcast(intent)
    }

    private fun showBroadcastTime() {
        val cal = Calendar.getInstance()
        val time =
            String.format(
                getString(R.string.broadcast_time_format),
                cal.get(Calendar.HOUR),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND)
            )
        binding.apply {
            card.visibility = View.VISIBLE
            textViewTime.text = applyFormattedString(R.string.broadcast_time_title, time)
        }
    }

    companion object {
        private const val INTENT_ACTION = "dev.shtanko.ipc"
        private const val COMPONENT_PACKAGE_NAME = "dev.shtanko.ipc.server"
        private const val BROADCAST_PATH = "dev.shtanko.ipc.server.IPCBroadcastReceiver"
    }
}
