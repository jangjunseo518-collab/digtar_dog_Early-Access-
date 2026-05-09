import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * 채팅 관련 UI 구성 및 말풍선 레이아웃 계산을 전담하는 클래스멍![cite: 1]
 */
public class ChatUI {

    private final PetState state;
    private final DesktopPet pet;

    private JTextPane dogSpeechArea;
    private JTextPane userReplyLabel;
    private JTextArea textArea;

    private final int CHAT_H;
    private final int PET_SIZE;
    private final int BOTTOM_MARGIN;

    private int lastBX, lastBY, lastBW, lastBH;
    private int lastInX, lastInY, lastInW, lastInH;
    private int lastReplyX, lastReplyY, lastReplyH;
    private String lastInputText = "";

    public ChatUI(PetState state, DesktopPet pet, int chatH, int petSize, int bottomMargin) {
        this.state = state;
        this.pet = pet;
        this.CHAT_H = chatH;
        this.PET_SIZE = petSize;
        this.BOTTOM_MARGIN = bottomMargin;
    }

    public void setupUI(JPanel panel) {
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);

        dogSpeechArea = new JTextPane();
        dogSpeechArea.setFont(new Font("돋움", Font.BOLD, 12));
        dogSpeechArea.setOpaque(false);
        dogSpeechArea.setEditable(false);
        dogSpeechArea.setFocusable(false);
        dogSpeechArea.setParagraphAttributes(center, false);
        panel.add(dogSpeechArea);

        userReplyLabel = new JTextPane();
        userReplyLabel.setFont(new Font("돋움", Font.BOLD, 12));
        userReplyLabel.setOpaque(false);
        userReplyLabel.setEditable(false);
        userReplyLabel.setFocusable(false);
        userReplyLabel.setParagraphAttributes(center, false);
        panel.add(userReplyLabel);

        textArea = new JTextArea();
        textArea.setFont(new Font("돋움", Font.BOLD, 12));
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setForeground(Color.BLACK);

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume();
                    String input = textArea.getText().trim();
                    if (!input.isEmpty()) {
                        lastInputText = input; // 여기서 직접 저장하므로 Setter가 필요 없어요![cite: 1]
                        pet.processQuestion(input);
                        textArea.setText("");
                    }
                }
            }
        });

        ((javax.swing.text.AbstractDocument) textArea.getDocument())
                .setDocumentFilter(new javax.swing.text.DocumentFilter() {
                    @Override
                    public void replace(FilterBypass fb, int offset, int length, String text,
                                        javax.swing.text.AttributeSet attrs)
                            throws javax.swing.text.BadLocationException {
                        String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                        StringBuilder sb = new StringBuilder(currentText);
                        sb.replace(offset, offset + length, text);
                        if (sb.toString().replace(" ", "").length() <= 25) {
                            super.replace(fb, offset, length, text, attrs);
                        } else {
                            state.dogSpeech = "너무 긴 말은 어려워요 멍!🐾";
                            updateSpeechBubbleUI();
                            SoundPlayer.playRandomBark(-10.0f); // 공통 메서드로 교체멍![cite: 14]
                        }
                    }
                });
        panel.add(textArea);
    }

    public void updateSpeechBubbleUI() {
        if (!state.isChatting) return;
        int drawX = 10, drawY = CHAT_H - PET_SIZE - BOTTOM_MARGIN;

        dogSpeechArea.setText(PetLogic.formatText(state.dogSpeech));
        dogSpeechArea.setSize(140, 600);
        int textH = dogSpeechArea.getPreferredSize().height;
        int bW = 180, bH = Math.max(60, textH + 40);
        lastBX = drawX - 10; lastBY = (drawY + 10) - bH; lastBW = bW; lastBH = bH;
        dogSpeechArea.setBounds(lastBX + 22, lastBY + (bH - textH) / 2 - 2, 140, textH);
        dogSpeechArea.setVisible(!state.dogSpeech.isEmpty() || state.isWaiting);

        lastInX = drawX + 15; lastInY = drawY + 60; lastInW = 160;
        int textPreferredHeight = textArea.getPreferredSize().height;
        lastInH = Math.max(50, textPreferredHeight + 35);
        textArea.setBounds(lastInX + 10, lastInY + 17, lastInW - 20, lastInH - 25);
        textArea.setVisible(state.isInputVisible);

        if (state.showReply) {
            userReplyLabel.setText(lastInputText);
            userReplyLabel.setSize(140, 600);
            int replyTextH = userReplyLabel.getPreferredSize().height;
            int replyBH = Math.max(50, replyTextH + 20);
            lastReplyX = drawX + 25;
            lastReplyY = lastInY - 5 - replyBH;
            lastReplyH = replyBH;
            userReplyLabel.setBounds(lastReplyX + 20, lastReplyY + (replyBH - replyTextH) / 2, 120, replyTextH);
            userReplyLabel.setVisible(true);
        } else {
            userReplyLabel.setVisible(false);
        }
    }

    public void hideAll() {
        dogSpeechArea.setVisible(false);
        userReplyLabel.setVisible(false);
        textArea.setVisible(false);
    }

    public void hideInputOnly() {
        if (textArea != null) textArea.setVisible(false);
    }

    public void requestTextFocus() {
        if (textArea != null) textArea.requestFocusInWindow();
    }

    // [정리 완료] setLastInputText와 getLastInputText를 삭제했습니다멍![cite: 1]

    public int getLastBX()     { return lastBX; }
    public int getLastBY()     { return lastBY; }
    public int getLastBW()     { return lastBW; }
    public int getLastBH()     { return lastBH; }
    public int getLastInX()    { return lastInX; }
    public int getLastInY()    { return lastInY; }
    public int getLastInW()    { return lastInW; }
    public int getLastInH()    { return lastInH; }
    public int getLastReplyX() { return lastReplyX; }
    public int getLastReplyY() { return lastReplyY; }
    public int getLastReplyH() { return lastReplyH; }
}