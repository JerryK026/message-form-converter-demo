package Soko.messageformconverter.utils

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestExecutionListeners

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Retention(AnnotationRetention.RUNTIME)
@TestExecutionListeners(value = [AcceptanceTestExecutionListener::class], mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
annotation class AcceptanceTest()
