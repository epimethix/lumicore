/*
 *  RemoteAI UI - Lumicore example application
 *  Copyright (C) 2023  epimethix@protonmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.epimethix.lumicore.remoteaiui.service;

import java.util.Optional;

import com.github.epimethix.lumicore.ioc.annotation.Autowired;
import com.github.epimethix.lumicore.ioc.annotation.PostConstruct;
import com.github.epimethix.lumicore.ioc.annotation.Service;
import com.github.epimethix.lumicore.remoteai.Generator;
import com.github.epimethix.lumicore.remoteai.OpenAI;
import com.github.epimethix.lumicore.remoteaiui.RemoteAIUI;
import com.github.epimethix.lumicore.remoteaiui.db.AppDB;

@Service
public final class GeneratorServiceImpl implements GeneratorService {

	private final Generator generator;

	@Autowired
	private AppDB appDB;

	@Autowired
	private RemoteAIUI application;

	public GeneratorServiceImpl() {
		this.generator = new OpenAI();
	}

	@PostConstruct
	public void init() {
		Optional<String> optKey;
		try {
			optKey = application.getApiKey();
			if (optKey.isPresent()) {
				initGenerator(optKey.get());
			}
		} catch (IllegalAccessException e) {}
	}

	@Override
	public void initGenerator(String apiKey) {
		generator.setKey(apiKey);
	}

	@Override
	public boolean isGeneratorInitialized() {
		return generator.isKeySet();
	}

//	public String textQuery(String prompt, String model, int maxTokens, float temperature) throws IOException {
//		return generator.textQuery(prompt, model, maxTokens, temperature);
//	}
//
//	public List<String> imageQuery(String prompt, String resolution, int nImages) throws IOException {
//		return generator.imageQuery(prompt, resolution, nImages);
//	}

	@Override
	public Generator getGenerator() {
		return generator;
	}
}
