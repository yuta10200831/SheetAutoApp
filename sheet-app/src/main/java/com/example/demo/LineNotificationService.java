package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class LineNotificationService {

    // application.properties に書いた鍵を自動で読み込むよ！
    @Value("${line.bot.channel-token}")
    private String channelToken;

    @Value("${line.bot.user-id}")
    private String userId;

    // LINEにメッセージを送るための専用URL
    private final String LINE_API_URL = "https://api.line.me/v2/bot/message/push";

    /**
     * LINEにメッセージを送信するメソッド
     * @param messageText 送りたいテキスト
     */
    public void sendLineMessage(String messageText) {
        RestTemplate restTemplate = new RestTemplate();

        // 1. ヘッダーの準備（「私は怪しい者じゃありません、これが鍵です」という証明）
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(channelToken);

        // 2. 送るメッセージの中身を作る
        // LINEのルールに合わせて、Map（辞書）を使ってデータを作るよ
        Map<String, Object> message = new HashMap<>();
        message.put("type", "text");
        message.put("text", messageText);

        Map<String, Object> body = new HashMap<>();
        body.put("to", userId); // 宛先（自分）
        body.put("messages", Collections.singletonList(message));

        // 3. ヘッダーとメッセージを合体！
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // 4. LINEのサーバーに向かって「送信（POST）」！
        try {
            restTemplate.postForEntity(LINE_API_URL, request, String.class);
            System.out.println("✅ LINE通知を送信しました！");
        } catch (Exception e) {
            System.err.println("❌ LINE通知の送信に失敗しました... 詳細: " + e.getMessage());
        }
    }
}
