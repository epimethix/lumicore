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
package com.github.epimethix.lumicore.common;

import com.github.epimethix.lumicore.common.ui.CryptoUI;
import com.github.epimethix.lumicore.ioc.annotation.ComponentScan;
import com.github.epimethix.lumicore.ioc.annotation.JarFileScan;

/**
 * The {@code CryptoDatabaseApplication} interface can be implemented to define
 * an application class.
 * <p>
 * Use {@code CryptoDatabaseApplication} to specify an application with an
 * encrypted database.
 * <p>
 * The application class, by convention, is located in the root package of the
 * project. The component scan uses the package of the application class as root
 * package to scan. If other packages should be scanned too or if the
 * application class does not reside in the root package then the annotation
 * {@code ComponentScan} can be used to specify the packages to scan.
 * <p>
 * If the project is being deployed as a jar file with sibling jar files to scan
 * the annotation {@code JarFileScan} can be used to specify additional jars to
 * scan.
 * 
 * @author epimethix
 * 
 * @see Application
 * @see ComponentScan
 * @see JarFileScan
 */
public interface CryptoDatabaseApplication extends DatabaseApplication {
	CryptoUI getCryptoUI();
}
