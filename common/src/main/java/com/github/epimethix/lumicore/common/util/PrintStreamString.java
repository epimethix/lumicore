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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@FunctionalInterface
public interface PrintStreamString {
	void process(PrintStream printStream);

	public static String toString(PrintStreamString ss) throws IOException {
		return toString(ss, StandardCharsets.UTF_8);
	}
	
	public static String toString(PrintStreamString ss, Charset charset) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			String charsetName = charset.name();
			try (PrintStream ps = new PrintStream(out, true, charsetName)) {
				ss.process(ps);
			}
			return out.toString(charsetName);
		}
	}
}