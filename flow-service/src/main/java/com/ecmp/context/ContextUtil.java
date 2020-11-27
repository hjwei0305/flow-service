package com.ecmp.context;

import com.ecmp.config.util.ApiClient;
import com.ecmp.enums.UserAuthorityPolicy;
import com.ecmp.enums.UserType;
import com.ecmp.flow.common.util.Constants;
import com.ecmp.log.util.LogUtil;
import com.ecmp.util.EnumUtils;
import com.ecmp.util.IdGenerator;
import com.ecmp.util.JwtTokenUtil;
import com.ecmp.vo.LoginStatus;
import com.ecmp.vo.ResponseData;
import com.ecmp.vo.SessionUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.core.GenericType;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * <strong>实现功能:</strong>
 * <p>ecmp平台上下文工具类</p>
 *
 * @author <a href="mailto:chao2.ma@changhong.com">马超(Vision.Mac)</a>
 * @version 1.0.1 2017/3/30 17:07
 */
@SuppressWarnings("unchecked")
public class ContextUtil extends BaseContextSupport {

    //InheritableThreadLocal
    private static ThreadLocal<SessionUser> userTokenHold = new InheritableThreadLocal<>();

    public static final String REQUEST_TOKEN_KEY = "_s";
    public static final String AUTHORIZATION_KEY = "Authorization";

    /**
     * @param key  多语言key
     * @param args 填充参数 如：key=参数A{0},参数B{1}  此时的args={"A", "B"}
     * @return 返回语意
     */
    public static String getMessage(String key, Object... args) {
        return getMessage(key, args, getLocale());
    }

