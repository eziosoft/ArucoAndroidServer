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

package com.eziosoft.arucomqtt.repository.roomba

class TestSensors {
    //    fun test() {
//        lifecycleScope.launch(Dispatchers.Main) {
//            while (true) {
//                delay(15)
//                val v: UByteArray = (Random.nextInt(-2000, 2000)).to16UByteArray()
//
//                val data1: ArrayList<UByte> =
//                    arrayListOf(
//                        19u,
//                        35u,
//                        46u,
//                        10u,
//                        Random.nextInt(255).toUByte(),
//                        47u,
//                        10u,
//                        Random.nextInt(255).toUByte(),
//                        48u,
//                        10u,
//                        Random.nextInt(255).toUByte(),
//                        49u,
//                        10u,
//                        Random.nextInt(255).toUByte(),
//                        50u,
//                        10u,
//                        Random.nextInt(255).toUByte(),
//                        51u,
//                        10u,
//                        Random.nextInt(255).toUByte(),
//                        26u,
//                        100u,
//                        0u,
//                        25u,
//                        80u,
//                        Random.nextInt(255).toUByte(),
//                        23u,
//                        v[0],
//                        v[1],
//                        22u,
//                        0u,
//                        Random.nextInt(200).toUByte(),
//                        29u,
//                        2u,
//                        Random.nextInt(200).toUByte(),
//                        13u,
//                        Random.nextInt(2).toUByte()
//                    )
//                val checksum = 256u - data1.sum()
//                data1.add((checksum.toUByte()))
//                viewModel.sensorParser.parse(data1.toUByteArray())
//            }
//        }
//    }
}