package com.sobev.openai.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sobev.openai.model.TextCompletionDto;
import com.sobev.openai.model.TextCompletionVo;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author luojx
 * @date 2023/2/14 9:31
 */
@RestController
@RequestMapping("openai")
@CrossOrigin
public class OpenAIController {

    private RestTemplate restTemplate = new RestTemplate();
    private static final String OPENAI_API_KEY = "";
    private static final String OPENAI_MODEL = "text-davinci-003";
    private static final String GRAMMAR = "";

    private CloseableHttpClient httpClient = HttpClients.createDefault();

    @PostMapping("generate")
    public ResponseEntity<TextCompletionVo> talk(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + OPENAI_API_KEY);
        TextCompletionDto textCompletionDto = new TextCompletionDto();
        textCompletionDto.setPrompt(GRAMMAR + body);
        String jsonBody = new Gson().toJson(textCompletionDto);
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("https://api.openai.com/v1/completions", httpEntity, String.class);
        System.out.println(responseEntity.getBody());
        TextCompletionVo textCompletionVo = new Gson().fromJson(responseEntity.getBody(), TextCompletionVo.class);
        return ResponseEntity.ok(textCompletionVo);
    }

    @PostMapping("stream")
    public void stream(String body, HttpServletResponse httpServletResponse) {
        StringBuilder builder = new StringBuilder();
        TextCompletionDto textCompletionDto = new TextCompletionDto();
        textCompletionDto.setPrompt(GRAMMAR + body);
        textCompletionDto.setStream(true);
        String jsonBody = new Gson().toJson(textCompletionDto);
        try {
            CloseableHttpResponse response = httpClient.execute(RequestBuilder.create("POST")
                    .setUri("https://api.openai.com/v1/completions")
                    .addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                    .setEntity(new StringEntity(jsonBody, Charset.forName("UTF-8")))
                    .build()
            );
            org.apache.http.HttpEntity responseEntity = response.getEntity();
            InputStream inputStream = responseEntity.getContent();
//            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            ServletOutputStream outputStream = httpServletResponse.getOutputStream();
            char[] buffer = new char[1024];
            int len;
            try {
                while ((len = bufferedReader.read(buffer)) > 0) {
                    String res = new String(buffer, 0, len);
                    res = res.replace("data: ", "");
                    if (res.equals("[DONE]")) {
                        break;
                    }
                    try {
                        Gson gson = new GsonBuilder()
                                .setLenient()
                                .create();
                        TextCompletionVo textCompletionVo = gson.fromJson(res, TextCompletionVo.class);
                        String text = textCompletionVo.getChoices().get(0).getText();
                        text = StringEscapeUtils.unescapeJava(text);
                        builder.append(text);
                        System.out.print(text);
                        outputStream.write(text.getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                        if (textCompletionVo.getChoices().get(0).getFinish_reason() != null
                                && textCompletionVo.getChoices().get(0).getFinish_reason().equals("stop")) {
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        continue;
                    }
                }
            }catch (Exception e) {
                System.out.println(e.getMessage());
                //close input stream
                inputStream.close();
            }
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
