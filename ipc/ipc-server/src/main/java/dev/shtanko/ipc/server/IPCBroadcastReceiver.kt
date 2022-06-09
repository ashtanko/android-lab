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
import dev.shtanko.ipc.common.DATA
import dev.shtanko.ipc.common.IPCMethod
import dev.shtanko.ipc.common.METHOD
import dev.shtanko.ipc.common.PACKAGE_NAME
import dev.shtanko.ipc.common.PID

class IPCBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val local = Intent().apply {
            action = PASS_TO_ACTIVITY_ACTION
            putExtra(PACKAGE_NAME, intent?.getStringExtra(PACKAGE_NAME))
            putExtra(PID, intent?.getStringExtra(PID))
            putExtra(DATA, intent?.getStringExtra(DATA))
            putExtra(METHOD, IPCMethod.BROADCAST_METHOD.desc)
        }
        context?.sendBroadcast(local)
    }

    companion object {
        const val PASS_TO_ACTIVITY_ACTION = "dev.shtanko.to.activity.transfer"
    }
}
