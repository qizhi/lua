/*
Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library.  If not, see <http://www.gnu.org/licenses/>.
*/

#include "PacketManager.h"

#include <algorithm>

namespace styx {

	PacketManager *PacketManager::instance = new PacketManager();

	PacketManager::PacketManager()
	{
	}

	void PacketManager::addHandler(PacketHandler *handler)
	{
		std::list<PacketHandler*>::iterator ix =  std::find(handlerList.begin(), handlerList.end(), handler);
		if ( ix == handlerList.end() ) {
			handlerList.push_back(handler);
		}
	}

	void PacketManager::removeHandler(PacketHandler *handler)
	{
		std::list<PacketHandler*>::iterator ix =  std::find(handlerList.begin(), handlerList.end(), handler);
		if ( ix != handlerList.end() ) {
			handlerList.remove(handler);
		}
	}

	void PacketManager::broadcastPacket(styx::ProtocolObject *protocolObject)
	{
		for ( std::list<PacketHandler*>::iterator ix = handlerList.begin(); ix != handlerList.end(); ix ++) {
			PacketHandler *handler = *ix;
			if ( handler->handlePacket(protocolObject) )
				break;
		}
	}

	PacketManager *PacketManager::getInstance(void)
	{
		return instance;
	}
}