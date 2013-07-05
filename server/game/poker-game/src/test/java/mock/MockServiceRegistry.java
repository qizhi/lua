/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
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

package mock;

import com.cubeia.firebase.api.service.Contract;
import com.cubeia.firebase.api.service.ServiceInfo;
import com.cubeia.firebase.api.service.ServiceRegistry;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockServiceRegistry implements ServiceRegistry {

    @SuppressWarnings("rawtypes")
    private Map<Class, Object> serviceMap = new HashMap<Class, Object>();

    public <T extends Contract> void addService(Class<T> contract, T service) {
        serviceMap.put(contract, service);
    }

    @SuppressWarnings("unchecked")
    public <T extends Contract> T getServiceInstance(Class<T> serviceClass) {
        return (T) serviceMap.get(serviceClass);
    }

    public ServiceInfo getServiceInfo(String arg0) {
        return null;
    }

    public <T extends Contract> ServiceInfo getServiceInfo(Class<T> arg0, String arg1) {
        return null;
    }

    public Contract getServiceInstance(String arg0) {
        return null;
    }

    public <T extends Contract> T getServiceInstance(Class<T> arg0, String arg1) {
        return null;
    }

    @Override
    public <T extends Annotation> List<ServiceInfo> listServicesByAnnotation(
            Class<T> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Contract> List<ServiceInfo> listServicesByContract(
            Class<T> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
