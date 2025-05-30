package dev.langchain4j.model.ollama.common;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.Utils.isNullOrEmpty;
import static dev.langchain4j.model.ollama.AbstractOllamaLanguageModelInfrastructure.OLLAMA_BASE_URL;
import static dev.langchain4j.model.ollama.AbstractOllamaLanguageModelInfrastructure.ollamaBaseUrl;
import static dev.langchain4j.model.ollama.OllamaImage.LLAMA_3_1;
import static dev.langchain4j.model.ollama.OllamaImage.LLAMA_3_2;
import static dev.langchain4j.model.ollama.OllamaImage.LLAMA_3_2_VISION;
import static dev.langchain4j.model.ollama.OllamaImage.OLLAMA_IMAGE;
import static dev.langchain4j.model.ollama.OllamaImage.localOllamaImage;
import static dev.langchain4j.model.ollama.OllamaImage.resolve;
import static java.time.Duration.ofSeconds;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.common.AbstractStreamingChatModelIT;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.ollama.LC4jOllamaContainer;
import dev.langchain4j.model.ollama.OllamaChatRequestParameters;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatResponseMetadata;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.langchain4j.model.openai.OpenAiTokenUsage;
import dev.langchain4j.model.output.TokenUsage;
import org.junit.jupiter.api.Disabled;

class OllamaStreamingChatModelIT extends AbstractStreamingChatModelIT {

    /**
     * Using map to avoid restarting the same ollama image.
     */
    private static final Map<String, LC4jOllamaContainer> CONTAINER_MAP = new HashMap<>();

    private static final String MODEL_WITH_TOOLS = LLAMA_3_1;
    private static LC4jOllamaContainer ollamaWithTools;

    private static final String MODEL_WITH_VISION = LLAMA_3_2_VISION;
    private static LC4jOllamaContainer ollamaWithVision;

    private static final String CUSTOM_MODEL_NAME = LLAMA_3_2;

    static {
        if (isNullOrEmpty(OLLAMA_BASE_URL)) {
            String localOllamaImageWithTools = localOllamaImage(MODEL_WITH_TOOLS);
            ollamaWithTools = new LC4jOllamaContainer(resolve(OLLAMA_IMAGE, localOllamaImageWithTools))
                    .withModel(MODEL_WITH_TOOLS)
                    .withModel(CUSTOM_MODEL_NAME);
            ollamaWithTools.start();
            ollamaWithTools.commitToImage(localOllamaImageWithTools);

            String localOllamaImageWithVision = localOllamaImage(MODEL_WITH_VISION);
            ollamaWithVision = new LC4jOllamaContainer(resolve(OLLAMA_IMAGE, localOllamaImageWithVision))
                    .withModel(MODEL_WITH_VISION)
                    .withModel(CUSTOM_MODEL_NAME);
            ollamaWithVision.start();
            ollamaWithVision.commitToImage(localOllamaImageWithVision);

            CONTAINER_MAP.put(localOllamaImageWithTools, ollamaWithTools);
            CONTAINER_MAP.put(localOllamaImageWithVision, ollamaWithVision);
        }
    }

    static final OllamaStreamingChatModel OLLAMA_CHAT_MODEL_WITH_TOOLS = OllamaStreamingChatModel.builder()
            .baseUrl(ollamaBaseUrl(ollamaWithTools))
            .modelName(MODEL_WITH_TOOLS)
            .temperature(0.0)
            .logRequests(true)
            .logResponses(true)
            .timeout(ofSeconds(180))
            .build();

    static final OllamaStreamingChatModel OLLAMA_CHAT_MODEL_WITH_VISION = OllamaStreamingChatModel.builder()
            .baseUrl(ollamaBaseUrl(ollamaWithVision))
            .modelName(MODEL_WITH_VISION)
            .temperature(0.0)
            .logRequests(true)
            .logResponses(true)
            .timeout(ofSeconds(180))
            .build();

    static final OpenAiStreamingChatModel OPEN_AI_CHAT_MODEL_WITH_TOOLS = OpenAiStreamingChatModel.builder()
            .baseUrl(ollamaBaseUrl(ollamaWithTools) + "/v1")
            .modelName(MODEL_WITH_TOOLS)
            .temperature(0.0)
            .logRequests(true)
            .logResponses(true)
            .timeout(ofSeconds(180))
            .build();

    static final OpenAiStreamingChatModel OPEN_AI_CHAT_MODEL_WITH_VISION = OpenAiStreamingChatModel.builder()
            .baseUrl(ollamaBaseUrl(ollamaWithVision) + "/v1")
            .modelName(MODEL_WITH_VISION)
            .temperature(0.0)
            .logRequests(true)
            .logResponses(true)
            .timeout(ofSeconds(180))
            .build();

    @Override
    protected List<StreamingChatModel> models() {
        return List.of(
                OLLAMA_CHAT_MODEL_WITH_TOOLS, OPEN_AI_CHAT_MODEL_WITH_TOOLS
                // TODO add more model configs, see OpenAiChatModelIT
                );
    }

