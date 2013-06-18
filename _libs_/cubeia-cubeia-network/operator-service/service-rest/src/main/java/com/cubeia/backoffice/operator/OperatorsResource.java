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
package com.cubeia.backoffice.operator;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.cubeia.backoffice.operator.api.OperatorDTO;
import com.cubeia.backoffice.operator.service.OperatorService;
import com.sun.jersey.api.core.ResourceContext;

@Path("/operator")
@Component
@Scope("request")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class OperatorsResource {

	Logger log = LoggerFactory.getLogger(getClass());
	
    @Autowired
    private OperatorService operatorService;

    @Context
    private ResourceContext ctx;
    
    @GET
    public List<OperatorDTO> getOperators() {
        return operatorService.getOperators();
    }

    @Path("id/{operatorId}")
    public OperatorResource getOperator(@PathParam("operatorId") Long operatorId) {
    	OperatorResource operatorResource = ctx.getResource(OperatorResource.class);
    	operatorResource.setOperatorId(operatorId);
        return operatorResource;
    }
    
    @Path("key/{apiKey}")
    @GET
    public OperatorDTO getOperator(@PathParam("apiKey") String apiKey) {
    	try {
    		log.info("Get operator by API Key: "+apiKey);
    		return operatorService.getOperatorByApiKey(apiKey);
    	} catch (IllegalArgumentException e) {
    		throw new WebApplicationException(Status.BAD_REQUEST);
    	}
    }
   
    @Path("update")
    @PUT
    public Response updateOperator(OperatorDTO operator) {
        operatorService.updateOperator(operator);
        return Response.status(Response.Status.OK).build();
    }

    @Path("create")
    @POST
    public Response createOperator(OperatorDTO operator) {
    	log.info("Create new operator: "+operator);
        operatorService.createOperator(operator);
        log.info("New operator created: "+operator);
        return Response.status(Response.Status.OK).build();
    }

}
