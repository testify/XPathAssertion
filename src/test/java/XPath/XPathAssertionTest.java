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

import org.codice.testify.objects.AssertionStatus;
import org.codice.testify.objects.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XPathAssertionTest {

    @Test
    public void testNoAssertionInfo() {
        XPathAssertion xpathAssertion = new XPathAssertion();
        AssertionStatus assertionStatus = xpathAssertion.evaluateAssertion(null, new Response("<a><b>something</b></a>"));
        assert ( assertionStatus.getFailureDetails().equals("No XPath expression provided with assertion") );
    }

    @Test
    public void testNoResponse() {
        XPathAssertion xpathAssertion = new XPathAssertion();
        AssertionStatus assertionStatus = xpathAssertion.evaluateAssertion("count(//a)=1", new Response(null));
        assert ( assertionStatus.getFailureDetails().equals("No processor response") );
    }

    @Test
    public void testBadResponseFormat() {
        XPathAssertion xpathAssertion = new XPathAssertion();
        AssertionStatus assertionStatus = xpathAssertion.evaluateAssertion("count(//a)=1", new Response("<abc><b>something</b></a>"));
        assert ( assertionStatus.getFailureDetails().equals("Could not create xml document from response") );
    }

    @Test
    public void testBadXPathExpression() {
        XPathAssertion xpathAssertion = new XPathAssertion();
        AssertionStatus assertionStatus = xpathAssertion.evaluateAssertion("counting(((//a)=1", new Response("<a><b>something</b></a>"));
        assert ( assertionStatus.getFailureDetails().equals("XPath expression formatted incorrectly") );
    }

    @Test
    public void testTrueXPathExpression() {
        XPathAssertion xpathAssertion = new XPathAssertion();
        AssertionStatus assertionStatus = xpathAssertion.evaluateAssertion("//*[local-name()='b']='something'", new Response("<a><b>something</b></a>"));
        assert ( assertionStatus.isSuccess() );
    }

    @Test
    public void testTrueXPathExpressionWithNamespace() {
        XPathAssertion xpathAssertion = new XPathAssertion();
        AssertionStatus assertionStatus = xpathAssertion.evaluateAssertion("//*[local-name()='id' and namespace-uri()='uri2']=12345", new Response("<soap:Envelope xmlns:soap=\"http://www.test.test\"><soap:Body><ns2:something xmlns=\"uri1\" xmlns:ns2=\"uri2\"><ns2:id>12345</ns2:id></ns2:something></soap:Body></soap:Envelope>"));
        assert ( assertionStatus.isSuccess() );
    }

    @Test
    public void testFalseXPathExpression() {
        XPathAssertion xpathAssertion = new XPathAssertion();
        AssertionStatus assertionStatus = xpathAssertion.evaluateAssertion("//*[local-name()='b']='something else'", new Response("<a><b>something</b></a>"));
        assert ( assertionStatus.getFailureDetails().equals("XPath expression returned false") );
    }

    @Test
    public void testFalseXPathExpressionWithNamespace() {
        XPathAssertion xpathAssertion = new XPathAssertion();
        AssertionStatus assertionStatus = xpathAssertion.evaluateAssertion("//*[local-name()='id' and namespace-uri()='uri2']=54321", new Response("<soap:Envelope xmlns:soap=\"http://www.test.test\"><soap:Body><ns2:something xmlns=\"uri1\" xmlns:ns2=\"uri2\"><ns2:id>12345</ns2:id></ns2:something></soap:Body></soap:Envelope>"));
        assert ( assertionStatus.getFailureDetails().equals("XPath expression returned false") );
    }

}