package hishopping.util;

import java.security.SecureRandom;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class CaptchaUtil {
    private static final String CAPTCHA_TEXT = "captchaText";
    private static final String CAPTCHA_EXPIRES_AT = "captchaExpiresAt";
    private static final String CAPTCHA_FAIL_COUNT = "captchaFailCount";
    private static final String CAPTCHA_LOCK_UNTIL = "captchaLockUntil";
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int LENGTH = 4;
    private static final long EXPIRE_MILLIS = 5L * 60L * 1000L;
    private static final int MAX_FAIL_COUNT = 5;
    private static final long LOCK_MILLIS = 60L * 1000L;
    private static final SecureRandom RANDOM = new SecureRandom();

    private CaptchaUtil() {
    }

    public static String create(HttpSession session) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            text.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        session.setAttribute(CAPTCHA_TEXT, text.toString());
        session.setAttribute(CAPTCHA_EXPIRES_AT, Long.valueOf(System.currentTimeMillis() + EXPIRE_MILLIS));
        return text.toString();
    }

    public static CaptchaResult verify(HttpServletRequest request, String input) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return CaptchaResult.fail("验证码错误或已过期，请重新获取");
        }
        long now = System.currentTimeMillis();
        Long lockUntil = longAttr(session, CAPTCHA_LOCK_UNTIL);
        if (lockUntil != null && lockUntil.longValue() > now) {
            clear(session);
            return CaptchaResult.fail("验证码错误次数过多，请稍后再试");
        }
        String expected = (String) session.getAttribute(CAPTCHA_TEXT);
        Long expiresAt = longAttr(session, CAPTCHA_EXPIRES_AT);
        boolean valid = input != null && input.trim().length() > 0
                && expected != null
                && expiresAt != null
                && expiresAt.longValue() >= now
                && expected.equalsIgnoreCase(input.trim());
        clear(session);
        if (valid) {
            session.removeAttribute(CAPTCHA_FAIL_COUNT);
            session.removeAttribute(CAPTCHA_LOCK_UNTIL);
            return CaptchaResult.ok();
        }
        int failCount = intAttr(session, CAPTCHA_FAIL_COUNT) + 1;
        session.setAttribute(CAPTCHA_FAIL_COUNT, Integer.valueOf(failCount));
        if (failCount >= MAX_FAIL_COUNT) {
            session.setAttribute(CAPTCHA_LOCK_UNTIL, Long.valueOf(now + LOCK_MILLIS));
            session.setAttribute(CAPTCHA_FAIL_COUNT, Integer.valueOf(0));
            return CaptchaResult.fail("验证码错误次数过多，请稍后再试");
        }
        return CaptchaResult.fail("验证码错误或已过期，请重新获取");
    }

    public static void clear(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(CAPTCHA_TEXT);
        session.removeAttribute(CAPTCHA_EXPIRES_AT);
    }

    private static Long longAttr(HttpSession session, String name) {
        Object value = session.getAttribute(name);
        return value instanceof Long ? (Long) value : null;
    }

    private static int intAttr(HttpSession session, String name) {
        Object value = session.getAttribute(name);
        return value instanceof Integer ? ((Integer) value).intValue() : 0;
    }

    public static class CaptchaResult {
        private final boolean valid;
        private final String message;

        private CaptchaResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static CaptchaResult ok() {
            return new CaptchaResult(true, "");
        }

        public static CaptchaResult fail(String message) {
            return new CaptchaResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
