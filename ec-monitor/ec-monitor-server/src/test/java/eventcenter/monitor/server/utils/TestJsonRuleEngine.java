package eventcenter.monitor.server.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by liumingjian on 16/2/28.
 */
public class TestJsonRuleEngine {
    JsonRuleEngine engine;

    @Before
    public void setUp() throws Exception {
        engine = new JsonRuleEngine();

    }

    @Test
    public void testAppendString1() throws Exception {
        StringBuilder sb = new StringBuilder();
        engine.appendString(sb, "test");
        Assert.assertEquals("test", sb.toString());
    }

    @Test
    public void testAppendString2() throws Exception {
        StringBuilder sb = new StringBuilder("test");
        engine.appendString(sb, "test");
        Assert.assertEquals("test test", sb.toString());
    }

    @Test
    public void testAppendString3() throws Exception {
        engine.setSeparatorChar("-");
        StringBuilder sb = new StringBuilder("test");
        engine.appendString(sb, "test");
        engine.appendString(sb, "test2");
        Assert.assertEquals("test-test-test2", sb.toString());
    }

    @Test
    public void testFindJsonArray1() throws Exception {
        JSONArray ja = new JSONArray();
        JSONObject jo = new JSONObject();
        jo.put("id", 1);
        ja.add(jo);
        Assert.assertEquals("1", engine.findJsonArray(ja, 0, "id"));
    }

    @Test
    public void testFindJsonArray2() throws Exception {
        JSONArray ja = new JSONArray();
        JSONObject jo = new JSONObject();
        jo.put("id", 1);
        ja.add(jo);
        jo = new JSONObject();
        jo.put("id",2);
        ja.add(jo);
        Assert.assertEquals("2", engine.findJsonArray(ja, 1, "id"));
    }

    @Test
    public void testFindJsonArray3() throws Exception {
        JSONArray ja = new JSONArray();
        JSONObject jo = new JSONObject();
        jo.put("id", 1);
        ja.add(jo);
        jo = new JSONObject();
        jo.put("id",2);
        ja.add(jo);
        Assert.assertEquals("1 2", engine.findJsonArray(ja, null, "id"));
    }

    @Test
    public void test_resolve1() throws Exception {
        final StringBuilder sb = new StringBuilder();
        JSONObject jo = new JSONObject();
        jo.put("id", 1);
        JsonRule jr = new JsonRule();
        jr.setField("id");
        jr.setArray(false);
        engine._resolve(sb, jo, jr);
        Assert.assertEquals("1", sb.toString());
    }

    @Test
    public void test_resolve2() throws Exception {
        final StringBuilder sb = new StringBuilder();
        JSONObject jo = new JSONObject();
        JSONArray classrooms = new JSONArray();
        jo.put("classroom", classrooms);
        JSONArray students = new JSONArray();
        JSONObject classroom = new JSONObject();
        classroom.put("name", "中一班");
        classroom.put("students", students);
        classrooms.add(classroom);
        JSONObject student = new JSONObject();
        student.put("name", "Jacky");
        students.add(student);

        JsonRuleResolver resolver = engine.findResolver("classroom.[0]students.[0]name");
        engine._resolve(sb, jo, resolver.getJsonRules()[0]);
        Assert.assertEquals("Jacky", sb.toString());
    }

    @Test
    public void test_resolve3() throws Exception {
        final StringBuilder sb = new StringBuilder();
        JSONObject jo = new JSONObject();
        JSONArray classrooms = new JSONArray();
        jo.put("classroom", classrooms);
        JSONArray students = new JSONArray();
        JSONObject classroom = new JSONObject();
        classroom.put("name", "中一班");
        classroom.put("students", students);
        classrooms.add(classroom);
        JSONObject student = new JSONObject();
        student.put("name", "Jacky");
        students.add(student);
        student = new JSONObject();
        student.put("name", "YUTAO");
        students.add(student);

        JsonRuleResolver resolver = engine.findResolver("classroom.[0]students.[]name");
        engine._resolve(sb, jo, resolver.getJsonRules()[0]);
        Assert.assertEquals("Jacky YUTAO", sb.toString());
    }

