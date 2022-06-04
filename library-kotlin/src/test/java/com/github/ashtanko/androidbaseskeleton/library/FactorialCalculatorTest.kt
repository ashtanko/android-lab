/*
 * Copyright 2022 Oleksii Shtanko
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

package com.github.ashtanko.androidbaseskeleton.library

import com.github.ashtanko.androidbaseskeleton.library.FactorialCalculator.add
import com.github.ashtanko.androidbaseskeleton.library.FactorialCalculator.computeFactorial
import java.util.stream.Stream
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class FactorialCalculatorTest {

    private class InputArgumentsProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext?): Stream<out Arguments> =
            Stream.of(
                Arguments.of(
                    2,
                    2,
                    4
                )
            )
    }

    @ParameterizedTest
    @ArgumentsSource(InputArgumentsProvider::class)
    fun addTest(a: Int, b: Int, expected: Int) {
        val actual = add(a, b)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun computeFactorial_withNegative_raiseException() {
        assertThrows(Exception::class.java) {
            computeFactorial(-1)
        }
    }

    @Test
    fun computeFactorial_forZero() {
        assertEquals(1, computeFactorial(0))
    }

    @Test
    fun computeFactorial_forFive() {
        assertEquals(120, computeFactorial(5))
    }
}
