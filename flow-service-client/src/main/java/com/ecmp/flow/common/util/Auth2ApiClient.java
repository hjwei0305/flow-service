package com.ecmp.flow.common.util;


import com.ecmp.flow.common.util.key.RSAUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.GenericType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.*;

/**
 * *************************************************************************************************
 * <p>
 * 实现功能： 按照auth2标准进行接口调用
 * 获取token
 * 发起真实的请求
 * </p><p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/9/22 15:52      谭军(tanjun)                    新建
 * </p>
 * *************************************************************************************************
 */
public class Auth2ApiClient {
    //用户名,密码
    static String USERNAME = Constants.AUTHDEFAULTUSERID;
    static String USERPASSWORD = Constants.AUTHDEFAULTUSERPWD;
    static String LOGINURL = Constants.AUTHLOGINURL;
    static String AUTHCODEURL = Constants.AUTHCODEURL;
    static String ACCESSTOKENURL = Constants.AUTHTOKENURL;
    static String CLIENTID = Constants.AUTHCLIENTID;
    static String CLIENTPASSWORD = Constants.AUTHCLIENTPASSWORD;


    public Auth2ApiClient(){

    }
    public Auth2ApiClient(String baseAddress, String path)throws Exception{
        addTokenParamsMap.put("userAccount","testUserAccount");
        this.setBaseAddress(baseAddress);
        this.setPath(path);
        this.setAccessToken(generateToken());
    }

    public Auth2ApiClient(String baseAddress, String path, Map<String,String> addTokenParamsMap)throws Exception{
        this.setBaseAddress(baseAddress);
        this.setPath(path);
        this.setAddTokenParamsMap(addTokenParamsMap);
        this.setAccessToken(generateToken());
    }
    /**
     * 需要放入token的信息键值对
     */
    private Map<String,String> addTokenParamsMap = new HashMap<String,String>();

    private String accessToken = null;

    /**
     * 基地址
     */
    String baseAddress;

    /**
     * 请求路径
     */
    String path;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Map<String, String> getAddTokenParamsMap() {
        return addTokenParamsMap;
    }

    public void setAddTokenParamsMap(Map<String, String> addTokenParamsMap) {
        this.addTokenParamsMap = addTokenParamsMap;
    }

    public String getBaseAddress() {
        return baseAddress;
    }

