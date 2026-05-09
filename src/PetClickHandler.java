import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 펫 클릭 이벤트 처리를 전담하는 클래스
 */
public class PetClickHandler extends MouseAdapter {

    private final PetState state;
    private final DesktopPet pet;
    private final HungerManager hungerManager;

    public PetClickHandler(PetState state, DesktopPet pet, HungerManager hungerManager) {
        this.state = state;
        this.pet = pet;
        this.hungerManager = hungerManager;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // 배고픈 상태에서 공이 활성화된 경우: 공 놀이 거부
        if (state.isHungry() && state.isBallActive) {
            int toggle = hungerManager.getHungerSoundToggle();
            String nextSound = (toggle == 0) ? "hunger_dog1.wav" : "hunger_dog2.wav";
            SoundPlayer.play(nextSound, -5.0f);
            hungerManager.setHungerSoundToggle(toggle == 0 ? 1 : 0);

            state.isChasingBall = false;
            state.isChatting = true;
            state.isInputVisible = true;
            state.dogSpeech = "배고파서 공을 못 잡겠어요 멍... 🎾";
            pet.updateWindowBounds();
            pet.requestTextFocus();
            return;
        }

        SoundPlayer.playRandomBark(-5.0f);

        // 정리 중에는 클릭 무시
        if (state.isCleaningUp) return;

        // 공 물어오는 중 클릭: 공 미리 전달
        if (state.isBringingBall) {
            state.isBringingBall = false;
            state.isBallActive = true;
            state.dogSpeech = "여기 공 가져왔어요 멍! 🎾";
            state.isInputVisible = true;
            if (Main.ball != null) Main.ball.releaseBall((int) state.x + 20, (int) state.y + 80);
            pet.updateSpeechBubbleUI();
            pet.requestTextFocus();
            return;
        }

        // 공이 활성화 상태이고 쫓는 중이 아닐 때 클릭: 공 쫓기 시작
        if (state.isBallActive && !state.isChasingBall) {
            state.isChasingBall = true;
            state.isChatting = true;
            state.showReply = false;
            state.isInputVisible = false;
            state.dogSpeech = "멍! 멍!️💨";
            pet.updateSpeechBubbleUI();
            return;
        }

        // 일반 클릭: 채팅 토글
        state.isChatting = !state.isChatting;
        if (state.isChatting) {
            state.isInputVisible = true;
            state.dogSpeech = "불렀어요 멍?🐾";
            pet.requestTextFocus();
        } else {
            pet.closeChat();
        }
        pet.updateWindowBounds();
        pet.repaint();
    }
}