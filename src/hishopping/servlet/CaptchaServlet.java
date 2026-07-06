package hishopping.servlet;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.SecureRandom;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.util.CaptchaUtil;

public class CaptchaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int WIDTH = 130;
    private static final int HEIGHT = 44;
    private static final SecureRandom RANDOM = new SecureRandom();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String text = CaptchaUtil.create(request.getSession());
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            paintBackground(g);
            paintNoise(g);
            paintText(g, text);
            paintLines(g);
        } finally {
            g.dispose();
        }
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        ImageIO.write(image, "png", response.getOutputStream());
    }

    private void paintBackground(Graphics2D g) {
        for (int y = 0; y < HEIGHT; y++) {
            float ratio = (float) y / (float) Math.max(1, HEIGHT - 1);
            int r = blend(250, 238, ratio);
            int gr = blend(245, 242, ratio);
            int b = blend(255, 255, ratio);
            g.setColor(new Color(r, gr, b));
            g.drawLine(0, y, WIDTH, y);
        }
        g.setColor(new Color(255, 255, 255, 160));
        g.fillRoundRect(4, 4, WIDTH - 8, HEIGHT - 8, 14, 14);
    }

    private void paintText(Graphics2D g, String text) {
        Font font = new Font("Arial", Font.BOLD, 31);
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        int charWidth = WIDTH / 5;
        for (int i = 0; i < text.length(); i++) {
            String ch = String.valueOf(text.charAt(i));
            int x = 13 + i * charWidth + RANDOM.nextInt(4);
            int y = 30 + RANDOM.nextInt(5) - 2;
            double angle = Math.toRadians(RANDOM.nextInt(13) - 6);
            AffineTransform old = g.getTransform();
            g.rotate(angle, x + charWidth / 2.0, y - metrics.getAscent() / 2.0);
            Color color = randomTextColor();
            g.setColor(new Color(255, 255, 255, 130));
            g.drawString(ch, x + 1, y + 1);
            g.setColor(color);
            g.drawString(ch, x, y);
            g.setTransform(old);
        }
    }

    private void paintLines(Graphics2D g) {
        g.setStroke(new BasicStroke(1.2f));
        for (int i = 0; i < 3; i++) {
            g.setColor(new Color(124, 58, 237, 50 + RANDOM.nextInt(35)));
            int y1 = 8 + RANDOM.nextInt(HEIGHT - 16);
            int y2 = 8 + RANDOM.nextInt(HEIGHT - 16);
            g.drawLine(4, y1, WIDTH - 5, y2);
        }
    }

    private void paintNoise(Graphics2D g) {
        for (int i = 0; i < 36; i++) {
            int alpha = 32 + RANDOM.nextInt(40);
            g.setColor(new Color(109, 40, 217, alpha));
            int x = RANDOM.nextInt(WIDTH);
            int y = RANDOM.nextInt(HEIGHT);
            g.fillOval(x, y, 1 + RANDOM.nextInt(2), 1 + RANDOM.nextInt(2));
        }
    }

    private Color randomTextColor() {
        Color[] colors = new Color[] {
            new Color(91, 33, 182),
            new Color(79, 70, 229),
            new Color(109, 40, 217),
            new Color(55, 48, 163)
        };
        return colors[RANDOM.nextInt(colors.length)];
    }

    private int blend(int start, int end, float ratio) {
        return Math.round(start + (end - start) * ratio);
    }
}
