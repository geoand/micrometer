/**
 * Copyright 2017 Pivotal Software, Inc.
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
package io.micrometer.core.instrument.stats.hist;

import java.util.concurrent.TimeUnit;

public class PercentileTimeHistogram extends TimeHistogram {
    PercentileTimeHistogram(DoubleHistogram delegate, TimeUnit fUnits) {
        super(delegate, fUnits);
    }

    public static class Builder extends Histogram.Builder<Double> {
        private final TimeUnit fUnits;

        Builder(TimeUnit fUnits) {
            super(PercentileBucketFunction.INSTANCE);
            this.fUnits = fUnits;
        }

        @Override
        public PercentileTimeHistogram create(Summation defaultSummationMode) {
            return new PercentileTimeHistogram(new DoubleHistogram(f, summation == null ? defaultSummationMode : summation), fUnits);
        }
    }
}
