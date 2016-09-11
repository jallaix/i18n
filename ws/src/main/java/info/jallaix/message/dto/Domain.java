package info.jallaix.message.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

/**
 * This bean contains persistent data related to a language:
 * TODO
 */
@Document(indexName = "message", type = "domain", shards = 1, replicas = 0)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Domain {

    /**
     * Identifier
     */
    @Id
    private String id;

    /**
     * Domain code
     */
    private String code;

    /**
     * Domain default language
     */
    private String defaultLanguage;

    /**
     * List of languages managed by the domain
     */
    private List<Language> languages;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Language {

        /**
         * Language code
         */
        private String code;

        /**
         * List of messages linked to the language
         */
        private List<Message> messages;


        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Message {

            /**
             * Message code
             */
            private String code;

            /**
             * Message content
             */
            private String content;
        }
    }
}
