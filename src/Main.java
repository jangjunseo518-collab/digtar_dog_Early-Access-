import javax.swing.SwingUtilities;

public class Main {
    public static DesktopBall ball;
    // [추가] 장난감 상자를 어디서든 부를 수 있게 static으로 선언해요 멍!
    public static ToyBox toyBox;
    public static DogFood dogFood;
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1. 강아지 생성
            DesktopPet pet = new DesktopPet();
            pet.setVisible(true);

            // 2. 공 생성
            // 생성자 내부에서 이미 유배지(-1000, -1000)로 가도록 설정했으므로
            // 여기서 setVisible(true)를 하지 않는 것이 잔상 방지에 좋습니다 멍!
            ball = new DesktopBall(pet);
            ball.setVisible(false);

            // 3. [추가] 장난감 상자 생성
            // 상자도 처음에는 숨겨져 있다가 "정리" 명령 시에만 나타나야 해요 멍!
            toyBox = new ToyBox();
            toyBox.setVisible(false);

        });
    }
}