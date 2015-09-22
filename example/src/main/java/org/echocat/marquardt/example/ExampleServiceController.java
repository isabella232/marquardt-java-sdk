/*
 * echocat Marquardt Java SDK, Copyright (c) 2015 echocat
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.echocat.marquardt.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

@Controller
@RequestMapping("/exampleservice")
public class ExampleServiceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleServiceController.class);

    @RequestMapping(value = "/someProtectedResource", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void someProtectedResource() throws IOException {
        LOGGER.info("/exampleservice/someProtectedResource received a request");
    }

    @RequestMapping(value = "/someUnprotectedResource", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void someUnprotectedResource() throws IOException {
        LOGGER.info("/exampleservice/someUnprotectedResource received a request");
    }

    @SuppressWarnings("UnusedParameters")
    @RequestMapping(value = "/someProtectedResourceWithPayload", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void someProtectedResourceWithPayload(final String payload) throws IOException {
        LOGGER.info("/exampleservice/someUnprotectedResource received a request");
    }

}
