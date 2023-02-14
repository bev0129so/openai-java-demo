package com.sobev.openai.model;

import lombok.Data;

/**
 * @author luojx
 * @date 2023/2/14 9:38
 */
@Data
public class TextCompletionDto {
    private String model = "text-davinci-003";
    private String prompt;
    private float temperature=0.7f;
    private int top_p=1;
    private int frequency_penalty=0;
    private int max_tokens=1536;
    private boolean stream=false;
    private int n=1;
    
}
