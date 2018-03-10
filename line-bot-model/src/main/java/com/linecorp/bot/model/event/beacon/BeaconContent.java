/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.bot.model.event.beacon;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Value;

@Value
public class BeaconContent {

    private final String hwid;


    private final String type;


    private final byte[] deviceMessage;


    public byte[] getDeviceMessage() {
        if (deviceMessage == null) {
            return null;
        }
        return deviceMessage.clone(); 
    }


    public String getDeviceMessageAsHex() {
        return BeaconContentUtil.printHexBinary(deviceMessage);
    }

    public BeaconContent(
            @JsonProperty("hwid") String hwid,
            @JsonProperty("type") String type,
            @JsonProperty("dm") String deviceMessageAsHex) {
        this.hwid = hwid;
        this.type = type;
        this.deviceMessage = BeaconContentUtil.parseBytesOrNull(deviceMessageAsHex);
    }


    @Override
    public String toString() {
        return "BeaconContent"
               + "(hwid=" + getHwid()
               + ", type=" + getType()
               + ", deviceMessage=" + getDeviceMessageAsHex() + ')';
    }
}
