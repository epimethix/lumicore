/*
 * Copyright 2022 epimethix@protonmail.com
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
package com.github.epimethix.lumicore.sourceutil;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.github.epimethix.lumicore.sourceutil.JavaSource.AnnotationSource;
import com.github.epimethix.lumicore.sourceutil.JavaSource.CommentSource;

/**
 * Interface to specify a java class member source.
 * 
 * @author epimethix
 *
 */
public interface Member extends Source {
	/**
	 * gets the source code portion of this {@code Member} element.
	 * <p>
	 * the source code portion of the {@code Member} element does not include an
	 * optional Java Doc Comment or any annotations.
	 * <p>
	 * to get these call {@link Member#getComment()} and
	 * {@link Member#getAnnotations()}.
	 * 
	 * @return the member elements source
	 */
	CharSequence getSource();

	/**
	 * Gets the members doc comment if there is one.
	 * 
	 * @return the doc comment
	 */
	Optional<CommentSource> getComment();

	/**
	 * Gets the members annotations.
	 * 
	 * @return the annotations
	 */
	List<AnnotationSource> getAnnotations();

	/**
	 * Gets the members visibility.
	 * 
	 * @return the visibility
	 */
	String getVisibility();

	/**
	 * Gets the members modifiers.
	 * 
	 * @return the modifiers
	 */
	List<String> getModifiers();

	/**
	 * Gets the members identifier.
	 * 
	 * @return the identifier
	 */
	String getIdentifier();

	/**
	 * Tests if this member is static.
	 * 
	 * @return true if modifiers contain the string "static"
	 */
	default boolean isStatic() {
		return getModifiers().contains("static");
	}

	default boolean isFinal() {
		return getModifiers().contains("final");
	}

	default boolean isTransient() {
		return getModifiers().contains("transient");
	}

	default boolean isVolatile() {
		return getModifiers().contains("volatile");
	}

	default boolean isPublic() {
		return "public".equals(getVisibility());
	}

	default boolean isPackagePrivate() {
		return "".equals(getVisibility()) || Objects.isNull(getVisibility());
	}

	default boolean isProtected() {
		return "protected".equals(getVisibility());
	}

	default boolean isPrivate() {
		return "private".equals(getVisibility());
	}

	@Override
	default public Integer checkIntegrity(Integer previousEnd, Map<Source, Integer[]> boundaries) {
		Integer end = previousEnd;
		CommentSource comment = getComment().orElse(null);
		if (Objects.nonNull(comment)) {
			end = comment.checkIntegrity(end, boundaries);
		}
		List<AnnotationSource> annotations = getAnnotations();
		if (Objects.nonNull(annotations)) {
			for (AnnotationSource as : annotations) {
				end = as.checkIntegrity(end, boundaries);
			}
		}
		return Source.super.checkIntegrity(end, boundaries);
	}

	@Override
	default int length() {
		int length = 0;
		Optional<CommentSource> javaDocOpt = getComment();
		if (javaDocOpt.isPresent()) {
			CommentSource javaDoc = javaDocOpt.get();
			if (Objects.nonNull(javaDoc)) {
				length += javaDoc.length();
			}
		}
		List<AnnotationSource> annotations = getAnnotations();
		if (Objects.nonNull(annotations)) {
			for (AnnotationSource as : annotations) {
				length += as.length();
			}
		}
		length += getSource().length();
		return length;
	}
}
