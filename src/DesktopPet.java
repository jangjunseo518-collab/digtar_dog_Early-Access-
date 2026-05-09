import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DesktopPet extends JFrame {

    // --- 상수 ---
    private final int PET_SIZE      = 80;
    private final int CHAT_W        = 280;
    private final int CHAT_H        = 290;
    private final int BOTTOM_MARGIN = 150;

    // --- 상태 & 이미지 ---
    public PetState state = new PetState();
    private BufferedImage dogImg, dogWithBallImg, dogEatImg, balloonImg, balloonRImg, inputImg;

    // --- 서브 시스템 ---
    private FullnessGaugeWindow gaugeWindow;
    private HungerManager hungerManager;
    private PetMovementController movementController;
    private ChatUI chatUI;
    private CommandHandler commandHandler;

    public DesktopPet() {
        gaugeWindow = new FullnessGaugeWindow(state);
        gaugeWindow.setVisible(false);

        loadImages();

        // 서브 시스템 초기화
        hungerManager      = new HungerManager(state, this, gaugeWindow);
        movementController = new PetMovementController(state, this);
        chatUI             = new ChatUI(state, this, CHAT_H, PET_SIZE, BOTTOM_MARGIN);
        commandHandler     = new CommandHandler(this);

        hungerManager.startTimer();

        // 창 설정
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);

        JPanel mainPanel = buildMainPanel();
        mainPanel.setOpaque(false);
        mainPanel.setLayout(null);
        setContentPane(mainPanel);

        chatUI.setupUI(mainPanel);

        setSize(PET_SIZE, PET_SIZE);
        setLocation((int) state.x, (int) state.y);

        addMouseListener(new PetClickHandler(state, this, hungerManager));

        // 메인 루프 타이머 (60FPS 근사)
        new Timer(16, e -> {
            if (!state.isChatting || state.isCleaningUp || state.isChasingBall ||
                    state.isBringingBall || state.isFoodActive || state.isReturning) {
                movementController.handlePetMovement();
            }
            updateWindowBounds();
            repaint();
        }).start();
    }

    public CommandHandler getCommandHandler() {
        return this.commandHandler;
    }

    private void loadImages() {
        try {
            dogImg         = ImageIO.read(new File("images/dog_front.png"));
            dogWithBallImg = ImageIO.read(new File("images/dog_with_ball.png"));
            dogEatImg      = ImageIO.read(new File("images/dog_eat_food.png"));
            balloonImg     = ImageIO.read(new File("images/balloon.png"));
            balloonRImg    = ImageIO.read(new File("images/balloonR.png"));
            inputImg       = ImageIO.read(new File("images/inputwindow.png"));
        } catch (IOException e) {
            System.out.println("이미지 로드 실패: " + e.getMessage());
        }
    }

    private JPanel buildMainPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // [1] 말풍선 및 입력창 그리기
                if (state.isChatting) {
                    if (!state.dogSpeech.isEmpty() || state.isWaiting) {
                        UIUtils.draw9Patch(g2d, balloonImg,
                                chatUI.getLastBX(), chatUI.getLastBY(),
                                chatUI.getLastBW(), chatUI.getLastBH());
                    }
                    if (state.showReply) {
                        g2d.drawImage(balloonRImg,
                                chatUI.getLastReplyX(), chatUI.getLastReplyY(),
                                170, chatUI.getLastReplyH(), null);
                    }
                    if (state.isInputVisible) {
                        UIUtils.draw9Patch(g2d, inputImg,
                                chatUI.getLastInX(), chatUI.getLastInY(),
                                chatUI.getLastInW(), chatUI.getLastInH());
                    }
                }

                // [2] 이미지 출력 위치 계산
                int dx = state.isChatting ? -20 : 0;
                int dy = state.isChatting ? CHAT_H - PET_SIZE - (BOTTOM_MARGIN + 5) : 0;

                BufferedImage currentImg;
                int drawSize = PET_SIZE;

                if (state.isEating) {
                    currentImg = dogEatImg;
                    drawSize = 110;
                } else if (state.isBringingBall) {
                    currentImg = dogWithBallImg;
                    drawSize = PET_SIZE;
                } else {
                    currentImg = dogImg;
                    drawSize = PET_SIZE;
                }

                // [3] 최종 그리기
                if (currentImg != null) {
                    if (state.isEating) {
                        int offset = (drawSize - PET_SIZE) / 2;
                        // 창 크기가 커졌으므로, 내부에서 그릴 때 잘리지 않도록
                        // 유저님의 기존 -30 보정값 대신 여유 공간(offset)을 활용해 그립니다.
                        g2d.drawImage(currentImg, dx + offset - 10, dy + offset - 30, drawSize, drawSize, null);
                    } else {
                        g2d.drawImage(currentImg, dx, dy, PET_SIZE, PET_SIZE, null);
                    }
                }
            }
        };
    }

    public void updateSpeechBubbleUI() { chatUI.updateSpeechBubbleUI(); }

    public void updateWindowBounds() {
        boolean needLargeWindow = state.isChatting ||
                (state.dogSpeech != null && !state.dogSpeech.isEmpty()) ||
                state.isEating;

        if (needLargeWindow) {
            // [수정] 식사 중일 때는 이미지가 잘리지 않게 창 크기만 200 정도로 더 키웁니다.
            if (state.isEating) {
                setSize(200, 200);
                // 위치는 기존 보정값(-35)을 유지하여 밥그릇 위치를 맞춥니다.
                setLocation((int) state.x - 35, (int) state.y - 35);
            } else {
                setSize(CHAT_W, CHAT_H);
                setLocation((int) state.x, (int) state.y - (CHAT_H - PET_SIZE - BOTTOM_MARGIN));
            }
            chatUI.updateSpeechBubbleUI();
        } else {
            setSize(PET_SIZE + 100, PET_SIZE + 100);
            setLocation((int) state.x, (int) state.y);
        }

        if (gaugeWindow != null) {
            int gx = (int) state.x - 50;
            int gy = (int) state.y + (PET_SIZE - 90);

            if (state.isEating) {
                gx = (int) state.x - 100;
                gy = (int) state.y + (PET_SIZE - 70);
            }

            gaugeWindow.setLocation(gx, gy);
            boolean shouldShowGauge = state.isHungry() || state.isEating ||
                    (state.isChatting && !state.isBringingBall && !state.isChasingBall);
            gaugeWindow.setVisible(shouldShowGauge);
        }
    }

    public void closeChat() {
        state.isChatting = false;
        state.isInputVisible = false;
        chatUI.hideAll();
        setSize(PET_SIZE, PET_SIZE);
        state.resetChat();
        updateWindowBounds();
        repaint();
    }

    public void hideInputOnly() {
        state.isInputVisible = false;
        chatUI.hideInputOnly();
        repaint();
    }

    public void requestTextFocus() { chatUI.requestTextFocus(); }

    public void processQuestion(String input) {
        state.showReply = true;
        if (commandHandler.handle(input)) {
            updateSpeechBubbleUI();
            return;
        }

        state.isWaiting = true;
        state.dogSpeech = "생각 중이에요 멍! 🐾";
        SoundPlayer.play("dogsound3.wav", -5.0f);
        updateSpeechBubbleUI();

        new Thread(() -> {
            try {
                String result = GeminiAPI.getResponse(input);
                SwingUtilities.invokeLater(() -> {
                    state.dogSpeech = result;
                    state.isWaiting = false;
                    SoundPlayer.playRandomBark(-5.0f);
                    updateSpeechBubbleUI();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    state.isWaiting = false;
                    state.dogSpeech = "에러가 났어요 멍!";
                    updateSpeechBubbleUI();
                });
            }
        }).start();
    }

    public void ballUpdate(int x, int y, boolean thrown) {
        movementController.updateBallTarget(x, y);
        if (thrown && !state.isBringingBall) state.isBallActive = true;
    }

    public int getInputX() { return chatUI.getLastInX(); }
    public int getInputY() { return chatUI.getLastInY(); }
    public int getInputW() { return chatUI.getLastInW(); }
    public int getInputH() { return chatUI.getLastInH(); }
}