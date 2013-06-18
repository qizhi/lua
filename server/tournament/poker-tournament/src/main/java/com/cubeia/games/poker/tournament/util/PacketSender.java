/**
 * Copyright (C) 2012 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cubeia.games.poker.tournament.util;

import com.cubeia.firebase.api.action.mtt.MttDataAction;
import com.cubeia.firebase.api.mtt.MttInstance;
import com.cubeia.firebase.api.mtt.MttNotifier;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.google.inject.assistedinject.Assisted;
import org.apache.log4j.Logger;

import javax.inject.Inject;

public class PacketSender {

    private static final Logger log = Logger.getLogger(PacketSender.class);

    private StyxSerializer serializer;

    private MttNotifier notifier;

    private MttInstance instance;

    @Inject
    public PacketSender(StyxSerializer serializer, @Assisted MttNotifier notifier, @Assisted MttInstance instance) {
        this.serializer = serializer;
        this.notifier = notifier;
        this.instance = instance;
    }

    public void sendPacketToPlayer(ProtocolObject packet, int playerId) {
        MttDataAction action = new MttDataAction(instance.getId(), playerId);
        action.setData(serializer.pack(packet));
        notifier.notifyPlayer(playerId, action);
    }
}
