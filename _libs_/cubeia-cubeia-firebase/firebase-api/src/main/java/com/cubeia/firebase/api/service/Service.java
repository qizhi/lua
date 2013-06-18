/**
 * Copyright (C) 2011 Cubeia Ltd <info@cubeia.com>
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
package com.cubeia.firebase.api.service;

import com.cubeia.firebase.api.server.Initializable;
import com.cubeia.firebase.api.server.Startable;

/**
 * A service is defined per server within the firebase cluster. It exposes
 * its {@link Contract contract} to all server members such as core modules, games and
 * other services. A service is:
 * 
 * <ul>
 *   <li>singleton - The server will only instantiate one class of the particular service.</li>
 *   <li>thread safe - A service must be able to handle concurrent threads.</li>
 *   <li>contract - Implements this interface <b>and</b> a {@link Contract contract} extension</li>
 * </ul>
 * 
 * <b>Life cycle</b><br>
 * A service will be instantiated and controlled by the server. It will only be 
 * instantiated once. The life cycle follows that of the ${@link Initializable} and
 * ${@link Startable} interfaces. In short the a life cycle looks like this:<br><br>
 * 
 * <ol>
 *   <li>init</li>
 *   <li>start</li>
 *   <li>&lt;running&gt;</li>
 *   <li>stop</li>
 *   <li>destroy</li>
 * </ol>
 * 
 * <b>Concurrency</b><br>
 * As a single instance of a service will be responsible for the entire server and its
 * node stack, inluding all games, it must be thread safe. Importantly, this applies even to
 * the ${@link Startable#stop() stop} and ${@link Initializable#destroy() destroy} methods. This 
 * is because long running tasks may not be interrupted correctly when the server shuts down, thus
 * arriving after the service has legally been shut down. At the least, a service must not, if such
 * requests are encountered, under any circumstances block the calling thread and thus causing the
 * entire server to hang during the shutdown sequence.<br><br>
 * 
 * <b>Contract</b><br>
 * This interface is the <em>internal</em> service contract and will not be seen by users of the service. A separate
 * {@link Contract contract} should be implemented which will be what is actually exposed to calling members
 * as the <em>external</em> interface. Ie., the minimal steps needed to create a service are these:<br><br>
 * 
 * <ol>
 *   <li>Create an interface extending {@link Contract} which contains the services business methods.</li>
 *   <li>Create a class which implements this interface as <b>and</b> the new interface declared in #1.</li>
 * </ol>
 * 
 * Services are packaged in separate archives and has their own dependencies and class loader isolation. Please
 * refer to the developers manual for information.
 * 
 * <p><b>Service Dependencies: </b> Services must not try to interact with each other during initialization
 * as the server do not guarantee startup order. Should a service depend on another service during initialization
 * it must declare so in its descriptor, please refer to the developers manual for more information.
 * 
 * @author lars.j.nilsson
 */
public interface Service extends Initializable<ServiceContext>, Startable { }