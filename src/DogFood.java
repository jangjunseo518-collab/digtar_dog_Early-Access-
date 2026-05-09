import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DogFood extends JWindow {
    private int mouseX, mouseY;
    private final DesktopPet pet;
    private final Image foodImg;
    private final int SIZE = 60;

    // [수정] CommandHandler에서 보내는 4개의 인자를 받도록 매개변수 구성 변경멍! 🐾
    public DogFood(PetState state, DesktopPet pet, int startX, int startY) {
        this.pet = pet;
        this.foodImg = new ImageIcon("images/dogfood.png").getImage();

        setSize(SIZE, SIZE);
        setAlwaysOnTop(true);
        setBackground(new Color(0, 0, 0, 0));

        // [수정] 전달받은 startX, startY를 사용하여 강아지 머리 위에 소환멍!
        setLocation(startX + 10, startY + 70);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 1. 밥 위치 정보 업데이트 (강아지가 달려갈 목표점)
                pet.state.isFoodActive = true;
                pet.state.foodX = getX() + SIZE / 2;
                pet.state.foodY = getY() + SIZE / 2;

                // 2. 드롭 시 유저의 답변 말풍선("밥 먹자" 등) 지우기
                pet.state.showReply = false;
                pet.updateSpeechBubbleUI();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        // 배경 투명 처리를 유지하면서 이미지를 그립니다멍!
        super.paint(g);
        g.drawImage(foodImg, 0, 0, SIZE, SIZE, this);
    }
}