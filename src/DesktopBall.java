import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;

public class DesktopBall extends JFrame {
    private Image ballImg;
    private int ballSize = 60;
    private double vX = 0, vY = 0, friction = 0.98;
    private int lastMX, lastMY, offX, offY;
    private DesktopPet pet;
    private boolean isDragging = false;

    public DesktopBall(DesktopPet pet) {
        this.pet = pet;
        setType(Window.Type.UTILITY);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);

        setLocation(-1000, -1000);
        setVisible(false);

        try {
            ballImg = ImageIO.read(new File("images/ball.png"));
        } catch (Exception e) {}

        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (ballImg != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.drawImage(ballImg, 0, 0, ballSize, ballSize, null);
                }
            }
        };
        p.setOpaque(false);
        add(p);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // PetBrain 대신 SoundPlayer를 직접 사용합니다멍!
                try { SoundPlayer.play("dogsound3.wav", -1.0f); } catch(Exception ex) {}
                isDragging = true;
                vX = 0; vY = 0;
                offX = e.getX(); offY = e.getY();
                lastMX = e.getXOnScreen(); lastMY = e.getYOnScreen();

                // [요구사항 1] 공을 클릭하면 주인님의 말풍선만 사라지고, 입력창은 유지!
                if (pet != null) {
                    pet.state.showReply = false; // 내 말풍선만 끄기
                    // pet.hideInputOnly(); 를 호출하지 않음으로써 입력창은 유지됩니다멍!
                    pet.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
                if (Math.abs(vX) > 2 || Math.abs(vY) > 2) {
                    // 랜덤 짖는 소리도 직접 호출로 변경[cite: 14]
                    String randomSound = (Math.random() < 0.5) ? "dogsound1.wav" : "dogsound2.wav";
                    try { SoundPlayer.play(randomSound, -5.0f); } catch(Exception ex) {}
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isVisible()) return;
                setLocation(e.getXOnScreen() - offX, e.getYOnScreen() - offY);
                vX = (e.getXOnScreen() - lastMX) * 0.8;
                vY = (e.getYOnScreen() - lastMY) * 0.8;
                lastMX = e.getXOnScreen();
                lastMY = e.getYOnScreen();

                // 드래그 중에는 강아지에게 위치만 보고 (false)
                if (pet != null) pet.ballUpdate(e.getXOnScreen(), e.getYOnScreen(), false);
            }
        });

        new Timer(30, e -> {
            if (!isVisible()) return;

            if (pet != null && (!pet.state.isBallActive || pet.state.isBringingBall)) {
                setCaptured();
                return;
            }

            if (isDragging) return;

            double s = Math.sqrt(vX * vX + vY * vY);
            if (s > 0.1) {
                GraphicsConfiguration config = getGraphicsConfiguration();
                Rectangle currentScreen = config.getBounds();

                Rectangle virtualBounds = new Rectangle();
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice[] gds = ge.getScreenDevices();
                for (GraphicsDevice gd : gds) {
                    virtualBounds = virtualBounds.union(gd.getDefaultConfiguration().getBounds());
                }

                int nx = (int) (getX() + vX);
                int ny = (int) (getY() + vY);

                int ceilingLimit = currentScreen.y + (currentScreen.x == 0 && currentScreen.y == 0 ? 40 : 0);
                if (ny < ceilingLimit) {
                    ny = ceilingLimit + 2;
                    vY = Math.abs(vY) * 0.8;
                } else if (ny > currentScreen.y + currentScreen.height - ballSize) {
                    ny = currentScreen.y + currentScreen.height - ballSize - 2;
                    vY = -Math.abs(vY) * 0.7;
                }

                boolean canGoRight = false;
                for (GraphicsDevice gd : gds) {
                    if (gd.getDefaultConfiguration().getBounds().contains(nx + ballSize, ny + ballSize / 2)) {
                        canGoRight = true;
                        break;
                    }
                }

                if (!canGoRight || nx > virtualBounds.x + virtualBounds.width - ballSize) {
                    vX = -Math.abs(vX) * 0.7;
                    nx = (int)getX();
                }

                if (nx < virtualBounds.x) {
                    nx = virtualBounds.x + 2;
                    vX = Math.abs(vX) * 0.7;
                }

                if (isVisible()) {
                    setLocation(nx, ny);
                    // [요구사항 2] 공이 던져졌을 때(s > 2.5) true를 보내지만,
                    // Pet 측에서는 이제 이 신호를 받아도 바로 출발하지 않고 대기하게 됩니다멍!
                    if (pet != null) pet.ballUpdate(nx + 30, ny + 30, s > 2.5);
                }

                vX *= friction;
                vY *= friction;
            }
        }).start();

        setSize(ballSize, ballSize);
    }

    public void setCaptured() {
        setVisible(false);
        setLocation(-1000, -1000);
        vX = 0; vY = 0;
        isDragging = false;
    }

    public void releaseBall(int x, int y) {
        setLocation(x, y);
        setVisible(true);
        toFront();
        requestFocus();
        isDragging = false;
        vX = 0; vY = 0;
    }


}