    public void setBaseAddress(String baseAddress) {
        this.baseAddress = baseAddress;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    public  String generateToken() throws Exception{
        return generateToken(this.getBaseAddress()+this.getPath());
    }
    public  String generateToken(String redirectUrl) throws Exception{
        final ClientHttpRequestFactory clientHttpRequestFactory =
                new MyCustomClientHttpRequestFactory(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        String accessToken = null;
        RestTemplate restTemplate = new RestTemplate();
        addAuthentication(restTemplate, USERNAME, USERPASSWORD);
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        restTemplate.setRequestFactory(clientHttpRequestFactory);

        ResponseEntity<String> response = restTemplate.postForEntity(LOGINURL, new HttpEntity(headers), String.class,CLIENTID,redirectUrl);

        //判断返回状态是否是200,如果不是跑出异常
        if (HttpStatus.OK.equals(response.getStatusCode())) {
            //获取cookie里面的JSSIONID cookie
            List<String> setCookie = response.getHeaders().get("Set-Cookie");
            String jSessionIdCookie = setCookie.get(0);
            String cookieValue = jSessionIdCookie.split(";")[0];

            //组织一个http请求头部,放入cookie
            headers = new HttpHeaders();
            headers.add("Cookie", cookieValue);
            headers.setContentType(type);
            headers.add("Accept", MediaType.APPLICATION_JSON.toString());
            RestTemplate restCodeTemplate = new RestTemplate();
            restCodeTemplate.setRequestFactory(clientHttpRequestFactory);
            addAuthentication(restCodeTemplate, USERNAME, USERPASSWORD);
            //通过用户名密码再次发起请求获取授权码
            ResponseEntity<String> responseCode = restCodeTemplate.postForEntity(AUTHCODEURL, new HttpEntity(headers), String.class,CLIENTID, redirectUrl);

            //判断是否是302跳转状态
            if (HttpStatus.FOUND.equals(responseCode.getStatusCode())) {
                String location = responseCode.getHeaders().get("Location").get(0);
                //获取url
                URI locationURI = new URI(location);
                //获取url后面的请求参数，即获取授权码code=xxx
                String query = locationURI.getQuery();
                StringBuffer addTokenParamsSecretBuf = new StringBuffer();
                if(!addTokenParamsMap.isEmpty()){
                    for(Map.Entry<String,String> entry :addTokenParamsMap.entrySet()){
                        addTokenParamsSecretBuf.append("&").append(entry.getKey()).append("=")
                                .append(entry.getValue());
                    }
                    //                String addTokenStr = "&userAccount=admin";
                    String addTokenStr = addTokenParamsSecretBuf.toString();
                    String selfPrivateKeyString = Constants.SELFPRIVATEKEY;
                    PrivateKey privateKey = RSAUtils.getPrivateKey(selfPrivateKeyString);
                    String addTokenParamsSecret = new String(RSAUtils.encryptByPrivateKey(addTokenStr,(RSAPrivateKey)privateKey));
                    //addTokenParamsSecret = URLEncoder.encode(addTokenParamsSecret, "utf-8");
                    query+="&addTokenParams="+addTokenParamsSecret;//针对需要注入到token中的变量进行加密
                }

                //组织授权码获取access token
                RestTemplate restTokenTemplate = new RestTemplate();
                restTokenTemplate.setRequestFactory(clientHttpRequestFactory);
                addAuthentication(restTokenTemplate, CLIENTID, CLIENTPASSWORD);
                //通过用户名密码再次发起请求获取授权码
                ResponseEntity<String> responseToken = restTokenTemplate.postForEntity(ACCESSTOKENURL+"&"+query, new HttpEntity(headers), String.class ,CLIENTID,redirectUrl);

                //判断获取access token 是否成功
                if (HttpStatus.OK.equals(responseToken.getStatusCode())) {
                    //获取access_token信息
                    HashMap<?, ?> jwtMap = new ObjectMapper().readValue(responseToken.getBody(), HashMap.class);
                    accessToken = (String) jwtMap.get("access_token");
                }
            }
        }
        return accessToken;
    }

    private static void addAuthentication(RestTemplate restTemplate, String userName, String password) {
        if (userName == null) {
            return;
        }
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        if (interceptors == null) {
            interceptors = Collections.emptyList();
        }
        interceptors = new ArrayList<ClientHttpRequestInterceptor>(interceptors);
        Iterator<ClientHttpRequestInterceptor> iterator = interceptors.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof BasicAuthorizationInterceptor) {
                iterator.remove();
            }
        }
        interceptors.add(new BasicAuthorizationInterceptor(userName, password));
        restTemplate.setInterceptors(interceptors);
    }
//
//    public Object call(String baseAddress,String path,GenericType entityClass, Map<String, Object> params,String accessToken)throws Exception{
//        if(StringUtils.isEmpty(accessToken)){
//            accessToken = this.getToken(baseAddress+path);
//        }
//        if(StringUtils.isEmpty(accessToken)){
//            throw new RuntimeException("获取token失败");
//        }
//        List<Object> providerList = new ArrayList<Object>();
//        providerList.add(new JacksonJsonProvider());
//
//       WebClient client= WebClient.create(baseAddress, providerList)
//                .path(path)
//                .accept(javax.ws.rs.core.MediaType.APPLICATION_JSON).header("Authorization",accessToken);
//        //拼装请求路径
//        if (params != null && !params.isEmpty()) {
//            for (Map.Entry<String, Object> p : params.entrySet()) {
//                client.query(p.getKey(), p.getValue());
//            }
//        }
//        Object productList = client.get(entityClass);
////        for (Product product : productList) {
////            System.out.println(product);
////        }
//        return productList;
//    }
//    public static void main(String[] args) throws URISyntaxException, IOException,Exception{
//        String baseAddress="http://localhost:8880/ws/rest";
//        String path="/products2";
//        String accessToken=null;
//        Long startTime = System.currentTimeMillis();
//        Map<String,Object> params = new HashMap<>();
//        params.put("test","test");
//        Map<String,String> addTokenParamsMap = new HashMap<>();
//        addTokenParamsMap.put("userAccount","testUserAccount");
//        Auth2ApiClient auth2ApiClient= new Auth2ApiClient(baseAddress,path,addTokenParamsMap);
//        List<Product> result = auth2ApiClient.getEntityViaProxy(new GenericType<List<Product>>() {},params);
//        Long endTime = System.currentTimeMillis();
//        System.out.println(endTime-startTime);
//        for (Product product : result) {
//            System.out.println(product);
//        }
//    }

