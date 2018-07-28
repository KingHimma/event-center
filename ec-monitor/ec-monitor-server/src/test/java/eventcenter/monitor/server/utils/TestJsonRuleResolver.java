package eventcenter.monitor.server.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by liumingjian on 16/2/26.
 */
public class TestJsonRuleResolver {

    @Test
    public void testResolveLevel1() throws Exception {
        JsonRuleResolver r = new JsonRuleResolver();
        JsonRule jr = new JsonRule();
        r.resolveLevel(jr, "[]id");
        Assert.assertEquals(true, jr.isArray());
        Assert.assertNull(jr.getArrayIndex());
        Assert.assertEquals("id", jr.getField());
    }

    @Test
    public void testResolveLevel2() throws Exception {
        JsonRuleResolver r = new JsonRuleResolver();
        JsonRule jr = new JsonRule();
        r.resolveLevel(jr, "[1]id");
        Assert.assertEquals(true, jr.isArray());
        Assert.assertEquals(1, jr.getArrayIndex().intValue());
        Assert.assertEquals("id", jr.getField());
    }

    @Test
    public void testResolveLevel3() throws Exception {
        JsonRuleResolver r = new JsonRuleResolver();
        JsonRule jr = new JsonRule();
        r.resolveLevel(jr, "id");
        Assert.assertEquals(false, jr.isArray());
        Assert.assertNull(jr.getArrayIndex());
        Assert.assertEquals("id", jr.getField());
    }

    @Test
    public void testResolveOne1(){
        JsonRuleResolver r = new JsonRuleResolver();
        JsonRule jr = new JsonRule();
        JsonRule jsonRule = r.resolveOne("[]child.id");
        Assert.assertEquals(true, jsonRule.isArray());
        Assert.assertNull(jsonRule.getArrayIndex());
        Assert.assertEquals("child", jsonRule.getField());
        JsonRule child = jsonRule.getEmbedded();
        Assert.assertNotNull(child);
        Assert.assertEquals("id", child.getField());
    }

    @Test
    public void testResolveOne2(){
        JsonRuleResolver r = new JsonRuleResolver();
        JsonRule jr = new JsonRule();
        JsonRule jsonRule = r.resolveOne("classroom.[]student.[]topics.name");
        Assert.assertFalse(jsonRule.isArray());
        Assert.assertEquals("classroom", jsonRule.getField());
        JsonRule child = jsonRule.getEmbedded();
        Assert.assertNotNull(child);
        Assert.assertTrue(child.isArray());
        Assert.assertNull(child.getArrayIndex());
        Assert.assertEquals("student", child.getField());
        child = child.getEmbedded();
        Assert.assertNotNull(child);
        Assert.assertEquals("topics", child.getField());
        child = child.getEmbedded();
        Assert.assertNotNull(child);
        Assert.assertEquals("name", child.getField());
    }
}
