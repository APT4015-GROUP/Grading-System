package com.gradingsystem.tesla.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.gradingsystem.tesla.service.CohereGradingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource("classpath:application.properties")
public class GradingServiceTests {

    private static final Logger logger = LoggerFactory.getLogger(GradingServiceTests.class);

    @Autowired
    private CohereGradingService gradingService;

    @Value("${cohere.api.key}") // Inject API key
    private String apiKey;

    @Value("${cohere.api.model}") // Inject model name
    private String model;

    String v = "Grading stability failed: variance exceeded 1 mark. Variance: ";

    @BeforeEach
    void setUp() {
        // Validate API Key and model
        if (apiKey == null || apiKey.isEmpty() || model == null || model.isEmpty()) {
            throw new IllegalStateException("API key or model is not configured in application.properties");
        }
        gradingService.setApiKeyForTest(apiKey);
    }

    @Tag("unit")
    @Test
    void testParseQuestionsAndAnswers() {
        // Given a document containing structured Q&A
        String documentText = "Question: What is Java? Answer: A programming language.\n"
                + "Question: Who developed Java? Answer: Sun Microsystems.";

        // When parsing questions and answers
        Map<String, String> result = gradingService.parseQuestionsAndAnswers(documentText);

        // Then expected key-value pairs should be extracted
        assertEquals(2, result.size());
        assertEquals("A programming language.", result.get("What is Java?"));
        assertEquals("Sun Microsystems.", result.get("Who developed Java?"));
    }

    @Tag("unit")
    @Test
    void testExtractScores() {
        // Given an evaluation response
        String evaluation = "7/10";

        // When extracting scores
        int[] scores = gradingService.extractScores(evaluation);

        // Then extracted values should be correct
        assertEquals(7, scores[0]);
        assertEquals(10, scores[1]);
    }

    @Tag("unit")
    @Test
    void testExtractEvaluation() {
        // Given an AI response body
        String responseBody = """
                {
                  "id": "c14c80c3-18eb-4519-9460-6c92edd8cfb4",
                  "finish_reason": "COMPLETE",
                  "message": {
                    "role": "assistant",
                    "content": [
                      {
                        "type": "text",
                        "text": "8/10"
                      }
                    ]
                  },
                  "usage": {
                    "billed_units": {
                      "input_tokens": 5,
                      "output_tokens": 418
                    },
                    "tokens": {
                      "input_tokens": 71,
                      "output_tokens": 418
                    }
                  }
                }
                """;

        // When extracting evaluation
        String extractedEvaluation = CohereGradingService.extractEvaluation(responseBody);

        // Then extracted score should be correct
        assertEquals("8/10", extractedEvaluation);
    }

    @Tag("unit")
    @Test
    void testCalculateAggregateScore() {
        // Given a set of graded answers
        Map<String, String> evaluationResults = new LinkedHashMap<>();
        evaluationResults.put("Q1", "8/10");
        evaluationResults.put("Q2", "7/10");
        evaluationResults.put("Q3", "9/10");

        // When calculating aggregate score
        Integer aggregateScore = gradingService.calculateAggregateScore(evaluationResults);

        // Then percentage should be correctly calculated
        assertEquals(80, aggregateScore);
    }

