package eventcenter.monitor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

/**
 * Created by liumingjian on 16/3/30.
 */
public class TestAbstractMonitorDataCodec {

    SampleMonitorDataCodec codec;
    
    final String eventName = "test";

    @Before
    public void setUp() throws Exception {
        codec = new SampleMonitorDataCodec();
    }

    /**
     * 测试UserData类型的编码
     * @throws Exception
     */
    @Test
    public void testCodec1() throws Exception {
        UserData data = new UserData();
        data.setId(1L);
        data.setName("test");
        Object result = codec.codec(eventName, data);
        Assert.assertEquals(true, result instanceof Map);
        Map<String, Object> map = (Map<String, Object>)result;
        Assert.assertTrue(map.containsKey("userId"));
        Assert.assertTrue(map.size() == 1);
    }

    /**
     * 测试AddressData类型的编码
     * @throws Exception
     */
    @Test
    public void testCodec2() throws Exception {
        AddressData data = new AddressData();
        data.setId(1L);
        data.setName("test");
        Object result = codec.codec(eventName, data);
        Assert.assertEquals(true, result instanceof AddressData);
    }

    /**
     * 测试Map类型的数据转换，其中一个key为字符串类型，另一个Key为UserData类型
     * @throws Exception
     */
    @Test
    public void testCodec3() throws Exception {
        UserData data = new UserData();
        data.setId(1L);
        data.setName("test");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("1", "test");
        map.put("2", data);
        Object result = codec.codec(eventName, map);
        Assert.assertEquals(true, result instanceof Map);
        Map<String, Object> m = (Map<String, Object>)result;
        Assert.assertEquals(2, m.size());
        Assert.assertTrue("test" == m.get("1").toString());
        Map<String, Object> userData = (Map<String, Object>)m.get("2");
        Assert.assertTrue(userData.containsKey("userId"));
        Assert.assertEquals(1, userData.size());
    }

    /**
     * 测试Map类型的数据转换，其中一个key为字符串类型，另一个Key为AddressData类型
     * @throws Exception
     */
    @Test
    public void testCodec4() throws Exception {
        AddressData data = new AddressData();
        data.setId(1L);
        data.setName("test");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("1", "test");
        map.put("2", data);
        Object result = codec.codec(eventName, map);
        Assert.assertEquals(true, result instanceof Map);
        Map<String, Object> m = (Map<String, Object>)result;
        Assert.assertEquals(2, m.size());
        Assert.assertTrue("test" == m.get("1").toString());
        Assert.assertTrue(m.get("2") instanceof AddressData);
    }

    /**
     * 测试Map类型的数据转换，其中一个key为字符串类型，另一个Key为AddressData类型，另一个是UserData类型
     * @throws Exception
     */
    @Test
    public void testCodec5() throws Exception {
        AddressData data = new AddressData();
        data.setId(1L);
        data.setName("test");
        UserData data2 = new UserData();
        data2.setId(2L);
        data2.setName("test");
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("1", "test");
        map.put("2", data);
        map.put("3", data2);
        Object result = codec.codec(eventName, map);
        Assert.assertEquals(true, result instanceof Map);
        Map<String, Object> m = (Map<String, Object>)result;
        Assert.assertEquals(3, m.size());
        Assert.assertTrue("test" == m.get("1").toString());
        Assert.assertTrue(m.get("2") instanceof AddressData);
        Assert.assertTrue(m.get("3") instanceof Map);
    }

    /**
     * 测试List类型数据转换，其中的数组一个为字符串，另一个为UserData类型
     */
    @Test
    public void testCodec6(){
        List<Object> list = new ArrayList<Object>();
        list.add("test");
        UserData data = new UserData();
        data.setId(1L);
        data.setName("test");
        list.add(data);
        Object result = codec.codec(eventName, list);
        Assert.assertTrue(result instanceof List);
        List col = (List)result;
        Assert.assertEquals(2, col.size());
        Assert.assertEquals("test", col.get(0).toString());
        Assert.assertTrue(col.get(1) instanceof Map);
    }

    /**
     * 测试Set类型数据转换，其中的数组一个为字符串，另一个为UserData类型
     */
    @Test
    public void testCodec7(){
        Set<Object> set = new HashSet<Object>();
        set.add("test");
        UserData data = new UserData();
        data.setId(1L);
        data.setName("test");
        set.add(data);
        Object result = codec.codec(eventName, set);
        Assert.assertTrue(result instanceof List);
        List col = (List)result;
        Assert.assertEquals(2, col.size());
        Assert.assertEquals("test", col.get(0).toString());
        Assert.assertTrue(col.get(1) instanceof Map);
    }

    /**
     * 测试List类型数据转换，其中的数组一个为字符串，另一个为AddressData类型
     */
    @Test
    public void testCodec8(){
        List<Object> list = new ArrayList<Object>();
        list.add("test");
        AddressData data = new AddressData();
        data.setId(1L);
        data.setName("test");
        list.add(data);
        Object result = codec.codec(eventName, list);
        Assert.assertTrue(result instanceof List);
        List col = (List)result;
        Assert.assertEquals(2, col.size());
        Assert.assertEquals("test", col.get(0).toString());
        Assert.assertTrue(col.get(1) instanceof AddressData);
    }

