/*
 * File: SetDatastreamVersionable.java
 *
 * Copyright 2009 2DC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fcrepo.server.security.xacml.pep.rest.objectshandlers;

import java.io.IOException;

import java.net.URI;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.ctx.RequestCtx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fcrepo.common.Constants;

import org.fcrepo.server.security.xacml.pdp.data.FedoraPolicyStore;
import org.fcrepo.server.security.xacml.pep.PEPException;
import org.fcrepo.server.security.xacml.pep.rest.filters.AbstractFilter;
import org.fcrepo.server.security.xacml.util.LogUtil;


/**
 * Handles the SetDatastreamVersionable operation.
 *
 * @author nish.naidoo@gmail.com
 */
public class SetDatastreamVersionable
        extends AbstractFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(SetDatastreamVersionable.class);

    /**
     * Default constructor.
     *
     * @throws PEPException
     */
    public SetDatastreamVersionable()
            throws PEPException {
        super();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.fcrepo.server.security.xacml.pep.rest.filters.RESTFilter#handleRequest(javax.servlet
     * .http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public RequestCtx handleRequest(HttpServletRequest request,
                                    HttpServletResponse response)
            throws IOException, ServletException {
        if (logger.isDebugEnabled()) {
            logger.debug(this.getClass().getName() + "/handleRequest!");
        }

        String path = request.getPathInfo();
        String[] parts = path.split("/");

        String pid = parts[1];
        String dsID = parts[3];
        String versionable = request.getParameter("versionable");

        RequestCtx req = null;
        Map<URI, AttributeValue> actions = new HashMap<URI, AttributeValue>();
        Map<URI, AttributeValue> resAttr = new HashMap<URI, AttributeValue>();
        try {
            if (pid != null && !"".equals(pid)) {
                resAttr.put(Constants.OBJECT.PID.getURI(),
                            new StringAttribute(pid));
            }
            if (pid != null && !"".equals(pid)) {
                resAttr.put(new URI(XACML_RESOURCE_ID),
                            new AnyURIAttribute(new URI(pid)));
            }
            if (dsID != null && !"".equals(dsID)) {
                resAttr.put(Constants.DATASTREAM.ID.getURI(),
                            new StringAttribute(dsID));
            }
            if (versionable != null && !"".equals(versionable)) {
                resAttr.put(Constants.DATASTREAM.NEW_VERSIONABLE.getURI(),
                            new StringAttribute(versionable));
            }

            actions
                    .put(Constants.ACTION.ID.getURI(),
                         new StringAttribute(Constants.ACTION.SET_DATASTREAM_VERSIONABLE
                                 .getURI().toASCIIString()));
            actions.put(Constants.ACTION.API.getURI(),
                        new StringAttribute(Constants.ACTION.APIM.getURI()
                                .toASCIIString()));
            // modifying the FeSL policy datastream requires policy management permissions
            if (dsID != null && dsID.equals(FedoraPolicyStore.POLICY_DATASTREAM)) {
                actions.put(Constants.ACTION.ID.getURI(),
                            new StringAttribute(Constants.ACTION.MANAGE_POLICIES.getURI().toASCIIString()));
            }


            req =
                    getContextHandler().buildRequest(getSubjects(request),
                                                     actions,
                                                     resAttr,
                                                     getEnvironment(request));

            LogUtil.statLog(request.getRemoteUser(),
                            Constants.ACTION.SET_DATASTREAM_VERSIONABLE
                                    .getURI().toASCIIString(),
                            pid,
                            dsID);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ServletException(e.getMessage(), e);
        }

        return req;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.fcrepo.server.security.xacml.pep.rest.filters.RESTFilter#handleResponse(javax.servlet
     * .http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public RequestCtx handleResponse(HttpServletRequest request,
                                     HttpServletResponse response)
            throws IOException, ServletException {
        return null;
    }
}
