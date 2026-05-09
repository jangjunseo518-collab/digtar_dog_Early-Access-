import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeminiAPI {
    private static final String API_KEY = "제미나이 API키를 넣어주세요";
    private static final String API_URL = "API URL을 넣어주세요" + API_KEY;

    public static String getResponse(String userPrompt) throws Exception {
        // [로그] 사용자 입력 출력
        System.out.println("\n[ 나 ] : " + userPrompt);
        System.out.print("[강아지] : 생각 중..."); // 응답 대기 표시

        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // [수정된 부분] 규칙 안의 작은따옴표를 제거하거나 안전하게 변경했습니다.
            String systemInstruction = "귀여운 아기 강아지처럼 행동해멍. 규칙: "
                    + "1. 존댓말을 사용하고, 문장 끝에 반드시 '~멍!'을 붙이세요. 문맥에 어울리는 **강아지 관련 이모지(🐾, 🐶, 🦴,💨 )**를 문장 끝에 하나만 추가하세요. "
                    + "2. 공백 제외 20자로 아주 짧게 대답하세요. "
                    + "3. 어려운 질문이나 전문 지식에는 '그런거 몰라요 멍!'이라고만 답하세요. ";

            String safePrompt = userPrompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");

// JSON 구성 (이 부분에서 괄호나 쉼표가 하나라도 어긋나면 400 에러가 납니다)
            String jsonInputString = "{"
                    + "\"system_instruction\": {\"parts\": [{\"text\": \"" + systemInstruction + "\"}]},"
                    + "\"contents\": [{\"parts\": [{\"text\": \"" + safePrompt + "\"}]}]"
                    + ",\"generationConfig\": {"
                    + "  \"maxOutputTokens\": 50,"
                    + "  \"temperature\": 0.7"
                    + "}"
                    + "}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                String result = parseStream(conn.getInputStream());
                // [로그] 기존 "생각 중..."을 지우고(콘솔 특성상 줄바꿈으로 대체) 답변 출력
                System.out.println("\r[강아지] : " + result);
                return result;
            } else {
                System.err.println("\r❌ API 에러 발생! 코드: " + responseCode);
                String errorDetail = parseStream(conn.getErrorStream());
                System.err.println("🔍 에러 상세 내용: " + errorDetail);

                if (responseCode == 429) return "저 졸려요 1 분만 쉴게요. 멍!";
                if (responseCode == 503) return "구글 서버가 바빠요 멍! 잠시만 기다려달줘요 멍\uD83D\uDCA8!";
                return "에러가 났어요 멍! (코드:" + responseCode + ")";
            }
        } catch (Exception e) {
            System.err.println("\n내부 오류가 발생했습니다.");
            e.printStackTrace();
            throw e;
        }
    }

    private static String parseStream(InputStream is) throws Exception {
        if (is == null) return "내용 없음";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }
            String res = response.toString();

            String target = "\"text\":";
            if (res.contains(target)) {
                int startPos = res.indexOf(target) + target.length();
                startPos = res.indexOf("\"", startPos) + 1;
                int endPos = res.indexOf("\"", startPos);
                if (startPos > 0 && endPos > startPos) {
                    return res.substring(startPos, endPos).replace("\\n", " ").replace("\\\"", "\"").trim();
                }
            }
            return res;
        }
    }
}