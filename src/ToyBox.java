import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

public class ToyBox extends JFrame {
    private Image boxImg;

    // [수정] 가로와 세로 크기를 분리했어요멍!
    private int boxWidth = 130;  // 좌우로 늘리고 싶으면 이 숫자를 키우세요멍!
    private int boxHeight = 90;  // 상하 크기는 여기멍!

    public ToyBox() {
        setType(Window.Type.UTILITY);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);
        try {
            boxImg = ImageIO.read(new File("images/dog_toy_box.png"));
        } catch (Exception e) {}

        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // [수정] 이미지를 그릴 때 가로, 세로 값을 각각 넣어줍니다멍!
                if (boxImg != null) {
                    g.drawImage(boxImg, 0, 0, boxWidth, boxHeight, null);
                }
            }
        };
        p.setOpaque(false);
        add(p);

        // [수정] 실제 창 크기도 가로, 세로에 맞춰서 설정멍!
        setSize(boxWidth, boxHeight);
        setVisible(false);
    }

    public void showAt(int x, int y) {
        setLocation(x, y);
        setVisible(true);
    }
}