    @Tag("unit")
    @Test
    void testEvaluateAnswersWithoutRubric_GradingStability() {
        // Given a question and a set of corresponding answers
        String question = "Explain photosynthesis.";
        String studentAnswer1 = "Photosynthesis is the process by which plants, algae, and some bacteria convert sunlight, carbon dioxide, and water into glucose and oxygen, providing energy for growth.";
        String studentAnswer2 = "Photosynthesis is how plants use sunlight to turn water and carbon dioxide into energy-rich glucose while releasing oxygen.";
        String studentAnswer3 = "Through photosynthesis, plants capture sunlight to produce food in the form of glucose, generating oxygen as a byproduct.";

        // Use unique keys for each question-answer pair
        Map<String, String> qaPairs = new LinkedHashMap<>();
        qaPairs.put("Q1: " + question, studentAnswer1); // Unique key for studentAnswer1
        qaPairs.put("Q2: " + question, studentAnswer2); // Unique key for studentAnswer2
        qaPairs.put("Q3: " + question, studentAnswer3); // Unique key for studentAnswer3

        logger.info("Evaluating answers without rubric for question: {}", question);

        // When evaluating answers without a rubric
        Map<String, String> results = gradingService.evaluateAnswersWithoutRubric(qaPairs);

        // Extract scores for each answer using the same unique keys
        int score1 = gradingService.extractScores(results.get("Q1: " + question))[0];
        int score2 = gradingService.extractScores(results.get("Q2: " + question))[0];
        int score3 = gradingService.extractScores(results.get("Q3: " + question))[0];

        logger.info("Scores received: Q1={} Q2={} Q3={}", score1, score2, score3);

        // Ensure variance is within acceptable limits
        int maxScore = Math.max(score1, Math.max(score2, score3));
        int minScore = Math.min(score1, Math.min(score2, score3));
        int variance = maxScore - minScore;

        logger.info("Computed variance: {}", variance);

        if (variance >= 2) {
            logger.error("Test failed: Variance exceeded limit. Score details: Q1={} Q2={} Q3={} | Variance={}", 
                         score1, score2, score3, variance);
        } else {
            logger.info("Test passed: Grading variance within acceptable range.");
        }
        
        // Assert that the variance is within an acceptable range
        assertTrue(variance < 2, v + variance);
    }

    @Tag("unit")
    @Test
    void testEvaluateAnswersWithRubric_Stability() {
        // Given a complex question that requires nuanced grading
        String question = "Explain the greenhouse effect and its impact on climate change.";

        // The rubric's ideal answer
        String rubricAnswer = "The greenhouse effect is the process where greenhouse gases trap heat in the atmosphere, leading to global warming. Human activities, such as burning fossil fuels, increase greenhouse gas emissions, which intensifies this effect and contributes to climate change.";

        // Three semantically identical but reworded student answers
        String studentAnswer1 = "Greenhouse gases trap heat in the Earth's atmosphere, causing global warming. Activities like burning fossil fuels release more greenhouse gases, making climate change worse.";
        String studentAnswer2 = "Climate change is driven by the greenhouse effect, where gases like CO2 retain heat. Fossil fuel use by humans raises these gases, strengthening the effect and increasing global temperatures.";
        String studentAnswer3 = "The Earth's atmosphere retains heat due to greenhouse gases, leading to warming. Human activities, such as burning fuels, add more gases, intensifying climate change.";

        // Use unique keys for each question-answer pair
        Map<String, String> qaPairs = new LinkedHashMap<>();
        qaPairs.put("Q1: " + question, studentAnswer1); // Unique key for studentAnswer1
        qaPairs.put("Q2: " + question, studentAnswer2); // Unique key for studentAnswer2
        qaPairs.put("Q3: " + question, studentAnswer3); // Unique key for studentAnswer3

        logger.info("Evaluating answers with rubric for question: {}", question);

        // Prepare rubric in question-answer format
        String rubricText = "Question: " + question + " Answer: " + rubricAnswer;

        // When evaluating with rubric
        Map<String, String> results = gradingService.evaluateAnswersWithRubric(qaPairs, rubricText);

        // Extract scores from the API responses
        int score1 = gradingService.extractScores(results.get("Q1: " + question))[0];
        int score2 = gradingService.extractScores(results.get("Q2: " + question))[0];
        int score3 = gradingService.extractScores(results.get("Q3: " + question))[0];

        logger.info("Scores received: Q1={} Q2={} Q3={}", score1, score2, score3);

        // Ensure variance is within acceptable limits
        int maxScore = Math.max(score1, Math.max(score2, score3));
        int minScore = Math.min(score1, Math.min(score2, score3));
        int variance = maxScore - minScore;

        logger.info("Computed variance: {}", variance);

        if (variance >= 2) {
            logger.error("Test failed: Variance exceeded limit. Score details: Q1={} Q2={} Q3={} | Variance={}", 
                         score1, score2, score3, variance);
        } else {
            logger.info("Test passed: Grading variance within acceptable range.");
        }

        // Assert that the variance is within an acceptable range
        assertTrue(variance < 2, v + variance);
    }
}