import javax.swing.*;

/**
 * 포만감 감소 타이머 및 배고픔 이벤트를 관리하는 클래스
 */
public class HungerManager {

    private final PetState state;
    private final DesktopPet pet;
    private final FullnessGaugeWindow gaugeWindow;

    private long lastHungryMessageTime = 0;
    private boolean isFirstHungry = true;
    private int lastHungryIdx = -1;
    public int hungerSoundToggle = 0;

    public HungerManager(PetState state, DesktopPet pet, FullnessGaugeWindow gaugeWindow) {
        this.state = state;
        this.pet = pet;
        this.gaugeWindow = gaugeWindow;
    }

    /** 포만감 감소 + 게이지 표시 타이머 시작 (1초 간격) */
    public void startTimer() {
        new Timer(1000, e -> {
            if (state.fullness > 0) {
                double decreasePerSecond = 100.0 / (15 * 60);
                state.fullness -= decreasePerSecond;

                if (gaugeWindow != null) {
                    gaugeWindow.repaint();
                    boolean shouldShowGauge = (state.fullness <= 30) ||
                            (state.isChatting && !state.isBringingBall && !state.isChasingBall);

                    if (gaugeWindow.isVisible() != shouldShowGauge) {
                        gaugeWindow.setVisible(shouldShowGauge);
                    }
                }

                if (state.isHungry()) {
                    checkHungryEvent();
                } else {
                    isFirstHungry = true;
                    lastHungryIdx = -1;
                }
            }
        }).start();
    }

    /** 배고픔 말풍선 + 소리 이벤트 (60초 간격 또는 최초 1회) */
    public void checkHungryEvent() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHungryMessageTime >= 60000 || isFirstHungry) {
            String nextSound = (hungerSoundToggle == 0) ? "hunger_dog1.wav" : "hunger_dog2.wav";
            SoundPlayer.play(nextSound, -5.0f);
            hungerSoundToggle = (hungerSoundToggle == 0) ? 1 : 0;

            String[] hungryReplies = {
                    "배고파요 멍... 밥 주세요! 🦴",
                    "꼬르륵... 배에서 소리가 나요 멍! 🐾",
                    "기운이 없어요 멍... 맛있는 거 주세요! 🐶"
            };
            int newIdx;
            do { newIdx = (int) (Math.random() * hungryReplies.length); } while (newIdx == lastHungryIdx);
            lastHungryIdx = newIdx;

            state.dogSpeech = hungryReplies[newIdx];
            state.isChatting = true;
            state.isInputVisible = true;
            pet.updateWindowBounds();
            pet.requestTextFocus();
            lastHungryMessageTime = currentTime;
            isFirstHungry = false;
        }
    }

    public int getHungerSoundToggle() { return hungerSoundToggle; }
    public void setHungerSoundToggle(int val) { hungerSoundToggle = val; }
}