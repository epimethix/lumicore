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

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.intellijava.core.controller.RemoteImageModel;
import com.intellijava.core.controller.RemoteLanguageModel;
import com.intellijava.core.model.input.ImageModelInput;
import com.intellijava.core.model.input.LanguageModelInput;

public class OpenAI implements Generator {
	public static enum Resolution {
		RES256X256("256x256"),

		RES512X512("512x512"),

		RES1024X1024("1024x1024");

		private final String res;

		Resolution(String res) {
			this.res = res;
		}

		@Override
		public String toString() {
			return res;
		}
	}

	public static enum TextModel {
		TEXT_DAVINCI_002("text-davinci-002"),

		CODE_DAVINCI_002("code-davinci-002"),

		TEXT_DAVINCI_003("text-davinci-003"),
//
//		GPT_3_5_TURBO("gpt-3.5-turbo"),
//
//		GPT_3_5_TURBO_0301("gpt-3.5-turbo-0301"),

		;

		private final String name;

		TextModel(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	private String apiKey;

	public OpenAI(String apiKey) {
		this.apiKey = apiKey;
	}

	public OpenAI() {}

	@Override
	public boolean isKeySet() {
		return Objects.nonNull(apiKey);
	}

	@Override
	public void setKey(String key) {
		this.apiKey = key;
	}

	@Override
	public String textQuery(String prompt, String model, int maxTokens, float temperature) throws IOException {
		RemoteLanguageModel openAi = new RemoteLanguageModel(apiKey, "openai");
		// @formatter:off
		LanguageModelInput input = new LanguageModelInput
				.Builder(prompt)
				.setModel(model)
				.setMaxTokens(maxTokens)
				.setTemperature(temperature)
				.build();
		// @formatter:on
		return openAi.generateText(input);
	}

	@Override
	public List<String> imageQuery(String prompt, String resolution, int nImages) throws IOException {
		RemoteImageModel openAi = new RemoteImageModel(apiKey, "openai");
		// @formatter:off
		ImageModelInput input = new ImageModelInput
				.Builder(prompt)
				.setImageSize(resolution)
				.setNumberOfImages(nImages)
				.build();
		// @formatter:on
//java.io.IOException: Unexpected HTTP response: 504 Error details:<html><head><title>504 Gateway Time-out</title></head><body><center><h1>504 Gateway Time-out</h1></center><hr><center>nginx/1.22.1</center></body></html>
//	at com.intellijava.core.wrappers.OpenAIWrapper.generateImages(OpenAIWrapper.java:119)
//	at com.intellijava.core.controller.RemoteImageModel.generateOpenaiImage(RemoteImageModel.java:103)
//	at com.intellijava.core.controller.RemoteImageModel.generateImages(RemoteImageModel.java:75)
//	at com.github.epimethix.lumicore.remoteai.OpenAI.imageQuery(OpenAI.java:99)
//	at com.github.epimethix.lumicore.swing.remoteai.ImageQueryPanel$3.doInBackground(ImageQueryPanel.java:161)
//	at com.github.epimethix.lumicore.swing.remoteai.ImageQueryPanel$3.doInBackground(ImageQueryPanel.java:1)
//	at java.desktop/javax.swing.SwingWorker$1.call(SwingWorker.java:304)
//	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
//	at java.desktop/javax.swing.SwingWorker.run(SwingWorker.java:343)
//	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
//	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
//	at java.base/java.lang.Thread.run(Thread.java:833)

//		java.net.SocketException: Unexpected end of file from server
//		at java.base/sun.net.www.http.HttpClient.parseHTTPHeader(HttpClient.java:936)
//		at java.base/sun.net.www.http.HttpClient.parseHTTP(HttpClient.java:759)
//		at java.base/sun.net.www.http.HttpClient.parseHTTPHeader(HttpClient.java:933)
//		at java.base/sun.net.www.http.HttpClient.parseHTTP(HttpClient.java:759)
//		at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream0(HttpURLConnection.java:1688)
//		at java.base/sun.net.www.protocol.http.HttpURLConnection.getInputStream(HttpURLConnection.java:1589)
//		at java.base/java.net.HttpURLConnection.getResponseCode(HttpURLConnection.java:529)
//		at java.base/sun.net.www.protocol.https.HttpsURLConnectionImpl.getResponseCode(HttpsURLConnectionImpl.java:308)
//		at com.intellijava.core.wrappers.OpenAIWrapper.generateImages(OpenAIWrapper.java:117)
//		at com.intellijava.core.controller.RemoteImageModel.generateOpenaiImage(RemoteImageModel.java:103)
//		at com.intellijava.core.controller.RemoteImageModel.generateImages(RemoteImageModel.java:75)
//		at com.github.epimethix.lumicore.remoteai.OpenAI.imageQuery(OpenAI.java:112)
//		at com.github.epimethix.lumicore.swing.remoteai.ImageQueryPanel$3.doInBackground(ImageQueryPanel.java:161)
//		at com.github.epimethix.lumicore.swing.remoteai.ImageQueryPanel$3.doInBackground(ImageQueryPanel.java:1)
//		at java.desktop/javax.swing.SwingWorker$1.call(SwingWorker.java:304)
//		at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
//		at java.desktop/javax.swing.SwingWorker.run(SwingWorker.java:343)
//		at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
//		at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
//		at java.base/java.lang.Thread.run(Thread.java:833)

//		java.io.IOException: Unexpected HTTP response: 500 Error details:{  "error": {    "code": null,    "message": "The server had an error while processing your request. Sorry about that! You can retry your request, or contact us through our help center at help.openai.com if the error persists. (Please include the request ID 48a7f79f39fe4b2fe7cae649b9cd3da3 in your message.)",    "param": null,    "type": "server_error"  }}
//		at com.intellijava.core.wrappers.OpenAIWrapper.generateImages(OpenAIWrapper.java:119)
//		at com.intellijava.core.controller.RemoteImageModel.generateOpenaiImage(RemoteImageModel.java:103)
//		at com.intellijava.core.controller.RemoteImageModel.generateImages(RemoteImageModel.java:75)
//		at com.github.epimethix.lumicore.remoteai.OpenAI.imageQuery(OpenAI.java:134)
//		at com.github.epimethix.lumicore.swing.remoteai.ImageQueryPanel$3.doInBackground(ImageQueryPanel.java:161)
//		at com.github.epimethix.lumicore.swing.remoteai.ImageQueryPanel$3.doInBackground(ImageQueryPanel.java:1)
//		at java.desktop/javax.swing.SwingWorker$1.call(SwingWorker.java:304)
//		at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264)
//		at java.desktop/javax.swing.SwingWorker.run(SwingWorker.java:343)
//		at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
//		at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
//		at java.base/java.lang.Thread.run(Thread.java:833)

		return openAi.generateImages(input);
	}

	@Override
	public String[] getTextModels() {
		return new String[] { TextModel.TEXT_DAVINCI_003.name, TextModel.TEXT_DAVINCI_002.name,
				TextModel.CODE_DAVINCI_002.name };
	}

	@Override
	public int getMaxTokensMax(String model) {
		int maxTokens = 250;
//		if (TextModel.GPT_3_5_TURBO.name.equals(model)) {
//			maxTokens = 4_096;
//		} else if (TextModel.GPT_3_5_TURBO_0301.name.equals(model)) {
//			maxTokens = 4_096;
//		} else 
		if (TextModel.TEXT_DAVINCI_003.name.equals(model)) {
			maxTokens = 4_000;
		} else if (TextModel.TEXT_DAVINCI_002.name.equals(model)) {
			maxTokens = 4_000;
		} else if (TextModel.CODE_DAVINCI_002.name.equals(model)) {
			maxTokens = 4_000;
		}
		return maxTokens;
	}
}
