package com.client.traveller

import io.kotlintest.matchers.string.shouldHaveLength
import io.kotlintest.specs.StringSpec

class ExampleUnitTest: StringSpec({

    "should be in firestore" {
        "hello" shouldHaveLength 5
    }
})
