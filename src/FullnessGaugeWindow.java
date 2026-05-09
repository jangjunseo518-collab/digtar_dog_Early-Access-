import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FullnessGaugeWindow extends JWindow {
    private BufferedImage frameImg, gaugeImg;

    // [수정 포인트!] 80이었던 값을 확 줄여서 투명한 '클릭 방해 벽'을 제거합니다멍!
    // 가로 30, 세로 60 정도로 설정하면 막대가 아주 큼직하고 클릭 방해도 없어져요멍.
    private int W = 60, H = 85;
    private PetState state;

    public FullnessGaugeWindow(PetState state) {
        this.state = state;

        try {
            frameImg = ImageIO.read(new File("images/fullness_gauge_frame.png"));
            gaugeImg = ImageIO.read(new File("images/fullness_gauge.png"));

            if (gaugeImg != null) System.out.println("✅ 게이지 이미지 로드 성공!");
            if (frameImg != null) System.out.println("✅ 프레임 이미지 로드 성공!");
        } catch (IOException e) {
            System.out.println("❌ 이미지 로드 실패: " + e.getMessage());
        }

        // 실제 마우스 클릭 영역을 W, H 크기로 제한합니다멍!
        setSize(W, H);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);

        JPanel paintPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (frameImg == null || gaugeImg == null) return;

                // [수정 포인트] 80, 80 대신 현재 창 크기인 W, H에 맞춰 그립니다멍!
                // 이렇게 하면 이미지가 작아지지 않고 창 크기에 꽉 차게 나옵니다멍.
                g2d.drawImage(frameImg, 0, 0, W, H, null);

                double fillRatio = Math.max(0.0, Math.min(1.0, state.fullness / 100.0));
                int gaugeHeight = (int) (H * fillRatio);
                int startY = H - gaugeHeight;

                Shape oldClip = g2d.getClip();

                if (gaugeHeight > 0) {
                    // 클리핑 영역도 줄어든 창 크기(W, H)에 맞춥니다멍!
                    g2d.setClip(0, startY, W, gaugeHeight);
                    g2d.drawImage(gaugeImg, 0, 0, W, H, null);
                }

                g2d.setClip(oldClip);
            }
        };

        paintPanel.setOpaque(false);
        setContentPane(paintPanel);
    }
}