    private static final Logger loger = LoggerFactory.getLogger(Auth2ApiClient.class);

    static {
    }

    /**
     * 创建应用模块的API服务代理
     * @param path API路径（含方法路径）
     * @param baseAddress 基地址
     * @return 服务代理
     */
    public WebClient createProxy(String baseAddress, String path) throws Exception{
        //记录API调用日志
        if (loger.isDebugEnabled()) {
            loger.debug("调用ApiClient 基地址:[{}],ApiPath:[{}]。", baseAddress, path);
        }
        if(StringUtils.isEmpty(accessToken)){
            throw new RuntimeException("accessToken 为空！");
        }
        //平台API服务使用的JSON序列化提供类
        List<Object> providerList = new ArrayList<Object>();
        providerList.add(new JacksonJsonProvider());
        WebClient client = WebClient.create(baseAddress, providerList);
        client.type(javax.ws.rs.core.MediaType.APPLICATION_JSON).accept(javax.ws.rs.core.MediaType.APPLICATION_JSON)
                .header("Authorization",accessToken);
        client.path(path);
        return client;
    }

    /**
     * 创建应用模块的API服务客户端代理
     * @param baseAddress 基地址
     * @param path API路径（含方法路径）
     * @param params 输入参数(K-参数名，V-参数值)
     * @return 返回结果
     */
    private WebClient createClient(String baseAddress, String path, Map<String, Object> params)throws Exception{
        WebClient client = createProxy(baseAddress,path);
        //拼装请求路径
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> p : params.entrySet()) {
                client.query(p.getKey(), p.getValue());
            }
        }
        return client;
    }

    /**
     * 创建应用模块的API服务客户端代理
     * @param params 输入参数(K-参数名，V-参数值)
     * @return 返回结果
     */
    private WebClient createClient(Map<String, Object> params)throws Exception{
        WebClient client = createProxy(baseAddress,path);
        //拼装请求路径
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> p : params.entrySet()) {
                client.query(p.getKey(), p.getValue());
            }
        }
        return client;
    }

    /**
     * 创建API服务代理并调用GET方法获取实体
     *
     * @param baseAddress 基地址
     * @param path          API路径（含方法路径）
     * @param entityClass 获取实体的类型
     * @param params        输入参数(K-参数名，V-参数值)
     * @param <T>           获取实体的泛型
     * @return 获取的实体
     */
    public  <T> T getEntityViaProxy(String baseAddress, String path, Class<T> entityClass, Map<String, Object> params)throws Exception {
        WebClient client = createClient(baseAddress,path,params);
        return client.get(entityClass);
    }

    /**
     * 创建API服务代理并调用GET方法获取实体
     *
     * @param entityClass 获取实体的类型
     * @param params        输入参数(K-参数名，V-参数值)
     * @param <T>           获取实体的泛型
     * @return 获取的实体
     */
    public  <T> T getEntityViaProxy(Class<T> entityClass, Map<String, Object> params)throws Exception {
        WebClient client = createClient(baseAddress,path,params);
        return client.get(entityClass);
    }

    /**
     * 创建API服务代理并调用GET方法获取实体
     *
     * @param  baseAddress 基地址
     * @param path          API路径（含方法路径）
     * @param entityClass 获取实体的类型(泛型)
     * @param params        输入参数(K-参数名，V-参数值)
     * @param <T>           获取实体的泛型
     * @return 获取的实体
     */
    public  <T> T getEntityViaProxy(String baseAddress, String path, GenericType<T> entityClass, Map<String, Object> params) throws Exception{
        WebClient client = createClient(baseAddress,path,params);
        return client.get(entityClass);
    }

    /**
     * 创建API服务代理并调用GET方法获取实体
     * @param entityClass 获取实体的类型(泛型)
     * @param params        输入参数(K-参数名，V-参数值)
     * @param <T>           获取实体的泛型
     * @return 获取的实体
     */
    public  <T> T getEntityViaProxy(GenericType<T> entityClass, Map<String, Object> params) throws Exception{
        WebClient client = createClient(baseAddress,path,params);
        return client.get(entityClass);
    }

    /**
     * 创建API服务代理调用POST方法并返回执行结果
     * @param baseAddress 基地址
     * @param path API路径（含方法路径）
     * @param resultClass 返回结果的类型
     * @param input 输入参数
     * @param <T> 返回结果的泛型
     * @return 返回结果
     */
    public  <T> T postViaProxyReturnResult(String baseAddress, String path, Class<T> resultClass,Object input) throws Exception{
        WebClient client = createProxy(baseAddress,path);
        return client.post(input,resultClass);
    }

    /**
     * 创建API服务代理调用POST方法并返回执行结果
     * @param resultClass 返回结果的类型
     * @param input 输入参数
     * @param <T> 返回结果的泛型
     * @return 返回结果
     */
    public  <T> T postViaProxyReturnResult(Class<T> resultClass,Object input) throws Exception{
        WebClient client = createProxy(baseAddress,path);
        return client.post(input,resultClass);
    }

    /**
     * 创建API服务代理调用POST方法并返回执行结果
     * @param baseAddress 基地址
     * @param path API路径（含方法路径）
     * @param resultClass 返回结果的类型(泛型)
     * @param input 输入参数
     * @param <T> 返回结果的泛型
     * @return 返回结果
     */
    public  <T> T postViaProxyReturnResult(String baseAddress, String path, GenericType<T> resultClass, Object input) throws Exception{
        WebClient client = createProxy(baseAddress,path);
        return client.post(input,resultClass);
    }

    /**
     * 创建API服务代理调用POST方法并返回执行结果
     * @param resultClass 返回结果的类型(泛型)
     * @param input 输入参数
     * @param <T> 返回结果的泛型
     * @return 返回结果
     */
    public  <T> T postViaProxyReturnResult(GenericType<T> resultClass, Object input) throws Exception{
        WebClient client = createProxy(baseAddress,path);
        return client.post(input,resultClass);
    }

    /**
     * 创建API服务代理调用POST方法并返回执行结果
     * @param baseAddress 基地址
     * @param path API路径（含方法路径）
     * @param resultClass 返回结果的类型
     * @param params        输入参数(K-参数名，V-参数值)
     * @param <T> 返回结果的泛型
     * @return 返回结果
     */
    public  <T> T postViaProxyReturnResult(String baseAddress, String path, Class<T> resultClass, Map<String, Object> params)throws Exception{
        WebClient client = createClient(baseAddress,path,params);
        return client.post(null,resultClass);
    }

    /**
     * 创建API服务代理调用POST方法并返回执行结果
     * @param resultClass 返回结果的类型
     * @param params        输入参数(K-参数名，V-参数值)
     * @param <T> 返回结果的泛型
     * @return 返回结果
     */
    public  <T> T postViaProxyReturnResult(Class<T> resultClass, Map<String, Object> params)throws Exception{
        WebClient client = createClient(baseAddress,path,params);
        return client.post(null,resultClass);
    }

    /**
     * 创建API服务代理调用POST方法并返回执行结果
     * @param baseAddress 基地址
     * @param path API路径（含方法路径）
     * @param resultClass 返回结果的类型(泛型)
     * @param params        输入参数(K-参数名，V-参数值)
     * @param <T> 返回结果的泛型
     * @return 返回结果
     */
    public  <T> T postViaProxyReturnResult(String baseAddress, String path,GenericType<T> resultClass, Map<String, Object> params) throws Exception{
        WebClient client = createClient(baseAddress,path,params);
        return client.post(null,resultClass);
    }

    /**
     * 创建API服务代理调用POST方法并返回执行结果
     * @param resultClass 返回结果的类型(泛型)
     * @param params        输入参数(K-参数名，V-参数值)
     * @param <T> 返回结果的泛型
     * @return 返回结果
     */
    public  <T> T postViaProxyReturnResult(GenericType<T> resultClass, Map<String, Object> params) throws Exception{
        WebClient client = createClient(baseAddress,path,params);
        return client.post(null,resultClass);
    }
}
