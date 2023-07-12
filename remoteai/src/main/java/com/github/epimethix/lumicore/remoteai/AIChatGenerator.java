/*
 * Copyright 2023 epimethix@protonmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.epimethix.lumicore.remoteai;

import java.util.ArrayList;
import java.util.List;

import io.github.aminovmaksim.chatgpt4j.ChatGPTClient;
import io.github.aminovmaksim.chatgpt4j.model.ChatMessage;
import io.github.aminovmaksim.chatgpt4j.model.ChatRequest;
import io.github.aminovmaksim.chatgpt4j.model.ChatResponse;
import io.github.aminovmaksim.chatgpt4j.model.enums.ModelType;

public class AIChatGenerator {

	private final ChatGPTClient client;

	private final List<ChatMessage> messages;

	public AIChatGenerator(String apiKey) {
		client = ChatGPTClient.builder().requestTimeout(60_000L).apiKey(apiKey).build();
		messages = new ArrayList<>();
	}

	public String request(String message, int maxTokens, float frequencyPenalty, float presencePenalty, int n,
			float topP, float temperature) {
		messages.add(new ChatMessage(message));
		ChatRequest chatRequest = ChatRequest.builder().messages(messages).frequency_penalty(frequencyPenalty)
				.max_tokens(maxTokens).n(n).top_p(topP).temperature(temperature).presence_penalty(presencePenalty)
				.model(ModelType.GPT_4.getName()).build();
		ChatResponse response = client.sendChat(chatRequest);
		ChatMessage m = response.getChoices().get(0).getMessage();
		return m.getContent();
	}
	
	public List<ChatMessage> getMessages() {
		return new ArrayList<>(messages);
	}
}
