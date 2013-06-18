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

package com.cubeia.backoffice.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;

import com.cubeia.backoffice.web.error.RemoteServiceError;

public class BackofficeRequestCycleListener extends AbstractRequestCycleListener {

    @Override
    public IRequestHandler onException(RequestCycle cycle, Exception ex) {
        // search for errors we want to intercept
        for (Throwable t : flattenCauses(ex)) {
            if (t instanceof HttpException) {
                cycle.setResponsePage(new RemoteServiceError(t.getMessage()));
            }
        }
        
        return super.onException(cycle, ex);
    }
    
    
    
//    @SuppressWarnings("unused")
//    private Logger log = LoggerFactory.getLogger(getClass());
//    
//    public BackofficeRequestCycle(BackofficeApplication application, WebRequest request, Response response) {
//        
//        new RequestCycleContext(request, response, requestMapper, exceptionMapper);
//        
//        super(application, request, response);
//    }
//
//    @Override
//    public Page onRuntimeException(Page page, RuntimeException e) {
//        
//        // search for errors we want to intercept
//        for (Throwable t : flattenCauses(e)) {
//            if (t instanceof HttpException) {
//                return new RemoteServiceError(t.getMessage());
//            }
//        }
//        
//        return super.onRuntimeException(page, e);
//    }
//    
    
    /**
     * Returns a list of causes by recursively following {@link Throwable#getCause()}.
     * @param e starting exception (throwable)
     * @return list of causes starting with the given exception
     */
    private List<Throwable> flattenCauses(Throwable e) {
        ArrayList<Throwable> causes = new ArrayList<Throwable>();
        fillInCausesRecursive(causes, e);
        return causes;
    }
    
    private void fillInCausesRecursive(List<Throwable> causes, Throwable e) {
        if (e != null) {
            causes.add(e);
            fillInCausesRecursive(causes, e.getCause());
        }
    }
//    
    
}
