import javax.sound.sampled.*;
import java.io.File;

public class SoundPlayer {
    // volume 값은 -80.0(무음)부터 6.0(최대)까지 가능합니다멍![cite: 14]
    public static void play(String fileName, float volume) {
        new Thread(() -> {
            try {
                File soundFile = new File("sounds/" + fileName);
                AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);

                // [핵심] 볼륨 조절 컨트롤을 가져옵니다멍![cite: 14]
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(volume);

                clip.start();

                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception e) {
                System.out.println("소리 재생 실패: " + fileName);
            }
        }).start();
    }

    /**
     * 상황에 따라 랜덤하게 멍멍 소리를 재생하는 편의 메서드멍!
     * 여기에 추가하시면 됩니다멍!
     */
    public static void playRandomBark(float volume) {
        // 기존에 여러 곳에 흩어져 있던 랜덤 로직을 한데 모았습니다멍![cite: 4, 10, 12]
        String selected = (Math.random() < 0.5) ? "dogsound1.wav" : "dogsound2.wav";
        play(selected, volume);
    }
}