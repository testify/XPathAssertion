/*
 * Copyright 2015 Codice Foundation
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package XPath;

import org.codice.testify.assertions.Assertion;
import org.codice.testify.objects.AssertionStatus;
import org.codice.testify.objects.TestifyLogger;
import org.codice.testify.objects.Response;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * The XPathAssertion class is a Testify Assertion service that performs XPath based checks
 */
public class XPathAssertion implements BundleActivator, Assertion {

    @Override
    public AssertionStatus evaluateAssertion(String assertionInfo, Response response) {

        TestifyLogger.debug("Running XPathAssertion", this.getClass().getSimpleName());

        //Get the processor response
        String responseValue = response.getResponse();
        AssertionStatus status;

        //If no assertion info is provided, return a failure
        if (assertionInfo == null) {
            status = new AssertionStatus("No XPath expression provided with assertion");

        //If the response from the processor is null, return a failure
        } else if (responseValue == null) {
            status = new AssertionStatus("No processor response");

        } else {

            //Set up document and xpath objects
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            //Parse processor response into document
            Document document = null;
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                document = builder.parse(new InputSource(new ByteArrayInputStream(responseValue.getBytes("utf-8"))));
            } catch (ParserConfigurationException | SAXException | IOException e) {
                TestifyLogger.error(e.getMessage(), this.getClass().getSimpleName());
            }

            //If document is not null, run xpath expression
            if (document != null) {
                try {

                    //Run xpath expression and return boolean
                    XPathExpression xpathExpression = xpath.compile(assertionInfo);
                    boolean xpathResult = (boolean)xpathExpression.evaluate(document.getDocumentElement(), XPathConstants.BOOLEAN);

                    //If the XPathExpression returns true, return failure details of null meaning a successful assertion
                    if (xpathResult) {
                        status =  new AssertionStatus(null);

                    //If the XPathExpression returns false, return a failure
                    } else {
                        status =  new AssertionStatus("XPath expression returned false");
                    }

                } catch (XPathExpressionException e) {
                    status =  new AssertionStatus("XPath expression formatted incorrectly");
                }

            } else {

                //If document is null, return a failure
                status =  new AssertionStatus("Could not create xml document from response");
            }
        }
        return status;
    }

    @Override
    public void start(BundleContext bundleContext) throws Exception {

        //Register the XPath service
        bundleContext.registerService(Assertion.class.getName(), new XPathAssertion(), null);

    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}