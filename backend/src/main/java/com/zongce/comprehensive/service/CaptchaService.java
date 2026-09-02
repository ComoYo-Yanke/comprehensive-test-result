package com.zongce.comprehensive.service;

import com.zongce.comprehensive.common.BusinessException;
import com.zongce.comprehensive.vo.CaptchaVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 图形验证码服务
 * <p>纯 Java 生成验证码（无第三方依赖），答案仅存 Redis（键 captcha:code:{id}，5 分钟有效），
 * 校验时取走即删，保证一次性使用。</p>
 */
@Service
@RequiredArgsConstructor
public class CaptchaService {

    /** 验证码有效时长 */
    private static final Duration EXPIRE = Duration.ofMinutes(5);
    /**
     * 原子"取出即删"脚本：兼容低版本 Redis（GETDEL 需 Redis 6.2+）。
     * 键存在则返回其值并删除，保证验证码一次性使用；不存在返回 null。
     */
    private static final DefaultRedisScript<String> GET_AND_DELETE = new DefaultRedisScript<>(
            "local v = redis.call('GET', KEYS[1]) "
                    + "if v then redis.call('DEL', KEYS[1]) end "
                    + "return v",
            String.class);
    /** 参与校验的字符集（剔除易混淆的 0/O/1/I） */
    private static final String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";

    private final StringRedisTemplate stringRedisTemplate;

    /** 生成验证码：随机 4 位 + 渲染图片，答案写入 Redis */
    public CaptchaVO generate() {
        String code = randomCode(4);
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(key(captchaId), code, EXPIRE);
        return new CaptchaVO(captchaId, renderBase64(code));
    }

    /**
     * 校验验证码（一次性）：取出即删除，校验失败或过期均返回 false。
     */
    public boolean verify(String captchaId, String code) {
        if (captchaId == null || captchaId.isBlank() || code == null || code.isBlank()) {
            return false;
        }
        String cached = stringRedisTemplate.execute(GET_AND_DELETE, Collections.singletonList(key(captchaId.trim())));
        return cached != null && cached.equalsIgnoreCase(code.trim());
    }

    private String key(String captchaId) {
        return "captcha:code:" + captchaId;
    }

    private String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /** 将验证码绘制为 PNG 并转为 Base64 字符串 */
    private String renderBase64(String code) {
        int width = 130;
        int height = 42;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        // 干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(randomColor(random));
            g.drawLine(random.nextInt(width), random.nextInt(height),
                    random.nextInt(width), random.nextInt(height));
        }
        // 干扰点
        for (int i = 0; i < 60; i++) {
            g.setColor(randomColor(random));
            g.fillRect(random.nextInt(width), random.nextInt(height), 1, 1);
        }
        // 字符（轻微旋转防 OCR）
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        char[] chars = code.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            g.setColor(randomColor(random));
            double angle = (random.nextDouble() - 0.5) * 0.5;
            int cx = 18 + i * 28;
            g.rotate(angle, cx, 20);
            g.drawString(String.valueOf(chars[i]), cx - 8, 30);
            g.rotate(-angle, cx, 20);
        }
        g.dispose();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            throw new BusinessException("验证码图片生成失败，请刷新重试");
        }
    }

    private Color randomColor(ThreadLocalRandom random) {
        return new Color(20 + random.nextInt(180), 20 + random.nextInt(180), 20 + random.nextInt(180));
    }
}