    @Override
    protected List<StreamingChatModel> modelsSupportingImageInputs() {
        return List.of(
                OLLAMA_CHAT_MODEL_WITH_VISION, OPEN_AI_CHAT_MODEL_WITH_VISION
                // TODO add more model configs, see OpenAiChatModelIT
                );
    }

    @Override
    protected void should_fail_if_tool_choice_REQUIRED_is_not_supported(StreamingChatModel model) {
        if (model instanceof OpenAiChatModel) {
            return;
        }
        super.should_fail_if_tool_choice_REQUIRED_is_not_supported(model);
    }

    @Override
    protected void should_fail_if_JSON_response_format_is_not_supported(StreamingChatModel model) {
        if (model instanceof OpenAiStreamingChatModel) {
            return;
        }
        super.should_fail_if_JSON_response_format_is_not_supported(model);
    }

    @Override
    protected void should_fail_if_JSON_response_format_with_schema_is_not_supported(StreamingChatModel model) {
        if (model instanceof OpenAiStreamingChatModel) {
            return;
        }
        super.should_fail_if_JSON_response_format_with_schema_is_not_supported(model);
    }

    @Override
    @Disabled("enable after validation is implemented in OllamaStreamingChatModel")
    protected void should_fail_if_images_as_public_URLs_are_not_supported(StreamingChatModel model) {
        if (model instanceof OpenAiStreamingChatModel) {
            return;
        }
        super.should_fail_if_images_as_public_URLs_are_not_supported(model);
    }

    @Override
    protected StreamingChatModel createModelWith(ChatRequestParameters parameters) {
        String modelName = getOrDefault(parameters.modelName(), LLAMA_3_1);
        String localOllamaImage = localOllamaImage(modelName);
        if (!CONTAINER_MAP.containsKey(localOllamaImage) && isNullOrEmpty(OLLAMA_BASE_URL)) {
            LC4jOllamaContainer ollamaContainer =
                    new LC4jOllamaContainer(resolve(OLLAMA_IMAGE, localOllamaImage)).withModel(modelName);
            ollamaContainer.start();
            ollamaContainer.commitToImage(localOllamaImage);

            CONTAINER_MAP.put(localOllamaImage, ollamaContainer);
        }

        OllamaStreamingChatModel.OllamaStreamingChatModelBuilder ollamaStreamingChatModelBuilder =
                OllamaStreamingChatModel.builder()
                        .baseUrl(ollamaBaseUrl(CONTAINER_MAP.get(localOllamaImage)))
                        .defaultRequestParameters(parameters)
                        .logRequests(true)
                        .logResponses(true);

        if (parameters.modelName() == null) {
            ollamaStreamingChatModelBuilder.modelName(modelName);
        }

        return ollamaStreamingChatModelBuilder.build();
    }

    @Override
    protected String customModelName() {
        return CUSTOM_MODEL_NAME;
    }

    @Override
    protected ChatRequestParameters createIntegrationSpecificParameters(int maxOutputTokens) {
        return OllamaChatRequestParameters.builder()
                .maxOutputTokens(maxOutputTokens)
                .build();
    }

    @Override
    protected boolean assertFinishReason() {
        return false; // TODO: Ollama does not support TOOL_EXECUTION finish reason.
    }

    @Override
    protected boolean supportsToolChoiceRequired() {
        return false; // TODO check if Ollama supports this
    }

    @Override
    protected boolean supportsJsonResponseFormatWithSchema() {
        return false; // TODO implement
    }

    @Override
    protected boolean supportsMultipleImageInputsAsBase64EncodedStrings() {
        return false; // vision model only supports a single image per message
    }

    @Override
    protected boolean supportsSingleImageInputAsPublicURL() {
        return false; // Ollama supports only base64-encoded images
    }

    @Override
    protected boolean supportsMultipleImageInputsAsPublicURLs() {
        return false; // Ollama supports only base64-encoded images
    }

    @Override
    protected boolean assertResponseId() {
        return false; // TODO implement
    }

    @Override
    protected boolean assertTimesOnPartialResponseWasCalled() {
        return false; // TODO
    }

    @Override
    protected Class<? extends ChatResponseMetadata> chatResponseMetadataType(StreamingChatModel streamingChatModel) {
        if (streamingChatModel instanceof OpenAiStreamingChatModel) {
            return OpenAiChatResponseMetadata.class;
        } else if (streamingChatModel instanceof OllamaStreamingChatModel) {
            return ChatResponseMetadata.class;
        } else {
            throw new IllegalStateException("Unknown model type: " + streamingChatModel.getClass());
        }
    }

    @Override
    protected Class<? extends TokenUsage> tokenUsageType(StreamingChatModel streamingChatModel) {
        if (streamingChatModel instanceof OpenAiStreamingChatModel) {
            return OpenAiTokenUsage.class;
        } else if (streamingChatModel instanceof OllamaStreamingChatModel) {
            return TokenUsage.class;
        } else {
            throw new IllegalStateException("Unknown model type: " + streamingChatModel.getClass());
        }
    }
}