    /**
     * @return 返回当前语言环境
     */
    public static String getLocaleLang() {
        SessionUser user = getSessionUser();
        if (!user.isAnonymous()) {
            String language = user.getLocale();
            if (StringUtils.isNotBlank(language)) {
                return language;
            }
        }
        Locale locale = Locale.getDefault();
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    /**
     * @return 返回当前语言环境
     */
    public static Locale getLocale() {
        Locale locale = Locale.getDefault();
        SessionUser user = getSessionUser();
        if (!user.isAnonymous()) {
            String language = user.getLocale();
            // cn_ZH  en_US
            if (StringUtils.isNotBlank(language) && StringUtils.contains(language, "_")) {
                String[] arr = language.split("[_]");
                locale = new Locale(arr[0], arr[1]);
            }
        }
        return locale;
    }

    public static String getDefaultLanguage() {
        Locale locale = Locale.getDefault();
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    /**
     * 设置当前语言环境
     *
     * @param locale 多语言环境
     */
    @Deprecated
    public static void setLocale(Locale locale) {
//        if (locale != null) {
//            SessionUser sessionUser = getSessionUser();
//            if (sessionUser != null && Objects.nonNull(sessionUser.getAccessToken())) {
//                sessionUser.setLocale(locale.getLanguage());
//                setSessionUser(sessionUser);
//            }
//        }
    }

    /**
     * 获取当前服务器IP
     *
     * @return 当前服务器ip
     */
    public static String getHost() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取当前会话用ID
     *
     * @return 返回当前会话用户ID。无会话信息，则返回anonymous
     */
    public static String getUserId() {
        return getSessionUser().getUserId();
    }

    /**
     * 获取当前会话用户账号
     *
     * @return 返回当前会话用户账号。无会话信息，则返回anonymous
     */
    public static String getUserAccount() {
        return getSessionUser().getAccount();
    }

    /**
     * 获取当前会话用户名
     *
     * @return 返回当前会话用户名。无会话信息，则返回anonymous
     */
    public static String getUserName() {
        return getSessionUser().getUserName();
    }

    /**
     * 获取当前会话用户信息
     * 返回格式：
     * 租户,用户名[账号]
     *
     * @return 返回当前会话用户信息。无会话信息，则返回anonymous
     */
    public static String getUserInfo() {
        return getSessionUser().getUserInfo();
    }

    /**
     * 获取当前会话租户代码
     *
     * @return 返回当前租户代码
     */
    public static String getTenantCode() {
        return getSessionUser().getTenantCode();
    }

    /**
     * @return 返回当前会话用户
     */
    public static SessionUser getSessionUser() {
        SessionUser sessionUser = userTokenHold.get();
        if (sessionUser == null) {
            sessionUser = new SessionUser();
        }
        return sessionUser;
    }

    /**
     * @param user 设置用户会话信息
     */
    public static void setSessionUser(SessionUser user) {
        userTokenHold.remove();
        if (user != null) {
            userTokenHold.set(user);
        } else {
            throw new RuntimeException("设置会话用户时，SessionUser不能为空。");
        }
    }

    /**
     * 是否匿名用户
     *
     * @return 返回true，则匿名用户；反之非匿名用户
     */
    public static boolean isAnonymous() {
        boolean isAnonymous = true;
        SessionUser sessionUser = getSessionUser();
        if (sessionUser != null && !sessionUser.isAnonymous()) {
            isAnonymous = false;
        }
        return isAnonymous;
    }

    /**
     * 清楚用户token信息
     */
    public static void cleanUserToken() {
        userTokenHold.remove();
    }

    /**
     * @return 返回AccessToken
     */
    public static String getAccessToken() {
        return getSessionUser().getAccessToken();
    }

    public static String generateToken(SessionUser sessionUser) {
        JwtTokenUtil jwtTokenUtil;
        try {
            jwtTokenUtil = getBean(JwtTokenUtil.class);
        } catch (Exception e) {
            jwtTokenUtil = new JwtTokenUtil();
            jwtTokenUtil.setJwtExpiration(2880000);
        }

        return generateToken(sessionUser, jwtTokenUtil);
    }

    public static String generateToken(SessionUser sessionUser, int expiration) {
        JwtTokenUtil jwtTokenUtil = new JwtTokenUtil();
        jwtTokenUtil.setJwtExpiration(expiration);

        return generateToken(sessionUser, jwtTokenUtil);
    }

    public static String generateToken(SessionUser sessionUser, JwtTokenUtil jwtTokenUtil) {
        String randomKey = sessionUser.getSessionId();
        if (StringUtils.isBlank(randomKey)) {
            randomKey = IdGenerator.uuid();
            sessionUser.setSessionId(randomKey);
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("appId", getAppId());
        claims.put("tenant", sessionUser.getTenantCode());
        claims.put("account", sessionUser.getAccount());
        claims.put("userId", sessionUser.getUserId());
        claims.put("userName", sessionUser.getUserName());
        claims.put("userType", sessionUser.getUserType().name());
        claims.put("email", sessionUser.getEmail());
        claims.put("locale", sessionUser.getLocale());
        claims.put("authorityPolicy", sessionUser.getAuthorityPolicy().name());
        claims.put("ip", sessionUser.getIp());
        claims.put("loginTime", sessionUser.getLoginTime());
        claims.put("logoutUrl", sessionUser.getLogoutUrl());
        String token = jwtTokenUtil.generateToken(sessionUser.getAccount(), randomKey, claims);
        sessionUser.setAccessToken(token);
        return token;
    }

    public static SessionUser getSessionUser(String token) {
        JwtTokenUtil jwtTokenUtil;
        try {
            jwtTokenUtil = getBean(JwtTokenUtil.class);
        } catch (Exception e) {
            jwtTokenUtil = new JwtTokenUtil();
            jwtTokenUtil.setJwtExpiration(2880000);
        }

        SessionUser sessionUser = new SessionUser();
        try {
            Claims claims = jwtTokenUtil.getClaimFromToken(token);
            //sessionUser.setSessionId(jwtTokenUtil.getRandomKeyFromToken(token));
            sessionUser.setSessionId(claims.get(JwtTokenUtil.RANDOM_KEY, String.class));
            sessionUser.setAppId(claims.get("appId", String.class));
            sessionUser.setAccessToken(token);
            sessionUser.setTenantCode(claims.get("tenant", String.class));
            sessionUser.setAccount(claims.get("account", String.class));
            sessionUser.setUserId(claims.get("userId", String.class));
            sessionUser.setUserName(claims.get("userName", String.class));
            sessionUser.setUserType(EnumUtils.getEnum(UserType.class, (String) claims.get("userType")));
            sessionUser.setEmail(claims.get("email", String.class));
            sessionUser.setLocale(claims.get("locale", String.class));
            sessionUser.setAuthorityPolicy(EnumUtils.getEnum(UserAuthorityPolicy.class, (String) claims.get("authorityPolicy")));
            sessionUser.setIp(claims.get("ip", String.class));
            try {
                sessionUser.setLoginTime(claims.get("loginTime", Date.class));
            } catch (Exception ignored) {
                sessionUser.setLoginTime(new Date());
            }
            sessionUser.setLogoutUrl(claims.get("logoutUrl", String.class));

            ContextUtil.setSessionUser(sessionUser);
        } catch (ExpiredJwtException e) {
            LogUtil.error("token已过期", e);
        } catch (Exception e) {
            LogUtil.error("错误的token", e);
        }
        return sessionUser;
    }

    ///////////////////////////////Mock User////////////////////////////////

    /**
     * 模拟用户
     *
     * @return 返回会话id
     */
    public static SessionUser mockUser() {
        return mockUser(IdGenerator.uuid2());
    }

    public static SessionUser mockUser(String sessionId) {
        String tenant = ContextUtil.getGlobalProperty("mock.user.tenant");
        String account = ContextUtil.getGlobalProperty("mock.user.account");
        return getSessionUser(sessionId, tenant, account);
    }

    /**
     * 供类似定时任务这样的接口调用指定租户、账号，以作接口会话检查
     *
     * @param tenant  租户代码
     * @param account 账号
     * @return 返回用户会话信息喊accessToken
     */
    public static SessionUser setSessionUser(String tenant, String account) {
        return getSessionUser(IdGenerator.uuid2(), tenant, account);
    }

    private static SessionUser getSessionUser(String sessionId, String tenant, String account) {
        if (StringUtils.isBlank(sessionId)) {
            throw new IllegalArgumentException("模拟登录用户时，会话ID不能为空！");
        }

        SessionUser sessionUser = new SessionUser();
        //模拟本地用户登录，采用正常的模式，不根据isConfigFile判断
        String url = Constants.getAuthBaseApi()+"/sei-auth/auth/getAnonymousToken";
        ResponseData res = ApiClient.getEntityViaProxy(url, new GenericType<ResponseData>() {
        }, null);
        String token = "";
        if(res.successful()){
            token = (String)res.getData();
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("tenant", StringUtils.isNotBlank(tenant) ? tenant : "");
        params.put("account", account);
        String path = Constants.getAuthBaseApi()+"/sei-auth/account/getByTenantAccount";

        sessionUser.setAccessToken(token);
        ContextUtil.setSessionUser(sessionUser);
        ResponseData<Map<String, Object>> responseData = ApiClient.getEntityViaProxy(path, new GenericType<ResponseData<Map<String, Object>>>() {
        }, params);
        if (responseData.successful()) {
            Map<String, Object> map = responseData.getData();
            if (map != null) {
                sessionUser.setUserId((String) map.get("userId"));
                sessionUser.setAccount(account);
                sessionUser.setUserName((String) map.get("userName"));
                sessionUser.setTenantCode((String) map.get("tenantCode"));
                sessionUser.setUserType(Enum.valueOf(UserType.class, (String) map.get("userType")));
                sessionUser.setAuthorityPolicy(Enum.valueOf(UserAuthorityPolicy.class, (String) map.get("authorityPolicy")));
                sessionUser.setLocale(getDefaultLanguage());
                sessionUser.setLoginTime(new Date());
                sessionUser.setSessionId(sessionId);
                sessionUser.setLoginStatus(LoginStatus.success);
            } else {
                throw new RuntimeException("account:[" + account + "], tenant:[" + tenant + "] 用户数据错误！");
            }
        }
        String accessToken = generateToken(sessionUser);
        sessionUser.setAccessToken(accessToken);

        ContextUtil.setSessionUser(sessionUser);
        return sessionUser;
    }
}