    /**
     * 测试UserData[]类型数据转换，
     */
    @Test
    public void testCodec9(){
        UserData[] datas = new UserData[2];
        UserData data = new UserData();
        data.setId(1L);
        data.setName("test");
        datas[0] = data;
        data = new UserData();
        data.setId(2L);
        data.setName("test");
        datas[1] = data;
        Object result = codec.codec(eventName, datas);
        Assert.assertTrue(result instanceof List);
        List col = (List)result;
        Assert.assertEquals(2, col.size());
        Assert.assertTrue(col.get(0) instanceof Map);
        Assert.assertTrue(col.get(1) instanceof Map);
    }

    /**
     * 测试AddressData[]类型数据转换，
     */
    @Test
    public void testCodec10(){
        AddressData[] datas = new AddressData[2];
        AddressData data = new AddressData();
        data.setId(1L);
        data.setName("test");
        datas[0] = data;
        data = new AddressData();
        data.setId(2L);
        data.setName("test");
        datas[1] = data;
        Object result = codec.codec(eventName, datas);
        Assert.assertTrue(result instanceof List);
        List col = (List)result;
        Assert.assertEquals(2, col.size());
        Assert.assertTrue(col.get(0) instanceof AddressData);
        Assert.assertTrue(col.get(1) instanceof AddressData);
    }

    /**
     * 测试Object[]类型数据转换，第一个下标为AddressData，第二个下标为UserData类型
     */
    @Test
    public void testCodec11(){
        Object[] datas = new Object[2];
        AddressData data = new AddressData();
        data.setId(1L);
        data.setName("test");
        datas[0] = data;
        UserData data2 = new UserData();
        data2.setId(2L);
        data2.setName("test");
        datas[1] = data2;
        Object result = codec.codec(eventName, datas);
        Assert.assertTrue(result instanceof List);
        List col = (List)result;
        Assert.assertEquals(2, col.size());
        Assert.assertTrue(col.get(0) instanceof AddressData);
        Assert.assertTrue(col.get(1) instanceof Map);
    }

    /**
     * 测试Object[]类型数据转换，第一个下标为AddressData，第二个下标为Map类型，其中一个key为String类型，另一个为UserData
     */
    @Test
    public void testCodec12(){
        Object[] datas = new Object[2];
        AddressData data = new AddressData();
        data.setId(1L);
        data.setName("test");
        datas[0] = data;
        Map<String, Object> map = new HashMap<String, Object>();
        UserData data2 = new UserData();
        data2.setId(1L);
        data2.setName("test");
        map.put("1", "test");
        map.put("2", data2);
        datas[1] = map;
        Object result = codec.codec(eventName, datas);
        Assert.assertTrue(result instanceof List);
        List col = (List)result;
        Assert.assertEquals(2, col.size());
        Assert.assertTrue(col.get(0) instanceof AddressData);
        Assert.assertTrue(col.get(1) instanceof Map);
        Map<String, Object> r = (Map<String, Object>)col.get(1);
        Assert.assertEquals("test", r.get("1"));
        Assert.assertTrue(r.get("2") instanceof Map);
    }

    /**
     * 测试自引用的类型，测试Map中的一个Key引用自身的Case
     */
    @Test
    public void testCodec13(){
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("1", "test");
        data.put("2", data);
        Object result = codec.codec(eventName, data);
        Assert.assertTrue(result instanceof Map);
        Map r = (Map<String, Object>)result;
        Assert.assertEquals(2, r.size());
        Assert.assertEquals("test", r.get("1").toString());
        Assert.assertTrue(r.get("2") instanceof Map);
    }

    /**
     * 测试自引用的类型，测试Collection中的一个元素引用自身的Case
     */
    @Test
    public void testCodec14(){
        List<Object> data = new ArrayList<Object>();
        data.add("test");
        data.add(data);
        Object result = codec.codec(eventName, data);
        Assert.assertTrue(result instanceof List);
        List<Object> r = (List<Object>)result;
        Assert.assertEquals(2, r.size());
        Assert.assertEquals("test", r.get(0).toString());
        Assert.assertTrue(r.get(1) instanceof List);
    }

    /**
     * 测试自引用的类型，测试Object[]中的一个元素引用自身的Case
     */
    @Test
    public void testCodec15(){
        Object[] data = new Object[2];
        data[0] = "test";
        data[1] = data;
        Object result = codec.codec(eventName, data);
        Assert.assertTrue(result instanceof List);
        List<Object> r = (List<Object>)result;
        Assert.assertEquals(2, r.size());
        Assert.assertEquals("test", r.get(0).toString());
        Assert.assertTrue(r.get(1) instanceof List);
    }

    class SampleMonitorDataCodec extends AbstractMonitorDataCodec {

        @Override
        protected Map<String, Object> codecElement(Object data) {
            if(data instanceof UserData){
                return codecUserData((UserData)data);
            }
            if(data instanceof ShopData){
                return codecShopData((ShopData)data);
            }
            return null;
        }

        protected Map<String, Object> codecUserData(UserData userData){
            return new Builder().append("userId", userData.getId()).build();
        }

        protected Map<String, Object> codecShopData(ShopData shopData){
            return new Builder().append("shopId", shopData.getId()).build();
        }
    }

    class UserData {
        private Long id;

        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    class ShopData {
        private Long id;

        private String name;

        private ShopData self;

        private UserData user;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ShopData getSelf() {
            return self;
        }

        public void setSelf(ShopData self) {
            this.self = self;
        }

        public UserData getUser() {
            return user;
        }

        public void setUser(UserData user) {
            this.user = user;
        }
    }

    class AddressData {
        private Long id;

        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
