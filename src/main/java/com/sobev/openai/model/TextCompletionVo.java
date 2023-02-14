package com.sobev.openai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author luojx
 * @date 2023/2/14 9:56
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TextCompletionVo {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Choice {
        private String text;
        private int index;
        private String logprobs;
        private String finish_reason;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Usage {
        private int prompt_tokens;
        private int completion_tokens;
        private int total_tokens;
    }
}
