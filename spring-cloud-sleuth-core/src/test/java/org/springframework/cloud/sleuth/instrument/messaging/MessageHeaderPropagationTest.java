/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.sleuth.instrument.messaging;

import java.util.Collections;

import brave.propagation.Propagation;
import org.junit.jupiter.api.Test;

import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageHeaderPropagationTest
		extends PropagationSetterTest<MessageHeaderAccessor, String> {

	MessageHeaderAccessor carrier = new MessageHeaderAccessor();

	@Override
	public Propagation.KeyFactory<String> keyFactory() {
		return Propagation.KeyFactory.STRING;
	}

	@Override
	protected MessageHeaderAccessor carrier() {
		return this.carrier;
	}

	@Override
	protected Propagation.Setter<MessageHeaderAccessor, String> setter() {
		return MessageHeaderPropagation.INSTANCE;
	}

	@Override
	protected Iterable<String> read(MessageHeaderAccessor carrier, String key) {
		Object result = carrier.getHeader(key);
		return result != null ? Collections.singleton(result.toString())
				: Collections.emptyList();
	}

	@Test
	public void testGetByteArrayValue() {
		MessageHeaderAccessor carrier = carrier();
		carrier.setHeader("b3", "48485a3953bb6124-1234".getBytes());
		carrier.setHeader("b3", "48485a3953bb6124000000-1234".getBytes());
		String value = MessageHeaderPropagation.INSTANCE.get(carrier, "b3");
		assertThat(value).isEqualTo("48485a3953bb6124000000-1234");
	}

	@Test
	public void testGetStringValue() {
		MessageHeaderAccessor carrier = carrier();
		carrier.setHeader("B3", "48485a3953bb6124-1234");
		carrier.setHeader("B3", "48485a3953bb61240000000-1234");
		String value = MessageHeaderPropagation.INSTANCE.get(carrier, "B3");
		assertThat(value).isEqualTo("48485a3953bb61240000000-1234");
	}

	@Test
	public void testGetNullValue() {
		MessageHeaderAccessor carrier = carrier();
		carrier.setHeader("B3", "48485a3953bb6124-1234");
		carrier.setHeader("B3", "48485a3953bb61240000000-1234");
		String value = MessageHeaderPropagation.INSTANCE.get(carrier, "non existent key");
		assertThat(value).isNull();
	}

	@Test
	public void testSkipWrongValueTypeForGet() {
		MessageHeaderAccessor carrier = carrier();
		carrier.setHeader(NativeMessageHeaderAccessor.NATIVE_HEADERS,
				"{spanTraceId=[123], spanId=[456], spanSampled=[0]}");
		MessageHeaderPropagation.INSTANCE.get(carrier, "b3");
	}

	@Test
	public void testSkipWrongValueTypeForRemoval() {
		MessageHeaderAccessor carrier = carrier();
		carrier.setHeader(NativeMessageHeaderAccessor.NATIVE_HEADERS,
				"{spanTraceId=[123], spanId=[456], spanSampled=[0]}");
		MessageHeaderPropagation.removeAnyTraceHeaders(carrier,
				Collections.singletonList("b3"));
	}

	@Test
	public void testSkipWrongValueTypeForPut() {
		MessageHeaderAccessor carrier = carrier();
		carrier.setHeader(NativeMessageHeaderAccessor.NATIVE_HEADERS,
				"{spanTraceId=[123], spanId=[456], spanSampled=[0]}");
		MessageHeaderPropagation.INSTANCE.put(carrier, "b3", "1234");
	}

}
