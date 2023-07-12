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
package com.github.epimethix.lumicore.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFile<T> {

	private final File jsonFile;

	private final Charset charset;

	private final ObjectMapper objectMapper;

	private final Class<T> dataClass;

	public JsonFile(Class<T> dataClass, File jsonFile) {
		this(dataClass, jsonFile, Charset.forName("UTF-8"));
	}

	public JsonFile(Class<T> dataClass, File jsonFile, Charset charset) {
		this.dataClass = dataClass;
		this.jsonFile = jsonFile;
		this.charset = charset;
		this.objectMapper = new ObjectMapper();
	}

	public void store(T data) throws IOException {
		store(data, false);
	}

	public void store(T data, boolean prettyPrinting) throws IOException {
		try (Writer out = new FileWriter(jsonFile, charset)) {
			if (prettyPrinting) {
				out.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data));
			} else {
				out.write(objectMapper.writeValueAsString(data));
			}
		}
	}

	public T load() throws IOException {
		try (Reader in = new FileReader(jsonFile, charset); BufferedReader br = new BufferedReader(in)) {
			String line;
			StringBuffer sb = new StringBuffer();
			while (Objects.nonNull(line = br.readLine())) {
				sb.append(line.trim()).append(" ");
			}
			return objectMapper.readValue(sb.toString(), dataClass);
		}
	}
}
