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

package dev.shtanko.ipc.client.aidl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dev.shtanko.ipc.client.R
import dev.shtanko.ipc.client.databinding.FragmentAidlBinding
import dev.shtanko.ipc.common.IPCMethod
import dev.shtanko.ipc.common.applyFormattedString
import dev.shtanko.ipc.common.applyNavigationIcon
import dev.shtanko.ipc.common.goneUnless
import dev.shtanko.ipc.server.IIPCExample
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

class AidlFragment : Fragment(), ServiceConnection {

    private lateinit var binding: FragmentAidlBinding
    private lateinit var viewModel: AidlViewModel
    private var iRemoteService: IIPCExample? = null
    private var connected = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAidlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val factory = AidlViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[AidlViewModel::class.java]

        binding.apply {
            btnConnect.setOnClickListener {
                connected = if (connected) {
                    disconnectToRemoteService()
                    binding.btnConnect.text = getString(R.string.connect)
                    binding.textInputLayout.visibility = View.VISIBLE
                    false
                } else {
                    connectToRemoteServerConcurrent()
                    //connectToRemoteServer()
                    binding.btnConnect.text = getString(R.string.disconnect)
                    binding.textInputLayout.visibility = View.GONE
                    true
                }
            }
        }
    }

    private fun disconnectToRemoteService() {
        Toast.makeText(requireContext(), R.string.disconnect, Toast.LENGTH_SHORT)
            .show()
        if (connected) {
            requireActivity().applicationContext.unbindService(this)
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        iRemoteService = IIPCExample.Stub.asInterface(service)
        iRemoteService?.setDisplayedValue(
            requireContext().packageName,
            Process.myPid(),
            binding.textInputLayout.editText?.text.toString(),
            Thread.currentThread().name
        )
        connected = true

        binding.apply {
            textViewProcessId.text = applyFormattedString(
                R.string.process_id_title,
                iRemoteService?.pid.toString()
            )

            textViewConnectionsCount.text = applyFormattedString(
                R.string.connection_count_title,
                iRemoteService?.connectionCount.toString()
            )
            textViewThread.text= applyFormattedString(
                R.string.thread_title,
                iRemoteService?.threadName().toString()
            )
            card.goneUnless(connected)
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        iRemoteService = null
        connected = false
        binding.apply {
            card.goneUnless(connected)
        }
    }

    private fun connectToRemoteServer() {
        val intent = Intent(IPCMethod.AIDL_METHOD.desc)
        val pack = IIPCExample::class.java.`package`
        pack?.let {
            intent.setPackage(pack.name)
            val isBounded = requireActivity().applicationContext.bindService(
                intent, this, Context.BIND_AUTO_CREATE
            )
            Timber.d("is service bound: $isBounded")
        }
    }

    private fun connectToRemoteServerConcurrent() =
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            connectToRemoteServer()
        }
}
