/*
 *  This file is part of MQTT_robot_control_Android.
 *
 *     MQTT_robot_control_Android is free software: you can redistribute it and/or modify
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
 *
 * Copyright (c) 2021. Bartosz Szczygiel
 *
 */

package com.eziosoft.arucomqtt.repository

import android.util.Log
import com.eziosoft.arucomqtt.repository.map.Map
import com.eziosoft.arucomqtt.repository.mqtt.Mqtt
import com.eziosoft.arucomqtt.repository.navigation.Mission
import com.eziosoft.arucomqtt.repository.roomba.RoombaParsedSensor
import com.eziosoft.arucomqtt.repository.roomba.RoombaSensorParser
import com.eziosoft.arucomqtt.repository.vision.camera.Camera
import com.eziosoft.arucomqtt.repository.vision.camera.calibration.CameraCalibrator
import com.eziosoft.arucomqtt.repository.vision.camera.position.CameraPosition
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalUnsignedTypes
@Singleton
class Repository @ExperimentalCoroutinesApi
@Inject constructor(
    private val mqtt: Mqtt,
    private val roombaSensorParser: RoombaSensorParser,
    val map: Map,
    val cameraPosition: CameraPosition,
    private val gson: Gson
) : RoombaSensorParser.SensorListener {

    private val job = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + job)

    val cameraCalibrator = CameraCalibrator(
        CameraConfiguration.CAMERA_WIDTH,
        CameraConfiguration.CAMERA_HEIGHT
    )

    private val sensorDataSet = arrayListOf<RoombaParsedSensor>()
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus = _connectionStatus.asStateFlow()

    private val _sensorsFlow = MutableSharedFlow<List<RoombaParsedSensor>>()
    val sensorFlow = _sensorsFlow.asSharedFlow().debounce(50)

    val targetFlow =
        MutableSharedFlow<Mission>()

    init {
        roombaSensorParser.setListener(this)
        setupObservers()
    }

    @ExperimentalUnsignedTypes
    private fun setupObservers() {
        coroutineScope.launch {
            mqtt.messageFlow.collect { message ->
                when (message.topic) {
                    MQTT_TELEMETRY_TOPIC -> {
                        val telemetry = String(message.message)
                    }
                    MQTT_STREAM_TOPIC -> {
                        val bytes = message.message.toUByteArray()
                        if (!bytes.isEmpty()) {
                            roombaSensorParser.parse(bytes)
                        }
                    }
                    MQTT_TARGET_LOCATION_TOPIC -> {
                        val missionJson = String(message.message)
                        Log.d("aaa", "MQTT_TARGET_LOCATION_TOPIC:$missionJson ")
                        val wpPath = gson.fromJson(missionJson, Mission::class.java)
                        Log.d("aaa", "setupObservers: $wpPath")
                        targetFlow.emit(wpPath)
                    }
                }
            }
        }
    }

    private fun getSensorValue(id: Int): Int? {
        return sensorDataSet.find { it.sensorID == id }?.signedValue
    }


    fun connectToMQTT(url: String, connected: (connected: Boolean) -> Unit) {
        if (mqtt.isConnected()) {
            mqtt.disconnectFromBroker { status, error ->
                setConnectionStatus(status)
            }
        }

        mqtt.connectToBroker(url, "user${System.currentTimeMillis()}") { status, error ->
            if (status) {
                mqtt.subscribeToTopic(MQTT_TELEMETRY_TOPIC)
                mqtt.subscribeToTopic(MQTT_STREAM_TOPIC)
                mqtt.subscribeToTopic(MQTT_TARGET_LOCATION_TOPIC)
            }
            setConnectionStatus(status)
            connected(status)
        }
    }

    fun disconnect() {
        mqtt.disconnectFromBroker { connected, exception ->
            setConnectionStatus(connected)
        }
    }

    fun isConnected(): Boolean {
        val connected = mqtt.isConnected()
        setConnectionStatus(connected)
        return connected
    }

    private fun setConnectionStatus(connected: Boolean) {
        _connectionStatus.value = if (connected) {
            ConnectionStatus.CONNECTED
        } else {
            ConnectionStatus.DISCONNECTED
        }
    }

    private fun publishMessage(message: String, topic: String, retain: Boolean = false) {
        mqtt.publishMessage(message, topic, retain) { _, _ -> }
    }

    fun publishMessage(bytes: ByteArray, topic: String, retain: Boolean = false) {
        mqtt.publishMessage(bytes, topic, retain) { _, _ -> }
    }

    override fun onSensors(sensors: List<RoombaParsedSensor>, checksumOK: Boolean) {
        Log.d("aaa", "onSensors: ")
        if (checksumOK) {
            coroutineScope.launch {
                processParsedSensors(sensors)
            }
        } else {
            Log.e("aaa", "CHECKSUM ERROR")
        }
    }

    private suspend fun processParsedSensors(sensors: List<RoombaParsedSensor>) {
        sensorDataSet.clear()
        sensorDataSet.addAll(sensors)
        _sensorsFlow.emit(sensorDataSet)
        Log.i("aaa", "processParsedSensors: ")
    }

    fun publishMap(retain: Boolean) {
        publishMessage(gson.toJson(map), MQTT_MAP_TOPIC, retain)
    }

    fun publishCameraLocation(camera: Camera) {
        publishMessage(gson.toJson(camera), MQTT_CAM_LOCATION_TOPIC, false)
    }

    fun close() {
        job.cancel()
    }

    companion object {
        private const val MAIN_TOPIC = "tank"
        const val MQTT_CONTROL_TOPIC = "$MAIN_TOPIC/in"
        const val MQTT_TELEMETRY_TOPIC = "$MAIN_TOPIC/out"
        const val MQTT_STREAM_TOPIC = "$MAIN_TOPIC/stream"
        const val MQTT_MAP_TOPIC = "map"
        const val MQTT_CAM_LOCATION_TOPIC = "cam"
        const val MQTT_TARGET_LOCATION_TOPIC = "target"
    }

    enum class ConnectionStatus {
        DISCONNECTED, CONNECTED
    }
}

fun List<RoombaParsedSensor>.getSensorValue(id: Int): Int? {
    return this.find { it.sensorID == id }?.signedValue
}
