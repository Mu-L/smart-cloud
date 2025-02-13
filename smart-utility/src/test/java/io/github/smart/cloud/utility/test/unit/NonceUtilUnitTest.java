/*
 * Copyright © 2019 collin (1634753825@qq.com)
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
package io.github.smart.cloud.utility.test.unit;

import io.github.smart.cloud.utility.NonceUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

class NonceUtilUnitTest {

    @RepeatedTest(128)
    void test() throws InterruptedException {
        int parties = 128;
        CountDownLatch latch = new CountDownLatch(parties);
        CopyOnWriteArraySet<String> values = new CopyOnWriteArraySet<>();
        CyclicBarrier cyclicBarrier = new CyclicBarrier(parties);
        for (int i = 0; i < parties; i++) {
            new Thread(() -> {
                try {
                    cyclicBarrier.await();
                    values.add(NonceUtil.getInstance().nextId());
                    latch.countDown();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }).start();
        }
        latch.await();

        Assertions.assertThat(values).hasSize(parties);
    }

}