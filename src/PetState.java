public class PetState {
    public double x = 500, y = 500;
    public boolean isChatting = false;
    public boolean showReply = false;
    public boolean isWaiting = false;
    public String dogSpeech = "";
    public boolean isBallActive = false; // 기본값은 꺼짐!
    public boolean isCleaningUp = false;
    // PetState.java 파일 안 어딘가에 추가멍!
    public boolean isReturning = false;
    public boolean isInputVisible = true; // 입력창 스위치
       // 정리 후 복귀 체크 스위치

    //밥 관련
    public boolean isFoodActive = false; // 밥그릇이 화면에 있는가?
    public boolean isEating = false;     // 지금 밥을 먹고 있는가?
    public int foodX, foodY;             // 밥그릇의 현재 위치

    // 공 놀이 관련 상태 🐾
    public boolean isChasingBall = false;
    public boolean isBringingBall = false;

    public void resetChat() {
        isChatting = false;
        showReply = false;
        dogSpeech = "";
        isWaiting = false;
    }

    public double fullness = 100.0;
    public final double MAX_FULLNESS = 100.0;

    /**
     * 포만감이 낮아 행동에 제약이 생기는 상태인지 체크멍!
     * 기준을 30.0으로 통일했습니다.[cite: 13]
     */
    public boolean isHungry() {
        return fullness <= 30.0;
    }
}