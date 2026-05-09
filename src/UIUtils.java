import java.awt.*;
import java.awt.image.BufferedImage;

public class UIUtils {
    /**
     * 이미지를 9개 구역으로 나누어 크기가 변해도 모서리가 깨지지 않게 그려주는 유틸리티입니다멍!
     */
    public static void draw9Patch(Graphics2D g2, BufferedImage img, int x, int y, int w, int h) {
        if (img == null) return;

        int iw = img.getWidth();
        int ih = img.getHeight();
        int cut = 20; // 모서리 절단 크기

        // 각 모서리와 중앙부 그리기 로직
        g2.drawImage(img, x, y, x + cut, y + cut, 0, 0, cut, cut, null);
        g2.drawImage(img, x + w - cut, y, x + w, y + cut, iw - cut, 0, iw, cut, null);
        g2.drawImage(img, x, y + h - cut, x + cut, y + h, 0, ih - cut, cut, ih, null);
        g2.drawImage(img, x + w - cut, y + h - cut, x + w, y + h, iw - cut, ih - cut, iw, ih, null);
        g2.drawImage(img, x + cut, y, x + w - cut, y + cut, cut, 0, iw - cut, cut, null);
        g2.drawImage(img, x + cut, y + h - cut, x + w - cut, y + h, cut, ih - cut, iw - cut, ih, null);
        g2.drawImage(img, x, y + cut, x + cut, y + h - cut, 0, cut, cut, ih - cut, null);
        g2.drawImage(img, x + w - cut, y + cut, x + w, y + h - cut, iw - cut, cut, iw, ih - cut, null);
        g2.drawImage(img, x + cut, y + cut, x + w - cut, y + h - cut, cut, cut, iw - cut, ih - cut, null);
    }
}