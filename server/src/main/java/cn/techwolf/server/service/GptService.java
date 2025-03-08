package cn.techwolf.server.service;

import cn.techwolf.server.model.Job;
import cn.techwolf.server.utils.HttpUtils;
import cn.techwolf.server.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GptService {

    private static final String gptUrl = "http://bzlai-system-service-http-qa.weizhipin.com/bais/v1/chat/completions";

    private static final String gptToken = "PLac44dM-6ff897d141684cf7a966a9227bd13bb8";

    private static final CloseableHttpClient httpClient = HttpUtils.createHttpClient(30000, 1000, 20000, 20000, 100);

    @Data
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GptMessage implements Serializable {
        private static final long serialVersionUID = 7549551903093688499L;
        private String role;
        private String content;
    }

    @Data
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GptResponse implements Serializable {
        private static final long serialVersionUID = 8487933544192862953L;
        private String id;
        private String object;
        private long created;
        private String model;
        private GptChoice[] choices;

    }

    @Data
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GptChoice implements Serializable {
        private static final long serialVersionUID = 1585287792223783045L;
        private int index;
        private GptMessage message;
    }

    /**
     * gpt调用
     *
     * @param messages
     * @param maxToken * 1.5 = 最大的字符数
     */
    public List<Job> requestGPT(List<GptMessage> messages, int maxToken) throws Exception {
        HttpPost httpPost = new HttpPost(new URI(gptUrl));
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpPost.setHeader("Authorization", "Bearer " + gptToken);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("stream", false);
        requestBody.put("temperature", 0.1);
        requestBody.put("max_tokens", maxToken);
        requestBody.put("output_accumulate", true);
        requestBody.put("model", "nbg-16b-stream-common-gray");
        requestBody.put("messages", messages);

        // 将JSON请求体设置为HttpEntity
        StringEntity entity = new StringEntity(JsonUtils.fetchString(requestBody), StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            // 获取响应实体
            HttpEntity responseEntity = response.getEntity();
            // 检查响应状态码
            log.info("requestGPT Status Code: {}, req = {}", response.getStatusLine().getStatusCode(), entity);
            if (responseEntity != null) {
                // 流式地读取响应体
                try (InputStream inputStream = responseEntity.getContent();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    int i = 1;
                    while ((line = reader.readLine()) != null) {
                        // 处理每一行响应数据
                        try {
                            log.info("requestGPT line = {}", line);
                            if (StringUtils.isNotBlank(line)) {
                                //{"id":"24d063d7e0c843729ea46c46ae758150","object":"chat.completion","created":1741424320,"model":"llama","choices":[{"index":0,"message":{"role":"assistant","content":"```json\n[\n    {\n        \"title\": \"搬家工人\",\n        \"description\": \"家具拆装、物品搬运及包装整理\",\n        \"workingTime\": \"08:00-18:00（按订单排期）\",\n        \"location\": \"全国范围内\",\n        \"contactPhone\": \"16666466666\"\n    },\n    {\n        \"title\": \"停车场管理员\",\n        \"description\": \"车辆引导、停车费收取及秩序维护\",\n        \"workingTime\": \"07:00-22:00（三班倒）\",\n        \"location\": \"北京市朝阳区冠捷停车场\",\n        \"contactPhone\": \"16666676666\"\n    }\n]\n```"}}],"usage":{"prompt_tokens":0,"total_tokens":0,"completion_tokens":0}}
                                return parseContentToJobList(line);
                            }
                        } catch (Exception e) {
                            log.error("requestGPT error messages = {}", line, e);
                        }
                    }
                }

                // 确保响应实体被完全消费（尽管在try-with-resources块中这通常是自动的）
                EntityUtils.consume(responseEntity);
            }
        } catch (IOException e) {
            log.error("requestGPT error messages = {}", messages, e);
        }
        return null;
    }

    private static List<Job> parseContentToJobList(String jsonResponse) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResponse);
        JsonNode choicesNode = rootNode.path("choices");
        if (choicesNode.isArray() && !choicesNode.isEmpty()) {
            JsonNode messageNode = choicesNode.get(0).path("message");
            String content = messageNode.path("content").asText();

            // 去除 Markdown 代码块标记
            content = content.replace("```json\\n", "[").replace("```", "").trim();

            // 解析 JSON 数组为 Job 对象列表
            return JsonUtils.fetchObject(content, mapper.getTypeFactory().constructCollectionType(List.class, Job.class));
        }
        return new ArrayList<>();
    }

}
