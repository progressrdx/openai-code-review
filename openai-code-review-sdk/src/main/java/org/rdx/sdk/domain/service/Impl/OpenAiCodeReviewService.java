package org.rdx.sdk.domain.service.Impl;

import org.rdx.sdk.domain.service.AbstractOpenAiCodeReviewService;
import org.rdx.sdk.infrastructure.git.GitCommand;
import org.rdx.sdk.infrastructure.openai.DTO.ChatCompletionRequestDTO;
import org.rdx.sdk.infrastructure.openai.DTO.ChatCompletionSyncResponseDTO;
import org.rdx.sdk.infrastructure.openai.IOpenAi;
import org.rdx.sdk.infrastructure.weixin.DTO.TemplateMessageDTO;
import org.rdx.sdk.infrastructure.weixin.WeiXin;
import org.rdx.sdk.types.enums.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @ author rdx
 * @ describe :
 * @ date  2024/12/3
 **/
public class OpenAiCodeReviewService extends AbstractOpenAiCodeReviewService {

    public OpenAiCodeReviewService(GitCommand gitCommand, IOpenAi openAI, WeiXin weiXin) {
        super(gitCommand, openAI, weiXin);
    }

    @Override
    protected void sendMessage(String logUrl) throws Exception {
        Map<String, Map<String, String>> data = new HashMap<>();
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.REPO_NAME, gitCommand.getProject());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.BRANCH_NAME, gitCommand.getBranch());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_AUTHOR, gitCommand.getAuthor());
        TemplateMessageDTO.put(data, TemplateMessageDTO.TemplateKey.COMMIT_MESSAGE, gitCommand.getMessage());
        weiXin.sendTemplateMessage(logUrl, data);

    }

    @Override
    protected String writeLog(String recommend) throws Exception {
         return gitCommand.commitAndPush(recommend);
    }
    @Override
    protected String reviewCode(String diffCode) throws Exception {
        ChatCompletionRequestDTO chatCompletionRequest = new ChatCompletionRequestDTO();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequestDTO.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;

            {
                add(new ChatCompletionRequestDTO.Prompt("user", "你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
                add(new ChatCompletionRequestDTO.Prompt("user", diffCode));
            }
        });

        ChatCompletionSyncResponseDTO completions = openAI.CodeReview(chatCompletionRequest);
        ChatCompletionSyncResponseDTO.Message message = completions.getChoices().get(0).getMessage();
        return message.getContent();

    }

    @Override
    protected String getDiff() throws Exception {
        return gitCommand.getCodeDiff();
    }
}
