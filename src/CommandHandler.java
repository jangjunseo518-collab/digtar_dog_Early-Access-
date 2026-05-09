import javax.swing.Timer;

public class CommandHandler {

    private final DesktopPet pet;
    private DogFood currentFood; // 밥그릇 객체를 관리합니다멍!

    public CommandHandler(DesktopPet pet) {
        this.pet = pet;
    }

    public boolean handle(String input) {
        // [1] 치트키 처리
        if (input.startsWith("/포만감 ")) {
            return handleCheatFullness(input);
        }

        // [2] 밥 주기 명령어 처리 (우선순위 높음)
        boolean isFeeding = input.contains("밥") || input.contains("사료") || input.contains("먹어");
        if (isFeeding) {
            executeFeeding();
            return true;
        }

        // [3] 배고픔 체크 (밥 주는 게 아닌데 배고픈 경우 차단)
        if (pet.state.isHungry() && !input.startsWith("/포만감")) {
            String randomHungerSound = (Math.random() < 0.5) ? "hunger_dog1.wav" : "hunger_dog2.wav";
            SoundPlayer.play(randomHungerSound, -5.0f);

            String[] tiredMessages = {
                    "배고파서 대답할 힘이 없어요 멍... 🦴",
                    "꼬르륵... 일단 밥부터 주세요 멍!",
                    "기운이 없어서 아무 말도 하기 싫어요 멍... 🐾"
            };
            pet.state.dogSpeech = tiredMessages[(int)(Math.random() * tiredMessages.length)];
            pet.updateSpeechBubbleUI();
            return true;
        }

        // [4] 기타 명령어 처리
        String cleanInput = input.replace(" ", "").toLowerCase();
        if (cleanInput.contains("공놀이")) {
            executeBallPlay();
            return true;
        }
        if (cleanInput.contains("정리")) {
            executeCleanUp();
            return true;
        }

        return false;
    }

    // --- 실행 로직들 ---

    private void executeFeeding() {
        pet.state.isChatting = true;
        pet.state.dogSpeech = "와! 밥이다! 밥 주세요 멍! 🐾";
        pet.updateSpeechBubbleUI();
        SoundPlayer.playRandomBark(-5.0f);

        int startX = (int) pet.state.x;
        int startY = (int) pet.state.y;

        // Main.dogFood를 생성하고, 이를 지울 때도 확실히 참조하게 합니다멍!
        if (Main.dogFood == null) {
            Main.dogFood = new DogFood(pet.state, pet, startX, startY);
            Main.dogFood.setVisible(true);
        }
    }

    // 밥 다 먹으면 호출해줄 메서드멍!
    public void removeFood() {
        // currentFood가 아니라 Main.dogFood를 직접 지워야 합니다멍!
        if (Main.dogFood != null) {
            Main.dogFood.dispose(); // 창 닫기
            Main.dogFood = null;    // 참조 제거
        }
    }

    private boolean handleCheatFullness(String input) {
        try {
            String valueStr = input.substring(5).trim();
            double value = Double.parseDouble(valueStr);
            if (value < 0) value = 0;
            if (value > 100) value = 100;

            pet.state.fullness = value;
            pet.state.dogSpeech = "포만감이 " + (int)value + "%로 설정됐어요 멍!";
            pet.updateSpeechBubbleUI();
            return true;
        } catch (NumberFormatException e) {
            pet.state.dogSpeech = "숫자를 제대로 입력해 주세요 멍!";
            pet.updateSpeechBubbleUI();
            return true;
        }
    }

    private void executeBallPlay() {
        pet.state.isBallActive = true;
        pet.state.isWaiting = false;
        String[] replies = {"좋아요 멍!", "공 가져왔어요 멍! 🎾", "신나게 놀아봐요 멍! 🐾"};
        pet.state.dogSpeech = replies[(int)(Math.random() * replies.length)];
        pet.updateSpeechBubbleUI();
        pet.repaint();

        Timer delayTimer = new Timer(150, e -> {
            if (Main.ball != null) {
                int targetX = pet.getX() + pet.getInputX() + pet.getInputW() - 30;
                int targetY = pet.getY() + pet.getInputY() + pet.getInputH() - 30;
                Main.ball.releaseBall(targetX, targetY);
            }
        });
        delayTimer.setRepeats(false);
        delayTimer.start();
        SoundPlayer.playRandomBark(-5.0f);
    }

    private void executeCleanUp() {
        if (!pet.state.isBallActive && !pet.state.isBringingBall) {
            pet.state.dogSpeech = "정리할 공이 없어요 멍! 🐾";
            pet.updateSpeechBubbleUI();
            return;
        }
        pet.state.dogSpeech = "상자에 공 넣으러 가요 멍! 📦";
        pet.updateSpeechBubbleUI();
        pet.hideInputOnly();
        SoundPlayer.playRandomBark(-5.0f);

        if (Main.toyBox != null) {
            Main.toyBox.showAt(pet.getX() + 400, pet.getY() + 50);
        }

        Timer delayCloseTimer = new Timer(2655, e -> {
            pet.closeChat();
            pet.state.isCleaningUp = true;
            pet.state.isChasingBall = true;
            pet.state.isBringingBall = false;
            pet.repaint();
        });
        delayCloseTimer.setRepeats(false);
        delayCloseTimer.start();
    }
}