    @Test
    public void test_resolve4() throws Exception {
        final StringBuilder sb = new StringBuilder();
        JSONObject jo = new JSONObject();
        JSONArray classrooms = new JSONArray();
        jo.put("classroom", classrooms);
        JSONArray students = new JSONArray();
        JSONObject classroom = new JSONObject();
        classroom.put("name", "中一班");
        classroom.put("students", students);
        classrooms.add(classroom);
        JSONObject student = new JSONObject();
        student.put("name", "Jacky");
        students.add(student);
        student = new JSONObject();
        student.put("name", "YUTAO");
        students.add(student);
        students = new JSONArray();
        classroom = new JSONObject();
        classroom.put("name", "中二班");
        classroom.put("students", students);
        classrooms.add(classroom);
        student = new JSONObject();
        student.put("name", "KIKI");
        students.add(student);

        JsonRuleResolver resolver = engine.findResolver("classroom.[]students.[]name");
        engine._resolve(sb, jo, resolver.getJsonRules()[0]);
        Assert.assertEquals("Jacky YUTAO KIKI", sb.toString());
    }

    @Test
    public void test_resolve5() throws Exception {
        final StringBuilder sb = new StringBuilder();
        JSONObject jo = new JSONObject();
        JSONArray classrooms = new JSONArray();
        jo.put("classroom", classrooms);
        JSONArray students = new JSONArray();
        JSONObject classroom = new JSONObject();
        classroom.put("name", "中一班");
        classroom.put("students", students);
        classrooms.add(classroom);
        JSONObject student = new JSONObject();
        student.put("name", "Jacky");
        students.add(student);
        student = new JSONObject();
        student.put("name", "YUTAO");
        students.add(student);
        students = new JSONArray();
        classroom = new JSONObject();
        classroom.put("name", "中二班");
        classroom.put("students", students);
        classrooms.add(classroom);
        student = new JSONObject();
        student.put("name", "KIKI");
        students.add(student);

        System.out.println(jo.toJSONString());
        JsonRuleResolver resolver = engine.findResolver("classroom.[]name");
        engine._resolve(sb, jo, resolver.getJsonRules()[0]);
        Assert.assertEquals("中一班 中二班", sb.toString());
    }

    @Test
    public void testResolve1() throws Exception {
        final StringBuilder sb = new StringBuilder();
        JSONObject jo = new JSONObject();
        JSONArray classrooms = new JSONArray();
        jo.put("classroom", classrooms);
        JSONArray students = new JSONArray();
        JSONObject classroom = new JSONObject();
        classroom.put("name", "中一班");
        classroom.put("students", students);
        classrooms.add(classroom);
        JSONObject student = new JSONObject();
        student.put("name", "Jacky");
        students.add(student);
        student = new JSONObject();
        student.put("name", "YUTAO");
        students.add(student);
        students = new JSONArray();
        classroom = new JSONObject();
        classroom.put("name", "中二班");
        classroom.put("students", students);
        classrooms.add(classroom);
        student = new JSONObject();
        student.put("name", "KIKI");
        students.add(student);

        Assert.assertEquals("中一班 中二班 Jacky YUTAO KIKI", engine.resolve(jo, "classroom.[]name,classroom.[]students.[]name"));
    }

    @Test
    public void testResolve2() throws Exception {
        final StringBuilder sb = new StringBuilder();
        JSONObject jo = new JSONObject();
        JSONArray classrooms = new JSONArray();
        jo.put("classroom", classrooms);
        JSONArray students = new JSONArray();
        JSONObject classroom = new JSONObject();
        classroom.put("name", "中一班");
        classroom.put("students", students);
        classrooms.add(classroom);
        JSONObject student = new JSONObject();
        student.put("name", "Jacky");
        students.add(student);
        student = new JSONObject();
        student.put("name", "YUTAO");
        students.add(student);
        students = new JSONArray();
        classroom = new JSONObject();
        classroom.put("name", "中二班");
        classroom.put("students", students);
        classrooms.add(classroom);
        student = new JSONObject();
        student.put("name", "KIKI");
        students.add(student);

        Assert.assertEquals("", engine.resolve(jo, ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolve3() throws Exception {
        engine.resolve(null, "classroom.[]name,classroom.[]students.[]name");
    }
}
