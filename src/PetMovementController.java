import java.awt.*;
import javax.swing.Timer; // 이 줄을 추가해야 Timer 에러가 안 납니다멍!
import java.awt.*;
/**
 * 펫의 이동 로직을 전담하는 클래스
 * - 마우스 따라다니기 (일반 모드)
 * - 공 쫓아가기 (isChasingBall)
 * - 공 물어오기 (isBringingBall)
 * - 장난감 정리하러 이동 (isCleaningUp)
 * - 정리 후 복귀 (isReturning)
 */
public class PetMovementController {

    private final PetState state;
    private final DesktopPet pet;

    private final double bringBallSpeed = 10.0;
    private final double cleanUpSpeed = 7.0;
    private final double returnSpeed = 8.0;
    private final double normalSpeed = 3.5;
    private final double chaseBallSpeed = 9.0;

    // 공의 마지막 위치 (ballUpdate 로부터 받음)
    private int bTX, bTY;

    public PetMovementController(PetState state, DesktopPet pet) {
        this.state = state;
        this.pet = pet;
    }

    /**
     * DesktopBall 로부터 공 위치 업데이트 수신
     */
    public void updateBallTarget(int x, int y) {
        this.bTX = x;
        this.bTY = y;
    }

    /**
     * 메인 타이머(16ms)마다 호출되는 이동 처리
     */
    public void handlePetMovement() {
        Point m = MouseInfo.getPointerInfo().getLocation();

        // 밥을 향해 달려가는 상태(isFoodActive)를 최우선으로 체크합니다멍!
        if (state.isFoodActive && !state.isEating) {
            handleFoodMovement();
        } else if (state.isCleaningUp || state.isChasingBall) {
            handleTargetedMovement(m);
        } else {
            handleFollowAndReturn(m);
        }
    }

