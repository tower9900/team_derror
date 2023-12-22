package com.example.demo.service.openai;

import com.example.demo.domain.openai.Sight;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theokanning.openai.OpenAiResponse;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.messages.Message;
import com.theokanning.openai.messages.MessageContent;
import com.theokanning.openai.messages.MessageRequest;
import com.theokanning.openai.messages.content.Text;
import com.theokanning.openai.runs.Run;
import com.theokanning.openai.runs.RunCreateRequest;
import com.theokanning.openai.service.FunctionExecutor;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.threads.Thread;
import com.theokanning.openai.threads.ThreadRequest;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Autor 이종현
 *
 */
@Service
public class GptService {

    @Value(value = "${openai.key}")
    private String token;

    public List<ChatCompletionChoice> chat(String prompt){
        OpenAiService service = new OpenAiService(token);

        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), prompt);
        messages.add(systemMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-4")
                .messages(messages)
                .n(1)
                .maxTokens(255)
                .logitBias(new HashMap<>())
                .build();
        return service.createChatCompletion(chatCompletionRequest).getChoices();

    }


   public Sight searchKeyWord(String keyword){
       OpenAiService service = new OpenAiService(token);
        String id = "asst_Es5Gqn9qr4aonTRGvLAgS9uE";


       ThreadRequest threadRequest = ThreadRequest.builder()
               .build();
       Thread thread = service.createThread(threadRequest);

       MessageRequest messageRequest = MessageRequest.builder()
               .content(keyword)
               .build();
       service.createMessage(thread.getId(),messageRequest);

       RunCreateRequest runCreateRequest = RunCreateRequest.builder()
               .assistantId(id)
               .build();
       Run run = service.createRun(thread.getId(),runCreateRequest);

       long startTime = System.currentTimeMillis();

       Run retrievedRun;
       try {
           do {
               retrievedRun = service.retrieveRun(thread.getId(), run.getId());
               System.out.println("request");
           } while (!(retrievedRun.getStatus().equals("completed")) && !(retrievedRun.getStatus().equals("failed")));

       }catch (Exception e){

       }finally {
           long endTime = System.currentTimeMillis();
           long elapsedTime = endTime - startTime;

           System.out.println("요청이 완료되었습니다. 소요 시간: " + elapsedTime + "밀리초");
       }



       OpenAiResponse<Message> messageOpenAiResponse = service.listMessages(thread.getId());
       ObjectMapper mapper = new ObjectMapper();
       Sight sight = null;
       try {
           System.out.println(messageOpenAiResponse.getData().get(0).getContent().get(0).getText().getValue());
           sight = mapper.readValue(messageOpenAiResponse.getData().get(0).getContent().get(0).getText().getValue(), Sight.class);
       } catch (JsonProcessingException e) {
           throw new RuntimeException(e);
       }
       return sight;
   }


}
