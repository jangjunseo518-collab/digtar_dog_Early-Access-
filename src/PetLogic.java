public class PetLogic {
    // 설정을 상수로 관리하면 유지보수가 훨씬 편해집니다멍!
    public static final int LINE_LIMIT = 14;     // 한 줄당 최대 글자 수
    public static final int SINGLE_LINE_HEIGHT = 30; // 한 줄로 간주할 높이 기준

    /**
     * 텍스트 줄바꿈 처리 로직 (기존 로직 유지)
     */
    public static String formatText(String input) {
        if (input == null || input.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        int currentCount = 0;

        String[] words = input.split(" ");
        for (String word : words) {
            // 단어 하나가 이미 limit을 넘는 경우 (강제 분할)
            if (word.length() > LINE_LIMIT) {
                if (currentCount > 0) {
                    sb.append("\n");
                    currentCount = 0;
                }
                for (char c : word.toCharArray()) {
                    sb.append(c);
                    currentCount++;
                    if (currentCount >= LINE_LIMIT) {
                        sb.append("\n");
                        currentCount = 0;
                    }
                }
                sb.append(" ");
                currentCount++;
                continue;
            }

            // 단어를 붙였을 때 넘어가면 줄바꿈
            if (currentCount + word.length() > LINE_LIMIT) {
                sb.append("\n");
                currentCount = 0;
            }
            sb.append(word).append(" ");
            currentCount += word.length() + 1;
        }
        return sb.toString().trim();
    }

    /**
     * 텍스트가 한 줄인지 판별하는 유틸리티 메서드입니다.
     * DesktopPet에서 중앙 정렬 여부를 결정할 때 사용합니다멍!
     */
    public static boolean isSingleLine(int textHeight) {
        return textHeight < SINGLE_LINE_HEIGHT;
    }
}