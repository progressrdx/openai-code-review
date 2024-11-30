package org.rdx.sdk;

import com.alibaba.fastjson2.JSON;
import org.rdx.sdk.domain.model.ChatCompletionRequest;
import org.rdx.sdk.domain.model.ChatCompletionSyncResponse;
import org.rdx.sdk.domain.model.Model;
import org.rdx.sdk.types.util.BearerTokenUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * @ author rdx
 * @ describe :
 * @ date  2024/11/22
 **/
public class OpenAiCodeReview {
    public static void main(String[] args)throws Exception {
        System.out.println("测试执行");
        // 1. 代码检出
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(new File("."));

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            diffCode.append(line);
        }

        int exitCode = process.waitFor();
        System.out.println("Exited with code:" + exitCode);

//        System.out.println("评审代码：" + diffCode.toString());

        //Gpt评审
        String log = codeReview(diffCode.toString());
        System.out.println("review :" +log);
    }

    private static String codeReview(String diffCode)throws Exception{
        String apiKeySecret = "a7ce6f4ab128d3403ae7bb5e89edc699.33DZiyacos8M4e5d";
        String token = BearerTokenUtils.getToken(apiKeySecret);

        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        connection.setDoOutput(true);

        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(new ChatCompletionRequest.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
                add(new ChatCompletionRequest.Prompt("user", diffCode));
            }
        });

        OutputStream outputStream = connection.getOutputStream();
        byte[] bytes = JSON.toJSONString(chatCompletionRequest).getBytes(StandardCharsets.UTF_8);
        outputStream.write(bytes);

        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);

        BufferedReader bufferedReader =new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder builder = new StringBuilder();
        while((line = bufferedReader.readLine())!= null){
            builder.append(line);
        }
        bufferedReader.close();
        outputStream.close();
        connection.disconnect();

        ChatCompletionSyncResponse response = JSON.parseObject(builder.toString(), ChatCompletionSyncResponse.class);
        return response.getChoices().get(0).getMessage().getContent();
    }
}
