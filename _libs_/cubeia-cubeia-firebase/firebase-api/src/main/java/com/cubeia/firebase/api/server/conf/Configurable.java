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
package com.cubeia.firebase.api.server.conf;

/**
 * This is the root interface to extend for configuration interfaces. Configurable interfaces 
 * will be fulfilled by the configuration system based on configuration sources which can
 * be flat files, databases or network sources.
 * 
 * <p>The rules for configurable interfaces are dependent on underlying
 * implementation, but in short these steps will happen:
 * 
 * <ol>
 *   <li>The interface is associated with one or more config sources.</li>
 *   <li>All "get", "is", "do", "has" methods are matched by rules to property names (see below)</li>
 *   <li>Default values are set using {@link Property annotations}</li>
 *   <li>An anonymous object implementing the interface is returned which returns values from the 
 *       config sources as calculated in previous steps</li>
 * </ol>
 *  
 * <b>Property Resolution:</b> Method names are matched to property names by converting
 * the method name, like so:<p>
 * 
 * <ol>
 *   <li>Method "prefix" ("get", "is", "do", "has") is removed.</li>
 *   <li>The character immediately following the prefix is converted to lower case.</li>
 *   <li>All other upper case characters are converted to lower case and prefixed with a hyphen.</li>
 * </ol>
 * 
 * For example, the method "getClassName" will match the property "class-name", the method "isAlone"
 * the property "alone" etc. A special case is if a prefix is not found, in which case only conversion
 * rule #3 above is used, eg. method "googleMe" will match "google-me".
 * 
 * <p>Property names can be overridden with a method {@link Property annotation}.
 * 
 * <p><b>Inheritance:</b> Each interface can be mapped to an optional {@link Namespace namespace} via
 * a root {@link Configurated annotation}. This is a logical string used to separate properties. 
 * Namespaces are resolved from left to right and exhibits relations on matching strings, ie. namespace 
 * "com.company" is a <em>child</em> namespace to "com", "com.busted" is a <em>parent</em> namespace 
 * to "com.busted.hard" etc. 
 * 
 * <p>A configurable interface mapped to a namespace will force the properties to to be resolved in relation
 * to the namespace. Some examples:
 * 
 * <ul>
 *   <li>Namespace "node.client" and method "getNodeClass" resolves to "node.client.node-class".</li>
 *   <li>Namespace "service" and method "doAutoStart" resolves to "service.auto-start".</li>
 *   <li>Namespace &lt;null&gt; and method "sayHello" resolves to "say-hello".</li>
 * </ul>
 * 
 * Both the interface and the methods can be marked using their annotations ({@link Configurated} and
 * {@link Property} respectively) as allowing {@link Inheritance#ALLOW inheritance}. This will allow
 * the system to resolve properties from their logical <em>parent</em> namespace if not found in the declared
 * namespace. For example, if an interface is mapped to "node.client" and has a method "getNodeClass" and is
 * marked to allow inheritance, the system will first search for property "node.client.node-class" and if it is not
 * found it will recurse "backwards" in the namespace and search for "node.node-class" and ultimately "node-class".
 * This allows the configuration to supply default values in parent namespaces and specific overriding values
 * in <em>child</em> namespaces.
 * 
 * <p><b>Fallback: </b> If a property is not found during resolution a fall back to another property can be made. This is 
 * indicated by the "fallback" member of the {@link Property} annotation. If a fallback is declared, the ordinary property
 * name is resolved first, including inheritance, after which the fallback name is tried, also using inheritance. This way
 * a configuration method can default to the value of another property if not specificly specified.
 * 
 * <p>If an interface is marked with inheritance rules it can still be overridden for single methods.
 * 
 * <p>All method on a configurable interface should declared that they throw a configuration 
 * {@link ConfigurationException exception} to allow the subsystem to pass up errors to the calling code.
 * 
 * @author lars.j.nilsson
 */
@Configurated
public interface Configurable { }
