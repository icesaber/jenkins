package org.jvnet.hudson.main;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import hudson.LocalPluginManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.jvnet.hudson.test.recipes.LocalData;
import org.jvnet.hudson.test.recipes.PresetData;
import org.jvnet.hudson.test.recipes.PresetData.DataSet;
import org.jvnet.hudson.test.recipes.WithPlugin;
import org.jvnet.hudson.test.recipes.WithPluginManager;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class UseRecipesWithJenkinsRuleTest {

    @Rule
    public JenkinsRule rule = new JenkinsRule();

    @Test
    @LocalData
    public void testGetItemFromLocalData() {
        assertNotNull(rule.jenkins.getItem("testJob"));
    }

    @Test
    @WithPlugin("tasks.hpi")
    public void testWithPlugin() {
        assertNotNull(rule.jenkins.getPlugin("tasks"));
    }

    @Test
    @PresetData(DataSet.ANONYMOUS_READONLY)
    public void testPresetData() throws Exception {
        WebClient wc = rule.createWebClient();
        try {
            wc.goTo("loginError");
            fail("Expecting a 401 error");
        } catch (FailingHttpStatusCodeException e) {
            e.printStackTrace();
            assertEquals(SC_UNAUTHORIZED,e.getStatusCode());
        }

        // but not once the user logs in.
        verifyNotError(wc.login("alice"));
    }

    @Test
    @WithPluginManager(MyPluginManager.class)
    public void testWithPluginManager() {
        assertEquals(MyPluginManager.class, rule.jenkins.pluginManager.getClass());
    }

    private void verifyNotError(WebClient wc) throws IOException, SAXException {
        HtmlPage p = wc.goTo("loginError");
        URL url = p.getWebResponse().getUrl();
        System.out.println(url);
        assertFalse(url.toExternalForm().contains("login"));
    }

    public static class MyPluginManager extends LocalPluginManager {
        public MyPluginManager(File rootDir) {
            super(rootDir);
        }
    }
}
