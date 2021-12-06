/*
 *     This file is part of ArucoAndroidServer.
 *
 *     ArucoAndroidServer is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Foobar is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Foobar.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.eziosoft.arucomqtt.mqtt

package com.netguru.moodshareapp

import android.util.Log
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.function.Consumer

class MQTT {
    data class MqttMessage(val message: ByteArray, val topic: String)

    private val TAG = "MQTT_CLIENT"
    private lateinit var client: Mqtt5AsyncClient

    private val _messageFlow = MutableSharedFlow<MqttMessage>()
    val messageFlow get() = _messageFlow.asSharedFlow()

    fun isConnected(): Boolean {
        return if (this::client.isInitialized)
            client.state.isConnected
        else
            false
    }

    fun publishMessage(
        message: String?,
        topic: String,
        retain: Boolean,
        status: (messageSent: Boolean, throwable: Throwable?) -> Unit
    ) {
        val messageBuilder =
            Mqtt5Publish.builder()
                .topic(topic)
                .retain(retain)

        Log.d(TAG, "publish: $message")
        if (message != null) {
            messageBuilder.payload(message.toByteArray())
        } else {
            Log.d(TAG, "publish: empty message")
            messageBuilder.payload(byteArrayOf())
        }

        val mqttMessage = messageBuilder.build()
        client.toAsync().publish(mqttMessage)
            .whenComplete { publishResult, throwable ->
                if (throwable != null) {
                    status(false, throwable)
                } else {
                    status(true, null)
                }
            }
    }


    fun connectToBroker(
        brokerURL: String, lastWillTopic: String, lastWillPayload: ByteArray,
        clientID: String, status: (connected: Boolean, exception: Throwable?) -> Unit
    ) {
        client = Mqtt5Client.builder()
            .identifier(clientID)
            .serverHost(brokerURL)
            .willPublish().topic(lastWillTopic).payload(lastWillPayload).applyWillPublish()
            .buildAsync()

        Log.d(TAG, "connect")
        client.connect().whenComplete { connAck, throwable ->
            if (throwable != null) {
                // Handle connection failure
                Log.e(TAG, "connect: ", throwable)
                status(false, throwable)
            } else {
                // Setup subscribes or start publishing
                Log.d(TAG, "connect: Connected")
                status(true, null)
            }
        }
    }

    fun disconnectFromBroker(status: (exception: Throwable?) -> Unit) {
        client.disconnect().whenComplete { _, throwable ->
            status(throwable)
        }
    }


    fun subscribeToTopic(topic: String) =
        client.subscribeWith().topicFilter(topic).callback(newMessageCallback).send()

    private val newMessageCallback =
        (Consumer<Mqtt5Publish> { message ->
            CoroutineScope(Dispatchers.Main).launch {
                _messageFlow.emit(MqttMessage(message.payloadAsBytes, message.topic.toString()))
            }
        })
}