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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.shtanko.ipc.common.applyFormattedString
import dev.shtanko.ipc.server.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        IPCService.clientData.observe(this) {
            binding.apply {
                textViewData.text = applyFormattedString(R.string.data_format, it.clientData)
                textViewIpcMethod.text =
                    applyFormattedString(R.string.ipc_method_format, it.ipcMethod)
                textViewPackage.text =
                    applyFormattedString(R.string.package_format, it.clientPackageName)
                textViewProcessId.text =
                    applyFormattedString(R.string.process_id_format, it.clientProcessId)
            }
        }
    }
}
