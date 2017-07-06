/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.ml.job.config;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.io.stream.Writeable;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.test.AbstractSerializingTestCase;
import org.elasticsearch.xpack.ml.job.messages.Messages;

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class AnalysisLimitsTests extends AbstractSerializingTestCase<AnalysisLimits> {

    @Override
    protected AnalysisLimits createTestInstance() {
        return new AnalysisLimits(randomBoolean() ? (long) randomIntBetween(1, 1000000) : null,
                randomBoolean() ? randomNonNegativeLong() : null);
    }

    @Override
    protected Writeable.Reader<AnalysisLimits> instanceReader() {
        return AnalysisLimits::new;
    }

    @Override
    protected AnalysisLimits doParseInstance(XContentParser parser) {
        return AnalysisLimits.PARSER.apply(parser, null);
    }

    public void testParseModelMemoryLimitGivenNegativeNumber() throws IOException {
        String json = "{\"model_memory_limit\": -1}";
        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, json);
        ParsingException e = expectThrows(ParsingException.class, () -> AnalysisLimits.PARSER.apply(parser, null));
        assertThat(e.getRootCause().getMessage(), containsString("model_memory_limit must be at least 1 MiB. Value = -1"));
    }

    public void testParseModelMemoryLimitGivenZero() throws IOException {
        String json = "{\"model_memory_limit\": 0}";
        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, json);
        ParsingException e = expectThrows(ParsingException.class, () -> AnalysisLimits.PARSER.apply(parser, null));
        assertThat(e.getRootCause().getMessage(), containsString("model_memory_limit must be at least 1 MiB. Value = 0"));
    }

    public void testParseModelMemoryLimitGivenPositiveNumber() throws IOException {
        String json = "{\"model_memory_limit\": 2048}";
        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, json);

        AnalysisLimits limits = AnalysisLimits.PARSER.apply(parser, null);

        assertThat(limits.getModelMemoryLimit(), equalTo(2048L));
    }

    public void testParseModelMemoryLimitGivenNegativeString() throws IOException {
        String json = "{\"model_memory_limit\":\"-4MB\"}";
        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, json);
        ParsingException e = expectThrows(ParsingException.class, () -> AnalysisLimits.PARSER.apply(parser, null));
        assertThat(e.getRootCause().getMessage(), containsString("model_memory_limit must be at least 1 MiB. Value = -4"));
    }

    public void testParseModelMemoryLimitGivenZeroString() throws IOException {
        String json = "{\"model_memory_limit\":\"0MB\"}";
        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, json);
        ParsingException e = expectThrows(ParsingException.class, () -> AnalysisLimits.PARSER.apply(parser, null));
        assertThat(e.getRootCause().getMessage(), containsString("model_memory_limit must be at least 1 MiB. Value = 0"));
    }

    public void testParseModelMemoryLimitGivenLessThanOneMBString() throws IOException {
        String json = "{\"model_memory_limit\":\"1000Kb\"}";
        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, json);
        ParsingException e = expectThrows(ParsingException.class, () -> AnalysisLimits.PARSER.apply(parser, null));
        assertThat(e.getRootCause().getMessage(), containsString("model_memory_limit must be at least 1 MiB. Value = 0"));
    }

    public void testParseModelMemoryLimitGivenStringMultipleOfMBs() throws IOException {
        String json = "{\"model_memory_limit\":\"4g\"}";
        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, json);

        AnalysisLimits limits = AnalysisLimits.PARSER.apply(parser, null);

        assertThat(limits.getModelMemoryLimit(), equalTo(4096L));
    }

    public void testParseModelMemoryLimitGivenStringNonMultipleOfMBs() throws IOException {
        String json = "{\"model_memory_limit\":\"1300kb\"}";
        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, json);

        AnalysisLimits limits = AnalysisLimits.PARSER.apply(parser, null);

        assertThat(limits.getModelMemoryLimit(), equalTo(1L));
    }

    public void testEquals_GivenEqual() {
        AnalysisLimits analysisLimits1 = new AnalysisLimits(10L, 20L);
        AnalysisLimits analysisLimits2 = new AnalysisLimits(10L, 20L);

        assertTrue(analysisLimits1.equals(analysisLimits1));
        assertTrue(analysisLimits1.equals(analysisLimits2));
        assertTrue(analysisLimits2.equals(analysisLimits1));
    }


    public void testEquals_GivenDifferentModelMemoryLimit() {
        AnalysisLimits analysisLimits1 = new AnalysisLimits(10L, 20L);
        AnalysisLimits analysisLimits2 = new AnalysisLimits(11L, 20L);

        assertFalse(analysisLimits1.equals(analysisLimits2));
        assertFalse(analysisLimits2.equals(analysisLimits1));
    }


    public void testEquals_GivenDifferentCategorizationExamplesLimit() {
        AnalysisLimits analysisLimits1 = new AnalysisLimits(10L, 20L);
        AnalysisLimits analysisLimits2 = new AnalysisLimits(10L, 21L);

        assertFalse(analysisLimits1.equals(analysisLimits2));
        assertFalse(analysisLimits2.equals(analysisLimits1));
    }


    public void testHashCode_GivenEqual() {
        AnalysisLimits analysisLimits1 = new AnalysisLimits(5555L, 3L);
        AnalysisLimits analysisLimits2 = new AnalysisLimits(5555L, 3L);

        assertEquals(analysisLimits1.hashCode(), analysisLimits2.hashCode());
    }

    public void testVerify_GivenNegativeCategorizationExamplesLimit() {
        ElasticsearchException e = expectThrows(ElasticsearchException.class, () -> new AnalysisLimits(1L, -1L));
        String errorMessage = Messages.getMessage(Messages.JOB_CONFIG_FIELD_VALUE_TOO_LOW,
                AnalysisLimits.CATEGORIZATION_EXAMPLES_LIMIT, 0, -1L);
        assertEquals(errorMessage, e.getMessage());
    }

    public void testVerify_GivenValid() {
        new AnalysisLimits(null, 1L);
        new AnalysisLimits(1L, null);
        new AnalysisLimits(1L, 1L);
    }
}