    /**
     * 밥그릇을 향해 달려가고, 도착 시 식사 로직 수행멍!
     */
    private void handleFoodMovement() {
        double dx = state.foodX - (state.x + 40);
        double dy = state.foodY - (state.y + 40);
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > 15) {
            state.x += (dx / dist) * 10.0;
            state.y += (dy / dist) * 10.0;
        } else {
            // [식사 시작] 밥그릇에 도착!멍!🐾
            state.isEating = true;
            state.isFoodActive = false;

            // 1. 밥그릇 창 제거
            if (pet.getCommandHandler() != null) {
                pet.getCommandHandler().removeFood();
            }

            // 2. 메시지 및 사운드 출력
            state.dogSpeech = "냠냠! 너무 맛있어요 멍!😋";
            pet.updateSpeechBubbleUI();
            SoundPlayer.play("dogsound2.wav", -5.0f); // 냠냠거리는 느낌의 사운드

            // 3. 포만감 상승 타이머 (0.5초마다 10씩 상승, 총 3초)
            Timer eatingTimer = new Timer(500, null);
            final int[] ticks = {0}; // 실행 횟수 추적용

            eatingTimer.addActionListener(e -> {
                ticks[0]++;

                // 포만감 10 상승 (최대 100)
                state.fullness = Math.min(100, state.fullness + 10);
                pet.repaint(); // 게이지 갱신을 위해 호출멍!

                // 3초 경과 (0.5초 * 6번)
                if (ticks[0] >= 6) {
                    eatingTimer.stop();
                    finishEating(); // 식사 종료 처리멍!
                }
            });
            eatingTimer.start();
        }
    }

    /** 식사 완료 후 복귀 로직멍! */
    /**
     * 식사 완료 후 복귀 로직 수정멍!
     */
    private void finishEating() {
        state.isEating = false;
        state.isFoodActive = false; // 밥 추적 모드 종료
        state.isReturning = true;   // 마우스로 돌아가기 시작멍! 🐾

        // 이동 중에는 말풍선을 비워두어 시야를 확보합니다.
        state.dogSpeech = "";
        pet.updateSpeechBubbleUI();

        // [수정] 기존의 2초 타이머(returnTimer)를 완전히 삭제했습니다멍!
        // 이제 대사를 지우거나 채팅창을 강제로 초기화하지 않습니다.
    }

    /**
     * 공 쫓기 / 장난감 정리 이동
     */
    private void handleTargetedMovement(Point m) {
        double targetX, targetY, currentMoveSpeed;

        if (state.isChasingBall) {
            if (Main.ball != null) {
                targetX = Main.ball.getX() + 20;
                targetY = Main.ball.getY() + 20;
            } else {
                targetX = bTX;
                targetY = bTY;
            }
            currentMoveSpeed = chaseBallSpeed;

        } else if (state.isCleaningUp && Main.toyBox != null) {
            targetX = Main.toyBox.getX() + 40;
            targetY = Main.toyBox.getY() + 40;
            currentMoveSpeed = cleanUpSpeed;
        } else {
            return;
        }

        double dx = targetX - (state.x + 40);
        double dy = targetY - (state.y + 40);
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > 15) {
            state.x += (dx / dist) * currentMoveSpeed;
            state.y += (dy / dist) * currentMoveSpeed;
        } else {
            handleArrival();
        }
    }

    /**
     * 목적지 도착 처리
     */
    private void handleArrival() {
        if (state.isChasingBall) {
            state.isChasingBall = false;
            state.isBringingBall = true;
            state.isBallActive = false;
            SoundPlayer.play("dogsound1.wav", -5.0f);
            state.dogSpeech = "멍! ✨🐾";
            if (Main.ball != null) Main.ball.setCaptured();
            pet.updateSpeechBubbleUI();

        } else if (state.isCleaningUp) {
            state.isCleaningUp = false;
            state.isReturning = true;
            state.isBringingBall = false;
            state.isBallActive = false;
            SoundPlayer.playRandomBark(-5.0f);
            if (Main.ball != null) {
                Main.ball.setVisible(false);
                Main.ball.setCaptured();
            }
            if (Main.toyBox != null) Main.toyBox.setVisible(false);
            state.dogSpeech = "정리 끝! 이제 돌아갈게요 멍!✨";
            pet.updateSpeechBubbleUI();
        }
    }

    /** 마우스 따라다니기 / 공 배달 / 복귀 이동 */
    /**
     * 마우스 따라다니기 / 복귀 이동 로직 (기존 기능 유지 + 밥 인사 추가)
     */
    private void handleFollowAndReturn(Point m) {
        double dx = m.x - (state.x + 40);
        double dy = m.y - (state.y + 40);
        double dist = Math.sqrt(dx * dx + dy * dy);

        double speed = state.isBringingBall ? bringBallSpeed
                : state.isReturning ? returnSpeed
                  : normalSpeed;

        // 1. 공 배달 완료 (기존 기능 유지멍!)
        if (state.isBringingBall && dist <= 60) {
            state.isBringingBall = false;
            state.isBallActive = true;
            state.dogSpeech = "여기 공 가져왔어요 멍! 🎾";
            SoundPlayer.playRandomBark(-5.0f);
            state.isInputVisible = true;
            if (Main.ball != null) Main.ball.releaseBall((int) state.x + 20, (int) state.y + 80);
            pet.updateSpeechBubbleUI();
            pet.requestTextFocus();
        }

        // 2. 복귀 완료 (상황에 따라 대사를 다르게 설정함멍! 🐾)
        if (state.isReturning && dist <= 60) {
            state.isReturning = false;
            state.isChatting = true;
            state.isInputVisible = true;

            // [핵심] 현재 포만감이 방금 찼거나, 특정 조건을 체크해서 대사를 분기합니다.
            // 밥을 먹고 온 직후라면 (포만감이 높고 방금 먹기 상태가 끝났을 때)
            if (state.fullness >= 90) {
                state.dogSpeech = "잘 먹었습니다 멍! ✨ 정말 든든해요!";
            } else {
                // 장난감 정리 등을 하고 왔을 때 (기존 대사)
                state.dogSpeech = "정리 다 했어요! 멍!✨🐾";
            }

            SoundPlayer.playRandomBark(-5.0f);
            pet.updateSpeechBubbleUI();
            pet.requestTextFocus();

        } else if (dist > 60) {
            state.x += (dx / dist) * speed;
            state.y += (dy / dist) * speed;
        }
    }
}